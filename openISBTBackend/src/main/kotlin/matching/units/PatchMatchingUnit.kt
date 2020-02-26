package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
import mapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class PatchMatchingUnit : MatchingUnit{

    val log = LoggerFactory.getLogger("PatchMatchingUnit");

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.UPDATE.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.patch != null) {
            val operation = PatternOperation(abstractOperation, AbstractPatternOperation.PATCH)
            operation.path = spec.servers[0].url + path

            //Determine input and output values
            val patchObject = pathItemObject.patch
            log.debug("    " + GsonBuilder().create().toJson(patchObject))
            if (patchObject.requestBody != null) {
                //Operation requires some request body
                val body = MatchingUtil().parseRequestBody(patchObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (patchObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(patchObject.parameters, spec)
                for (headerparam in MatchingUtil().parseHeaderParameter(patchObject.parameters, spec)) {
                    operation.headers.add(Pair(headerparam.get("name").asString, headerparam.getAsJsonObject("schema")))
                }
            }
            if (patchObject.security != null) {
                val header = MatchingUtil().parseApiKey(patchObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }

            return operation
        }
        return null
    }

}