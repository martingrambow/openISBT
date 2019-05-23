package de.tuberlin.mcc.openapispecification

data class OpenAPISPecifcation (val openapi : String,
                                val info : InfoObject,
                                val servers: Array<ServerObject>,
                                val paths:PathsObject){
    }