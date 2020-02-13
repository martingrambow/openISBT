package tools.evaluation

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import util.checkFile
import java.io.File

class BoxPlotArguments(parser: ArgParser) {
    val overwrite by parser.flagging(
            "-o", "--overwrite",
            help = "overwrite existing boxplot file").default(false)


    val measurementFile: File by parser.storing(
            "-r", "--results",
            help = "Input: results file") { File(this) }
            .addValidator {
                if (!checkFile(measurementFile)) {
                    throw InvalidArgumentException("Invalid measurement file")
                }
            }

    val csvFile: File by parser.storing(
            "-c", "--csv",
            help = "Output: name of generated csv file") { File(this) }
            .default(File("results.csv"))
            .addValidator {
                if (csvFile.exists() && !overwrite) {
                    throw InvalidArgumentException(csvFile.absolutePath + " already exists.")
                }
                if (csvFile.exists() && overwrite) {
                    println("Will overwrite boxplot file file")
                }
            }
}