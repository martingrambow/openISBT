package dataobjects

data class ResourceMapping (var resourcePath:String,
                            val patternMappingList:Array<Pattern>,
                            val supported:Boolean,
                            var numberOfRequests:Int,
                            var enabled:Boolean){
}