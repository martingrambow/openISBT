package de.tuberlin.mcc.patternconfiguration

import patternconfiguration.AbstractPattern

data class PatternConfiguration (val totalPatternRequests : Int,
                                 val patterns : Array<AbstractPattern>,
                                 val manualDecision: Boolean){
    }