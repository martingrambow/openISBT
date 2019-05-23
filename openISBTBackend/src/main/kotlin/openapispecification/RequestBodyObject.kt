package de.tuberlin.mcc.openapispecification

data class RequestBodyObject (val description: String,
                            val content: Map<String, MediaTypeObject>,
                            val required: Boolean){
}