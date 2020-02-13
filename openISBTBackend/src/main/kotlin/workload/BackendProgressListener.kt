package workload

class BackendProgressListener : ProgressListener {

    var currentProgress:Int = 0
    private var done = false

    override fun setProgress(percentage: Int) {
        currentProgress = percentage
        if (currentProgress == 100) {
            done = true
        }
    }

}