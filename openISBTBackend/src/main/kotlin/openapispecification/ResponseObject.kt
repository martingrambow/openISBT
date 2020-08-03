package de.tuberlin.mcc.openapispecification

data class ResponseObject (val `$ref`: String,
                           val description: String,
                           val headers: Map<String, HeaderObject>,
                           val content: Map<String, MediaTypeObject>?,
                           val links: ReferenceObject,
                           var schema: SchemaObject) {
}