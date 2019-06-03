package de.tuberlin.mcc.openapispecification

data class ServerObject (val url:String,
                         val description:String,
                         val variables:Map<String,ServerVariableObject>){
}