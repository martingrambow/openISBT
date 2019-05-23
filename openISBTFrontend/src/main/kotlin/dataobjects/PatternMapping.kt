package dataobjects

data class PatternMapping (var resourcePath:String,
                           val pattern:Array<Pair<Pattern, Boolean>>,
                           val supported:Boolean,
                           var numberOfRequests:Int,
                           var enabled:Boolean){
}