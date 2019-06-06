
import dataobjects.ParameterObject
import dataobjects.Pattern
import dataobjects.PatternOperation
import dataobjects.ResourceMapping
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.hasClass
import kotlin.dom.removeClass

class check {

    var mappingID:Int = -1

    fun init() {
        println("Init check called")

        //Get spec and config; and produce mapping
        val oas = getCookie("oasFile")
        val patternConf = getCookie("patternConfig")
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            //parse mapping ID
            mappingID = -1
            val parts = req.responseText.split(" ")
            if (parts.size == 6) {
                mappingID = parts[5].toInt()
                document.cookie = "mapping=" + parts[5]
            }

            if (mappingID != -1) {
                reloadPatternTable()
            }
        }
        req.open("GET", "http://localhost:8080/api/mapping?oasFile=" + oas + "&patternConfig=" + patternConf, true)
        req.send()

        //Register eventlisteners which close the modal
        var span = document.getElementById("patternDetailsClose");
        if (span != null) {
            span.addEventListener("click", fun(event: Event) {
                closePatternDetails()
            })
        }

        window.addEventListener("click", fun(event: Event) {
            var modal = document.getElementById("patternDetails");
            if (modal != null) {
                if (event.target == modal) {
                    closePatternDetails()
                }
            }
        })

        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(event: Event) {
            handleNextButtonClick()

        })

        println("check initialized")
    }

    fun reloadPatternTable() {
        //Check if there is a valid mapping
        if (mappingID != -1) {
            var table = document.getElementById("tblmapping")

            //Remove all lines except the headline
            var lines = document.getElementsByClassName("patternline").asList();
            while (lines.size > 0 && lines.get(0) != null){
                lines.get(0).remove()
            }

            //Get mapping from server
            val req2 = XMLHttpRequest()
            req2.onloadend = fun(event: Event) {
                var text = req2.responseText
                if (text != "not found") {
                    //Transform to HTML elements and append to table
                    //println("TEXT:" + text)
                    val mappings = JSON.parse<Array<ResourceMapping>>(text)
                    for (m in mappings) {
                        //println("Build row for:")
                        //println(JSON.stringify(m))
                        val row = buildMappingRow(m)
                        if (table != null) {
                            table.appendChild(row)
                        }
                    }
                }
            }
            req2.open("GET", "http://localhost:8080/api/mapping/" + mappingID, true)
            req2.send()
        }

    }


    fun buildMappingRow(mapping:ResourceMapping):HTMLTableRowElement {
        //Create row and set its color
        val row = document.createElement("tr") as HTMLTableRowElement
        if (mapping.supported) {
            row.addClass("supportedRow")
        } else {
            row.addClass("notSupportedRow")
        }

        //Append path column
        val path = document.createElement("td") as HTMLTableCellElement
        val pathName = document.createElement("p") as HTMLParagraphElement
        pathName.innerHTML = mapping.resourcePath
        path.appendChild(pathName)

        //Append Pattern column
        val pattern = document.createElement("td") as HTMLTableCellElement
        val patterncell = document.createElement("div") as HTMLDivElement
        for (a in mapping.patternMappingList) {
            val patternName = document.createElement("p") as HTMLParagraphElement
            if (a.supported) {
                //Show Modal if it's supported
                patternName.addClass("pointable")
                patternName.addEventListener("click", fun(event: Event) {
                    clickPatternName(mapping.resourcePath, a)
                })
            }
            var patternNameText = a.aPattern.name
            if (a.supported) {
                //Append number of requests if it's supported
                if (mapping.supported && mapping.enabled) {
                    patternNameText += " (" + a.requests + ")"
                }
                patternName.addClass("supported")
            } else {
                patternName.addClass("notSupported")
            }
            patternName.innerHTML = patternNameText
            patterncell.appendChild(patternName)
        }
        pattern.appendChild(patterncell)

        //Append Benchmark(Checkbox) column
        val benchmark = document.createElement("td") as HTMLTableCellElement
        val cbxbenchmark = document.createElement("input") as HTMLInputElement
        cbxbenchmark.type="checkbox"
        cbxbenchmark.checked = mapping.supported && mapping.enabled
        cbxbenchmark.addEventListener("click", fun(event: Event) {
            cbxBenchmarkClick(cbxbenchmark, mapping.resourcePath)
        })
        if (!mapping.supported) {
            cbxbenchmark.disabled = true
        }
        benchmark.appendChild(cbxbenchmark)

        //Append total number of requests column
        val requests = document.createElement("td") as HTMLTableCellElement
        requests.innerHTML = mapping.numberOfRequests.toString()

        //Append all column elements
        row.appendChild(path)
        row.appendChild(pattern)
        row.appendChild(benchmark)
        row.appendChild(requests)

        row.addClass("patternline")

        return row
    }

    fun clickPatternName(path:String, pattern: Pattern) {
        println("Clicked on " + path  + " " + pattern.aPattern.name)

        // Get the modal
        var modal = document.getElementById("patternDetails");

        if (modal != null) {
            //Fill pattern name
            val patternNameElemement = document.getElementById("details_patternName") as HTMLParagraphElement
            patternNameElemement.innerHTML = pattern.aPattern.name

            val detailsTable = document.getElementById("tbldetails")
            //Remove all lines except the headline
            var lines = document.getElementsByClassName("detailsline").asList();
            while (lines.size > 0 && lines.get(0) != null){
                lines.get(0).remove()
            }

            //Fill table
            for (operation in pattern.operationSequence) {
                val row = buildDetailPatternRow(operation)
                if (detailsTable != null) {
                    detailsTable.appendChild(row)
                }
            }

            //Show modal
            modal.setAttribute("style", "display: block")
        }

    }

    fun buildDetailPatternRow(operations:Array<PatternOperation>):HTMLTableRowElement {
        //Create row element
        val row = document.createElement("tr") as HTMLTableRowElement

        //Operation cell on the left
        val operationCell = document.createElement("td") as HTMLTableCellElement
        operationCell.innerHTML = operations[0].aPatternOperation;

        //Mapping cell on the right
        val mappingCell = document.createElement("td") as HTMLTableCellElement
        //Mapping cell is split into abstract (top) and concrete mapping (below)
        mappingCell.appendChild(getAbstractMappingDiv(operations[0]))
        mappingCell.appendChild(getConcreteMappingDiv(operations))

        //Build & style row
        row.appendChild(operationCell)
        row.appendChild(mappingCell)
        row.addClass("detailsline")
        return row
    }

    fun getAbstractMappingDiv(operation : PatternOperation) : HTMLDivElement{
        val abstractMappingDiv = document.createElement("div") as HTMLDivElement

        val abstractPatternHeadline = document.createElement("p") as HTMLParagraphElement
        abstractPatternHeadline.innerHTML = "Abstract Mapping:"
        abstractPatternHeadline.addClass("detailsHeadline")
        abstractMappingDiv.appendChild(abstractPatternHeadline)

        //Fill abstract mapping div
        val inputLabel = createParagraphElement(getTextOrMinus("input", operation.aOperation.input))
        inputLabel.addClass("abstractMappingLabel")

        val selectorLabel = createParagraphElement(getTextOrMinus("selector", operation.aOperation.selector))
        selectorLabel.addClass("abstractMappingLabel")

        val outputLabel = createParagraphElement(getTextOrMinus("output", operation.aOperation.output))
        outputLabel.addClass("abstractMappingLabel")

        val waitLabel = createParagraphElement(getTextOrMinus("wait", operation.aOperation.wait.toString()))
        waitLabel.addClass("abstractMappingLabel")

        abstractMappingDiv.appendChild(inputLabel)
        abstractMappingDiv.appendChild(selectorLabel)
        abstractMappingDiv.appendChild(outputLabel)
        abstractMappingDiv.appendChild(waitLabel)

        val clearDiv = document.createElement("div");
        clearDiv.addClass("clear")
        abstractMappingDiv.appendChild(clearDiv)

        abstractMappingDiv.addClass("abstractMappingLine")
        return abstractMappingDiv
    }

    fun getConcreteMappingDiv(operations: Array<PatternOperation>) :HTMLDivElement {
        val concreteMappingDiv = document.createElement("div") as HTMLDivElement

        //Create Headline
        val concretePatternHeadline = document.createElement("p") as HTMLParagraphElement
        concretePatternHeadline.innerHTML = "Concrete Mappings:"
        concretePatternHeadline.addClass("detailsHeadline")
        concreteMappingDiv.appendChild(concretePatternHeadline)

        //Create button (overview) and table (details) for every pattern operation
        for (i:Int in 0 .. operations.size-1) {

            var patternoperation = operations[i]
            println("Operation: " + JSON.stringify(patternoperation))

            //Create Button
            var operationButton = document.createElement("button") as HTMLButtonElement
            operationButton.innerHTML = patternoperation.requests.toString() + " requests to path: " + patternoperation.path
            operationButton.addClass("collapsible")
            operationButton.addClass("collapsible_closed")
            operationButton.addEventListener("click", fun(event: Event) {
                var content = operationButton.nextElementSibling;
                if (content != null) {
                    if (content.hasClass("hideDetails")) {
                        //Show details
                        content.removeClass("hideDetails")
                        content.addClass("showDetails")
                        operationButton.removeClass("collapsible_closed")
                        operationButton.addClass("collapsible_open")
                    } else {
                        //Hide details
                        content.removeClass("showDetails")
                        content.addClass("hideDetails")
                        operationButton.removeClass("collapsible_open")
                        operationButton.addClass("collapsible_closed")
                    }
                }
            })

            //Create details table and hide per default
            val detailsDiv = document.createElement("div") as HTMLDivElement
            val detailsTable = document.createElement("table") as HTMLTableElement
            detailsDiv.addClass("hideDetails")
            detailsTable.addClass("mappingDetailsTable")

            //Fill details div
            if (patternoperation.parameters != null ) {
                detailsTable.appendChild(getDetailsParameterRow(patternoperation.parameters))
            }
            detailsTable.appendChild(getDetailsRow("Body:", JSON.stringify(patternoperation.requiredBody)))
            detailsTable.appendChild(getDetailsRow("Produces:", patternoperation.produces))

            detailsDiv.appendChild(detailsTable)

            concreteMappingDiv.appendChild(operationButton)
            concreteMappingDiv.appendChild(detailsDiv)
        }
        return concreteMappingDiv
    }

    fun getDetailsParameterRow(parameters : Array<ParameterObject>) : HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement
        val parameterLabel = document.createElement("td") as HTMLTableCellElement
        parameterLabel.innerHTML = "Parameters:"
        row.appendChild(parameterLabel)

        val parameterValues = document.createElement("td") as HTMLTableCellElement
        for (p in parameters) {
            parameterValues.appendChild(createParagraphElement("->" + JSON.stringify(p)))
        }
        row.appendChild(parameterValues)
        return row
    }

    fun getDetailsRow(label: String, value: String) : HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement
        val labelCell = document.createElement("td") as HTMLTableCellElement
        labelCell.innerHTML = label
        row.appendChild(labelCell)

        val valueCell = document.createElement("td") as HTMLTableCellElement
        valueCell.innerHTML = value
        row.appendChild(valueCell)
        return row
    }

    fun createParagraphElement(text:String) : HTMLParagraphElement {
        var element = document.createElement("p") as HTMLParagraphElement
        element.innerHTML = text
        return element
    }

    fun getTextOrMinus(keyname:String, element:String?):String {
        var text = keyname + ": "
        if (element != null) {
            text += element
        } else {
            text += "-"
        }
        return text
    }

    fun closePatternDetails() {
        // Get the modal
        var modal = document.getElementById("patternDetails");
        if (modal != null) {
            //Hide modal
            modal.setAttribute("style", "display: none")
        }

    }

    fun cbxBenchmarkClick(checkbox:HTMLInputElement, path:String) {
        val newState = checkbox.checked
        if (mappingID != -1) {
            //Update mapping on server side
            val req = XMLHttpRequest()
            req.onloadend = fun(event: Event) {
                //Reload Table if answer is ok
                var text = req.responseText
                if (text == "ok") {
                    //Reload
                    reloadPatternTable()
                }
            }
            req.open("PUT", "http://localhost:8080/api/mapping/" + mappingID + "?path=" + path + "&enabled=" + newState + "&patternConfig=" + getCookie("patternConfig"), true)
            req.send()
        }
    }

    private fun handleNextButtonClick() {
        redirectToUrl("generate.html")
    }

}
