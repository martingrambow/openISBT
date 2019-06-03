package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import mapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import patternconfiguration.AbstractPatternOperation

class DeleteMatchingUnit : MatchingUnit{

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.DELETE.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.delete != null) {
            var operation = PatternOperation(abstractOperation, AbstractPatternOperation.DELETE)
            operation.path = path

            //Determine input and output values
            println("DELETE:")
            var deleteObject = pathItemObject.delete
            println("    " + GsonBuilder().create().toJson(deleteObject))
            if (deleteObject.requestBody != null) {
                //Operation requires some request body
                var body = MatchingUtil().parseRequestBody(deleteObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (deleteObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(deleteObject.parameters, spec)
            }

            return operation
        }
        return null
    }

}