package de.tuberlin.mcc.openapispecification

data class ComponentsObject (val schemas : Map<String, SchemaObject>,
                             val responses : Map<String, ResponseObject>,
                             val parameters: Map<String, ParameterObject>,
                             val examples:Map<String, ExampleObject>,
                             val requestBodies:Map<String, RequestBodyObject>,
                             val headers: Map<String, HeaderObject>,
                             val securitySchemes: Map<String, SecuritySchemeObject>,
                             val links: Map<String, LinkObject>,
                             val callbacks: Map<String, CallBackObject>){
    }