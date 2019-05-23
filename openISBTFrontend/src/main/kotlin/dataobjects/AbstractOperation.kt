package dataobjects

data class AbstractOperation (var operation:String,
                              var output:String,
                              var input:String,
                              var selector:String,
                              var wait:Int,
                              var paths:Array<String>){
}