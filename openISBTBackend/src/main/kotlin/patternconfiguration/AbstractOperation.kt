package de.tuberlin.mcc.patternconfiguration

data class AbstractOperation (val operation : String,
                              val input : String,
                              val output: String,
                              val selector : String,
                              val wait : Int){
}