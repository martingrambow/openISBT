package workload

import dataobjects.ResourceMapping
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WorkloadGenerator {

    var workload = HashMap<Int, PatternRequest>()

    fun generateWorkload(resourceMappings : Array<ResourceMapping>) : ArrayList<PatternRequest> {
        workload.clear()

        for (topLevelMapping in resourceMappings) {
            if (topLevelMapping.supported && topLevelMapping.enabled) {
                for (patternMapping in topLevelMapping.patternMappingList) {
                    for (i in 1 .. patternMapping.requests) {
                        val id = getNextID()
                        var req = PatternRequest(id, patternMapping.aPattern.name)
                        req.generateApiRequests(patternMapping.operationSequence)
                        workload.put(id, req)
                    }
                }
            }
        }

        var result = ArrayList<PatternRequest>()
        for (entry in workload.entries) {
            result.add(entry.value)
        }
        return result
    }

    private fun getNextID() :Int{
        var found = false
        var id: Int
        do {
            id = (1 .. 100000).shuffled().first()
            if (workload.get(id) != null) {
                found = true
            } else {
                found = false
            }
        } while (found)
        return id
    }
}