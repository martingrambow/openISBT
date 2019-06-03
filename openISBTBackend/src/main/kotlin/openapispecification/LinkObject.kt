package de.tuberlin.mcc.openapispecification

data class LinkObject (val `$ref`: String,
                       val operationRef : String,
                       val operationId : String,
                       val parameters : Map<String, Any>,
                       val rquestBody : Any,
                       val description : String,
                       val  server : ServerObject){
    }