package tools.runner

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import measurement.PatternMeasurement
import org.slf4j.LoggerFactory
import run.Worker
import run.Workerhandler
import util.loadWorkload
import util.readFile
import java.io.File

val  log = LoggerFactory.getLogger("RunTool")!!

/**
 * sample calls:
 * -o -r results.json -w workload.json -e http://ec2-34-249-43-215.eu-west-1.compute.amazonaws.com:8080 -t 5 -u localhost:8000 -u localhost:8010
 */
fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::RunnerArguments).run {

        // load workload
        val workload = loadWorkload(readFile(workloadFile)) ?: throw InvalidArgumentException("Could not parse workload")

        //check and init workers
        val workerhandler = Workerhandler()
        val workerlist = ArrayList<Worker>()
        for ((nextid, w) in workerURLS.withIndex()) {
            val worker = Worker(url = w, threads = threadsPerWorker, id = nextid)
            runBlocking {
                worker.status = workerhandler.getWorkerStatus(worker)
            }
            workerlist.add(worker)
        }
        val workers: Array<Worker> = workerlist.toTypedArray()

        if (workers.isEmpty()) {
            log.error("no worker")
            return@mainBody
        }

        //ensure all worker waiting
        for (worker in workers) {
            if (worker.status != "waiting") {
                log.error("worker " + worker.id + " (" + worker.url + ") is not waiting; [" + worker.status + "]")
                return@mainBody
            }
        }

        //init workers
        var noErrors = true
        for (w in workers) {
            runBlocking {
                if (workerhandler.clearWorker(w)) {
                    if (noErrors && workerhandler.setID(w)) {
                        if (noErrors && workerhandler.setEndpoint(w, endpoint)) {
                            if (noErrors && workerhandler.setThreads(w, w.threads)) {
                                //Everything ok
                            } else {
                                log.error("worker " + w.id + "(" + w.url + "), unable to set threads")
                                noErrors = false
                            }
                        } else {
                            log.error("worker " + w.id + "(" + w.url + "), unable to set endpoint")
                            noErrors = false
                        }

                    } else {
                        log.error("worker " + w.id + "(" + w.url + "), unable to set ID")
                        noErrors = false
                    }
                } else {
                    log.error("worker " + w.id + "(" + w.url + ") can't be cleared")
                    noErrors = false
                }
            }
        }
        if (!noErrors) {
            log.error("Unable to initialize all workers")
            return@mainBody
        } else {
            log.info("initialized " + workers.size + " worker(s)")
        }

        //distribute workload
        runBlocking {
            noErrors = workerhandler.distributeWorkload(workers, workload)
        }
        if (!noErrors) {
            log.error("Unable to distribute workload")
            return@mainBody
        }

        //start benchmark
        runBlocking {
            noErrors = workerhandler.startBenchmark(workers)
        }
        if (!noErrors) {
            log.error("Unable to start benchmark")
            return@mainBody
        }
        log.info("Experiment started")

        //request status updates
        var end = false
        workerhandler.startNotificationListener(workers
        ) { id: Int, x: String ->
            log.info("worker $id: $x")
            if (x == "done") {
                end = true
            }
        }

        //wait for end
        log.info("Wait for experiment end")
        while (!end) {
            runBlocking {
                delay(1000)
            }
        }
        log.info("Experiment done")

        // collect results
        var measurements = ArrayList<PatternMeasurement>()
        runBlocking {
            measurements = workerhandler.collectResults(workers)
        }
        log.info("Collected measurements")

        val gson: Gson = GsonBuilder().create()
        File(resultsFileName).writeText(gson.toJson(measurements))
        println("Done. See measurements in " + File(resultsFileName).absoluteFile)

        return@mainBody
    }
}