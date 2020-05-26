package mapping.simplemapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPattern
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import matching.MatchController
import matching.units.*
import org.slf4j.LoggerFactory

class ResourceMapping//Add the pattern mapping for the current pattern to list of mappings//Init MatchController
(spec: OpenAPISPecifcation, config: PatternConfiguration, path: String) {

    //top level path, e.g. /user
    var resourcePath:String = path
    var patternMappingList:ArrayList<PatternMapping> = ArrayList()
    //true, if all patternMappingList in a given patternMappingList config are supported by this resource path
    var supported:Boolean = false
    //number of requests which are sent to this resource endpoint
    var numberOfRequests:Int = 0
    //true, if the user wants to benchmark this resource endpoint; false otherwise (true per default if the path is supported, can be changed in GUI)
    var enabled:Boolean = false

    //MatchConroller handles matching from abstract pattern to concrete operations
    @Transient
    var matchController:MatchController = MatchController()

    //Just to log what's happening
    @Transient
    val log = LoggerFactory.getLogger("ResourceMapping")!!

    init {
        matchController.registerMatchingUnit(CreateMatchingUnit())
        matchController.registerMatchingUnit(DeleteMatchingUnit())
        matchController.registerMatchingUnit(UpdateMatchingUnit())
        matchController.registerMatchingUnit(PatchMatchingUnit())
        matchController.registerMatchingUnit(ScanMatchingUnit())
        matchController.registerMatchingUnit(ReadMatchingUnit())
        for (p in config.patterns) {
            //Add the pattern mapping for the current pattern to list of mappings
            this.patternMappingList.add(getPatternMappingInclOperations(p, spec, this.resourcePath))
        }
        this.checkAndSetSupport()
    }

    fun checkAndSetSupport() {

        for (mapping in patternMappingList) {
            setMappingSupportBasedOnOperationSequences(mapping)
        }

        //This resource mapping is only supported if all pattern mappings are supported
        this.supported = true
        for (mapping in patternMappingList) {
            if (!mapping.supported) {
                this.supported = false
            }
        }

        //Per default, it's enabled if it's supported
        if (this.supported) {
            this.enabled = true
        }
    }

    private fun getPatternMappingInclOperations(aPattern: AbstractPattern, spec: OpenAPISPecifcation, topLevelResourcePath: String): PatternMapping {

        val mapping = PatternMapping(aPattern)

        //Each abstract operation in the given abstract Pattern must be matched against all pathItemObjects with the given topLevelPath
        // and a PatternOperation must be created if this matching is successful
        // !!! multiple pathItemObjects can match one abstract operation!!!
        for (aOperation in aPattern.sequence) {
            val patternOperations:ArrayList<PatternOperation> = ArrayList()

            //find resource paths which belong to the given top level resource path
            for (path in spec.paths.paths.keys) {
                if (path.startsWith(topLevelResourcePath)) {
                    val pathItemObject = spec.paths.paths[path]
                    if (pathItemObject != null) {
                        //This pathItemObject belongs to the given top level resource path and one of its operations
                        // can potentially support the current abstract operation
                        // -> Start matching for this pathItemObject, abstractOperation and spec
                        val patternOperation = matchPathItemObject(pathItemObject, aOperation, spec, path)
                        if (patternOperation != null) {
                            //matching was successful
                            patternOperations.add(patternOperation)
                        }
                    }
                }
            }
            mapping.operationSequence.add(patternOperations)
        }

        setMappingSupportBasedOnOperationSequences(mapping)

        return mapping
    }

    private fun setMappingSupportBasedOnOperationSequences(mapping: PatternMapping) {
        //The given pattern is supported by this topLevelPath if at least one element is in each operation sequence list
        mapping.supported = true
        for (list in mapping.operationSequence) {
            if (list.isEmpty()) {
                //No mapping could be found for one abstract operation
                mapping.supported = false
            }
        }
    }

    private fun matchPathItemObject(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path:String): PatternOperation? {
        //Cancel if there is a subresource
        if (path.contains("{") && path.contains("}/")) {
            return null
        }
        return matchController.matchPatternOperation(pathItemObject, abstractOperation, spec, path)
    }
}