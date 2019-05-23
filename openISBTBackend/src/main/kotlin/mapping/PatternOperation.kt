package mapping

import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPatternOperation

class PatternOperation(abstractOperation: AbstractOperation, abstractPatternOperation:AbstractPatternOperation) {

    var aOperation = abstractOperation //name, input, output, selector, wait
    var aPatternOperation = abstractPatternOperation //CREATE; UPDATE; DELETE; ...
    var path:String = "" //Concrete Path which supports this operation
    var consumes:String = "" //Parameters etc which is required to call this operation
    var produces:String ="" //Values which are produced by this operation

}