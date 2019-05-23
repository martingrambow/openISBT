import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window

class pasteOAS {

    fun init() {
        println("Init pasteOAS called")
        val btnUrl = document.getElementById("button_url")
        btnUrl?.addEventListener("click", fun(event: Event) {
            handleUrlButtonClick()

        })
        val btnNext = document.getElementById("button_next")
        btnNext?.addEventListener("click", fun(event: Event) {
            handleNextButtonClick()

        })
        getOASFile(getCookie("oasFile"))

        println("pasteOAS initialized")
    }

    fun handleUrlButtonClick() {
        var tbxUrl = document.getElementById("tbxUrl") as HTMLInputElement
        val url = tbxUrl?.value

        //Get File from URL
        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            var taOASFile = document.getElementById("taOASFile") as HTMLTextAreaElement
            taOASFile.value= ""
            taOASFile.value = text
        }
        req.open("GET", url, true)
        req.send()
    }

    fun handleNextButtonClick() {
        var taOASFile = document.getElementById("taOASFile") as HTMLTextAreaElement
        var text = taOASFile.value

        val req = XMLHttpRequest()
        req.onloadend = fun(event: Event) {
            var text = req.responseText
            val parts = text.split(" ")
            if (parts.size == 6) {
                document.cookie = "oasFile=" + parts[5]
                redirectToUrl("patternConfig.html")
            }
        }
        req.open("POST", "http://localhost:8080/api/oasFiles", true)
        req.send(text)
    }

    fun getOASFile(id:String) {
        if (id.length > 0) {
            val req = XMLHttpRequest()
            req.onloadend = fun(event: Event) {
                var text = req.responseText
                if (text !== "not found") {
                    var taOASFile = document.getElementById("taOASFile") as HTMLTextAreaElement
                    taOASFile.value = ""
                    taOASFile.value = text
                }
            }
            req.open("GET", "http://localhost:8080/api/oasFiles/" + id, true)
            req.send()
        }
    }
}
