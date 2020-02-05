package tools.matchingtool

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import util.checkFile
import java.io.File

class MatchingArguments (parser: ArgParser) {
    val overwrite by parser.flagging(
            "-o", "--overwrite",
            help = "overwrite existing mapping file").default(false)


    val openApiSpecFileName:String by parser.storing(
            "-s", "--specification",
            help = "Input: openAPI 3.0 specification file").addValidator {
        if (!checkFile(File(openApiSpecFileName))) {
            throw InvalidArgumentException("Invalid openAPISpec")
        }
    }

    val workloadDefinitionFileName:String by parser.storing(
            "-w", "--workload",
            help = "Input: workload definition file").addValidator {
        if (!checkFile(File(workloadDefinitionFileName))) {
            throw InvalidArgumentException("Invalid workload definition")
        }
    }

    val mappingFile:String by parser.storing(
            "-m", "--mapping",
            help = "Output: name of generated mapping file").default("mapping.json").addValidator {
        if (File(mappingFile).exists() && overwrite == false) {
            throw InvalidArgumentException(File(mappingFile).absolutePath + " already exists.")
        }
        if (File(mappingFile).exists() && overwrite) {
            println("Will overwrite mapping file")
        }
        if (!mappingFile.endsWith(".json")) {
            throw InvalidArgumentException("mapping file must be a json file (end with .json)")
        }
    }

    val excludePaths by parser.adding(
            "-e", "--exclude",
            help = "path which should be excluded from mapping") { this }


}