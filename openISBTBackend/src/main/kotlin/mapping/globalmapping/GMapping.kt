package mapping.globalmapping

import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import mapping.simplemapping.PatternOperation
import matching.MatchController
import matching.link.ServiceLinkObject
import matching.units.*
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractOperation

class GMapping(private val openAPiSpecs: Array<OpenAPISPecifcation>, private val serviceLinks: ArrayList<ServiceLinkObject>) {

    // Sequence of concrete operations
    var patternOperations:ArrayList<PatternOperation> = ArrayList()
    //number of requests which should be sent according to this mapping
    var numberOfRequests:Int = 0
    //true, if the user wants to benchmark this mapping; (true per default, can be changed in GUI)
    var enabled:Boolean = true

    @Transient
    val log = LoggerFactory.getLogger("GMapping")!!


    companion object {
        @Transient
        val matchController = MatchController()
        init {
            matchController.registerMatchingUnit(CreateMatchingUnit())
            matchController.registerMatchingUnit(DeleteMatchingUnit())
            matchController.registerMatchingUnit(UpdateMatchingUnit())
            matchController.registerMatchingUnit(PatchMatchingUnit())
            matchController.registerMatchingUnit(ScanMatchingUnit())
            matchController.registerMatchingUnit(ReadMatchingUnit())
        }

    }


