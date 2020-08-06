package workload

import kotlinx.coroutines.runBlocking
import mapping.globalmapping.GPatternBinding
import org.slf4j.LoggerFactory

class WorkloadGenerator {

    private var patternRequests = HashMap<Int, PatternRequest>()
    var listener:ProgressListener? = null
    private var workload = ArrayList<PatternRequest>()
    private val log = LoggerFactory.getLogger("WorkloadGenerator")

    fun generateWorkload(bindings : Array<GPatternBinding>) {
        patternRequests.clear()

        var total = 0
        //determine number of total patternRequests items
        for (binding in bindings) {
            if (binding.supported) {
                for (mapping in binding.gMappingList) {
                    if (mapping.enabled) {
                        total += mapping.numberOfRequests
                    }
                }
            }
        }
        log.info("Generate workload ($total pattern requests)...")

        for (binding in bindings) {
            if (binding.supported ) {
                for (mapping in binding.gMappingList) {
                    if (mapping.enabled) {
                        for (i in 1..mapping.numberOfRequests) {
                            runBlocking {
                                val id = getNextID((total * 1.2).toInt())
                                val req = PatternRequest(id, binding.abstractPattern)
                                req.generateApiRequests(mapping.patternOperations)
                                patternRequests[id] = req
                                val current = patternRequests.size
                                if (listener != null) {
                                    listener?.setProgress((current * 100) / total)
                                }
                            }
                        }
                    }
                }
            }
        }

        workload = ArrayList()
        for (entry in patternRequests.entries) {
            workload.add(entry.value)
        }
        log.info("Workload generated (" + (workload.size) + " pattern requests).")
    }

    fun getWorkload() : Array<PatternRequest> {
        return workload.toTypedArray()
    }

    private fun getNextID(max : Int) :Int{
        var found: Boolean
        var id: Int
        do {
            id = (1 .. max).shuffled().first()
            found = patternRequests[id] != null
        } while (found)
        return id
    }
}