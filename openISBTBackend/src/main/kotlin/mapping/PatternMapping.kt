package mapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import de.tuberlin.mcc.patternconfiguration.Condition
import de.tuberlin.mcc.patternconfiguration.Pattern
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class PatternMapping {

    //top level path, e.g. /user
    var resourcePath:String = "";
    var pattern:ArrayList<Pair<Pattern, Boolean>> = ArrayList();
    //true, if all pattern in a given pattern config are supported by this resource path
    var supported:Boolean = true;
    //number of requests which are sent to this resource endpoint
    var numberOfRequests:Int = 0;
    //true, if the user wants to benchmark this resource endpoint; false otherwise (true per default if the path is supported, can be changed in GUI)
    var enabled:Boolean = false;
    //Just to log what's happening
    val log = LoggerFactory.getLogger(Class.forName("PatternMapping"));

    constructor(spec:OpenAPISPecifcation, config:PatternConfiguration, path:String) {
        this.resourcePath = path;

        //get supported operations for a given resource path
        var supportedOperations = getSupportedOperations(this.resourcePath, spec)

        log.debug(this.resourcePath + " supports the following operations:")
        for (sup in supportedOperations) {
            log.debug("-->" + sup.first.name + " is supported by " + sup.second)
        }

        for (p in config.patterns) {
            if (p.paths == null) {
                p.paths = emptyArray<String>()
            }
            if (p.conditions == null) {
                p.conditions = emptyArray<Condition>()
            }
            var supported:Boolean = true
            //Check if the given resource path supports the given pattern p
            //all operations in the pattern sequence must be supported
            for (operation in p.sequence) {
                var operationName = operation.operation.toUpperCase()
                var operationIsSupported = false
                for (sup in supportedOperations) {
                    if (sup.first.name == operationName) {
                        operationIsSupported = true
                    }
                }
                if (!operationIsSupported) {
                    //one operation is not supported -> the pattern is not supported
                    supported = false;
                }
            }
            //input and output of operation sequence must match

            //TODO: Implement sequence check
            //clone abstract operations

            var sequenceCopy = ArrayList<AbstractOperation>()
            for (o in p.sequence) {
                var operationValue = "";
                if (o.operation != null) {
                    operationValue = o.operation
                }
                var inputValue = "";
                if (o.input != null) {
                    inputValue = o.input
                }
                var outputValue = "";
                if (o.output != null) {
                    outputValue = o.output
                }
                var selectorValue = "";
                if (o.selector != null) {
                    selectorValue = o.selector
                }
                var waitValue = 0;
                if (o.wait != null) {
                    waitValue = o.wait
                }
                var copy = AbstractOperation(operationValue, inputValue, outputValue, selectorValue, waitValue, emptyArray())
                sequenceCopy.add(copy)
            }

            val newPattern = Pattern(p.name, sequenceCopy.toTypedArray(), p.weight, p.requests, p.conditions, emptyArray())
            this.pattern.add(Pair(newPattern, supported))
        }

        for (pair in this.pattern) {
            log.debug("Current pair: " + pair.first.name + " Supported: " + pair.second)
            for (operation in pair.first.sequence) {
                log.debug("Finding paths for operation " + operation)
                if (operation.paths == null) {
                    log.debug("Old paths object is null")
                } else {
                    log.debug("Old paths object: " + operation.paths.toString())
                }
                var operationName = operation.operation.toUpperCase()
                var supportedPaths = ArrayList<String>()
                for (sup in supportedOperations) {
                    if (sup.first.name == operationName) {
                        log.debug(sup.second  + " supports " + operationName)
                        supportedPaths.add(sup.second)
                    }
                }
                log.debug("New Paths for operation " + operation.operation + ": " + supportedPaths.toString())
                operation.paths = supportedPaths.toTypedArray()
            }
        }

        for(pair in this.pattern) {
            if (pair.second == false){
                this.supported = false;
            }
        }

        if (this.supported) {
            this.enabled = true;
        }
    }

    fun getSupportedOperations(topLevelPath: String, spec: OpenAPISPecifcation):ArrayList<Pair<AbstractPatternOperation, String>> {
        //Pair: which pattern is supported and under which path
        var operations:ArrayList<Pair<AbstractPatternOperation, String>> = ArrayList()

        for (path in spec.paths.paths.keys) {
            if (path.startsWith(topLevelPath)) {
                var pathItemObject = spec.paths.paths.get(path);
                if (pathItemObject != null) {
                    for (operation in getSupportedOperations(path, pathItemObject)) {
                        operations.add(Pair(operation, path))
                    }
                }
            }
        }

        return operations
    }

    fun getSupportedOperations(path: String, pathItem: PathItemObject):ArrayList<AbstractPatternOperation> {
        //Pair: which pattern is supported and under which path
        var operations:ArrayList<AbstractPatternOperation> = ArrayList()

        //Cancel if there is a subresource
        if (path.contains("{") && path.contains("}/")) {
            return operations
        }

        //add all supported operations which are supported by the given pathItemObject
        if (pathItem.post != null) {
            operations.add(AbstractPatternOperation.CREATE)
        }
        if (pathItem.delete != null) {
            operations.add(AbstractPatternOperation.DELETE)
        }
        if (pathItem.put != null) {
            operations.add(AbstractPatternOperation.UPDATE)
        }
        if (pathItem.get != null) {
            //get could support READ or SCAN
            //If an array of items is returned, SCAN is supported; READ otherwise
            var listReturned = false;
            for (response in pathItem.get.responses.responses.values) {
                if (response.content != null) {
                    for (content in response.content.values) {
                        if (content.schema != null) {
                            if (content.schema.type == "array") {
                                listReturned = true;
                            }
                        }
                    }
                }
            }
            if (listReturned) {
                operations.add(AbstractPatternOperation.SCAN)
            } else {
                operations.add(AbstractPatternOperation.READ)
            }
        }
        return operations
    }

}