    fun expand(operation:AbstractOperation): ArrayList<GMapping>{
        val expandedMappings:ArrayList<GMapping> = ArrayList()
        /*
            Check if current operation is supported:
            - check all openAPI specifications
            - include in-/output of previous operations
            expand the this Global Mapping with the found operation and
            add this mapping to the expandedMappingList
        */

        val involvedServices = ArrayList<String>()
        if (patternOperations.isNotEmpty()) {
            //There are previous operations, do not check all other specs
            for (op in patternOperations) {
                //Add all services which are already in the interaction sequence (if they are not already in the list)
                if (!involvedServices.contains(op.serviceName)) {
                    involvedServices.add(op.serviceName)
                }
                //Add service links to other services
                for (link in serviceLinks) {
                    if (trimPath(op.path).startsWith(link.prefix1)) {
                        //link.prefix2 is related
                        for (spec in openAPiSpecs) {
                            for (pathitem in spec.paths.paths) {
                                if (pathitem.key.startsWith(link.prefix2)) {
                                    //found link
                                    if (!involvedServices.contains(spec.info.title)) {
                                        involvedServices.add(spec.info.title)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } else {
            //Add all services
            for (spec in openAPiSpecs) {
                involvedServices.add(spec.info.title)
            }
        }

        for (spec in openAPiSpecs) {
            //Only analyze the specs of involved (previously called or linked) services
            if (involvedServices.contains(spec.info.title)) {
                for (path in spec.paths.paths.keys) {

                    log.debug("Match ${operation.operation} to $path ...")
                    val patternOperation = matchController.matchPatternOperation(spec.paths.paths.getValue(path), operation, spec, path)

                    if (patternOperation != null) {
                        //matching was successful, create new GMapping with this extended pattern operation
                        log.debug("Successfully matched ${operation.operation} to $path, check dependencies ...")

                        //Check dependencies for the matched operation
                        var resolved = true

                        //Is there an input or a parameter in path?
                        if (operation.input != null || (patternOperation.path.contains("{") && patternOperation.path.contains("}"))) {
                            //There is some input which must be resolved
                            resolved = false
                        }

                        //Does the found operation require input(s)?
                        if (operation.input != null) {
                            //There is some input, resolve
                            if (patternOperation.path.contains("{") && patternOperation.path.contains("}")) {
                                //There is some input required in the path
                                log.debug("operation requires a path input: ${operation.input}")
                                //Find dependent operation
                                val inputName = operation.input
                                val dependentOperation = findDependentOperation(inputName)
                                if (dependentOperation == null) {
                                    log.debug("Found no output for input $inputName, skip here")
                                    resolved = false
                                } else {
                                    //Inspect dependent operation and try to link to current one

                                    //same path prefix?
                                    if (resolveSamePrefixLinks(dependentOperation, patternOperation)) {
                                        resolved = true
                                    }
                                    else {
                                        //not same domain, search for links
                                        log.debug("not in the same domain, search for links...")
                                        if (resolveManualLink(involvedServices, dependentOperation, patternOperation)) {
                                            resolved = true
                                        }
                                    }
                                }
                            } else {
                                //There is some input in the request body
                                log.debug("operation requires a request body input: ${operation.input}")
                                val inputName = operation.input
                                val dependentOperation = findDependentOperation(inputName)
                                if (dependentOperation == null) {
                                    log.debug("Found no output for input $inputName, skip here")
                                    resolved = false
                                } else {
                                    if (resolveManualLink(involvedServices, dependentOperation, patternOperation)) {
                                        log.debug("RESOLVED!")
                                        resolved = true
                                    }

                                }

                            }

                        }

                        //If all dependencies could be resolved, add to expanded list
                        if (resolved) {
                            //Deep copy this object
                            val newMapping = GMapping(openAPiSpecs, serviceLinks)
                            @Suppress("UNCHECKED_CAST")
                            newMapping.patternOperations = this.patternOperations.clone() as ArrayList<PatternOperation>
                            //Add new found operation to deep copy
                            newMapping.patternOperations.add(patternOperation)

                            expandedMappings.add(newMapping)
                        }
                    }
                }
            }
        }

        return expandedMappings
    }

    private fun lookForKey(key: String, json: JsonObject): Boolean {
        for (prop in json.entrySet()) {
            if (prop.key == key) {
                return true
            }
            if (prop.value.isJsonObject) {
                val keyInValue = lookForKey(key, prop.value.asJsonObject)
                if (keyInValue) {
                    return true
                }
            }
        }
        return false
    }

    private fun comparePathLevels(path1 : String, path2: String) : Int {
        var level = 0

        //trim front
        var tmp1 = trimPath(path1)
        var tmp2 = trimPath(path2)

        //compare parts
        while (true) {
            log.debug("IN LOOP for $tmp1 and $tmp2")
            val i1 = tmp1.indexOf('/',1)
            val i2 = tmp2.indexOf('/',1)
            var part1: String
            var part2: String
            part1 = if (i1 >= 0) {
                tmp1.substring(0, i1)
            } else {
                tmp1
            }
            part2 = if (i2 >= 0) {
                tmp2.substring(0, i2)
            } else {
                tmp2
            }
            if (i1 == -1 && i2 == -1) {
                return level
            }
            if (part1 == part2) {
                level++
                tmp1 = tmp1.substring(i1 + 1)
                tmp2 = tmp2.substring(i2 + 1)
            } else {
                return level
            }
        }
    }

    private fun trimPath(path : String) : String {
        //trim front
        var tmp = path
        if (tmp.startsWith("http://")) {
            tmp = tmp.substring(7)
        }

        //Trim Server URL
        val i = tmp.indexOf('/')
        tmp = tmp.substring(i)
        return tmp
    }

    private fun findDependentOperation(inputName: String) : PatternOperation? {
        var dependentOperation: PatternOperation? = null
        for (i in patternOperations.size - 1 downTo 0) {
            if (dependentOperation == null && inputName == patternOperations[i].abstractOperation.output) {
                dependentOperation = patternOperations[i]
                log.debug("Found dependent operation: ${dependentOperation.abstractOperation.operation} to ${dependentOperation.path}")
                return dependentOperation
            }
        }
        return null
    }

    private fun resolveSamePrefixLinks(dependentOperation: PatternOperation, patternOperation:PatternOperation) : Boolean {
        if (comparePathLevels(dependentOperation.path, patternOperation.path) > 0) {
            //same domain, go on
            log.debug("same path prefix")
            var requiredParameterName = ""
            for (parameter in patternOperation.parameters) {
                if (parameter.has("in")) {
                    if (parameter.get("in").asString == "path") {
                        requiredParameterName = parameter.get("name").asString
                    }
                }
            }
            //Look for parameter name in output of dependent request
            val output: JsonObject = dependentOperation.produces!!
            val keyThere = lookForKey(requiredParameterName, output)
            if (keyThere) {
                log.debug("key found, dependency resolved")
                return true
            }
        }
        return false
    }

    private fun resolveManualLink(involvedServices : ArrayList<String>, dependentOperation: PatternOperation, patternOperation: PatternOperation) : Boolean {
        var dependentPath: String? = null
        for (spec2 in openAPiSpecs) {
            if (involvedServices.contains(spec2.info.title)) {
                for (path2 in spec2.paths.paths.keys) {
                    if (trimPath(dependentOperation.path) == path2) {
                        dependentPath = path2
                    }

                }
            }
        }
        //try to resolve dependency and iterate over links
        if (dependentPath != null) {
            for (link in serviceLinks) {
                if (dependentPath.startsWith(link.prefix1) && trimPath(patternOperation.path).startsWith(link.prefix2)) {
                    //found link, try to match patameters

                    var linkFound = false
                    if (lookForKey(link.parameterName1, dependentOperation.produces!!)) {
                        for (p in patternOperation.parameters) {
                            if (p.has("name") && p.get("name").asString == link.parameterName2) {
                                //Found link in parameter
                                log.debug("Found link in parameter")
                                linkFound = true
                            }
                        }
                        if (lookForKey(link.parameterName2, patternOperation.requiredBody)) {
                            //Found link in required body
                            log.debug("Found link in required body")
                            linkFound = true
                        }
                    }
                    if (linkFound) {
                        log.debug("Found link between " + link.prefix1 + " and " + patternOperation.path)
                        return true
                    }
                }
            }
        }
        return false
    }

}