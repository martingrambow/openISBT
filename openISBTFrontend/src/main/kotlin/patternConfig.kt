import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document

class patternConfig {

    fun init() {
        println("Init patternConfig called")
        val btnLoadDefault = document.getElementById("button_loadDefault")
        btnLoadDefault?.addEventListener("click", fun(event: Event) {
            handleLoadDefaultButtonClick()

        })

        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(event: Event) {
            handleNextButtonClick()

        })
        val name = getCookie("patternConfig")
        getPatternConfig(name)

        println("patternConfig initialized")
    }

    private fun handleNextButtonClick() {
        var taPatternConfig = document.getElementById("taPatternConfig") as HTMLTextAreaElement
        var text = taPatternConfig.value

        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            val parts = text.split(" ")
            if (parts.size == 6) {
                document.cookie = "patternConfig=" + parts[5]
                redirectToUrl("check.html")
            }
        }
        req.open("POST", "/api/patternConfigs", true)
        req.send(text)
    }

    private fun handleLoadDefaultButtonClick() {
        val selPatternConfig = document.getElementById("selPatternConfig") as HTMLSelectElement
        var url = "patternConfigs/allPattern.json"

        if (selPatternConfig != null) {
            val option = selPatternConfig.options[selPatternConfig.selectedIndex]?.textContent
            url = "patternConfigs/" + option + ".json"
        }
        println("URL is " + url)

        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            var taPatternConfig = document.getElementById("taPatternConfig") as HTMLTextAreaElement
            taPatternConfig.value= ""
            taPatternConfig.value = text
        }
        req.open("GET", url, true)
        req.send()
    }

    fun getPatternConfig(id:String) {
        if (id.length > 0) {
            val req = XMLHttpRequest()
            req.onloadend = fun(event: Event) {
                var text = req.responseText
                if (text != "\"not found\"") {
                    var taPatternConfig = document.getElementById("taPatternConfig") as HTMLTextAreaElement
                    taPatternConfig.value = ""
                    taPatternConfig.value = text
                }
            }
            req.open("GET", "/api/patternConfigs/" + id, true)
            req.send()
        }
    }
}
