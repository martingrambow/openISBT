package tools.matchingtool

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import util.checkFile
import java.io.File

class MatchingArguments(parser: ArgParser) {
    val overwrite by parser.flagging(
            "-o", "--overwrite",
            help = "overwrite existing mapping file").default(false)


    val openApiSpecFiles: MutableList<File> by parser.adding(
            "-s", "--specification",
            help = "Input: openAPI 3.0 specification file(s)") { File(this) }
            .addValidator {
                for (f in openApiSpecFiles) {
                    if (!checkFile(f)) {
                        throw InvalidArgumentException("Invalid openAPISpec: " + f.absoluteFile)
                    }
                }
            }

    val workloadDefinitionFile: File by parser.storing(
            "-d", "--workloadDefinition",
            help = "Input: workload definition file") { File(this) }
            .addValidator {
                if (!checkFile(workloadDefinitionFile)) {
                    throw InvalidArgumentException("Invalid workload definition")
                }
            }

    val serviceLinksFile: File by parser.storing(
            "-l", "--serviceLinks",
            help = "Input: service links file") { File(this) }
            .addValidator {
                if (!checkFile(serviceLinksFile)) {
                    throw InvalidArgumentException("Invalid workload definition")
                }
            }

    val excludePaths by parser.adding(
            "-e", "--exclude",
            help = "paths which should be excluded from mapping")

    val mappingFile: File by parser.storing(
            "-m", "--mapping",
            help = "Output: name of generated mapping file") { File(this) }
            .default(File("mapping.json"))
            .addValidator {
                if (mappingFile.exists() && !overwrite) {
                    throw InvalidArgumentException(mappingFile.absolutePath + " already exists.")
                }
                if (mappingFile.exists() && overwrite) {
                    println("Will overwrite mapping file")
                }
            }
}