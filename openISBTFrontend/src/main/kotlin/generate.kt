import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import workload.ApiRequest
import workload.PatternRequest
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass

external fun encodeURIComponent(str: String): String

class generate {

    private var mappingID = -1
    private var workloadID = -1
    private var workload: Array<PatternRequest>? = null

    private var notificationListenerID:Int = -1
    private var generationFinished = false

    fun init() {
        println("Init generate called")
        val btnGenerate = document.getElementById("button_generate")
        btnGenerate?.addEventListener("click", fun(_: Event) {
            btnGenerateClicked()

        })

        val btnDownload = document.getElementById("btnDownload")
        btnDownload?.addEventListener("click", fun(_: Event) {
            btnDownloadClicked()

        })

        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(_: Event) {
            handleNextButtonClick()

        })

        val mappingStr = getCookie("mapping")
        if (mappingStr.isNotEmpty()) {
            mappingID = mappingStr.toInt()
            btnGenerate?.removeClass("hidden")
        }
        val workloadStr = getCookie("workload")
        if (workloadStr.isNotEmpty()) {
            workloadID = getCookie("workload").toInt()
        }

        if (workloadID != -1) {
            println("Try to load workload $workloadID")
            getWorkload()
        }

        //Register eventlisteners which close the modal
        document.getElementById("workloadDetailsClose")?.addEventListener("click", fun(_: Event) {
            closeWorkloadDetails()
        })

        window.addEventListener("click", fun(event: Event) {
            val modal = document.getElementById("workloadDetails")
            if (modal != null) {
                if (event.target == modal) {
                    closeWorkloadDetails()
                }
            }
        })

