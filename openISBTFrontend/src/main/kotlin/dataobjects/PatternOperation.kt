package dataobjects

data class PatternOperation (var aOperation:AbstractOperation,
                             var aPatternOperation:String,
                             var path:String,
                             var requests:Int,
                             var parameters:Array<String>, //List of required parameters
                             var requiredBody:String, //Schema required in body
                             var produces:String){
}