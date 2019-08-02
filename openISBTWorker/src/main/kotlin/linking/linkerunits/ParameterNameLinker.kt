package linking.linkerunits

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import linking.Linker
import linking.LinkerUtil
import org.slf4j.LoggerFactory
import workload.AbstractOperation
import workload.ApiRequest

class ParameterNameLinker : Linker {

    val log = LoggerFactory.getLogger("ParameterNameLinker")

    override fun link(dependingRequest: ApiRequest, currentReqest: ApiRequest, abstractOperation: AbstractOperation): ApiRequest? {
        //Fill current parameters with values from depending request
        var changed : Boolean = false;
        for (j in 0..currentReqest.parameter.size - 1) {
            var p = currentReqest.parameter[j]
            var dependentText = GsonBuilder().create().toJson(dependingRequest)
            if (dependentText.length > 200) {
                dependentText = dependentText.substring(0,199)
            }
            log.debug("Try to fill " + p.first + "(" + p.second + ") with infos from " + dependentText + " ...")
            var filledValue = ""

            if (dependingRequest.response != null) {
                var responseText = dependingRequest.response
                if (responseText.contains(p.first)) {
                    log.debug("Found " + p.first + " in previous response text, create JSON elemnt ... ")
                    var responseJson = GsonBuilder().create().fromJson(responseText, JsonObject::class.java)
                    var value = LinkerUtil().getJSonValueForKey(p.first, responseJson, abstractOperation.selector)
                    if (value != null) {
                        filledValue = value.asString
                        log.debug("Found " + p.first + " in previous response text and filled with " + filledValue)
                    } else {
                        log.warn("Found " + p.first + " in previous response text but value was null")
                    }
                }
            }

            if (filledValue == "") {
                //Nothing found in previous response, now looking into previous request body
                if (dependingRequest.body != null) {
                    var bodyJsonElement = dependingRequest.body
                    var value = LinkerUtil().getJSonValueForKey(p.first, bodyJsonElement, abstractOperation.selector)
                    if (value != null) {
                        filledValue = value.asString
                        log.debug("Found " + p.first + " in previous request body and filled with " + filledValue)
                    }
                }
            }

            if (filledValue != "") {
                log.debug("Linked Parameter detected!")
                currentReqest.parameter[j] = Pair(p.first, filledValue)
                changed = true

                //Perhaps there are still some values to adjust in the request body
                currentReqest.body = LinkerUtil().replaceValueInJson(p.first, filledValue, currentReqest.body)

            } else {
                log.debug("Unable to fill " + p.first + "(" + p.second + ") with infos from previous request")
            }
        }
        if (changed) {
            return currentReqest
        } else {
            return null
        }
    }
}