package mapping.globalmapping

import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import mapping.simplemapping.PatternOperation
import matching.MatchController
import matching.link.ServiceLinkObject
import matching.units.*
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPatternOperation

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

        log.debug("Start expansion for " + operation.operation + " ()")

        val involvedServices = ArrayList<String>()
        for (spec in openAPiSpecs) {
            involvedServices.add(spec.info.title)
        }
        log.debug("  Start Expansion with ${involvedServices.size} involved service descriptions")

        for (spec in openAPiSpecs) {
                for (path in spec.paths.paths.keys) {
                    log.debug("    -----")
                    log.debug("    Match ${operation.operation} to $path ...")
                    val patternOperation = matchController.matchPatternOperation(spec.paths.paths.getValue(path), operation, spec, path)

                    if (patternOperation != null) {
                        //matching was successful, create new GMapping with this extended pattern operation
                        log.debug("      Successfully matched ${operation.operation} to $path, check dependencies ...")

                        //Check dependencies for the matched operation
                        var resolved = true

                        //find path inputs
                        val pathInputs = findPathInputs(patternOperation.path)
                        log.debug("      Operation requires ${pathInputs.size} path inputs:")
                        for (input in pathInputs) {
                            log.debug("        - $input")
                        }

                        var bodyInputs = ArrayList<String>()
                        val bodyOperationNames = ArrayList<String>()
                        bodyOperationNames.add(AbstractPatternOperation.CREATE.name)
                        bodyOperationNames.add(AbstractPatternOperation.UPDATE.name)
                        bodyOperationNames.add(AbstractPatternOperation.PATCH.name)
                        if (bodyOperationNames.contains(operation.operation)) {
                            //a create might require some inputs in required body
                            bodyInputs = findIdKeysInBody(patternOperation.requiredBody)
                        }
                        log.debug("      Operation requires ${bodyInputs.size} body inputs:")
                        for (input in bodyInputs) {
                            log.debug("        - $input")
                        }

                        //There are some inputs for the found operation
                        if (pathInputs.size > 0 || bodyInputs.size > 0) {
                            resolved = false
                        }
                        //There are some inputs defined in the abstract operation
                        if (operation.input != null) {
                            resolved = false
                        }

                        var abort = false

                        //There are inputs defined but this operation does not require any
                        if (operation.input != null && pathInputs.size + bodyInputs.size == 0) {
                            abort = true
                        }

                        var numberOfRequiredAbstractInputs = 0
                        if (operation.input != null) {
                            numberOfRequiredAbstractInputs = operation.input.split(",").size
                        }
                        //Are there more required inputs than found ones?
                        if (numberOfRequiredAbstractInputs > (pathInputs.size + bodyInputs.size)) {
                            abort = true
                        }


                        //Start resolving inputs
                        if (!resolved && !abort && operation.input != null) {
                            //find dependent operations (abstract input name -> dependent operation)
                            val dependentOperations = HashMap<String, PatternOperation>()
                            for (abstractInput in operation.input.split(",")) {
                                val op = findDependentOperation(abstractInput)
                                if (op != null) {
                                    dependentOperations[abstractInput] = op
                                } else {
                                    log.debug("      No dependent operation for $abstractInput")
                                    abort = true
                                }
                            }

                            //If all dependent operation have been found, resolve each input
                            val resolvedAbstractInputNames = ArrayList<String>()
                            if (!abort) {
                                //Resolve path inputs
                                for (pathInput in pathInputs) {
                                    var pathInputResolved = false
                                    for (dependency in dependentOperations.entries) {
                                        if (isInputInDependentOperation(pathInput, involvedServices, dependency.value, patternOperation)) {
                                            //this path input is resolved
                                            log.debug("      Resolved path input $pathInput")
                                            pathInputResolved = true
                                            resolvedAbstractInputNames.add(dependency.key)
                                        }
                                    }
                                    if (!pathInputResolved) {
                                        //at least one path input was not resolved
                                        log.debug("      Unable to resolve all path inputs")
                                        abort = true
                                    }
                                }
                            }

                            if (!abort) {
                                //Resolve Body inputs
                                for (bodyInput in bodyInputs) {
                                    var bodyinputResolved = false
                                    for (dependency in dependentOperations.entries) {
                                        if (isInputInDependentOperation(bodyInput, involvedServices, dependency.value, patternOperation)) {
                                            //this path input is resolved
                                            log.debug("      Resolved body input $bodyInput")
                                            bodyinputResolved = true
                                            resolvedAbstractInputNames.add(dependency.key)
                                        }
                                    }
                                    if (!bodyinputResolved) {
                                        //at least one path input was not resolved
                                        log.debug("      Unable to resolve all body inputs")
                                        abort = true
                                    }
                                }
                            }

                            //All inputs resolved?
                            if (!abort) {
                                resolved = true
                                log.debug("      All inputs were resolved")
                            }
                            //All abstract input values resolved?
                            val numberOfResolvedAbstractNames = resolvedAbstractInputNames.distinct().size
                            if (numberOfResolvedAbstractNames != numberOfRequiredAbstractInputs) {
                                log.debug("      Not all abstract inputs were resolved")
                                for (r in resolvedAbstractInputNames.distinct()) {
                                    log.debug("        $r was resolved")
                                }
                                resolved = false
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
                            log.debug("    MATCH!")
                        } else {
                            log.debug("    no Match (dependency failure)")
                        }
                    } else {
                        log.debug("    no Match (abstract operation)")
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

    private fun findPathInputs(path : String) : ArrayList<String> {
        val inputs = ArrayList<String>()
        var pos = 0
        while (pos >= 0) {
            pos = path.indexOf("{", pos+1)
            if (pos >= 0) {
                //there is some path input
                val endpos = path.indexOf("}", pos)
                val inputname = path.substring(pos+1, endpos)
                inputs.add(inputname)
            }
        }
        return inputs
    }

    private fun findIdKeysInBody(json : JsonObject) : ArrayList<String> {
        val keys = ArrayList<String>()
        for (prop in json.entrySet()) {
            if (prop.key.endsWith("id", true)) {
                var noEnum = true
                //if it's an enum, then there are values defined
                if (prop.value.isJsonObject && prop.value.asJsonObject.has("enum")) {
                    noEnum = false
                }
                if (noEnum) {
                    keys.add(prop.key)
                }
            }
            if (prop.value.isJsonObject) {
                keys.addAll(findIdKeysInBody(prop.value.asJsonObject))
            }
        }
        return keys
    }

    private fun isInputInDependentOperation(inputName : String, involvedServices : ArrayList<String>, dependentOperation: PatternOperation, patternOperation: PatternOperation) : Boolean {
        if (comparePathLevels(dependentOperation.path, patternOperation.path) > 0) {
            //same domain, go on
            log.debug("        same path prefix")
            //Look for parameter name in output of dependent request
            val output: JsonObject = dependentOperation.produces!!
            val keyThere = lookForKey(inputName, output)
            if (keyThere) {
                log.debug("        key found, dependency resolved")
                return true
            }
        }
        return resolveManualLink(involvedServices, inputName, dependentOperation, patternOperation)
    }

    private fun comparePathLevels(path1 : String, path2: String) : Int {
        var level = 0

        //trim front
        var tmp1 = trimPath(path1)
        var tmp2 = trimPath(path2)

        //compare parts
        while (true) {
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
                log.debug("      Found dependent operation: ${dependentOperation.abstractOperation.operation} to ${dependentOperation.path}")
                return dependentOperation
            }
        }
        return null
    }


    private fun resolveManualLink(involvedServices : ArrayList<String>, inputName:String, dependentOperation: PatternOperation, patternOperation: PatternOperation) : Boolean {
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
                    //found link, try to match parameters

                    var linkFound = false
                    if (inputName == link.parameterName2 && lookForKey(link.parameterName1, dependentOperation.produces!!)) {
                            log.debug("        Found link in produced object")
                            linkFound = true
                    }
                    for (p in dependentOperation.parameters) {
                        if (inputName == link.parameterName2 && lookForKey(link.parameterName1, p)) {
                            log.debug("        Found link in parameter of dependent request")
                            linkFound = true
                        }
                    }
                    if (linkFound) {
                        log.debug("        Found link between " + link.prefix1 + " and " + patternOperation.path)
                        return true
                    }
                }
            }
        }
        return false
    }

}