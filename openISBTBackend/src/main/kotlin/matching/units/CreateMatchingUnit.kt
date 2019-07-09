package matching.units

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.*
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import io.ktor.http.ContentType
import mapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import matching.ReferenceResolver
import patternconfiguration.AbstractPatternOperation

class CreateMatchingUnit : MatchingUnit{

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.CREATE.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.post != null) {
            var operation = PatternOperation(abstractOperation, AbstractPatternOperation.CREATE)
            operation.path = path

            //Determine input and output values
            println("CREATE:")
            var postObject = pathItemObject.post
            println("    " + GsonBuilder().create().toJson(postObject))
            if (postObject.requestBody != null) {
                //Operation requires some request body
                var body = MatchingUtil().parseRequestBody(postObject.requestBody, spec)
                if (body != null) {
                    operation.requiredBody = body
                }
            }
            if (postObject.parameters != null) {
                operation.parameters = MatchingUtil().parseParameter(postObject.parameters, spec)
            }
            if (postObject.security != null) {
                var header = MatchingUtil().parseApiKey(postObject.security, spec)
                if (header != null) {
                    operation.headers.add(header)
                }
            }

            return operation
        }
        return null
    }



}