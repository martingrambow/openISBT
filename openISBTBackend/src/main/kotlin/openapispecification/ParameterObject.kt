package de.tuberlin.mcc.openapispecification


data class ParameterObject (val name: String,
                            val inValue: String,
                            val description: String,
                            val required: Boolean,
                            val deprecated: Boolean,
                            val allowEmptyValue: Boolean){
}