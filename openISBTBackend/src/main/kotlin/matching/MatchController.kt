package matching

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import mapping.PatternOperation

class MatchController {

    var matchUnitList:ArrayList<MatchingUnit> = ArrayList()

    fun registerMatchingUnit(unit:MatchingUnit) {
        matchUnitList.add(unit)
    }

    fun matchPatternOperation(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path:String):PatternOperation? {
        for (unit in matchUnitList) {
            var operation:PatternOperation? = unit.match(pathItemObject, abstractOperation, spec, path)
            if (operation != null) {
                //successfully matched
                return operation
            }
        }
        //No matching unit could match the given pathItenObject to given abstract operation
        return null
    }
}