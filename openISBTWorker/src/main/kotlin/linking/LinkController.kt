package linking

import linking.linkerunits.IdLinker
import linking.linkerunits.ParameterNameLinker
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

open class LinkController {

    var linkerList:ArrayList<Linker> = ArrayList()
    val log = LoggerFactory.getLogger("LinkController")

    constructor() {
        registerLinker(ParameterNameLinker())
        registerLinker(IdLinker())
    }

    fun registerLinker(linker:Linker) {
        linkerList.add(linker)
    }

    fun linkRequests(dependingRequest: ApiRequest, currentReqest: ApiRequest, abstractOperation: AbstractOperation):ApiRequest? {
        for (linker in linkerList) {
            var request = linker.link(dependingRequest, currentReqest, abstractOperation)
            if (request != null) {
                log.debug(linker.javaClass.name + " matches.")
                return request
            }
        }
        return null
    }
}