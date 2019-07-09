package de.tuberlin.mcc.openapispecification

import com.google.gson.JsonElement
import openapispecification.ResponsesObject

data class OperationObject (val tags: Array<String>,
                            val summary: String,
                            val description: String,
                            val externalDocs: ExternalDocumentationObject,
                            val operationID: String,
                            val parameters: Array<ParameterObject>,
                            val requestBody: RequestBodyObject,
                            val responses: ResponsesObject,
                            val callBacks: Map<String, CallBackObject>,
                            val deprecated: Boolean,
                            val security: JsonElement,
                            val servers: Array<ServerObject>){
}