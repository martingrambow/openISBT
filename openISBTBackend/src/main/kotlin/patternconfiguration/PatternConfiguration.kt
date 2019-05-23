package de.tuberlin.mcc.patternconfiguration

data class PatternConfiguration (val totalPatternRequests : Int,
                                 val abstractPatterns : Array<AbstractPattern>,
                                 val manualDecision: Boolean){
    }