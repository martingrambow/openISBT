
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Json


fun main(args: Array<String>) {

    loadConfiguration()

    window.onload = {
        val content = document.getElementById("content")
        if (content != null) {
            val contentName = content.getAttribute("name")
            println(contentName)
            if (contentName == "pasteOAS") {
                val p = pasteOAS()
                p.init()
            }
            if (contentName == "patternConfig") {
                val p = patternConfig()
                p.init()
            }

            if (contentName == "check") {
                val p = check()
                p.init()
            }

            if (contentName == "generate") {
                val p = generate()
                p.init()
            }

            if (contentName == "run") {
                val p = run()
                p.init()
            }

            if (contentName == "results") {
                val p = results()
                p.init()
            }
        }
    }
}

fun redirectToUrl(url:String){
    window.open(url, "_self")
}

fun getCookie(name:String):String {
    val cookies = document.cookie.split(";");
    for (cookie:String in cookies) {
        val pos = cookie.indexOf('=', 0, false)
        if (pos >= 0) {
            var key = cookie.substring(0, pos)
            key = key.trim();
            if (key == name) {
                val content = cookie.substring(pos+1)
                return content
            }
        }
    }
    return ""
}

fun loadConfiguration() {
    var url = "config/config.json"
    val req = XMLHttpRequest()
    req.onloadend = fun(event: Event) {
        var config = JSON.parse<Json>(req.responseText);
        val host = config.get("backendHost").unsafeCast<String>()
        val port = config.get("backendPort").unsafeCast<Int>()
        Backend.url = host;
        Backend.port = port;
    }
    req.open("GET", url, true)
    req.send()
}