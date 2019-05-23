package mapping

import de.tuberlin.mcc.patternconfiguration.AbstractPattern

class PatternMapping(abstractPattern: AbstractPattern) {

    var aPattern = abstractPattern //AbstractPattern like its given in the pattern config: name, sequence, weight, ...
    var supported = false; // per default, it's not supported
    var requests = 0 // number of requests which should be executed

    // Sequence of concrete operations
    // e.g., If CREATE is supported by multiple endpoints (/user and /user/creteWithArray), both Pattern operations are composed to a list
    var operationSequence:ArrayList<List<PatternOperation>> = ArrayList()

}