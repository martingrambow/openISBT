package mapping.globalmapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import patternconfiguration.AbstractOperation

class GMapping(val openAPiSpecs: Array<OpenAPISPecifcation>) {

    // Sequence of concrete operations
    var patternOperations:ArrayList<GPatternOperation> = ArrayList()
    //number of requests which should be sent according to this mapping
    var numberOfRequests:Int = 0
    //true, if the user wants to benchmark this mapping; (true per default, can be changed in GUI)
    var enabled:Boolean = true


    fun expand(operation:AbstractOperation): ArrayList<GMapping>{
        var expandedMappings:ArrayList<GMapping> = ArrayList()
        /*
            Check if current operation is supported:
            - check all openAPI specifications
            - include in-/output of previous operations
            expand the this Global Mapping with the found operation and
            add this mapping to the expandedMappingList
        */


        return expandedMappings
    }

}