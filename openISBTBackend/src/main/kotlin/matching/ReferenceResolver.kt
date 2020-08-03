package matching

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.SchemaObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReferenceResolver {

    val log: Logger = LoggerFactory.getLogger("ReferenceResolver")

    fun resolveReference(reference:String, spec: OpenAPISPecifcation) : Any?{
        val parts = reference.split("/")
        if (parts.size > 1) {
            when {
                reference.startsWith("#/components/requestBodies") -> return spec.components.requestBodies[parts[parts.size-1]]
                reference.startsWith("#/components/schemas") -> return spec.components.schemas[parts[parts.size-1]]
            }
        }
        return null
    }

    fun resolveReferencesInJsonObject(obj: JsonObject, spec: OpenAPISPecifcation):JsonObject {
        for (prop in obj.entrySet()) {
            if (prop.value.isJsonObject) {
                val value: JsonObject = prop.value.asJsonObject
                prop.setValue(resolveReferencesInJsonObject(value, spec))
            }
            if (prop.key == "\$ref" && prop.value.asString.contains("/components/schemas")) {
                val resolvedSubSchema = ReferenceResolver().resolveReference(prop.value.asString, spec) as SchemaObject
                log.debug("Resolved: " + GsonBuilder().create().toJson(resolvedSubSchema))
                //There might be other references added
                val tmp= GsonBuilder().create().toJsonTree(resolvedSubSchema).asJsonObject
                return resolveReferencesInJsonObject(tmp, spec)
            }
        }
        return obj

    }
}