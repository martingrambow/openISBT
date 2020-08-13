package linking

import workload.AbstractOperation
import workload.ApiRequest

interface Linker {
    fun linkParameter(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation):ApiRequest?
    fun linkBody(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation):ApiRequest?

}