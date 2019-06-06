import dataobjects.ResourceMapping
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import workload.PatternRequest
import workload.WorkloadGenerator
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

class generate {

    var mappingID = -1
    var workloadID = -1
    var workload: ArrayList<PatternRequest>? = null

    fun init() {
        println("Init generate called")
        val btnGenerate = document.getElementById("button_generate")
        btnGenerate?.addEventListener("click", fun(event: Event) {
            btnGenerateClicked()

        })

        mappingID = getCookie("mapping").toInt()

        println("Generate initialized")
    }


    private fun btnGenerateClicked() {
        var btn = document.getElementById("button_generate") as HTMLButtonElement
        if (btn != null) {
            if (btn.textContent != "..." && mappingID != -1)  {
                btn.textContent = "..."
                //Change button text and get ressource mappings
                val req = XMLHttpRequest()
                req.onloadend = fun(event: Event) {
                    var text = req.responseText
                    if (text != "not found") {
                        val mappings = JSON.parse<Array<ResourceMapping>>(text)
                        if (mappings != null) {
                            //Generate workload
                            workload = WorkloadGenerator().generateWorkload(mappings)
                            println("WORKLOAD GENERATED")
                            showWorkload()
                        }
                    }
                    btn.textContent = "Generate Workload"
                }
                req.open("GET", "http://localhost:8080/api/mapping/" + mappingID, true)
                req.send()
            }
        }
    }

    private fun showWorkload() {
        if (workload != null) {
            var taWorkload = document.getElementById("taWorkload") as HTMLTextAreaElement
            taWorkload.value = ""
            taWorkload.value = JSON.stringify(workload)
            taWorkload.removeClass("ta_hidden")
            taWorkload.addClass("ta_show")
        }
    }
}
