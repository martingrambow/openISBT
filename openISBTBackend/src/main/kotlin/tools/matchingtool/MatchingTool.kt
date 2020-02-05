package tools.matchingtool

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import mapping.Mapper
import util.loadOAS
import util.loadPatternConfig
import util.readFile
import java.io.File

/**
 * Sample calls:
 * -o -s openISBTFrontend/web/oasFiles/petstore.json -w openISBTFrontend/web/patternConfigs/experiment.json
 * -o -s openISBTFrontend/web/oasFiles/petstore.json -w openISBTFrontend/web/patternConfigs/onlyCreate.json
 * -o -s openISBTFrontend/web/oasFiles/petstore.json -w openISBTFrontend/web/patternConfigs/onlyCreate.json -e /store/order
 */

fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::MatchingArguments).run {
        val openAPISpec = File(openApiSpecFileName)
        val workloadDefinition = File(workloadDefinitionFileName)

        val spec: OpenAPISPecifcation? = loadOAS(readFile(openAPISpec))
        val config: PatternConfiguration? = loadPatternConfig(readFile(workloadDefinition))

        if (spec == null) {
            throw InvalidArgumentException("Could not parse specification")
        }

        if (config == null) {
            throw InvalidArgumentException("Could not parse workload definition")
        }

        val mapper = Mapper()
        var mapping = mapper.mapPattern(spec, config)

        for (exclude in excludePaths) {
            for (rmapping in mapping) {
                if (exclude.equals(rmapping.resourcePath)) {
                    rmapping.enabled = false;
                }
            }
        }
        mapping = mapper.calculateRequests(mapping, config)

        println("Supported resource mappings:")
        for (rmapping in mapping) {
            if (rmapping.supported) {
                println("Resource Mapping for " + rmapping.resourcePath + ":")
                for (m in rmapping.patternMappingList) {
                    println("  Pattern " + m.aPattern.name + ", supported=" + m.supported + ", requests=" + m.requests + ":")
                    for (operationlist in m.operationSequence) {
                        for (entry in operationlist) {
                            println("      Operation: " + entry.aOperation.operation + " path=" + entry.path)
                        }
                    }
                }
            }
        }

        val gson: Gson = GsonBuilder().create()
        File(mappingFile).writeText(gson.toJson(mapping))
        println("Done. See mappings in " + File(mappingFile).absoluteFile)
    }
}