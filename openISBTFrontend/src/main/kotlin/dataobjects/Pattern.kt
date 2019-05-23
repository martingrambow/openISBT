package dataobjects

data class Pattern (var aPattern:AbstractPattern,
                    var supported:Boolean,
                    var requests:Int,
                    var operationSequence:Array<Array<PatternOperation>>){
}