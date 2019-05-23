
import kotlin.browser.document
import kotlin.browser.window

fun main(args: Array<String>) {
    println(args)
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