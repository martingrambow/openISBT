package dataobjects

data class Pattern (var name:String,
                    var requests:Int,
                    var weight:Int,
                    var sequence:Array<AbstractOperation>){
}