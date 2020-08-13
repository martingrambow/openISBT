package linking.linkerunits

import linking.Linker
import linking.LinkerUtil
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

class IdLinker : Linker {

    val log = LoggerFactory.getLogger("IDLinker")

    override fun linkParameter(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation): ApiRequest? {
        //Fill current parameters with values from depending request

        //find id keys
        val keys = ArrayList<String>()
        for (pathIn in LinkerUtil().findPathInputs(dependingRequest.path)) {
            if (pathIn.endsWith("id", true)) {
                keys.add(pathIn)
            }
        }
        if (dependingRequest.body.isJsonObject) {
            keys.addAll(LinkerUtil().findIdKeysInJson(dependingRequest.body.asJsonObject))
        }

        for (j in 0 until currentReqest.parameter.size) {
            val p = currentReqest.parameter[j]
            if (p.first == inputNameInCurrentRequest) {

                if (keys.size == 1) {
                    //resolvable id is unique
                    val newValue = LinkerUtil().getValueForKeyInRequest(keys[0], dependingRequest, abstractOperation.selector)
                    if (newValue != null) {
                        currentReqest.parameter[j] = Pair(p.first, newValue)
                        log.debug("Found ${keys[0]} in dependent request, filled ${p.first} with $newValue")
                        return currentReqest
                    }
                }
            }
        }
        return null
    }

    override fun linkBody(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation): ApiRequest? {

        //find id keys
        val keys = ArrayList<String>()
        for (pathIn in LinkerUtil().findPathInputs(dependingRequest.path)) {
            if (pathIn.endsWith("id", true)) {
                keys.add(pathIn)
            }
        }
        if (dependingRequest.body.isJsonObject) {
            keys.addAll(LinkerUtil().findIdKeysInJson(dependingRequest.body.asJsonObject))
        }

        if (keys.size == 1) {
            //resolvable id is unique
            val newValue = LinkerUtil().getValueForKeyInRequest(keys[0], dependingRequest, abstractOperation.selector)
            if (newValue != null) {
                currentReqest.body = LinkerUtil().replaceValueInJson(inputNameInCurrentRequest, newValue, currentReqest.body)
                log.debug("Found $inputNameInCurrentRequest in dependent request, filled with $newValue")
                return currentReqest
            }
        }
        return null
    }
}