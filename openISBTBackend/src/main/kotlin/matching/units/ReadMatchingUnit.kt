package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import patternconfiguration.AbstractOperation
import mapping.simplemapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class ReadMatchingUnit : MatchingUnit{

    val log = LoggerFactory.getLogger("ReadMatchingUnit");

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.READ.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.get != null) {

            //get could support READ or SCAN
            //If no array is returned, READ is supported
            var listReturned = false;
            for (response in pathItemObject.get.responses.responses.values) {
                if (response.content != null) {
                    for (content in response.content.values) {
                        if (content.schema != null) {
                            if (content.schema.type == "array") {
                                listReturned = true;
                            }
                        }
                    }
                }
            }
            if (!listReturned && path.contains("{") && path.contains("}")) {
                val operation = PatternOperation(abstractOperation, AbstractPatternOperation.READ)
                operation.path = spec.servers[0].url + path

                //Determine input and output values
                val getObject = pathItemObject.get
                log.debug("    " + GsonBuilder().create().toJson(getObject))
                if (getObject.requestBody != null) {
                    //Operation requires some request body
                    val body = MatchingUtil().parseRequestBody(getObject.requestBody, spec)
                    if (body != null) {
                        operation.requiredBody = body
                    }
                }
                if (getObject.parameters != null) {
                    operation.parameters = MatchingUtil().parseParameter(getObject.parameters, spec)
                    log.debug("Found " + operation.parameters.size + " parameters")
                    for (headerparam in MatchingUtil().parseHeaderParameter(getObject.parameters, spec)) {
                        operation.headers.add(Pair(headerparam.get("name").asString, headerparam.getAsJsonObject("schema")))
                    }
                }

                if (getObject.security != null) {
                    val header = MatchingUtil().parseApiKey(getObject.security, spec)
                    if (header != null) {
                        operation.headers.add(header)
                    }
                }
                return operation
            }
        }
        return null
    }

}