package openapispecification.deserializer

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.openapispecification.PathsObject
import de.tuberlin.mcc.openapispecification.ResponseObject
import openapispecification.ResponsesObject
import java.lang.reflect.Type

class ResponsesObjectDeserializer() : JsonDeserializer<ResponsesObject> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ResponsesObject? {
        //println("Try to deserialize response " + json);
        val responses:HashMap<String, ResponseObject> = HashMap()


        if (json != null) {
            for (element in json.asJsonObject.entrySet()) {
                responses.put(element.key, Gson().fromJson(element.value,ResponseObject::class.java))
            }
        }

        val responsesobject:ResponsesObject = ResponsesObject(responses)
        return responsesobject;

    }
}