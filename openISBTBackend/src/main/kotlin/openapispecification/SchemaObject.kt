package de.tuberlin.mcc.openapispecification

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class SchemaObject (var `$ref`: String,
                         var type:String,
                         var properties:JsonObject,
                         var items:JsonObject,
                         var enum: JsonArray,
                         var faker: String,
                         var required:Array<String>,
                         var format: String,
                         var minimum:Int,
                         var maximum:Int,
                         var minItems:Int,
                         var maxItems:Int){
}