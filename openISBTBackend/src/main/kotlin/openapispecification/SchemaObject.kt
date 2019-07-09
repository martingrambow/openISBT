package de.tuberlin.mcc.openapispecification

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class SchemaObject (val `$ref`: String,
                         val type:String,
                         var properties:JsonObject,
                         var items:JsonObject,
                         var enum: JsonArray,
                         var faker: String,
                         val required:Array<String>){
}