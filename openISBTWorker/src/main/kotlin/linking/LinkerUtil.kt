package linking

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.random.Random

class LinkerUtil {

    val log = LoggerFactory.getLogger("LinkerUtil");

    fun replaceValueInJson(key: String, newValue:String, element: JsonElement) : JsonElement {
        if (element.isJsonObject) {
            for (childValue in element.asJsonObject.entrySet()) {
                if (childValue.key == key) {
                    childValue.setValue(GsonBuilder().create().toJsonTree(newValue))
                    log.debug("Replaced value for key " + key + " in body with " + newValue)
                } else {
                    replaceValueInJson(key, newValue, childValue.value)
                }
            }
        }
        return element
    }

    fun getJSonValueForKey(key: String, element: JsonElement, selector : String?) : JsonElement? {
        if (element.isJsonObject) {
            if (element.asJsonObject.has(key)) {
                return element.asJsonObject.get(key)
            }
            for (childValues in element.asJsonObject.entrySet()) {
                var child = getJSonValueForKey(key, childValues.value, selector)
                if (child != null) {
                    return child
                }
            }
        }
        if (element.isJsonArray) {
            var sel = selector
            if (sel == null) {
                sel = "random"
            }

            var myarray = element.asJsonArray
            //Random is default
            var pos = Random(Date().time).nextInt(myarray.size())

            when (sel) {
                "first" -> {
                    var pos = 0
                }
                "last" -> {
                    var pos = myarray.size()-1
                }
            }
            return getJSonValueForKey(key, myarray.get(pos), selector)
        }
        return null
    }
}