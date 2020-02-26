package mapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import org.slf4j.LoggerFactory

class Mapper(val openApi:OpenAPISPecifcation?, val config: PatternConfiguration) {

    val log = LoggerFactory.getLogger("Mapper")

    var resourceMappings:ArrayList<ResourceMapping> = ArrayList()

    fun mapPattern(): Boolean {
        if (openApi == null) {
            log.error("No OpenAPI specification.")
            return false
        }

        var resourceMappingList = ArrayList<ResourceMapping>()

        //Determine top level resources
        val resourcePaths = getTopLevelResourcePaths(openApi)
        log.info("Top level paths:")
        for (path in resourcePaths) {
            log.info("-> " + path)
        }
        log.info("--------------------")
        log.debug("Binding starts...")

        //and map them to given patternMappingList
        for (path in resourcePaths) {
            log.debug("Top Level Path: " + path)
            var mapping = ResourceMapping(openApi, config, path)
            resourceMappingList.add(mapping)

            log.info("Resource Mapping for " + path + ":")

            for (mapping in mapping.patternMappingList) {
                log.info("  Pattern " + mapping.aPattern.name + ", supported=" + mapping.supported + ", requests=" + mapping.requests + ":")
                for (operationlist in mapping.operationSequence) {
                    for (entry in operationlist) {
                        log.info("      Operation: " + entry.aOperation.operation + " path=" + entry.path)
                    }
                }
            }
        }

        //Manually assign /register to /customers (custom binding definition)
        for (resourceMapping in resourceMappingList) {
            if (resourceMapping.resourcePath == "/customers") {
                for (i in 0 .. resourceMapping.patternMappingList.size-1) {
                    var mapping = resourceMapping.patternMappingList.get(i)
                    if (mapping.supported == false && mapping.aPattern.name == "CRE") {

                        var createOperations : List<PatternOperation> = ArrayList()
                        for (tmp in resourceMappingList) {
                            if (tmp.resourcePath == "/register") {
                                for (tmp2 in tmp.patternMappingList) {
                                    if (tmp2.aPattern.name == "CRE") {
                                        createOperations = tmp2.operationSequence.get(0)
                                    }
                                }
                            }
                        }
                        if (createOperations != null) {
                            resourceMapping.patternMappingList.get(i).operationSequence.set(0, createOperations)
                        }
                    }
                }

                //Recheck pattern support
                resourceMapping.checkAndSetSupport()
            }
        }
        resourceMappings = resourceMappingList
        return true
    }

    fun calculateRequests() {
        //Set all request numbers to 0
        for (mapping in resourceMappings) {
            mapping.numberOfRequests = 0
            for (patternMapping in mapping.patternMappingList) {
                patternMapping.requests = 0
                for (list in patternMapping.operationSequence) {
                    for (item in list) {
                        item.requests = 0
                    }
                }
            }
        }

        //Distribute total requests according to weights
        var supportedAndEnabledPaths:ArrayList<ResourceMapping> = ArrayList()
        for (mapping in resourceMappings) {
            if (mapping.supported && mapping.enabled) {
                supportedAndEnabledPaths.add(mapping)
            }
        }
        if (supportedAndEnabledPaths.size > 0) {
            var requestsPerTopLevelPath = config.totalPatternRequests / supportedAndEnabledPaths.size;
            for (mapping in resourceMappings) {
                if (mapping.supported && mapping.enabled) {
                    //Distribute according to weight
                    mapping.numberOfRequests = requestsPerTopLevelPath

                    //find totalWeight
                    var totalWeight:Int = 0
                    mapping.patternMappingList.forEach { patternMapping: PatternMapping ->
                        totalWeight += patternMapping.aPattern.weight
                    }
                    mapping.patternMappingList.forEach { patternMapping: PatternMapping ->
                        var numberofPatternRequests = Math.round( requestsPerTopLevelPath / totalWeight.toFloat() * patternMapping.aPattern.weight.toFloat())
                        patternMapping.requests = numberofPatternRequests
                        //assign value also to pattern mappings and split them if there are multiple resource paths
                        for (list in patternMapping.operationSequence) {
                            var requestsToDistribute = numberofPatternRequests
                            var numberOfPatternMappings = list.size
                            if (numberOfPatternMappings > 0) {
                                while (requestsToDistribute > 0) {
                                    for (item in list) {
                                        if (requestsToDistribute > 0) {
                                            item.requests += 1
                                            requestsToDistribute--
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
                if ((parts.size == counter && parts[0] == "") || (parts.size == counter+1 && parts[parts.size-1] == "")) {
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
            counter++
            remainingPathsToCheck = tmpRemainingPathsToCheck
        }
        return resourcePaths
    }

}