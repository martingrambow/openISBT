package mapping

import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.patternconfiguration.AbstractOperation
import de.tuberlin.mcc.patternconfiguration.AbstractPattern
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import matching.MatchController
import matching.units.*
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class ResourceMapping {

    //top level path, e.g. /user
    var resourcePath:String = "";
    var patternMappingList:ArrayList<PatternMapping> = ArrayList();
    //true, if all patternMappingList in a given patternMappingList config are supported by this resource path
    var supported:Boolean = false;
    //number of requests which are sent to this resource endpoint
    var numberOfRequests:Int = 0;
    //true, if the user wants to benchmark this resource endpoint; false otherwise (true per default if the path is supported, can be changed in GUI)
    var enabled:Boolean = false;
    //MatchConroller handles matching from abstract pattern to concrete operations
    var matchController:MatchController = MatchController()
    //Just to log what's happening
    val log = LoggerFactory.getLogger("ResourceMapping");

    constructor(spec:OpenAPISPecifcation, config:PatternConfiguration, path:String) {
        this.resourcePath = path;

        //Init MatchController
        matchController.registerMatchingUnit(CreateMatchingUnit())
        matchController.registerMatchingUnit(DeleteMatchingUnit())
        matchController.registerMatchingUnit(UpdateMatchingUnit())
        matchController.registerMatchingUnit(ScanMatchingUnit())
        matchController.registerMatchingUnit(ReadMatchingUnit())

        for (p in config.patterns) {
            //Add the pattern mapping for the current pattern to list of mappings
            this.patternMappingList.add(getPatternMappingInclOperations(p, spec, this.resourcePath))
        }

        //This resource mapping is only supported if all pattern mappings are supported
        this.supported = true;
        for (mapping in patternMappingList) {
            if (!mapping.supported) {
                this.supported = false;
            }
        }

        //Per default, it's enabled if it's supported
        if (this.supported) {
            this.enabled = true;
        }
    }

    fun getPatternMappingInclOperations(aPattern:AbstractPattern, spec: OpenAPISPecifcation, topLevelResourcePath: String): PatternMapping {

        var mapping = PatternMapping(aPattern);

        //Each abstract operation in the given abstract Pattern must be matched against all pathItemObjects with the given topLevelPath
        // and a PatternOperation must be created if this matching is successful
        // !!! multiple pathItemObjects can match one abstract operation!!!
        for (aOperation in aPattern.sequence) {
            var patternOperations:ArrayList<PatternOperation> = ArrayList()

            //find resouce paths which belong to the given top level resource path
            for (path in spec.paths.paths.keys) {
                if (path.startsWith(topLevelResourcePath)) {
                    var pathItemObject = spec.paths.paths.get(path);
                    if (pathItemObject != null) {
                        //This pathItemObject belongs to the given top level resource path and one of its operations
                        // can potentially support the current abstract operation
                        // -> Start matching for this pathItemObject, abstractOperation and spec
                        var patternOperation = matchPathItemObject(pathItemObject, aOperation, spec, path)
                        if (patternOperation != null) {
                            //matching was successful
                            patternOperations.add(patternOperation)
                        }
                    }
                }
            }
            mapping.operationSequence.add(patternOperations)
        }

        //The given pattern is supported by this topLevelPath if at least one element is in each operation sequence list
        mapping.supported = true
        for (list in mapping.operationSequence) {
            if (list.size == 0) {
                //No mapping could be found for one abstract operation
                mapping.supported = false;
            }
        }

        return mapping
    }

    fun matchPathItemObject(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path:String):PatternOperation? {
        //Cancel if there is a subresource
        if (path.contains("{") && path.contains("}/")) {
            return null
        }
        return matchController.matchPatternOperation(pathItemObject, abstractOperation, spec, path)
    }
}