import workload.PatternRequest

class WorkloadRunnable(var patternRequest: PatternRequest, val statisticshandler: Statisticshandler) : Runnable {



    override fun run() {
        println("Some request " + patternRequest.id + ", " + patternRequest.apiRequests.size + " API requests")
        Thread.sleep(500)
        statisticshandler.addDone()
    }


}