package patternconfiguration

data class AbstractOperation (val operation : String,
                              val input : String,
                              val output: String,
                              val selector : String,
                              val level : Int = 0,
                              val wait : Int)