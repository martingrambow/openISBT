package de.tuberlin.mcc.openapispecification

data class SecuritySchemeObject (val `$ref`: String,
                                 val type : String,
                                 val description : String,
                                 val name : String?,
                                 val enum : Array<String>,
                                 val `in` : String,
                                 val scheme : String,
                                 val bearerFormat : String,
                                 val openIdConnectUrl : String){
    }