package mapping.globalmapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import org.slf4j.LoggerFactory

class GMapper(val openAPiSpecs: Array<OpenAPISPecifcation>, val patternConfiguration:PatternConfiguration) {

    val log = LoggerFactory.getLogger("GMapper")
    var globalExcludedPaths:ArrayList<String> = ArrayList()

    var gPatternMappings:ArrayList<GPatternMapping> = ArrayList()

    fun mapPattern() : Boolean {
        log.info("--------------------")
        log.info("Binding starts...")

        if (openAPiSpecs.size == 0) {
            log.error("No OpenAPI specifications")
            return false
        }

        for (pattern in patternConfiguration.patterns) {
            //Find all possible ResourceMappings for this pattern in all openAPI specifications

            log.debug("Find all mappings for pattern ${pattern.name}")
            var prevMappings:ArrayList<GMapping> = ArrayList()
            prevMappings.add(GMapping(openAPiSpecs))
            var nextMappings:ArrayList<GMapping> = ArrayList()

            for (index in 0 .. pattern.sequence.size-1) {
                log.debug("Current index is $index, there are ${prevMappings.size} mappings from the previous loop to check")
                var operation = pattern.sequence.get(index)
                for (mapping in prevMappings) {
                    nextMappings.addAll(mapping.expand(operation))
                }
                log.debug("There are ${nextMappings.size} mappings left after expanding with operation ${operation.operation}")
                prevMappings = nextMappings
                if (prevMappings.isEmpty()) {
                    //This pattern is not supported at all
                    log.error("Could not find at least one resource mapping for every pattern.")
                    return false
                }
            }
            log.info("Found ${prevMappings.size} mappings for ${pattern.name}")
            //All possible Mappings for the current pattern are in prevMappings

            gPatternMappings.add(GPatternMapping(pattern, prevMappings))
        }
        return true
    }


    fun calculateRequests() {
        //Set all request numbers to 0
        for (mappings in gPatternMappings) {
            mappings.requests = 0
            for (mapping in mappings.gMappingList) {
                mapping.numberOfRequests = 0
            }
        }

        //Distribute total requests according to weights
        val supportedMappings:ArrayList<GPatternMapping> = ArrayList()
        for (globalMapping in gPatternMappings) {
            if (globalMapping.supported) {
                supportedMappings.add(globalMapping)
            }
        }
        if (supportedMappings.size > 0) {
            //Distribute requests across patterns according to weight
            val totalRequests = patternConfiguration.totalPatternRequests
            //find total weight
            var totalWeight: Int = 0
            for (pattern in patternConfiguration.patterns) {
                totalWeight += pattern.weight
            }
            for (globalMapping in gPatternMappings) {
                globalMapping.requests = Math.round(totalRequests / totalWeight.toFloat() * globalMapping.abstractPattern.weight.toFloat())
                var requestsToDistribute = globalMapping.requests
                //Distribute equally across all possible mappings
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
    }

}