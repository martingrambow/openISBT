import dataobjects.ResourceMapping
import dataobjects.ServerNotifcation
import measurement.PatternMeasurement
import measurements.BoxPlotValues
import measurements.MeasurementContainer
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
import kotlin.js.Json
import kotlin.random.Random

class results {

    var measurementsID:Int = -1
    var rawMeasurements:Array<PatternMeasurement> = arrayOf()

    fun init() {
        println("Init results called")

        val btnDownload = document.getElementById("btnDownload")
        btnDownload?.addEventListener("click", fun(event: Event) {
            btnDownloadClicked()

        })

        //There must be results; if not redirect to generate
        measurementsID = getCookie("measurementsID").toInt()
        println("MeasurementsID is " + measurementsID)
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            if (req.responseText.contains("not found") && req.responseText.length < 15) {
                //workload not found
                redirectToUrl("generate.html")
            } else {
                rawMeasurements = JSON.parse<Array<PatternMeasurement>>(req.responseText)
                showDownloadButton()
                generateAndShowTable()
            }

        }
        req.open("GET", "http://" + Backend.url + ":8080/api/results/" + measurementsID, true)
        req.send()

        println("run initialized")
    }

    fun generateAndShowTable() {

        var container = MeasurementContainer()
        for (m in rawMeasurements) {
            var resource = m.resource
            if (!container.isResource(resource)) {
                container.addResource(resource)
            }
            var pattern = m.patternName
            if (!container.isPattern(resource, pattern)) {
                container.addPattern(resource, pattern)
            }
            for (n in m.apiRequestMeasurements) {
                var operation = n.abstractOperation
                if (!container.isOperation(resource, pattern, operation)) {
                    container.addOperation(resource, pattern, operation, n.index)
                }
                var method = n.path
                if (!container.isMethod(resource, pattern, operation, method)) {
                    container.addMethod(resource, pattern, operation, method, BoxPlotValues(getDurations(resource, pattern, operation, method)))
                }
            }
        }

        var divTable = document.getElementById("divTable") as HTMLDivElement

        val table = document.createElement("table") as HTMLTableElement

        table.appendChild(getTableHeader())

        var lastResource = ""
        var lastPattern = ""
        var lastOperation = ""
        var lastMethod = ""

        for (resource in container.measuremnts.keys) {
            for (pattern in container.measuremnts.getValue(resource).keys) {
                for (o in container.measuremnts.getValue(resource).getValue(pattern)) {
                    var newResource = false
                    if (resource != lastResource) {
                        newResource = true
                    }

                    if (pattern!= lastPattern || resource != lastResource) {
                        //Insert Total pattern line
                        var total = BoxPlotValues(getDurations(resource, pattern, "total", ""))
                        var totalrow = getTableRow(resource, pattern, "total", "", total)
                        totalrow.addClass("totalPatternRow")
                        if (newResource) {
                            totalrow.addClass("newResource")
                        }
                        table.appendChild(totalrow)
                        lastResource = resource
                        lastPattern = pattern
                    }
                    var operation = o.first
                    if (o.second.entries.size > 1) {
                        //Multiple methods, insert total line
                        var total = BoxPlotValues(getDurations(resource, pattern, operation, "total"))
                        var cResource = if (resource != lastResource) resource else ""
                        var cPattern = if (pattern != lastPattern) pattern else ""
                        var totalrow = getTableRow(cResource, cPattern, operation, "total", total)
                        totalrow.addClass("totalMethodRow")
                        table.appendChild(totalrow)
                        lastOperation = operation
                    }
                    for (method in o.second.entries) {
                        var cResource = if (resource != lastResource) resource else ""
                        var cPattern = if (pattern != lastPattern) pattern else ""
                        var cOperation = if (operation != lastOperation) operation else ""
                        var cMethod = method.key
                        table.appendChild(getTableRow(cResource, cPattern, cOperation, cMethod, method.value))
                        lastResource = resource
                        lastPattern = pattern
                        lastOperation = operation
                        lastMethod = method.key
                    }
                }
            }
        }

        table.addClass("resultsTable")


        divTable.innerHTML = ""
        divTable.appendChild(table)


        //var t = BoxPlotValues(getDurations("/user", "CRE", "total", ""))
        //println("Boxplot1: " + JSON.stringify(t))

        //t = BoxPlotValues(getDurations("/user", "CRE", "CREATE", "total"))
        //println("Boxplot2: " + JSON.stringify(t))

        //t = BoxPlotValues(getDurations("/user", "CRE", "CREATE", "/user/createWithList"))
        //println("Boxplot3: " + JSON.stringify(t))

    }

    fun getTableHeader() : HTMLTableRowElement {
        val header = document.createElement("tr") as HTMLTableRowElement
        val column1 = document.createElement("th") as HTMLTableCellElement
        column1.innerHTML = "Resource"
        val column2 = document.createElement("th") as HTMLTableCellElement
        column2.innerHTML = "Pattern"
        val column3 = document.createElement("th") as HTMLTableCellElement
        column3.innerHTML = "Operation"
        val column4 = document.createElement("th") as HTMLTableCellElement
        column4.innerHTML = "Path"
        val column5 = document.createElement("th") as HTMLTableCellElement
        column5.innerHTML = "Min"
        val column6 = document.createElement("th") as HTMLTableCellElement
        column6.innerHTML = "Q1"
        val column7 = document.createElement("th") as HTMLTableCellElement
        column7.innerHTML = "Med"
        val column8 = document.createElement("th") as HTMLTableCellElement
        column8.innerHTML = "Q3"
        val column9 = document.createElement("th") as HTMLTableCellElement
        column9.innerHTML = "Max"
        val column10 = document.createElement("th") as HTMLTableCellElement
        column10.innerHTML = "n"

        header.appendChild(column1)
        header.appendChild(column2)
        header.appendChild(column3)
        header.appendChild(column4)
        header.appendChild(column5)
        header.appendChild(column6)
        header.appendChild(column7)
        header.appendChild(column8)
        header.appendChild(column9)
        header.appendChild(column10)

        header.addClass("headline")

        return header

    }

    fun getTableRow(resource: String, pattern: String, operation: String, method: String, boxPlotValues: BoxPlotValues) : HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement
        val column1 = document.createElement("th") as HTMLTableCellElement
        column1.innerHTML = resource
        val column2 = document.createElement("th") as HTMLTableCellElement
        column2.innerHTML = pattern
        val column3 = document.createElement("th") as HTMLTableCellElement
        column3.innerHTML = operation
        val column4 = document.createElement("th") as HTMLTableCellElement
        column4.innerHTML = method
        val column5 = document.createElement("th") as HTMLTableCellElement
        column5.innerHTML = boxPlotValues.min.toString()
        val column6 = document.createElement("th") as HTMLTableCellElement
        column6.innerHTML = boxPlotValues.lowerQuartile.toString()
        val column7 = document.createElement("th") as HTMLTableCellElement
        column7.innerHTML = boxPlotValues.median.toString()
        val column8 = document.createElement("th") as HTMLTableCellElement
        column8.innerHTML = boxPlotValues.upperQuartile.toString()
        val column9 = document.createElement("th") as HTMLTableCellElement
        column9.innerHTML = boxPlotValues.max.toString()
        val column10 = document.createElement("th") as HTMLTableCellElement
        column10.innerHTML = boxPlotValues.size.toString()

        row.appendChild(column1)
        row.appendChild(column2)
        row.appendChild(column3)
        row.appendChild(column4)
        row.appendChild(column5)
        row.appendChild(column6)
        row.appendChild(column7)
        row.appendChild(column8)
        row.appendChild(column9)
        row.appendChild(column10)

        row.addClass("resultRow")

        return row

    }

    fun getDurations(resource : String, pattern:String, operation:String, method:String) : Array<Int> {
        var values: ArrayList<Int> = ArrayList()

        println("Call with: " + resource + ", " + pattern + ", " + operation + ", " + method)

        for (m in rawMeasurements) {
            if (resource == m.resource) {
                if (pattern == m.patternName) {
                    if (operation == "total") {
                        values.add(getDuration(m.start, m.end))
                    } else {
                        for (n in m.apiRequestMeasurements) {
                            if (operation == n.abstractOperation) {
                                if (method == "total") {
                                    values.add(getDuration(n.start, n.end))
                                } else {
                                    if (method == n.path) {
                                        values.add(getDuration(n.start, n.end))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (values.size == 0) {
            println("EMPTY ARRAY FOR: " + resource + ", " + pattern + ", " + operation + ", " + method)
        }
        return values.toTypedArray()
    }

    private fun getDuration(start : Long, end : Long) : Int{
        var s = Date(start)
        var e = Date(end)
        var d = e.getTime() - s.getTime()
        return d.toInt()
    }

    fun showDownloadButton() {
        val btnDownload = document.getElementById("btnDownload")as HTMLButtonElement
        btnDownload.removeClass("hidden")

    }

    private fun btnDownloadClicked() {
        if (rawMeasurements != null) {
            var measurementsAsDownload = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(rawMeasurements))
            var downloadLink = document.getElementById("downloadLink") as HTMLElement
            downloadLink.setAttribute("href", measurementsAsDownload);
            downloadLink.setAttribute("download", "measurements.json");
            downloadLink.click();
        }
    }
}
