package mapping

import com.google.gson.JsonObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPatternOperation

class PatternOperation(abstractOperation: AbstractOperation, abstractPatternOperation:AbstractPatternOperation) {

    var aOperation = abstractOperation //name, input, output, selector, wait
    var aPatternOperation = abstractPatternOperation //CREATE; UPDATE; DELETE; ...
    var path:String = "" //Concrete Path which supports this operation
    var requests:Int = 0 //Number of requests to that path
    var parameters:ArrayList<JsonObject> = ArrayList() //List of required parameters
    var requiredBody:String = "" //Schema required in body
    var produces:String ="" //Values which are produced by this operation

}