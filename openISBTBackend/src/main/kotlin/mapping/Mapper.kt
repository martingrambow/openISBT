package mapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import org.slf4j.LoggerFactory

class Mapper {

    val log = LoggerFactory.getLogger(Class.forName("mapping.Mapper"));

    fun mapPattern(openApi:OpenAPISPecifcation, config: PatternConfiguration):ArrayList<ResourceMapping> {

        var resourceMappingList = ArrayList<ResourceMapping>()

        //Determine top level resources
        val resourcePaths = getTopLevelResourcePaths(openApi)
        //and map them to given patternMappingList
        for (path in resourcePaths) {
            log.debug("Top Level Path: " + path)
            var mapping = ResourceMapping(openApi, config, path)
            resourceMappingList.add(mapping)
        }

        return calculateRequests(resourceMappingList, config)
    }

    fun calculateRequests(resourceMappingList:ArrayList<ResourceMapping>, config: PatternConfiguration) : ArrayList<ResourceMapping> {
        //Set all request numbers to 0
        for (mapping in resourceMappingList) {
            mapping.numberOfRequests = 0
            for (patternMapping in mapping.patternMappingList) {
                patternMapping.requests = 0
            }
        }

        //Distribute total requests according to weights
        var supportedAndEnabledPaths:ArrayList<ResourceMapping> = ArrayList()
        for (mapping in resourceMappingList) {
            if (mapping.supported && mapping.enabled) {
                supportedAndEnabledPaths.add(mapping)
            }
        }
        if (supportedAndEnabledPaths.size > 0) {
            var requestsPerTopLevelPath = config.totalPatternRequests / supportedAndEnabledPaths.size;
            for (mapping in resourceMappingList) {
                if (mapping.supported && mapping.enabled) {
                    //Distribute according to weight
                    mapping.numberOfRequests = requestsPerTopLevelPath

                    //find totalWeight
                    var totalWeight:Int = 0
                    mapping.patternMappingList.forEach { patternMapping: PatternMapping ->
                        totalWeight += patternMapping.aPattern.weight
                    }
                    log.debug("TotalWeight for " + mapping.resourcePath + " is " + totalWeight)
                    mapping.patternMappingList.forEach { patternMapping: PatternMapping ->
                        patternMapping.requests = Math.round( requestsPerTopLevelPath / totalWeight.toFloat() * patternMapping.aPattern.weight.toFloat())
                        log.debug("Assigned some value to " + patternMapping.aPattern.name)
                    }
                }
            }
        }
        return resourceMappingList
    }


    fun getTopLevelResourcePaths(openApi: OpenAPISPecifcation):ArrayList<String> {
        var resourcePaths = ArrayList<String>()

        var remainingPathsToCheck:ArrayList<String> = ArrayList()
        var tmpRemainingPathsToCheck:ArrayList<String>

        for (path in openApi.paths.paths.keys) {
            remainingPathsToCheck.add(path);
        }

        var counter:Int = 2;
        while (remainingPathsToCheck.size > 0) {
            tmpRemainingPathsToCheck = ArrayList()
            for (path in remainingPathsToCheck) {
                var parts = path.split('/');
                if (parts.size == counter && parts[0] == "") {
                    //path ends here and could be a new top level resource, if not already discovered
                    var alreadyCovered = false;
                    //concat possible top level resource path
                    var resourcePath:String = "";
                    for (i in 1..counter-1) {
                        resourcePath += '/' + parts[i];
                        if (resourcePaths.indexOf(resourcePath) !== -1) {
                            alreadyCovered = true;
                        }
                    }
                    if (!alreadyCovered) {
                        //check if this path is not already discovered
                        if (resourcePaths.indexOf(resourcePath) == -1) {
                            //not yet discovered
                            resourcePaths.add(resourcePath);
                        }
                    }
                } else {
                    //this path goes further and has to be checked in the next iteration
                    tmpRemainingPathsToCheck.add(path);
                }
            }
            counter++;
            remainingPathsToCheck = tmpRemainingPathsToCheck;
        }
        return resourcePaths
    }

}