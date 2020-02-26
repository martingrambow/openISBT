package tools.matchingtool

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import mapping.Mapper
import mapping.globalmapping.GMapper
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
        var specs: ArrayList<OpenAPISPecifcation> = ArrayList()
        specs.add(spec)
        //val mapper = GMapper(specs.toTypedArray(), config)
        //Old Mapper:
        val mapper = Mapper(spec, config)
        var mapping = mapper.mapPattern()

        for (exclude in excludePaths) {
            for (rmapping in mapper.resourceMappings) {
                if (exclude == rmapping.resourcePath) {
                    rmapping.enabled = false
                }
            }
        }
        mapper.calculateRequests()

        log.info("Supported resource mappings:")
        for (rmapping in mapper.resourceMappings) {
            if (rmapping.supported) {
                log.info("Resource Mapping for " + rmapping.resourcePath + ":")
                for (m in rmapping.patternMappingList) {
                    log.info("  Pattern " + m.aPattern.name + ", supported=" + m.supported + ", requests=" + m.requests + ":")
                    for (operationlist in m.operationSequence) {
                        for (entry in operationlist) {
                            log.info("      Operation: " + entry.aOperation.operation + " path=" + entry.path)
                        }
                    }
                }
            }
        }

        val gson: Gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        mappingFile.writeText(gson.toJson(mapper.resourceMappings))
        log.info("Done. See mappings in " + mappingFile .absoluteFile)
    }
}