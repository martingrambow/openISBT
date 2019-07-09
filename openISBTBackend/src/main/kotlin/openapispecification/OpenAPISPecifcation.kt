package de.tuberlin.mcc.openapispecification

import com.google.gson.JsonElement

data class OpenAPISPecifcation (val openapi : String,
                                val info : InfoObject,
                                val servers: Array<ServerObject>,
                                val paths:PathsObject,
                                val components:ComponentsObject,
                                val security: JsonElement,
                                val tags: Array<TagObject>,
                                val externalDocs: ExternalDocumentationObject){
    }