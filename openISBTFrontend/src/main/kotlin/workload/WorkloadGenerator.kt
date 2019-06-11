package workload

import dataobjects.ResourceMapping
//import kotlinx.coroutines.*

class WorkloadGenerator {

    private var patternRequests = HashMap<Int, PatternRequest>()
    var listener:ProgressListener? = null
    private var workload = ArrayList<PatternRequest>()

    suspend fun generateWorkloadAsync(resourceMappings : Array<ResourceMapping>, callback : () -> Unit) :Unit {
        //GlobalScope.launch {
        //   generateWorkload(resourceMappings)
        //    callback()
        //}
    }

    fun generateWorkload(resourceMappings : Array<ResourceMapping>) {
        patternRequests.clear()

        var total:Int = 0
        //determine number of total patternRequests items
        for (topLevelMapping in resourceMappings) {
            if (topLevelMapping.supported && topLevelMapping.enabled) {
                for (patternMapping in topLevelMapping.patternMappingList) {
                    total += patternMapping.requests
                }
            }
        }

        for (topLevelMapping in resourceMappings) {
            if (topLevelMapping.supported && topLevelMapping.enabled) {
                for (patternMapping in topLevelMapping.patternMappingList) {
                    for (i in 1 .. patternMapping.requests) {
                        val id = getNextID((total * 1.2).toInt())
                        var req = PatternRequest(id, patternMapping.aPattern.name)
                        req.generateApiRequests(patternMapping.operationSequence)
                        patternRequests.put(id, req)
                        val current = patternRequests.size
                        if (listener != null) {
                            listener?.setProgress((current * 100) / total)
                        }
                    }
                }
            }
        }

        workload = ArrayList<PatternRequest>()
        for (entry in patternRequests.entries) {
            workload.add(entry.value)
        }
    }

    fun getWorkload() : Array<PatternRequest> {
        return workload.toTypedArray()
    }

    private fun getNextID(max : Int) :Int{
        var found = false
        var id: Int
        do {
            id = (1 .. 100000).shuffled().first()
            if (patternRequests.get(id) != null) {
                found = true
            } else {
                found = false
            }
        } while (found)
        return id
    }
}