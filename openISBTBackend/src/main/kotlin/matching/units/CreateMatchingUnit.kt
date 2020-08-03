package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.*
import patternconfiguration.AbstractOperation
import mapping.simplemapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class CreateMatchingUnit : MatchingUnit{

    val log: Logger = LoggerFactory.getLogger("CreateMatchingUnit")

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.CREATE.name
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.post != null) {
            val operation = PatternOperation(abstractOperation, AbstractPatternOperation.CREATE)
            operation.serviceName = spec.info.title
            operation.path = spec.servers[0].url + path

            //Determine input and output values
            val postObject = pathItemObject.post
            log.debug("    " + GsonBuilder().create().toJson(postObject))
            if (postObject.requestBody != null) {
                //Operation requires some request body
                val body = MatchingUtil().parseRequestBody(postObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (postObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(postObject.parameters, spec)
                for (headerparam in MatchingUtil().parseHeaderParameter(postObject.parameters, spec)) {
                    operation.headers.add(Pair(headerparam.get("name").asString, headerparam.getAsJsonObject("schema")))
                }
            }
            if (postObject.security != null) {
                val header = MatchingUtil().parseApiKey(postObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }
            if (postObject.responses != null){
                operation.produces = MatchingUtil().parseResponse(postObject.responses, spec)
            }

            return operation
        }
        return null
    }
}