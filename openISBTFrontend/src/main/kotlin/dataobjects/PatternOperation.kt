package dataobjects

data class PatternOperation (var aOperation:AbstractOperation,
                             var aPatternOperation:String,
                             var path:String,
                             var requests:Int,
                             var consumes:String,
                             var produces:String){
}