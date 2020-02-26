package tools.runner

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import java.io.File

class RunnerArguments(parser: ArgParser) {

    val overwrite by parser.flagging(
            "-o", "--overwrite",
            help = "overwrite existing result file").default(false)

    val threadsPerWorker: Int by parser.storing(
            "-t", "--threads",
            help = "Threads per worker") { toInt() }.default(10)


    val resultsFileName: String by parser.storing(
            "-r", "--results",
            help = "Output: result file").addValidator {
        if (File(resultsFileName).exists() && !overwrite) {
            throw InvalidArgumentException(File(resultsFileName).absolutePath + " already exists.")
        }
        if (File(resultsFileName).exists() && overwrite) {
            println("Will overwrite results file")
        }
    }

    val workerURLS by parser.adding(
            "-u", "--workerURL",
            help = "URL of worker")

    val workloadFile by parser.storing(
            "-w", "--workload",
            help = "workload file") { File(this) }
            .addValidator {
                if (!this.value.exists()) {
                    throw InvalidArgumentException(this.value.absolutePath + " does not exist.")
                }
            }

    val endpoint by parser.storing(
            "-e", "--endpoint",
            help = "endpoint url")
}