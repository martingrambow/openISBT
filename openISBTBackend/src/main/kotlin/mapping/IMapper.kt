package mapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import java.io.File

interface IMapper {

    fun mapPattern(excludePaths:Array<String>):Boolean
    fun setPatternConfiguration(configuration: PatternConfiguration)
    fun addOpenAPISpec(spec:OpenAPISPecifcation)
    fun calculateRequests()
    fun printSupportInfo()
    fun saveMapping(file:File)

}