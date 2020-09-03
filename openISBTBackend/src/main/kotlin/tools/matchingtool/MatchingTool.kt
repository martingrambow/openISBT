package tools.matchingtool

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import mapping.globalmapping.GMapper
import org.slf4j.LoggerFactory
import util.loadOAS
import util.loadPatternConfig
import util.loadServiceLinksFile
import util.readFile

val  log = LoggerFactory.getLogger("MatchingTool")!!

/**
 * Sample calls:
 * -o -s resources/oasFiles/sockshop.json -d resources/patternConfigs/experiment2.json
 */

fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::MatchingArguments).run {

        val mapper = GMapper()
        for (spec in openApiSpecFiles) {
            val tmp = loadOAS(readFile(spec)) ?: throw InvalidArgumentException("Could not parse specification " + spec.absoluteFile)
            mapper.addOpenAPISpec(tmp)
        }
        val config: PatternConfiguration = loadPatternConfig(readFile(workloadDefinitionFile)) ?: throw InvalidArgumentException("Could not parse workload definition")
        mapper.setPatternConfiguration(config)

        if (serviceLinksFile.name != "none") {
            val links = loadServiceLinksFile(readFile(serviceLinksFile))
                    ?: throw InvalidArgumentException("Could not parse service links")
            mapper.setServiceLinks(links)
        }

        if (!overwrite && mappingFile.exists()) {
            throw InvalidArgumentException("Would overwrite mapping file, rename or use -o flag")
        }

        mapper.mapPattern(excludePaths.toTypedArray())
        mapper.calculateRequests()
        mapper.printSupportInfo()
        mapper.saveMapping(mappingFile)
        log.info("Done. See mappings in " + mappingFile .absoluteFile)
    }
}