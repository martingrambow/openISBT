package workload

data class AbstractPattern (var name:String,
                            var requests:Int,
                            var weight:Int,
                            var sequence:Array<AbstractOperation>){
}