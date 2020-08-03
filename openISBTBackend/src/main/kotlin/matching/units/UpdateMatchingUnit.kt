package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
import mapping.simplemapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class UpdateMatchingUnit : MatchingUnit{

    val log: Logger = LoggerFactory.getLogger("UpdateMatchingUnit")

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.UPDATE.name
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.put != null) {
            val operation = PatternOperation(abstractOperation, AbstractPatternOperation.UPDATE)
            operation.serviceName = spec.info.title
            operation.path = spec.servers[0].url + path

            //Determine input and output values
            val updateObject = pathItemObject.put
            log.debug("    " + GsonBuilder().create().toJson(updateObject))
            if (updateObject.requestBody != null) {
                //Operation requires some request body
                val body = MatchingUtil().parseRequestBody(updateObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (updateObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(updateObject.parameters, spec)
                for (headerparam in MatchingUtil().parseHeaderParameter(updateObject.parameters, spec)) {
                    operation.headers.add(Pair(headerparam.get("name").asString, headerparam.getAsJsonObject("schema")))
                }
            }
            if (updateObject.security != null) {
                val header = MatchingUtil().parseApiKey(updateObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }

            if (updateObject.responses != null){
                operation.produces = MatchingUtil().parseResponse(updateObject.responses, spec)
            }

            return operation
        }
        return null
    }
}