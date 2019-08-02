package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import io.ktor.http.ContentType
import mapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class UpdateMatchingUnit : MatchingUnit{

    val log = LoggerFactory.getLogger("UpdateMatchingUnit");

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.UPDATE.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.put != null) {
            var operation = PatternOperation(abstractOperation, AbstractPatternOperation.UPDATE)
            operation.path = path

            //Determine input and output values
            var updateObject = pathItemObject.put
            log.debug("    " + GsonBuilder().create().toJson(updateObject))
            if (updateObject.requestBody != null) {
                //Operation requires some request body
                var body = MatchingUtil().parseRequestBody(updateObject.requestBody, spec)
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
                var header = MatchingUtil().parseApiKey(updateObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }

            return operation
        }
        return null
    }

}