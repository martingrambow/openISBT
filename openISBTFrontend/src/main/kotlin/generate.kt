import dataobjects.ResourceMapping
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import workload.ApiRequest
import workload.PatternRequest
import workload.ProgressListener
import workload.WorkloadGenerator
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass

external fun encodeURIComponent(str: String): String

class generate : ProgressListener{

    var mappingID = -1
    var workloadID = -1
    var workload: Array<PatternRequest>? = null

    fun init() {
        println("Init generate called")
        val btnGenerate = document.getElementById("button_generate")
        btnGenerate?.addEventListener("click", fun(event: Event) {
            btnGenerateClicked()

        })

        val btnDownload = document.getElementById("btnDownload")
        btnDownload?.addEventListener("click", fun(event: Event) {
            btnDownloadClicked()

        })

        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(event: Event) {
            handleNextButtonClick()

        })

        val mappingStr = getCookie("mapping")
        if (mappingStr.length > 0) {
            mappingID = mappingStr.toInt()
            btnGenerate?.removeClass("hidden")
        }
        val workloadStr = getCookie("workload")
        if (workloadStr.length > 0) {
            workloadID = getCookie("workload").toInt()
        }

        if (workloadID != -1) {
            println("Try to load workload " + workloadID)
            getWorkload()
        }

        //Register eventlisteners which close the modal
        var span = document.getElementById("workloadDetailsClose");
        if (span != null) {
            span.addEventListener("click", fun(event: Event) {
                closeWorkloadDetails()
            })
        }

        window.addEventListener("click", fun(event: Event) {
            var modal = document.getElementById("workloadDetails");
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
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            if (text != "\"not found\"") {
                workload = JSON.parse<Array<PatternRequest>>(text)
                fillWorkloadTable()
            }
        }
        req.open("GET", "http://localhost:8080/api/workload/" + workloadID.toString(), true)
        req.send()
    }

    private fun btnGenerateClicked() {
        var btn = document.getElementById("button_generate") as HTMLButtonElement
        var bar = document.getElementById("progressbar") as HTMLDivElement
        if (btn != null && bar != null && mappingID != -1) {
            //show progressbar
            //btn.addClass("hidden")
            bar.removeClass("hidden")

            var divWorkload = document.getElementById("divWorkload") as HTMLDivElement
            var tblWorkload = document.getElementById("tblWorkload") as HTMLTableElement
            var btnDownload = document.getElementById("btnDownload") as HTMLButtonElement
            var btnNext = document.getElementById("button_next") as HTMLButtonElement

            btnDownload.addClass("hidden")
            btnNext.addClass("hidden")
            divWorkload.addClass("hidden")
            tblWorkload.addClass("hidden")

            //get ressource mappings
            val req = XMLHttpRequest()
            req.onloadend = fun(event: Event) {
                var text = req.responseText
                if (text != "\"not found\"") {
                    val mappings = JSON.parse<Array<ResourceMapping>>(text)
                    if (mappings != null) {
                        //Generate workload
                        var generator = WorkloadGenerator()
                        generator.listener = this
                        generator.generateWorkload(mappings)
                        workload = generator.getWorkload()
                        uploadWorkload()
                        //GlobalScope.launch {
                        //    generator.generateWorkloadAsync(mappings, {
                        //        workload = generator.workload
                        //        onWorkloadGenerated()
                        //    })
                        //}
                    }
                }
            }
            req.open("GET", "http://localhost:8080/api/mapping/" + mappingID, true)
            req.send()
        }
    }

    private fun btnDownloadClicked() {
        if (workload != null) {
            var workloadAsDownload = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(workload))
            var downloadLink = document.getElementById("downloadLink") as HTMLElement
            downloadLink.setAttribute("href", workloadAsDownload);
            downloadLink.setAttribute("download", "workload" + workloadID + ".json");
            downloadLink.click();
        }
    }

    override fun setProgress(percentage : Int) {
        //println("Called with " + percentage + "%")
        var elem = document.getElementById("progress") as HTMLDivElement
        if (percentage < 100) {
            elem.style.width = percentage.toString() + "%"
            elem.innerHTML = percentage.toString() + "%"
        } else {
            elem.style.width = "100%"
            elem.innerHTML = "Workload generated"
        }
    }

    private fun uploadWorkload() {
        if (workload != null) {
            //Send workload to backend
            val req = XMLHttpRequest()
            req.onloadend = fun(event: Event) {
                var text = req.responseText
                val parts = text.split(" ")
                if (parts.size == 6) {
                    document.cookie = "workload=" + parts[5]

                    //Show workload
                    fillWorkloadTable()
                }
            }
            req.open("POST", "http://localhost:8080/api/workload", true)
            req.send(JSON.stringify(workload))
        }
    }

    private fun fillWorkloadTable() {
        if (workload != null) {
            val tmpWL = workload
            var divWorkload = document.getElementById("divWorkload") as HTMLDivElement
            var tblWorkload = document.getElementById("tblWorkload") as HTMLTableElement
            var btnDownload = document.getElementById("btnDownload") as HTMLButtonElement
            var btnNext = document.getElementById("button_next") as HTMLButtonElement

            btnDownload.removeClass("hidden")
            btnNext.removeClass("hidden")
            divWorkload.removeClass("hidden")
            tblWorkload.removeClass("hidden")
            tblWorkload.addClass("workloadTable")

            //Remove all lines except the headline
            var lines = document.getElementsByClassName("workloadRow").asList();
            while (lines.size > 0 && lines.get(0) != null){
                lines.get(0).remove()
            }

            if (tmpWL != null) {
                for (wl in tmpWL) {
                    val row = buildWorkloadRow(wl)
                    if (tblWorkload != null) {
                        tblWorkload.appendChild(row)
                    }
                }
            }
        }
    }

    private fun buildWorkloadRow(request : PatternRequest) : HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement
        row.addClass("workloadRow")
        row.addClass("pointable")

        row.addEventListener("click", fun(event: Event) {
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
        var modal = document.getElementById("workloadDetails");

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
                if (detailsDiv != null) {
                    detailsDiv.appendChild(elem)
                }
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

    fun closeWorkloadDetails() {
        // Get the modal
        var modal = document.getElementById("workloadDetails");
        if (modal != null) {
            //Hide modal
            modal.setAttribute("style", "display: none")
        }

    }

}
