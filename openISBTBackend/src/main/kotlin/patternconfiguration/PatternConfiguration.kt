package de.tuberlin.mcc.patternconfiguration

data class PatternConfiguration (val totalPatternRequests : Int,
                                 val patterns : Array<Pattern>,
                                 val manualDecision: Boolean){
    }