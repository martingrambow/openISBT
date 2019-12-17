import dataobjects.ServerNotifcation
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.workers.ServiceWorker
import org.w3c.xhr.XMLHttpRequest
import worker.Worker
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.appendText
import kotlin.dom.removeClass
import kotlin.js.Date
import kotlin.random.Random

class run {

    var nextWorkerID = 1
    var workersetID = -1
    var workloadID:Int = -1
    var measurementsID:Int = -1

    var notificationListenerID:Int = -1
    var benchmarkRunFinished = true

    var worker: Array<Worker> = arrayOf()

    fun init() {
        println("Init run called")

        //There must be a workload; if not redirect to generate
        workloadID = getCookie("workload").toInt()
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            if (req.responseText.contains("not found") && req.responseText.length < 15) {
                //workload not found
                redirectToUrl("generate.html")
            }
        }
        req.open("GET", "/api/workload/" + workloadID, true)
        req.send()

        //Get URL(s) of SUT from oas file
        val req2 = XMLHttpRequest()
        val oas = getCookie("oasFile")
        req2.onloadend = fun(event: Event) {
            if (!req2.responseText.contains("not found")) {
                //Fill tbxEndpoint
                var tbxEndpoint = document.getElementById("tbxEndpoint") as HTMLInputElement
                var parts = req2.responseText.split("\"")
                for (p in parts) {
                    if (p.startsWith("http")){
                        tbxEndpoint.value = p
                    }
                }
            }
        }
        req2.open("GET", "/api/oasFiles/" + oas + "/endpoints", true)
        req2.send()

        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(event: Event) {
            handleNextButtonClick()

        })

        val btnAddWorker = document.getElementById("btnAddWorker")
        btnAddWorker?.addEventListener("click", fun(event: Event) {
            addWorker()
        })

        val btnRefrehStatus = document.getElementById("btnRefresh")
        btnRefrehStatus?.addEventListener("click", fun(event: Event) {
            refreshWorkerStatus()
        })

        val btnBenchmark = document.getElementById("btnBenchmark")
        btnBenchmark?.addEventListener("click", fun(event: Event) {
            btnBenchmarkClicked()
        })

        loadWorkerInfoFromCookie()

        println("run initialized")
    }

    private fun loadWorkerInfoFromCookie() {
        var workerInfo = getCookie("workerinfo")
        var threadInfo = getCookie("threadinfo")
        if (workerInfo.length > 0) {
            var parts = workerInfo.split("#")
            for (p in parts) {
                if (p.length > 0) {
                    addWorker(p)
                }
            }
        } else {
            addWorker()
        }

        if (threadInfo.length > 0) {
            var tbxThreads = document.getElementById("tbxThreads") as HTMLInputElement
            tbxThreads.value = threadInfo
        }


    }

    private fun handleNextButtonClick() {
        redirectToUrl("results.html")
    }

    private fun btnBenchmarkClicked() {
        var taStatus = document.getElementById("taStatus") as HTMLTextAreaElement
        taStatus.removeClass("hidden")
        taStatus.textContent = ""
        taStatus.appendText("Benchmark process started\n")

        //Send worker info to backend
        taStatus.appendText("Transmit worker information to backend...")
        sendWorkerInfoToBackend()
    }

    private fun appendToStatus(message:String) {
        var taStatus = document.getElementById("taStatus") as HTMLTextAreaElement
        taStatus.appendText(message)
    }

    private fun sendWorkerInfoToBackend() {
        refreshWorkerObjects()

        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            workersetID = -1
            val parts = req.responseText.split(" ")
            if (parts.size == 6) {
                workersetID = parts[5].toInt()
                document.cookie = "workerset=" + parts[5]
                appendToStatus("ok\n")
                checkStatusOfBackendWorker()
            } else {
                appendToStatus("Error: " + text + "\n")
            }
        }
        req.open("POST", "/api/run/worker", true)
        req.send(JSON.stringify(worker))
    }

    private fun checkStatusOfBackendWorker() {
        appendToStatus("Ensure that all workers are waiting for new tasks...")
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                initWorkers()
            } else {
                appendToStatus("Error: " + text + "\n")
            }
        }
        req.open("GET", "/api/run/ensureWorkerWaiting/" + workersetID, true)
        req.send()
    }

    private fun initWorkers() {
        appendToStatus("Init workers...")
        var tbxEndpoint = document.getElementById("tbxEndpoint") as HTMLInputElement
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                distributeWorkload()
            } else {
                appendToStatus("Error: " + text + "\n")
            }
        }
        req.open("GET", "/api/run/initWorker/" + workersetID + "?endpoint=" + tbxEndpoint.value, true)
        req.send()
    }

    private fun distributeWorkload() {
        appendToStatus("Distribute workload...")
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                startBenchmark()
            } else {
                appendToStatus("Error: " + text + "\n")
            }
        }
        req.open("GET", "/api/run/distribute/" + workersetID + "?workload=" + workloadID, true)
        req.send()
    }

    private fun startBenchmark() {
        appendToStatus("Start benchmark...")
        benchmarkRunFinished = false
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            if (text == "OK") {
                appendToStatus("ok\n")
                appendToStatus("Messages from workers:\n")
                notificationListenerID = window.setInterval({receiveNotifications()}, 1000)
            } else {
                appendToStatus("Error: " + text + "\n")
            }
        }
        req.open("GET", "/api/run/start/" + workersetID, true)
        req.send()
    }

    fun receiveNotifications() {
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            var notifications = JSON.parse<Array<ServerNotifcation>>(text)
            for (n in notifications) {
                if (n.message.contains("All workers finished") && !benchmarkRunFinished){
                    benchmarkRunFinished = true
                    appendToStatus(n.message + "\n")
                    window.clearInterval(notificationListenerID)
                    collectResults()
                } else {
                    if (!benchmarkRunFinished) {
                        appendToStatus("Worker" + n.workerID + ": " + n.message + "\n")
                    }
                }
            }
        }
        req.open("GET", "/api/run/notification/" + workersetID, true)
        req.send()
    }

    private fun collectResults() {
        appendToStatus("Collecting results...")

        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            measurementsID = -1
            val parts = req.responseText.split(" ")
            if (parts.size == 6) {
                measurementsID = parts[5].toInt()
                document.cookie = "measurementsID=" + parts[5]
                appendToStatus("ok\n")
                showNextButton()
            } else {
                appendToStatus("Error: " + text + "\n")
            }
        }
        req.open("GET", "/api/run/collect/" + workersetID, true)
        req.send()
    }

    private fun showNextButton() {
        var btnNext = document.getElementById("button_next") as HTMLButtonElement
        btnNext.removeClass("hidden")
    }

    fun addWorker(url:String = "") {
        var divWorkers = document.getElementById("divWorkers") as HTMLDivElement

        var divWorker = document.createElement("div") as HTMLDivElement
        divWorker.id = "worker" + nextWorkerID
        divWorker.addClass("addmargin")

        var lblWorkerURL = document.createElement("label") as HTMLLabelElement
        lblWorkerURL.innerHTML = "Worker URL (and port):"
        lblWorkerURL.addClass("horizontalSpace")

        var tbxURL = document.createElement("input") as HTMLInputElement
        tbxURL.id = "tbxURLWorker" + nextWorkerID.toString()
        tbxURL.value = url
        tbxURL.addClass("horizontalSpace")

        var lblStatus = document.createElement("label") as HTMLLabelElement
        lblStatus.id = "lblStatusWorker" + nextWorkerID.toString()
        lblStatus.addClass("horizontalSpace")
        lblStatus.innerHTML = "X"

        var btnRemove = document.createElement("button") as HTMLButtonElement
        btnRemove.addClass("horizontalSpace")
        btnRemove.innerHTML = "Remove Worker"
        btnRemove.addEventListener("click", fun(event: Event) {
            removeWorker(divWorker.id)
        })

        divWorker.appendChild(lblWorkerURL)
        divWorker.appendChild(tbxURL)
        divWorker.appendChild(lblStatus)
        divWorker.appendChild(btnRemove)

        divWorkers.appendChild(divWorker)

        nextWorkerID++;

    }

    fun removeWorker(workerID:String) {
        var divWorkers = document.getElementById("divWorkers") as HTMLDivElement
        var divWorkerToDelete = document.getElementById(workerID) as HTMLDivElement
        divWorkers.removeChild(divWorkerToDelete)
    }

    fun refreshWorkerObjects() {
        var workerlist = ArrayList<Worker>()
        var tbxThreads = document.getElementById("tbxThreads") as HTMLInputElement

        var divWorkers = document.getElementById("divWorkers") as HTMLDivElement
        var workerCookie:String = ""
        var threadCookie:String = tbxThreads.value
        for (c in divWorkers.children.asList()) {
            if (c != null) {
                var w = c as HTMLDivElement
                var id = w.id.substring(6)
                var tbxUrl = document.getElementById("tbxURLWorker" + id) as HTMLInputElement

                var tmpWorker = Worker()
                tmpWorker.id = id.toInt()
                tmpWorker.url = tbxUrl.value
                workerCookie += tmpWorker.url + "#"
                tmpWorker.threads = tbxThreads.value.toInt()
                workerlist.add(tmpWorker)
            }
        }

        document.cookie = "workerinfo=" + workerCookie
        document.cookie = "threadinfo=" + threadCookie

        worker = workerlist.toTypedArray()
    }

    fun refreshWorkerStatus() {
        refreshWorkerObjects()
        for (w in worker.asList()) {
            //Request status from backend
            var url = "/api/run/workerstatus?url=" + w.url
            val req = XMLHttpRequest()
            req.onloadend = fun(event: Event) {
                var answer = req.responseText

                var lblWorkerStatus = document.getElementById("lblStatusWorker" + w.id) as HTMLLabelElement

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
