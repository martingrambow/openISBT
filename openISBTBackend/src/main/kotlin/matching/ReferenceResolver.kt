package matching

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.SchemaObject

class ReferenceResolver {

    fun resolveReference(reference:String, spec: OpenAPISPecifcation) : Any?{
        var parts = reference.split("/")
        if (parts.size > 1) {
            when {
                reference.startsWith("#/components/requestBodies") -> return spec.components.requestBodies.get(parts[parts.size-1])
                reference.startsWith("#/components/schemas") -> return spec.components.schemas.get(parts[parts.size-1])
            }
        }
        return null
    }

    fun resolveReferencesInJsonObject(obj: JsonObject, spec: OpenAPISPecifcation):JsonObject {
        for (prop in obj.entrySet()) {
            if (prop.value.isJsonObject) {
                var value: JsonObject = prop.value.asJsonObject
                prop.setValue(resolveReferencesInJsonObject(value, spec))
            }
            if (prop.key.equals("\$ref") && prop.value.asString.contains("/components/schemas")) {
                var resolvedSubSchema = ReferenceResolver().resolveReference(prop.value.asString, spec) as SchemaObject
                println("Resolved: " + GsonBuilder().create().toJson(resolvedSubSchema))
                return GsonBuilder().create().toJsonTree(resolvedSubSchema).asJsonObject
            }
        }
        return obj

    }
}