package linking

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import org.slf4j.LoggerFactory
import workload.ApiRequest
import java.util.*
import kotlin.random.Random

class LinkerUtil {

    val log = LoggerFactory.getLogger("LinkerUtil");

    fun  replaceValueInJson(key: String, newValue:String, element: JsonElement) : JsonElement {
        if (element.isJsonObject) {
            for (childValue in element.asJsonObject.entrySet()) {
                if (childValue.key == key) {
                    childValue.setValue(GsonBuilder().create().toJsonTree(newValue))
                    log.debug("            Replaced value for key $key in body with $newValue")
                } else {
                    replaceValueInJson(key, newValue, childValue.value)
                }
            }
        }
        return element
    }

    fun isKeyInJson(key: String, element: JsonElement) : Boolean {
        var keyFound = false
        if (element.isJsonObject) {
            for (childValue in element.asJsonObject.entrySet()) {
                if (childValue.key == key) {
                    return true
                } else {
                     keyFound = isKeyInJson(key, childValue.value)
                }
            }
        }
        return keyFound
    }

    fun getValueForKeyInRequest(key: String, dependentRequest : ApiRequest, selector : String?) : String? {
        var value:String? = null

        //Try to parse respone to Json Oject
        try {
            val responseJson = GsonBuilder().create().fromJson(dependentRequest.response, JsonElement::class.java)
            value = getStringValueForKey(key, responseJson, selector)
        } catch (e : JsonSyntaxException) {
            //response might be not a json
        }

        if (value != null) {
            log.debug("            Found value in dependent request response")
            return value
        }

        for (parameter in dependentRequest.parameter) {
            if (parameter.first == key) {
                value = parameter.second
            }
        }

        if (value != null) {
            log.debug("            Found value in dependent parameter value")
            return value
        }

        value = getStringValueForKey(key, dependentRequest.body, selector)

        if (value != null) {
            log.debug("            Found value in dependent body value")
            return value
        }
        return value
    }

    fun isKeyInRequest(key: String, dependentRequest : ApiRequest) : Boolean {
        //Try to parse respone to Json Oject
        try {
            val responseJson = GsonBuilder().create().fromJson(dependentRequest.response, JsonElement::class.java)
            if (isKeyInJson(key, responseJson)) {
                return true
            }
        } catch (e : JsonSyntaxException) {
            //response might be not a json
        }

        for (parameter in dependentRequest.parameter) {
            if (parameter.first == key) {
                return true
            }
        }

        if (isKeyInJson(key, dependentRequest.body)) {
            return true
        }
        return false
    }

    fun getStringValueForKey(key: String, element: JsonElement, selector : String?) : String? {
        if (element.isJsonObject) {
            if (element.asJsonObject.has(key)) {
                return element.asJsonObject.get(key).asString
            }
            for (childValues in element.asJsonObject.entrySet()) {
                val child = getStringValueForKey(key, childValues.value, selector)
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

            val myarray = element.asJsonArray
            //Random is default
            val pos = Random(Date().time).nextInt(myarray.size())

            when (sel) {
                "first" -> {
                    var pos = 0
                }
                "last" -> {
                    var pos = myarray.size()-1
                }
            }
            return getStringValueForKey(key, myarray.get(pos), selector)
        }
        return null
    }

    fun findPathInputs(path : String) : ArrayList<String> {
        val inputs = ArrayList<String>()
        var pos = 0
        while (pos >= 0) {
            pos = path.indexOf("{", pos+1)
            if (pos >= 0) {
                //there is some path input
                val endpos = path.indexOf("}", pos)
                val inputname = path.substring(pos+1, endpos)
                inputs.add(inputname)
            }
        }
        return inputs
    }

    fun findIdKeysInJson(json : JsonObject) : ArrayList<String> {
        val keys = ArrayList<String>()
        for (prop in json.entrySet()) {
            if (prop.key.endsWith("id", true)) {
                var noEnum = true
                //if it's an enum, then there are values defined
                if (prop.value.isJsonObject && prop.value.asJsonObject.has("enum")) {
                    noEnum = false
                }
                if (noEnum) {
                    keys.add(prop.key)
                }
            }
            if (prop.value.isJsonObject) {
                keys.addAll(findIdKeysInJson(prop.value.asJsonObject))
            }
        }
        return keys
    }

}