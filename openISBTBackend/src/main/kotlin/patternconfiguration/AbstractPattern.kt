package de.tuberlin.mcc.patternconfiguration

data class AbstractPattern (val name : String,
                            val sequence : Array<AbstractOperation>,
                            val weight: Int,
                            val requests: Int,
                            val conditions : Array<Condition>){
}