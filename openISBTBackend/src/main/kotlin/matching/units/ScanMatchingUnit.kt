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

class ScanMatchingUnit : MatchingUnit{

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.SCAN.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.get != null) {
            //get could support READ or SCAN
            //If an array of items is returned, SCAN is supported
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
            if (listReturned) {
                var operation = PatternOperation(abstractOperation, AbstractPatternOperation.SCAN)
                operation.path = path

                //Determine input and output values
                println("SCAN:")
                var scanObject = pathItemObject.get
                println("    " + GsonBuilder().create().toJson(scanObject))
                if (scanObject.requestBody != null) {
                    //Operation requires some request body
                    var body = MatchingUtil().parseRequestBody(scanObject.requestBody, spec)
                    if (body != null) {
                        operation.requiredBody = body
                    }
                }
                if (scanObject.parameters != null) {
                    operation.parameters = MatchingUtil().parseParameter(scanObject.parameters, spec)
                }
                if (scanObject.security != null) {
                    var header = MatchingUtil().parseApiKey(scanObject.security, spec)
                    if (header != null) {
                        operation.headers.add(header)
                    }
                }

                return operation
            }
        }
        return null
    }

}