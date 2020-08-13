package linking

import linking.linkerunits.BindingLinker
import linking.linkerunits.IdLinker
import linking.linkerunits.ParameterNameLinker
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

open class LinkController() {

    var linkerList:ArrayList<Linker> = ArrayList()
    val log = LoggerFactory.getLogger("LinkController")

    init {
        registerLinker(BindingLinker())
        registerLinker(ParameterNameLinker())
        registerLinker(IdLinker())
    }

    private fun registerLinker(linker:Linker) {
        linkerList.add(linker)
    }

    fun linkRequestParameter(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest : String, abstractOperation: AbstractOperation):ApiRequest? {
        for (linker in linkerList) {
            val request = linker.linkParameter(dependingRequest, currentReqest, inputNameInCurrentRequest, abstractOperation)
            if (request != null) {
                log.debug("        ${linker.javaClass.name} matches.")
                return request
            }
        }
        return null
    }

    fun linkRequestBody(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest : String, abstractOperation: AbstractOperation):ApiRequest? {
        for (linker in linkerList) {
            val request = linker.linkBody(dependingRequest, currentReqest, inputNameInCurrentRequest, abstractOperation)
            if (request != null) {
                log.debug("        ${linker.javaClass.name} matches.")
                return request
            }
        }
        return null
    }
}