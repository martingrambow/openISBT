package mapping.globalmapping

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import mapping.IMapper
import matching.link.ServiceLinkObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.Modifier

class GMapper:IMapper {

    val log: Logger = LoggerFactory.getLogger("GMapper")

    private var gPatternMappings:ArrayList<GPatternBinding> = ArrayList()

    private var patternConfiguration: PatternConfiguration? = null
    private var serviceLinks:ArrayList<ServiceLinkObject>? = null
    private var openApispecs:ArrayList<OpenAPISPecifcation> = ArrayList()

    override fun setPatternConfiguration(configuration: PatternConfiguration) {
        patternConfiguration = configuration
    }

    fun setServiceLinks(links: Array<ServiceLinkObject>) {
        serviceLinks = ArrayList()
        for (link in links) {
            serviceLinks!!.add(ServiceLinkObject(link.prefix1, link.parameterName1, link.prefix2, link.parameterName2))
            serviceLinks!!.add(ServiceLinkObject(link.prefix2, link.parameterName2, link.prefix1, link.parameterName1))
        }
    }

    override fun addOpenAPISpec(spec: OpenAPISPecifcation) {
        openApispecs.add(spec)
    }

    override fun mapPattern(excludePaths:Array<String>) : Boolean {
        log.info("--------------------")
        log.info("Binding starts...")

        if (openApispecs.size == 0) {
            log.error("No OpenAPI specifications")
            return false
        }

        for (pattern in patternConfiguration!!.patterns) {
            //Find all possible interaction sequences for this pattern in all openAPI specifications
            log.debug("Find all mappings for pattern ${pattern.name}:")
            var prevMappings:ArrayList<GMapping> = ArrayList()
            val links = ArrayList<ServiceLinkObject>()
            if (serviceLinks != null) {
                links.addAll(serviceLinks!!)
            }
            prevMappings.add(GMapping(openApispecs.toTypedArray(),links))
            var nextMappings:ArrayList<GMapping> = ArrayList()

            for (index in 0 until pattern.sequence.size) {
                log.debug("Current index is $index, there is/are ${prevMappings.size} sequence(s) left from the previous loop.")
                val operation = pattern.sequence[index]
                for (mapping in prevMappings) {
                    //Expand with next operation and check dependencies
                    nextMappings.addAll(mapping.expand(operation))
                }
                log.debug("There are ${nextMappings.size} sequences after expanding with operation ${operation.operation}")
                if (nextMappings.isEmpty()) {
                    //This pattern is not supported at all; all sequences were cut
                    log.error("Could not find at least one interaction sequence for pattern ${pattern.name}, abbort.")
                    return false
                }
                prevMappings = nextMappings
                nextMappings = ArrayList()
            }
            log.info("Found ${prevMappings.size} mappings for ${pattern.name}")
            //All possible Mappings for the current pattern are in prevMappings
            gPatternMappings.add(GPatternBinding(pattern, prevMappings))
        }
        return true
    }


    override fun calculateRequests() {
        //(re)set all request numbers to 0
        for (mappings in gPatternMappings) {
            mappings.requests = 0
            for (mapping in mappings.gMappingList) {
                mapping.numberOfRequests = 0
            }
        }

        //Ensure that all patterns are supported
        for (globalMapping in gPatternMappings) {
            if (!globalMapping.supported) {
                throw IllegalArgumentException("Pattern ${globalMapping.abstractPattern.name} is not supported.")
            }
        }

        //Distribute requests across patterns according to weight
        val totalRequests = patternConfiguration!!.totalPatternRequests
        //find total weight
        var totalWeight = 0
        for (pattern in patternConfiguration!!.patterns) {
            totalWeight += pattern.weight
        }
        for (globalMapping in gPatternMappings) {
            globalMapping.requests = Math.round(totalRequests / totalWeight.toFloat() * globalMapping.abstractPattern.weight.toFloat())
            var requestsToDistribute = globalMapping.requests
            //Distribute equally across all possible interaction sequences
            while (requestsToDistribute > 0) {
                for (mapping in globalMapping.gMappingList) {
                    if (requestsToDistribute > 0) {
                        mapping.numberOfRequests += 1
                        requestsToDistribute--
                    }
                }
            }
        }
    }

    override fun printSupportInfo() {
        log.info("Supported resource mappings:")
        for (rmapping in gPatternMappings) {
            log.info("Resource Mapping for pattern ${rmapping.abstractPattern.name} (supported=${rmapping.supported}, requests=${rmapping.requests}):")
            for (m in rmapping.gMappingList) {
                log.info("  New Interaction Sequence:")
                for (op in m.patternOperations) {
                    log.info("    Operation: ${op.abstractOperation.operation} path=${op.path}")
                }
            }
        }
    }

    override fun saveMapping(file: File) {
        val gson: Gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        file.writeText(gson.toJson(gPatternMappings))
    }

}