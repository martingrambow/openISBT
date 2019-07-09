package linking

import workload.AbstractOperation
import workload.ApiRequest

interface Linker {
    fun link(dependingRequest: ApiRequest, currentReqest: ApiRequest, abstractOperation: AbstractOperation):ApiRequest?

}