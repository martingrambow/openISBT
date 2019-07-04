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

class ReadMatchingUnit : MatchingUnit{

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.READ.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.get != null) {

            //get could support READ or SCAN
            //If no array is returned, READ is supported
            var listReturned = false;
            for (response in pathItemObject.get.responses.responses.values) {
                if (response.content != null) {
                    for (content in response.content.values) {
                        if (content.schema != null) {
                            if (content.schema.type == "array") {
                                listReturned = true;
                            }
                        }
                    }
                }
            }
            if (!listReturned && path.contains("{") && path.contains("}")) {
                var operation = PatternOperation(abstractOperation, AbstractPatternOperation.READ)
                operation.path = path

                //Determine input and output values
                println("READ:")
                var getObject = pathItemObject.get
                println("    " + GsonBuilder().create().toJson(getObject))
                if (getObject.requestBody != null) {
                    //Operation requires some request body
                    var body = MatchingUtil().parseRequestBody(getObject.requestBody, spec)
                    if (body != null) {
                        operation.requiredBody = body
                    }
                }
                if (getObject.parameters != null) {
                    operation.parameters = MatchingUtil().parseParameter(getObject.parameters, spec)
                    println("Found " + operation.parameters.size + " parameters")
                }
                return operation
            }
        }
        return null
    }

}