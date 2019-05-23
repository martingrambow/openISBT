package de.tuberlin.mcc.openapispecification

data class HeaderObject (val name: String,
                         val inValue: String,
                         val description: String,
                         val required: Boolean,
                         val deprecated: Boolean,
                         val allowEmptyValue: Boolean,
                         val style: String,
                         val explode: Boolean,
                         val allowReserved: String,
                         val examples: Map<String, ExampleObject>,
                         val content: Map<String, MediaTypeObject>){
}