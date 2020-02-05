package util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathsObject
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
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

        val gsonBuilder: GsonBuilder = GsonBuilder()
        val pathsObjectDeserializer: JsonDeserializer<PathsObject> = PathsObjectDeserializer()
        gsonBuilder.registerTypeAdapter(PathsObject::class.java, pathsObjectDeserializer)
        val responsesObjectDeserializer: JsonDeserializer<ResponsesObject> = ResponsesObjectDeserializer()
        gsonBuilder.registerTypeAdapter(ResponsesObject::class.java, responsesObjectDeserializer)

        val customGson: Gson = gsonBuilder.create();

        var openAPISpec = customGson.fromJson(oasFile, OpenAPISPecifcation::class.java)
        return openAPISpec
    }
    return null
}

fun loadPatternConfig(patternConfigFile: String): PatternConfiguration? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var patternConfig = customGson.fromJson(patternConfigFile, PatternConfiguration::class.java)
    return patternConfig
}

fun loadWorkload(workloadAsText : String) : Array<PatternRequest>? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var patternConfig = customGson.fromJson(workloadAsText, Array<PatternRequest>::class.java)
    return patternConfig
}


fun loadWorker(workerAsText : String) : Array<Worker>? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var worker = customGson.fromJson(workerAsText, Array<Worker>::class.java)
    return worker
}

class Util {
}