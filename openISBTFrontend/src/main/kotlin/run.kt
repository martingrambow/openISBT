import dataobjects.ServerNotifcation
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import worker.Worker
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.appendText
import kotlin.dom.removeClass

class run {

    private var nextWorkerID = 1
    private var workersetID = -1
    private var workloadID:Int = -1
    private var measurementsID:Int = -1

    private var notificationListenerID:Int = -1
    private var benchmarkRunFinished = true

    private var worker: Array<Worker> = arrayOf()

    fun init() {
        println("Init run called")

        //There must be a workload; if not redirect to generate
        workloadID = getCookie("workload").toInt()
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            if (req.responseText.contains("not found") && req.responseText.length < 15) {
                //workload not found
                redirectToUrl("generate.html")
            }
        }
        req.open("GET", "/api/workload/$workloadID", true)
        req.send()

        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(_: Event) {
            handleNextButtonClick()

        })

        val btnAddWorker = document.getElementById("btnAddWorker")
        btnAddWorker?.addEventListener("click", fun(_: Event) {
            addWorker()
        })

        val btnRefrehStatus = document.getElementById("btnRefresh")
        btnRefrehStatus?.addEventListener("click", fun(_: Event) {
            refreshWorkerStatus()
        })

        val btnBenchmark = document.getElementById("btnBenchmark")
        btnBenchmark?.addEventListener("click", fun(_: Event) {
            btnBenchmarkClicked()
        })

        loadWorkerInfoFromCookie()

        println("run initialized")
    }

    private fun loadWorkerInfoFromCookie() {
        val workerInfo = getCookie("workerinfo")
        val threadInfo = getCookie("threadinfo")
        if (workerInfo.isNotEmpty()) {
            val parts = workerInfo.split("#")
            for (p in parts) {
                if (p.isNotEmpty()) {
                    addWorker(p)
                }
            }
        } else {
            addWorker()
        }

        if (threadInfo.isNotEmpty()) {
            val tbxThreads = document.getElementById("tbxThreads") as HTMLInputElement
            tbxThreads.value = threadInfo
        }


    }

    private fun handleNextButtonClick() {
        redirectToUrl("results.html")
    }

    private fun btnBenchmarkClicked() {
        val taStatus = document.getElementById("taStatus") as HTMLTextAreaElement
        taStatus.removeClass("hidden")
        taStatus.textContent = ""
        taStatus.appendText("Benchmark process started\n")

        //Send worker info to backend
        taStatus.appendText("Transmit worker information to backend...")
        sendWorkerInfoToBackend()
    }

    private fun appendToStatus(message:String) {
        val taStatus = document.getElementById("taStatus") as HTMLTextAreaElement
        taStatus.appendText(message)
    }

    private fun sendWorkerInfoToBackend() {
        refreshWorkerObjects()

        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            workersetID = -1
            val parts = req.responseText.split(" ")
            if (parts.size == 6) {
                workersetID = parts[5].toInt()
                document.cookie = "workerset=${parts[5]}"
                appendToStatus("ok\n")
                checkStatusOfBackendWorker()
            } else {
                appendToStatus("Error: $text\n")
            }
        }
        req.open("POST", "/api/run/worker", true)
        req.send(JSON.stringify(worker))
    }

    private fun checkStatusOfBackendWorker() {
        appendToStatus("Ensure that all workers are waiting for new tasks...")
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                initWorkers()
            } else {
                appendToStatus("Error: $text\n")
            }
        }
        req.open("GET", "/api/run/ensureWorkerWaiting/$workersetID", true)
        req.send()
    }

    private fun initWorkers() {
        appendToStatus("Init workers...")
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                distributeWorkload()
            } else {
                appendToStatus("Error: $text\n")
            }
        }
        req.open("GET", "/api/run/initWorker/$workersetID", true)
        req.send()
    }

    private fun distributeWorkload() {
        appendToStatus("Distribute workload...")
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                startBenchmark()
            } else {
                appendToStatus("Error: $text\n")
            }
        }
        req.open("GET", "/api/run/distribute/$workersetID?workload=$workloadID", true)
        req.send()
    }

    private fun startBenchmark() {
        appendToStatus("Start benchmark...")
        benchmarkRunFinished = false
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                appendToStatus("Messages from workers:\n")
                notificationListenerID = window.setInterval({receiveNotifications()}, 1000)
            } else {
                appendToStatus("Error: $text\n")
            }
        }
        req.open("GET", "/api/run/start/$workersetID", true)
        req.send()
    }

    private fun receiveNotifications() {
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            val notifications = JSON.parse<Array<ServerNotifcation>>(text)
            for (n in notifications) {
                if (n.message.contains("All workers finished") && !benchmarkRunFinished){
                    benchmarkRunFinished = true
                    appendToStatus(n.message + "\n")
                    window.clearInterval(notificationListenerID)
                    collectResults()
                } else {
                    if (!benchmarkRunFinished) {
                        appendToStatus("Worker${n.workerID}: ${n.message}\n")
                    }
                }
            }
        }
        req.open("GET", "/api/run/notification/$workersetID", true)
        req.send()
    }

    private fun collectResults() {
        appendToStatus("Collecting results...")

        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            measurementsID = -1
            val parts = req.responseText.split(" ")
            if (parts.size == 6) {
                measurementsID = parts[5].toInt()
                document.cookie = "measurementsID=${parts[5]}"
                appendToStatus("ok\n")
                showNextButton()
            } else {
                appendToStatus("Error: $text\n")
            }
        }
        req.open("GET", "/api/run/collect/$workersetID", true)
        req.send()
    }

    private fun showNextButton() {
        val btnNext = document.getElementById("button_next") as HTMLButtonElement
        btnNext.removeClass("hidden")
    }

    private fun addWorker(url:String = "") {
        val divWorkers = document.getElementById("divWorkers") as HTMLDivElement

        val divWorker = document.createElement("div") as HTMLDivElement
        divWorker.id = "worker$nextWorkerID"
        divWorker.addClass("addmargin")

        val lblWorkerURL = document.createElement("label") as HTMLLabelElement
        lblWorkerURL.innerHTML = "Worker URL (and port):"
        lblWorkerURL.addClass("horizontalSpace")

        val tbxURL = document.createElement("input") as HTMLInputElement
        tbxURL.id = "tbxURLWorker$nextWorkerID"
        tbxURL.value = url
        tbxURL.addClass("horizontalSpace")

        val lblStatus = document.createElement("label") as HTMLLabelElement
        lblStatus.id = "lblStatusWorker$nextWorkerID"
        lblStatus.addClass("horizontalSpace")
        lblStatus.innerHTML = "X"

        val btnRemove = document.createElement("button") as HTMLButtonElement
        btnRemove.addClass("horizontalSpace")
        btnRemove.innerHTML = "Remove Worker"
        btnRemove.addEventListener("click", fun(_: Event) {
            removeWorker(divWorker.id)
        })

        divWorker.appendChild(lblWorkerURL)
        divWorker.appendChild(tbxURL)
        divWorker.appendChild(lblStatus)
        divWorker.appendChild(btnRemove)

        divWorkers.appendChild(divWorker)

        nextWorkerID++

    }

    private fun removeWorker(workerID:String) {
        val divWorkers = document.getElementById("divWorkers") as HTMLDivElement
        val divWorkerToDelete = document.getElementById(workerID) as HTMLDivElement
        divWorkers.removeChild(divWorkerToDelete)
    }

    private fun refreshWorkerObjects() {
        val workerlist = ArrayList<Worker>()
        val tbxThreads = document.getElementById("tbxThreads") as HTMLInputElement

        val divWorkers = document.getElementById("divWorkers") as HTMLDivElement
        var workerCookie = ""
        val threadCookie:String = tbxThreads.value
        for (c in divWorkers.children.asList()) {
            val w = c as HTMLDivElement
            val id = w.id.substring(6)
            val tbxUrl = document.getElementById("tbxURLWorker$id") as HTMLInputElement

            val tmpWorker = Worker()
            tmpWorker.id = id.toInt()
            tmpWorker.url = tbxUrl.value
            workerCookie += tmpWorker.url + "#"
            tmpWorker.threads = tbxThreads.value.toInt()
            workerlist.add(tmpWorker)
        }

        document.cookie = "workerinfo=$workerCookie"
        document.cookie = "threadinfo=$threadCookie"

        worker = workerlist.toTypedArray()
    }

    private fun refreshWorkerStatus() {
        refreshWorkerObjects()
        for (w in worker.asList()) {
            //Request status from backend
            val url = "/api/run/workerstatus?url=${w.url}"
            val req = XMLHttpRequest()
            req.onloadend = fun(_: Event) {
                val answer = req.responseText

                val lblWorkerStatus = document.getElementById("lblStatusWorker${w.id}") as HTMLLabelElement

                with(answer) {
                    when {
                        contains("Error") -> {
                            println("ANSWER: " + req.responseText)
                            lblWorkerStatus.textContent = "E"
                        }
                        contains("waiting") -> lblWorkerStatus.textContent = "W"
                        contains("running") -> lblWorkerStatus.textContent = "R"
                        else -> lblWorkerStatus.textContent = "O"
                    }
                }
            }
            req.open("GET",  url, true)
            req.send()
        }
    }
}
