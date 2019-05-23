import dataobjects.AbstractOperation
import dataobjects.Pattern
import dataobjects.PatternOperation
import dataobjects.ResourceMapping
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass

class check {

    var mappingID:Int = -1

    fun init() {
        println("Init check called")
        val oas = getCookie("oasFile")
        val patternConf = getCookie("patternConfig")
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            mappingID = -1
            val parts = req.responseText.split(" ")
            if (parts.size == 6) {
                mappingID = parts[5].toInt()
            }

            if (mappingID != -1) {
                reloadPatternTable()
            }
        }
        req.open("GET", "http://localhost:8080/api/mapping?oasFile=" + oas + "&patternConfig=" + patternConf, true)
        req.send()

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

        // When the user clicks anywhere outside of the modal, close it
        //window.onclick = function(event) {
        //    if (event.target == modal) {
        //        modal.style.display = "none";
        //    }

        println("check initialized")
    }

    fun reloadPatternTable() {
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
                        val row = buildRow(m)
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


    fun buildRow(mapping:ResourceMapping):HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement
        if (mapping.supported) {
            row.addClass("supportedRow")
        } else {
            row.addClass("notSupportedRow")
        }

        val path = document.createElement("td") as HTMLTableCellElement
        val pathName = document.createElement("p") as HTMLParagraphElement
        pathName.innerHTML = mapping.resourcePath
        path.appendChild(pathName)

        val pattern = document.createElement("td") as HTMLTableCellElement

        val patterncell = document.createElement("div") as HTMLDivElement
        for (a in mapping.patternMappingList) {
            val patternName = document.createElement("p") as HTMLParagraphElement
            if (a.supported) {
                patternName.addClass("pointable")
                patternName.addEventListener("click", fun(event: Event) {
                    clickPatternName(mapping.resourcePath, a)
                })
            }
            var patternNameText = a.aPattern.name
            if (a.supported) {
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

        val requests = document.createElement("td") as HTMLTableCellElement
        requests.innerHTML = mapping.numberOfRequests.toString()

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

            //Set visible
            modal.setAttribute("style", "display: block")
        }

    }

    fun buildDetailPatternRow(operations:Array<PatternOperation>):HTMLTableRowElement {
        val row = document.createElement("tr") as HTMLTableRowElement

        val operationCell = document.createElement("td") as HTMLTableCellElement
        operationCell.innerHTML = operations[0].aPatternOperation;

        val abstractMappingCell = document.createElement("td") as HTMLTableCellElement
        val abstractMappingDiv = document.createElement("div") as HTMLDivElement

        //Fill abstract mapping div

        abstractMappingDiv.appendChild(createParagraphElement(getTextOrMinus("input", operations[0].aOperation.input)) )
        abstractMappingDiv.appendChild(createParagraphElement(getTextOrMinus("output", operations[0].aOperation.output)))
        abstractMappingDiv.appendChild(createParagraphElement(getTextOrMinus("selector", operations[0].aOperation.selector)))
        abstractMappingDiv.appendChild(createParagraphElement(getTextOrMinus("wait", operations[0].aOperation.wait.toString())))

        abstractMappingCell.appendChild(abstractMappingDiv)


        val concreteMappingCell = document.createElement("td") as HTMLTableCellElement
        val concreteMappingDiv = document.createElement("div") as HTMLDivElement

        //Fill concrete mapping div
        for (i:Int in 0 .. operations.size-1) {
            var patternoperation = operations[i]
            val patternOperationDiv = document.createElement("div") as HTMLDivElement

            //Fill patternoperation div
            patternOperationDiv.appendChild(createParagraphElement(getTextOrMinus("path", patternoperation.path)))
            patternOperationDiv.appendChild(createParagraphElement(getTextOrMinus("requests", patternoperation.requests.toString())))
            patternOperationDiv.appendChild(createParagraphElement(getTextOrMinus("consumes", patternoperation.consumes)))
            patternOperationDiv.appendChild(createParagraphElement(getTextOrMinus("produces", patternoperation.produces)))

            if (i != operations.size-1) {
                patternOperationDiv.addClass("patternoperation")
            }
            concreteMappingDiv.appendChild(patternOperationDiv)
        }

        concreteMappingCell.appendChild(concreteMappingDiv)

        //for (path in operation.paths) {
        //    val pathNameElement = document.createElement("p") as HTMLParagraphElement
        //    pathNameElement.innerHTML = path
        //    pathElementCell.appendChild(pathNameElement)
        //}

        //var inputValue = ""
        //if (operation.input != null && operation.input.length > 0) {
        //    inputValue += operation.input
        //    if (operation.selector != null && operation.selector.length > 0) {
        //        inputValue += "(" + operation.selector + ")"
        //    }
        //}
        //inputElement.innerHTML = inputValue;

        val outputElement = document.createElement("td") as HTMLTableCellElement
        var outputValue = ""
        //if (operation.output != null) {
        //    outputValue += operation.output
        //}
        //outputElement.innerHTML = outputValue

        val pathElement = document.createElement("td") as HTMLTableCellElement

        row.appendChild(operationCell)
        row.appendChild(abstractMappingCell)
        row.appendChild(concreteMappingCell)

        row.addClass("detailsline")

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
            modal.setAttribute("style", "display: none")
        }

    }

    fun cbxBenchmarkClick(checkbox:HTMLInputElement, path:String) {
        println("Clicked on " + path)
        val newState = checkbox.checked

        if (mappingID != -1) {
            //Get mapping from server
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

}
