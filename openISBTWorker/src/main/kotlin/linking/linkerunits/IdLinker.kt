package linking.linkerunits

import com.google.gson.GsonBuilder
import linking.Linker
import linking.LinkerUtil
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

class IdLinker : Linker {

    val log = LoggerFactory.getLogger("IDLinker")

    override fun link(dependingRequest: ApiRequest, currentReqest: ApiRequest, abstractOperation: AbstractOperation): ApiRequest? {
        //Fill current parameters with values from depending request
        var changed : Boolean = false;
        for (j in 0..currentReqest.parameter.size - 1) {
            var p = currentReqest.parameter[j]
            if (p.first.toLowerCase().contains("id")) {
                log.debug("Try to fill " + p.first + "(" + p.second + ") with infos from " + GsonBuilder().create().toJson(dependingRequest) + " ...")
                var filledValue = ""

                if (dependingRequest.response != null) {
                    var responseText = dependingRequest.response
                    if (responseText.toLowerCase().contains("id")) {
                        log.debug("Found some id in previous response text, create JSON elemnt ... ")
                        var responseJson = GsonBuilder().create().toJsonTree(responseText)
                        var value = LinkerUtil().getJSonValueForKey("id", responseJson, abstractOperation.selector)
                        if (value == null) {
                            value = LinkerUtil().getJSonValueForKey("ID", responseJson, abstractOperation.selector)
                        }
                        if (value == null) {
                            value = LinkerUtil().getJSonValueForKey("Id", responseJson, abstractOperation.selector)
                        }
                        if (value != null) {
                            filledValue = value.asString
                            log.debug(p.first + " filled with " + filledValue)
                        }
                    }
                }

                if (filledValue == "") {
                    //Nothing found in previous response, now looking into previous request body
                    if (dependingRequest.body != null) {
                        var bodyJsonElement = dependingRequest.body
                        var value = LinkerUtil().getJSonValueForKey("id", bodyJsonElement, abstractOperation.selector)
                        if (value == null) {
                            value = LinkerUtil().getJSonValueForKey("ID", bodyJsonElement, abstractOperation.selector)
                        }
                        if (value == null) {
                            value = LinkerUtil().getJSonValueForKey("Id", bodyJsonElement, abstractOperation.selector)
                        }
                        if (value != null) {
                            filledValue = value.asString
                            log.debug(p.first + " filled with " + filledValue)
                        }
                        if (value != null) {
                            log.debug("Found " + p.first + " in previous request body")
                            filledValue = value.asString
                            log.debug(p.first + " filled with " + filledValue)
                        }
                    }
                }

                if (filledValue != "") {
                    log.debug("ID link detected!")
                    currentReqest.parameter[j] = Pair(p.first, filledValue)
                    changed = true
                }
            }
        }
        if (changed) {
            return currentReqest
        } else {
            return null
        }
    }

}