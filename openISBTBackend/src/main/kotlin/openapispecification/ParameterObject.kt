package de.tuberlin.mcc.openapispecification


data class ParameterObject (val `$ref`: String,
                            val name: String,
                            val `in`: String,
                            val description: String,
                            val required: Boolean,
                            val deprecated: Boolean,
                            val allowEmptyValue: Boolean,
                            val style:String,
                            val explode: Boolean,
                            val schema: SchemaObject){
}