package mapping

import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPatternOperation

class PatternOperation(abstractOperation: AbstractOperation, abstractPatternOperation:AbstractPatternOperation) {

    var aOperation = abstractOperation //name, input, output, selector, wait
    var aPatternOperation = abstractPatternOperation //CREATE; UPDATE; DELETE; ...
    var path:String = "" //Concrete Path which supports this operation
    var requests:Int = 0 //Number of requests to that path
    var consumes:String = "" //Parameters etc which is required to call this operation
    var produces:String ="" //Values which are produced by this operation

}