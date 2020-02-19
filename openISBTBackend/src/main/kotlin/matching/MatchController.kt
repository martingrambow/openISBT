package matching

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
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
            if (abstractOperation.operation.equals(unit.getSupportedOperation())) {
                log.debug("Check " + abstractOperation.operation + " for path " + path + " ...")
                var operation: PatternOperation? = unit.match(pathItemObject, abstractOperation, spec, path)
                if (operation != null) {
                    //successfully matched
                    log.debug(unit.javaClass.name + " matches " + path)
                    log.debug("-----------")
                    return operation
                } else {
                    log.debug(unit.javaClass.name + " does not match " + path)
                }
                log.debug("-----------")
            }
        }
        //No matching unit could match the given pathItenObject to given abstract operation
        return null
    }
}