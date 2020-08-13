package linking.linkerunits

import linking.Linker
import linking.LinkerUtil
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

class ParameterNameLinker : Linker {

    private val log = LoggerFactory.getLogger("ParameterNameLinker")

    override fun linkParameter(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation): ApiRequest? {
        //Fill current parameters with values from depending request
        for (j in 0 until currentReqest.parameter.size) {
            val p = currentReqest.parameter[j]
            if (p.first == inputNameInCurrentRequest) {
                val newValue = LinkerUtil().getValueForKeyInRequest(p.first, dependingRequest, abstractOperation.selector)
                if (newValue != null) {
                    currentReqest.parameter[j] = Pair(p.first, newValue)
                    log.debug("Found ${p.first} in dependent request, filled with $newValue")
                    return currentReqest
                }
            }
        }
        return null
    }

    override fun linkBody(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation): ApiRequest? {
        val newValue = LinkerUtil().getValueForKeyInRequest(inputNameInCurrentRequest, dependingRequest, abstractOperation.selector)
        if (newValue != null) {
            currentReqest.body = LinkerUtil().replaceValueInJson(inputNameInCurrentRequest, newValue, currentReqest.body)
            log.debug("Found $inputNameInCurrentRequest in dependent request, filled with $newValue")
            return currentReqest
        }
        return null
    }
}