package patternconfiguration

import de.tuberlin.mcc.patternconfiguration.Condition

data class AbstractPattern (val name : String,
                            val sequence : Array<AbstractOperation>,
                            val weight: Int,
                            val requests: Int,
                            val conditions : Array<Condition>){
}