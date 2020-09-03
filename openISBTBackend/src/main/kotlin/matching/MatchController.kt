package matching

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
import mapping.globalmapping.GPatternOperation
import matching.units.ScanMatchingUnit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class MatchController {

    private var matchUnitList:ArrayList<MatchingUnit> = ArrayList()
    //Just to log what's happening
    val log: Logger = LoggerFactory.getLogger("MatchController")

    fun registerMatchingUnit(unit:MatchingUnit) {
        matchUnitList.add(unit)
    }

    fun matchPatternOperation(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path:String): GPatternOperation? {
        for (unit in matchUnitList) {
            if (abstractOperation.operation == unit.getSupportedOperation()) {
                log.debug("Check " + abstractOperation.operation + " for path " + path + " ...")
                val operation: GPatternOperation? = unit.match(pathItemObject, abstractOperation, spec, path)
                if (operation != null) {
                    //successfully matched
                    log.debug(unit.javaClass.name + " matches " + path)
                    if (unit.getSupportedOperation() == AbstractPatternOperation.READ.name) {
                        //Check if this operation also matches SCAN
                        val unit2 = ScanMatchingUnit()
                        if (unit2.match(pathItemObject, abstractOperation, spec, path ) != null) {
                            //SCAN matches as well and has priority
                            log.debug("Scan unit matches also, no read operation")
                            continue
                        }
                    }
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