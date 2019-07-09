package dataobjects

import kotlin.js.Json

data class PatternOperation (var aOperation:AbstractOperation,
                             var aPatternOperation:String,
                             var path:String,
                             var requests:Int,
                             var parameters:Array<ParameterObject>, //List of required parameters
                             var headers:Array<Pair<String, Json>>, //List of headers
                             var requiredBody:Json, //Schema required in body
                             var produces:String){
}