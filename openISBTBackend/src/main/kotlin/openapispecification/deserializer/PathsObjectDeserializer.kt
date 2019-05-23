package openapispecification.deserializer

import com.google.gson.*
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.openapispecification.PathsObject
import openapispecification.ResponsesObject
import java.lang.reflect.Type

class PathsObjectDeserializer() : JsonDeserializer<PathsObject> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PathsObject? {
        println("Try to deserialize " + json);
        val paths:HashMap<String, PathItemObject> = HashMap()

        val gsonBuilder: GsonBuilder = GsonBuilder()
        val responsesObjectDeserializer:JsonDeserializer<ResponsesObject> = ResponsesObjectDeserializer()
        gsonBuilder.registerTypeAdapter(ResponsesObject::class.java, responsesObjectDeserializer)
        val customGson:Gson = gsonBuilder.create();

        if (json != null) {
            for (element in json.asJsonObject.entrySet()) {
                paths.put(element.key, customGson.fromJson(element.value,PathItemObject::class.java))
            }
        }

        val pathsobject:PathsObject = PathsObject(paths)
        return pathsobject;

    }
}