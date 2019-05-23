package de.tuberlin.mcc.openapispecification

data class InfoObject (val title:String,
                       val description:String,
                       val termsOfService:String,
                       val contact:ContactObject,
                       val license:LicenseObject,
                       val version:String){
}