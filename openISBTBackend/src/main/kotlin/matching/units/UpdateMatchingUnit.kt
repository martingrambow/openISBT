package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import io.ktor.http.ContentType
import mapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import patternconfiguration.AbstractPatternOperation

class UpdateMatchingUnit : MatchingUnit{

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.UPDATE.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.put != null) {
            var operation = PatternOperation(abstractOperation, AbstractPatternOperation.UPDATE)
            operation.path = path

            //Determine input and output values
            println("UPDATE:")
            var updateObject = pathItemObject.put
            println("    " + GsonBuilder().create().toJson(updateObject))
            if (updateObject.requestBody != null) {
                //Operation requires some request body
                var body = MatchingUtil().parseRequestBody(updateObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (updateObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(updateObject.parameters, spec)
            }

            return operation
        }
        return null
    }

}