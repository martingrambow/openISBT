package tools.wlgenerator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import mapping.globalmapping.GPatternBinding
import org.slf4j.LoggerFactory
import util.loadMapping
import util.readFile
import workload.SchemaFaker
import workload.WorkloadGenerator
import java.io.File

val  log = LoggerFactory.getLogger("GeneratorTool")!!

/**
 * Sample calls:
 * -o -m mapping.json -w workload.json
 */

fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::GeneratorArguments).run {
        val mappingFile = File(mappingFileName)

        val mapping: Array<GPatternBinding> = loadMapping(readFile(mappingFile))
                ?: throw InvalidArgumentException("Could not parse specification")

        if (!overwrite && File(workloadFileName).exists()) {
            throw InvalidArgumentException("Would overwrite workload file, rename or use -o flag")
        }

        SchemaFaker.port = 9080
        val generator = WorkloadGenerator()
        generator.generateWorkload(mapping)
        SchemaFaker.close()

        val workload = generator.getWorkload()

        val gson: Gson = GsonBuilder().create()
        File(workloadFileName).writeText(gson.toJson(workload))
        tools.requestdistribution.log.info("Done. See workload in ${File(workloadFileName).absoluteFile}")

        return@mainBody
    }
}