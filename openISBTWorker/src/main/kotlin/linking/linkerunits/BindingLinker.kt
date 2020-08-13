package linking.linkerunits

import linking.Linker
import linking.LinkerUtil
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

class BindingLinker : Linker {

    val log = LoggerFactory.getLogger("BindingLinker")

    override fun linkParameter(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation): ApiRequest? {
        //Fill current parameters with values from depending request

        //Replace parameter values in current request
        for (link in currentReqest.links) {
            if (trimPath(dependingRequest.path).startsWith(link.prefix1) && trimPath(currentReqest.path).startsWith(link.prefix2)) {
                for (j in 0 until currentReqest.parameter.size) {
                    if (currentReqest.parameter[j].first == inputNameInCurrentRequest) {
                        if (link.parameterName2 == currentReqest.parameter[j].first) {
                            //Replace the current parameter value with value from parameterName1
                            log.debug("         Found link, try to replace value for ${link.parameterName2} with value from ${link.parameterName1}")
                            val value = LinkerUtil().getValueForKeyInRequest(link.parameterName1, dependingRequest, abstractOperation.selector)
                            if (value != null) {
                                log.debug("         Found value of ${link.parameterName1}: $value")
                                currentReqest.parameter[j] = Pair(currentReqest.parameter[j].first, value)
                                return currentReqest
                            }
                        }
                    }
                }
            }
        }

        return null
    }

    override fun linkBody(dependingRequest: ApiRequest, currentReqest: ApiRequest, inputNameInCurrentRequest: String, abstractOperation: AbstractOperation): ApiRequest? {

        //Replace body values in current request
        for (link in currentReqest.links) {
            if (trimPath(dependingRequest.path).startsWith(link.prefix1) && trimPath(currentReqest.path).startsWith(link.prefix2)) {
                if (link.parameterName2 == inputNameInCurrentRequest) {
                    val newValue = LinkerUtil().getValueForKeyInRequest(link.parameterName1, dependingRequest, abstractOperation.selector)
                    if (newValue != null) {
                        currentReqest.body = LinkerUtil().replaceValueInJson(inputNameInCurrentRequest, newValue, currentReqest.body)
                        log.debug("         Found value of ${link.parameterName1}: $newValue")
                        return currentReqest
                    }
                }
            }
        }
        return null
    }

    private fun trimPath(path : String) : String {
        //trim front
        var tmp = path
        if (tmp.startsWith("http://")) {
            tmp = tmp.substring(7)
        }

        //Trim Server URL
        val i = tmp.indexOf('/')
        tmp = tmp.substring(i)
        return tmp
    }
}