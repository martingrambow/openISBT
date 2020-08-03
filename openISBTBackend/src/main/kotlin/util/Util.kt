package util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathsObject
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import mapping.simplemapping.ResourceMapping
import matching.link.ServiceLinkObject
import measurement.PatternMeasurement
import openapispecification.ResponsesObject
import openapispecification.deserializer.PathsObjectDeserializer
import openapispecification.deserializer.ResponsesObjectDeserializer
import run.Worker
import workload.PatternRequest
import java.io.File

fun readFile(file: File) : String {
    return file.readText()
}

fun checkFile(file : File) : Boolean {
    if (!file.exists()) {
        println("File not found: " + file.absoluteFile)
        return false
    }
    if (!file.isFile) {
        println("no file: " + file.absoluteFile)
        return false
    }
    return true
}

fun loadOAS(oasFile:String): OpenAPISPecifcation? {
    if (oasFile.length > 15) {
        val gsonBuilder = GsonBuilder()
        val pathsObjectDeserializer: JsonDeserializer<PathsObject> = PathsObjectDeserializer()
        gsonBuilder.registerTypeAdapter(PathsObject::class.java, pathsObjectDeserializer)
        val responsesObjectDeserializer: JsonDeserializer<ResponsesObject> = ResponsesObjectDeserializer()
        gsonBuilder.registerTypeAdapter(ResponsesObject::class.java, responsesObjectDeserializer)
        val customGson: Gson = gsonBuilder.create()
        return customGson.fromJson(oasFile, OpenAPISPecifcation::class.java)
    }
    return null
}

fun loadMapping(mappingFile: String): Array<ResourceMapping>? {
    val customGson:Gson = GsonBuilder().create()
    return customGson.fromJson(mappingFile, Array<ResourceMapping>::class.java)
}

fun loadPatternConfig(patternConfigFile: String): PatternConfiguration? {
    val customGson = GsonBuilder().create()
    return customGson.fromJson(patternConfigFile, PatternConfiguration::class.java)
}

fun loadServiceLinksFile(serviceLinksFile: String): Array<ServiceLinkObject>? {
    val customGson = GsonBuilder().create()
    return customGson.fromJson(serviceLinksFile, Array<ServiceLinkObject>::class.java)
}

fun loadWorkload(workloadAsText : String) : Array<PatternRequest>? {
    val customGson = GsonBuilder().create()
    return customGson.fromJson(workloadAsText, Array<PatternRequest>::class.java)
}


fun loadWorker(workerAsText : String) : Array<Worker>? {
    val customGson = GsonBuilder().create()
    return customGson.fromJson(workerAsText, Array<Worker>::class.java)
}

fun loadMeasurements(measurmentsAsText : String) : Array<PatternMeasurement>? {
    val customGson = GsonBuilder().create()
    return customGson.fromJson(measurmentsAsText, Array<PatternMeasurement>::class.java)
}
