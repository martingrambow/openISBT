package de.tuberlin.mcc.openapispecification

data class EncodingObject (val contentType: String,
                           val headers: Map<String, HeaderObject>,
                           val style: String,
                           val explode: Boolean,
                           val allowReserved: Boolean){
}
