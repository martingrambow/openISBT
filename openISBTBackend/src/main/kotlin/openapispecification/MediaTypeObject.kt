package de.tuberlin.mcc.openapispecification

data class MediaTypeObject (val schema:SchemaObject,
                            val examples: Map<String, ReferenceObject>,
                            val encoding:Map<String, EncodingObject>){
}