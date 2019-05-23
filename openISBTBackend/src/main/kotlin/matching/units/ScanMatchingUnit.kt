package matching.units

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import mapping.PatternOperation
import matching.MatchingUnit
import patternconfiguration.AbstractPatternOperation

class ScanMatchingUnit : MatchingUnit{
    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (abstractOperation.operation == AbstractPatternOperation.SCAN.name) {
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
                    return operation
                }
            }
        }
        return null
    }

}