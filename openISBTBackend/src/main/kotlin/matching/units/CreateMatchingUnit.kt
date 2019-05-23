package matching.units

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import mapping.PatternOperation
import matching.MatchingUnit
import patternconfiguration.AbstractPatternOperation

class CreateMatchingUnit : MatchingUnit{
    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (abstractOperation.operation == AbstractPatternOperation.CREATE.name) {
            if (pathItemObject.post != null) {
                var operation = PatternOperation(abstractOperation, AbstractPatternOperation.CREATE)
                operation.path = path
                return operation
            }
        }
        return null
    }

}