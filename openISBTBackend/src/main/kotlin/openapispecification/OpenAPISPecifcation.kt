package de.tuberlin.mcc.openapispecification

data class OpenAPISPecifcation (val openapi : String,
                                val info : InfoObject,
                                val servers: Array<ServerObject>,
                                val paths:PathsObject,
                                val components:ComponentsObject,
                                val security: Array<SecurityRequirementObject>,
                                val tags: Array<TagObject>,
                                val externalDocs: ExternalDocumentationObject){
    }