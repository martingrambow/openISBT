package de.tuberlin.mcc.openapispecification

data class RequestBodyObject (val `$ref`: String,
                              val description: String,
                              val content: Map<String, MediaTypeObject>,
                              val required: Boolean){
}