package matching.units

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import mapping.PatternOperation
import matching.MatchingUnit
import patternconfiguration.AbstractPatternOperation

class UpdateMatchingUnit : MatchingUnit{
    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.put != null) {
            var operation = PatternOperation(abstractOperation, AbstractPatternOperation.UPDATE)
            operation.path = path
            return operation
        }
        return null
    }

}