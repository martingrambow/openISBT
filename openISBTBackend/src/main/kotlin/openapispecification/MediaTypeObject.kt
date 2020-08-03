package de.tuberlin.mcc.openapispecification

data class MediaTypeObject (val `$ref`: String?,
                            val schema:SchemaObject?,
                            val examples: Map<String, ReferenceObject>,
                            val encoding:Map<String, EncodingObject>){
}