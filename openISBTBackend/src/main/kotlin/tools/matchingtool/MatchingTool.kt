package tools.matchingtool

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import mapping.IMapper
import mapping.globalmapping.GMapper
import mapping.simplemapping.Mapper
import org.slf4j.LoggerFactory
import util.loadOAS
import util.loadPatternConfig
import util.readFile
import java.lang.reflect.Modifier

val  log = LoggerFactory.getLogger("MatchingTool")!!

/**
 * Sample calls:
 * -o -s resources/oasFiles/sockshop.json -d resources/patternConfigs/experiment2.json -e /cards
 */

fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::MatchingArguments).run {

        val spec: OpenAPISPecifcation? = loadOAS(readFile(openApiSpecFile))
        val config: PatternConfiguration? = loadPatternConfig(readFile(workloadDefinitionFile))

        if (spec == null) {
            throw InvalidArgumentException("Could not parse specification")
        }

        if (config == null) {
            throw InvalidArgumentException("Could not parse workload definition")
        }

        if (!overwrite && mappingFile.exists()) {
            throw InvalidArgumentException("Would overwrite mapping file, rename or use -o flag")
        }

        //Global Mapper:
        //Old Mapper:
        val mapper:IMapper = Mapper()
        mapper.addOpenAPISpec(spec)
        mapper.setPatternConfiguration(config)
        mapper.mapPattern(excludePaths.toTypedArray())
        mapper.calculateRequests()
        mapper.printSupportInfo()
        mapper.saveMapping(mappingFile)
        log.info("Done. See mappings in " + mappingFile .absoluteFile)
    }
}