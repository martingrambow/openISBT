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

class DeleteMatchingUnit : MatchingUnit{

    val log = LoggerFactory.getLogger("DeleteMatchingUnit");

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.DELETE.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.delete != null) {
            var operation = PatternOperation(abstractOperation, AbstractPatternOperation.DELETE)
            operation.path = path

            //Determine input and output values
            var deleteObject = pathItemObject.delete
            log.debug("    " + GsonBuilder().create().toJson(deleteObject))
            if (deleteObject.requestBody != null) {
                //Operation requires some request body
                var body = MatchingUtil().parseRequestBody(deleteObject.requestBody, spec)
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
                var header = MatchingUtil().parseApiKey(deleteObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }

            return operation
        }
        return null
    }

}