        println("Generate initialized")
    }

    private fun handleNextButtonClick() {
        redirectToUrl("run.html")
    }

    private fun getWorkload() {
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            if (text != "\"not found\"") {
                workload = JSON.parse<Array<PatternRequest>>(text)
                fillWorkloadTable()
            }
        }
        req.open("GET", "/api/workload/$workloadID", true)
        req.send()
    }

    private fun btnGenerateClicked() {
        val bar = document.getElementById("progressbar") as HTMLDivElement
        if (mappingID != -1) {
            //show progressbar
            //btn.addClass("hidden")
            bar.removeClass("hidden")

            val divWorkload = document.getElementById("divWorkload") as HTMLDivElement
            val tblWorkload = document.getElementById("tblWorkload") as HTMLTableElement
            val btnDownload = document.getElementById("btnDownload") as HTMLButtonElement
            val btnNext = document.getElementById("button_next") as HTMLButtonElement

            btnDownload.addClass("hidden")
            btnNext.addClass("hidden")
            divWorkload.addClass("hidden")
            tblWorkload.addClass("hidden")

            //Start workload generation
            generationFinished = false
            val req1 = XMLHttpRequest()
            req1.onloadend = fun(_: Event) {
                val text = req1.responseText
                val parts = text.split(" ")
                if (parts.size == 6) {
                    document.cookie = "workload=" + parts[5]
                    workloadID = parts[5].toInt()
                    notificationListenerID = window.setInterval({refreshGenerationStatus()}, 1000)
                }
            }
            req1.open("GET", "/api/workload/generate/$mappingID", true)
            req1.send()

        }
    }

    private fun refreshGenerationStatus() {
        val req = XMLHttpRequest()
        req.onloadend = fun(_: Event) {
            val text = req.responseText
            setProgress(text.toInt())
        }
        req.open("GET", "/api/workload/status/$workloadID", true)
        req.send()
    }

    private fun btnDownloadClicked() {
        if (workload != null) {
            val workloadAsDownload = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(workload))
            val downloadLink = document.getElementById("downloadLink") as HTMLElement
            downloadLink.setAttribute("href", workloadAsDownload)
            downloadLink.setAttribute("download", "workload$workloadID.json")
            downloadLink.click()
        }
    }

    private fun setProgress(percentage : Int) {
        //println("Called with " + percentage + "%")
        val elem = document.getElementById("progress") as HTMLDivElement
        if (percentage < 100) {
            elem.style.width = "$percentage%"
            elem.innerHTML = "$percentage%"
        } else {
            elem.style.width = "100%"
            elem.innerHTML = "Workload generated"
            generationFinished = true
            fillWorkloadTable()
            getWorkload()
        }
    }

    private fun fillWorkloadTable() {
        if (workload != null) {
            val tmpWL = workload
            val divWorkload = document.getElementById("divWorkload") as HTMLDivElement
            val tblWorkload = document.getElementById("tblWorkload") as HTMLTableElement
            val btnDownload = document.getElementById("btnDownload") as HTMLButtonElement
            val btnNext = document.getElementById("button_next") as HTMLButtonElement

            btnDownload.removeClass("hidden")
            btnNext.removeClass("hidden")
            divWorkload.removeClass("hidden")
            tblWorkload.removeClass("hidden")
            tblWorkload.addClass("workloadTable")

            //Remove all lines except the headline
            val lines = document.getElementsByClassName("workloadRow").asList()
            while (lines.isNotEmpty()){
                lines[0].remove()
            }

            if (tmpWL != null) {
                for (wl in tmpWL) {
                    val row = buildWorkloadRow(wl)
                    tblWorkload.appendChild(row)
                }
            }
        }
    }

    private fun buildWorkloadRow(request : PatternRequest) : HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement
        row.addClass("workloadRow")
        row.addClass("pointable")

        row.addEventListener("click", fun(_: Event) {
            clickRequestForDetails(request)
        })

        //Append id column
        val idCell = document.createElement("td") as HTMLTableCellElement
        val idLabel = document.createElement("p") as HTMLParagraphElement
        idLabel.innerHTML = request.id.toString()
        idCell.appendChild(idLabel)

        //Append Pattern name column
        val patternCell = document.createElement("td") as HTMLTableCellElement
        val patternLabel = document.createElement("p") as HTMLParagraphElement
        patternLabel.innerHTML = request.abstractPattern.name
        patternCell.appendChild(patternLabel)

        //Append Request number column
        val requestsCell = document.createElement("td") as HTMLTableCellElement
        val requestsLabel = document.createElement("p") as HTMLParagraphElement

        requestsLabel.innerHTML = request.apiRequests.size.toString()
        requestsCell.appendChild(requestsLabel)

        //Append all column elements
        row.appendChild(idCell)
        row.appendChild(patternCell)
        row.appendChild(requestsCell)

        return row
    }

    private fun clickRequestForDetails(request: PatternRequest) {
        // Get the modal
        val modal = document.getElementById("workloadDetails")

        if (modal != null) {
            //Fill modal
            val detailsDiv = document.getElementById("workloadDetailsDiv")
            //Remove all childs
            if (detailsDiv != null) {
                detailsDiv.innerHTML = ""
            }

            //Fill div
            for (request in request.apiRequests) {
                val elem = buildApiRequestDiv(request)
                detailsDiv?.appendChild(elem)
            }

            //Show modal
            modal.setAttribute("style", "display: block")
        }
    }

    private fun buildApiRequestDiv(request: ApiRequest): Node {
        //Create div element
        val requestDiv = document.createElement("div") as HTMLDivElement

        requestDiv.appendChild(getParagraph("Path: " + request.path))
        requestDiv.appendChild(getParagraph("Method: " + request.method))
        for (p in request.parameter) {
            requestDiv.appendChild(getParagraph("Parameter " + p.first + ": " + JSON.stringify(p.second)))
        }
        for (h in request.headers) {
            requestDiv.appendChild(getParagraph("Header " + h.first + ": " + JSON.stringify(h.second)))
        }
        if (request.body != null) {
            requestDiv.appendChild(getParagraph("Body: " + JSON.stringify(request.body)))
        }
        requestDiv.addClass("requestDetailsDiv")
        return requestDiv
    }

    private fun getParagraph(text: String) : HTMLParagraphElement {
        val p = document.createElement("p") as HTMLParagraphElement
        p.innerHTML = text
        return p
    }

    private fun closeWorkloadDetails() {
        // Get the modal
        document.getElementById("workloadDetails")?.setAttribute("style", "display: none")
    }
}
