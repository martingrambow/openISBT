package run

data class Worker (var url:String = "",
                   var id:Int = 0,
                   var status:String = "",
                   var threads:Int = 0) {
}