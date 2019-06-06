package matching

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.ParameterObject
import de.tuberlin.mcc.openapispecification.RequestBodyObject
import de.tuberlin.mcc.openapispecification.SchemaObject

class MatchingUtil {

    fun parseParameter(parameters : Array<ParameterObject>, spec: OpenAPISPecifcation) : ArrayList<JsonObject> {
        var result : ArrayList<JsonObject> = ArrayList()
        for (entry in parameters) {
            result.add(ReferenceResolver().resolveReferencesInJsonObject(GsonBuilder().create().toJsonTree(entry).asJsonObject, spec))
        }
        return result
    }

    fun parseRequestBody(requestBody: RequestBodyObject, spec: OpenAPISPecifcation) : JsonObject? {
        var requestBodyObject = requestBody
        if (requestBodyObject.`$ref` != null) {
            //There is a reference which has to be resolved
            requestBodyObject = ReferenceResolver().resolveReference(requestBodyObject.`$ref`, spec) as RequestBodyObject
        }
        if (requestBodyObject.content != null) {
            var contentObject = requestBodyObject.content
            //Currently, we only support json schemes
            var mediaTypeObject = contentObject.get("application/json")
            if (mediaTypeObject == null) {
                mediaTypeObject = contentObject.get("application/x-www-form-urlencoded")
            }
            if (mediaTypeObject != null) {
                println("    MediaTypeObject: " + GsonBuilder().create().toJson(mediaTypeObject))
                var schemaObject: SchemaObject? = null
                if (mediaTypeObject.schema != null) {
                    schemaObject = mediaTypeObject.schema
                    println("    SchemaObjectX: " + GsonBuilder().create().toJson(schemaObject))

                    if (schemaObject.`$ref` != null) {
                        //There is a reference which has to be resolved
                        schemaObject = ReferenceResolver().resolveReference(schemaObject.`$ref`, spec) as SchemaObject
                    }
                }
                if (schemaObject != null) {
                    //Found a schema object
                    if ("object".equals(schemaObject.type.toLowerCase())) {
                        //Resolve References in schema object
                        schemaObject.properties = ReferenceResolver().resolveReferencesInJsonObject(schemaObject.properties, spec);
                        return GsonBuilder().create().toJsonTree(schemaObject).asJsonObject
                    }
                    if ("array".equals(schemaObject.type.toLowerCase())) {
                        //Resolve References in schema object
                        schemaObject.items = ReferenceResolver().resolveReferencesInJsonObject(schemaObject.items, spec);
                        return GsonBuilder().create().toJsonTree(schemaObject).asJsonObject
                    }

                }
            }
        }
        return null
    }
}