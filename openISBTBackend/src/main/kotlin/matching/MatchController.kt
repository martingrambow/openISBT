package matching

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import mapping.PatternOperation
import org.slf4j.LoggerFactory

class MatchController {

    var matchUnitList:ArrayList<MatchingUnit> = ArrayList()
    //Just to log what's happening
    val log = LoggerFactory.getLogger("MatchController");

    fun registerMatchingUnit(unit:MatchingUnit) {
        matchUnitList.add(unit)
    }

    fun matchPatternOperation(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path:String):PatternOperation? {
        for (unit in matchUnitList) {
            var operation:PatternOperation? = unit.match(pathItemObject, abstractOperation, spec, path)
            if (operation != null) {
                //successfully matched
                log.debug(unit.javaClass.name + " matches " + path)
                return operation
            }
        }
        //No matching unit could match the given pathItenObject to given abstract operation
        return null
    }
}