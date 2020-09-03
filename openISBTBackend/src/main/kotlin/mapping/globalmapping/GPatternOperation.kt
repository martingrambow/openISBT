package mapping.globalmapping

import com.google.gson.JsonObject
import matching.link.ServiceLinkObject
import patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPatternOperation
//abstractOperation: name, input, output, selector, wait
//abstractPatternOperation: //CREATE; UPDATE; DELETE; ...
class GPatternOperation(var abstractOperation: AbstractOperation, var abstractPatternOperation:AbstractPatternOperation) {
    var serviceName:String = "" //microservice name
    var path:String = "" //Concrete Path which supports this operation
    var requests:Int = 0 //Number of requests to that path
    var parameters:ArrayList<JsonObject> = ArrayList() //List of required parameters
    var headers:ArrayList<Pair<String, JsonObject>> = ArrayList() //List of headers
    var requiredBody:JsonObject = JsonObject() //Schema required in body
    var links:ArrayList<ServiceLinkObject> = ArrayList()
    var produces:JsonObject? = JsonObject() //Values which are produced by this operation
}