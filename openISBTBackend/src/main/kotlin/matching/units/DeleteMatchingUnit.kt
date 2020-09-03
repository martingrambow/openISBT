package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
import mapping.globalmapping.GPatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class DeleteMatchingUnit : MatchingUnit{

    val log: Logger = LoggerFactory.getLogger("DeleteMatchingUnit")

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.DELETE.name
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): GPatternOperation? {
        if (pathItemObject.delete != null) {
            val operation = GPatternOperation(abstractOperation, AbstractPatternOperation.DELETE)
            operation.serviceName = spec.info.title
            operation.path = spec.servers[0].url + path

            //Determine input and output values
            val deleteObject = pathItemObject.delete
            log.debug("    " + GsonBuilder().create().toJson(deleteObject))
            if (deleteObject.requestBody != null) {
                //Operation requires some request body
                val body = MatchingUtil().parseRequestBody(deleteObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (deleteObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(deleteObject.parameters, spec)
                for (headerparam in MatchingUtil().parseHeaderParameter(deleteObject.parameters, spec)) {
                    operation.headers.add(Pair(headerparam.get("name").asString, headerparam.getAsJsonObject("schema")))
                }
            }

            if (deleteObject.security != null) {
                val header = MatchingUtil().parseApiKey(deleteObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }

            if (deleteObject.responses != null){
                operation.produces = MatchingUtil().parseResponse(deleteObject.responses, spec)
            }

            return operation
        }
        return null
    }

}