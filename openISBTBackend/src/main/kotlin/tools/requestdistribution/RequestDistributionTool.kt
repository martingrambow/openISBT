package tools.requestdistribution

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import mapping.globalmapping.GPatternBinding
import org.slf4j.LoggerFactory
import util.loadMapping
import util.readFile
import java.io.File
import java.lang.reflect.Modifier

val log = LoggerFactory.getLogger("RequestDistributionTool")!!
var mapping: Array<GPatternBinding> = arrayOf()
var fileName:String = ""

/**
 * Sample calls:
 * -o -m mapping.json
 */
fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::DistributionArguments).run {
        val mappingFile = File(mappingFileName)
        fileName = mappingFileName

        mapping = loadMapping(readFile(mappingFile))
                ?: throw InvalidArgumentException("Could not parse specification")

        var end = false

        while (!end) {
            printMapping(mapping)
            printInstructions()
            val cmd = readLine()

            if (cmd != null) {
                with(cmd) {
                    when {
                        startsWith("del") -> deleteMapping(cmd)
                        startsWith("tog") -> toogleSequence(cmd)
                        startsWith("setreq") -> setReq(cmd)
                        startsWith("cancel") -> end = true
                        startsWith("quit") -> {
                            quit(cmd, overwrite)
                            end = true
                        }
                        else -> println("Invalid command")

                    }
                }
            }
        }


        return@mainBody
    }
}

fun deleteMapping(cmd : String) {
    val parts = cmd.split(" ")
    if (parts.size == 2) {
        val patternName = parts[1]
        val newMapping = ArrayList<GPatternBinding>()

        for (m in mapping) {
            if (m.abstractPattern.name != patternName) {
                newMapping.add(m)
            }
        }
        mapping = newMapping.toTypedArray()

    } else {
        println("Invalid delete command")
    }
}

fun toogleSequence(cmd: String) {
    val parts = cmd.split(" ")
    if (parts.size == 3) {
        val patternName = parts[1]
        val seqIndex = parts[2].toIntOrNull()

        if (seqIndex != null) {
            for (i in 0 until mapping.size) {
                if (mapping[i].abstractPattern.name == patternName) {
                    if (mapping[i].gMappingList.size > seqIndex) {
                        mapping[i].gMappingList[seqIndex].enabled = !mapping[i].gMappingList[seqIndex].enabled
                    } else {
                        println("Invalid toggle command (large index)")
                    }
                }
            }
            updateRequestNumbers(patternName)
        } else {
            println("Invalid toggle command (wrong index)")
        }
    } else {
        println("Invalid toggle command")
    }
}

fun setReq(cmd: String) {
    val parts = cmd.split(" ")
    if (parts.size == 3) {
        //set requests of pattern
        val patternName = parts[1]
        val newNumber = parts[2].toIntOrNull()

        if (newNumber != null) {
            for (i in 0 until mapping.size) {
                if (mapping[i].abstractPattern.name == patternName) {
                    mapping[i].requests = newNumber
                }
            }
            updateRequestNumbers(patternName)
        } else {
            println("Invalid setreq command (wrong number)")
        }
    } else {
        if (parts.size == 4) {
            //set requests of pattern and sequence (no update)
            val patternName = parts[1]
            val seqIndex = parts[2].toIntOrNull()
            val newNumber = parts[3].toIntOrNull()

            if (seqIndex != null) {
                if (newNumber != null) {
                    for (i in 0 until mapping.size) {
                        if (mapping[i].abstractPattern.name == patternName) {
                            if (mapping[i].gMappingList.size > seqIndex) {
                                mapping[i].gMappingList[seqIndex].numberOfRequests = newNumber
                            } else {
                                println("Invalid toggle command (large index)")
                            }
                        }
                    }
                    updatePatternRequestNumber(patternName)
                } else {
                    println("Invalid toggle command (wrong number)")
                }
            } else {
                println("Invalid toggle command (wrong index)")
            }
        }
        println("Invalid setreq command")
    }
}

fun quit(cmd: String, overwrite:Boolean) {
    val parts = cmd.split(" ")
    var writeToFileName = ""
    if (parts.size == 1) {
        if (overwrite) {
            writeToFileName = fileName
        }
    } else {
        if (parts.size == 2) {
            writeToFileName = parts[1]
        } else {
            println("Wrong quit command")
            return
        }
    }
    if (writeToFileName != "") {
        saveMapping(File(writeToFileName))
        println("Saved to $writeToFileName")
    }
}

fun updateRequestNumbers(patternName: String) {
    for (i in 0 until mapping.size) {
        if (mapping[i].abstractPattern.name == patternName) {
            val totalNumber = mapping[i].requests
            //count enabled ones
            var enabled = 0
            for (gm in mapping[i].gMappingList) {
                if (gm.enabled) {
                    enabled++
                }
            }
            for (j in 0 until mapping[i].gMappingList.size) {
                if (mapping[i].gMappingList[j].enabled) {
                    mapping[i].gMappingList[j].numberOfRequests = totalNumber / enabled
                } else {
                    mapping[i].gMappingList[j].numberOfRequests = 0
                }
            }
        }
    }
}

fun updatePatternRequestNumber(patternName: String) {
    for (i in 0 until mapping.size) {
        if (mapping[i].abstractPattern.name == patternName) {
            var totalNumber = 0
            for (gm in mapping[i].gMappingList) {
                if (gm.enabled) {
                    totalNumber += gm.numberOfRequests
                }
            }
            mapping[i].requests = totalNumber
        }
    }
}

fun saveMapping(file: File) {
    val gson: Gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
    file.writeText(gson.toJson(mapping))
}

fun printMapping(mapping: Array<GPatternBinding>) {
    for (m in mapping) {
        println(m.abstractPattern.name + "(supported=" + m.supported + ", requests=" + m.requests + "): ")
        var i = 0
        for (n in m.gMappingList) {
            println("  [" + i + "]: enabled=" + n.enabled + " , requests=" + n.numberOfRequests)
            i++

            var opNo = 1
            for (o in n.patternOperations) {
                println("    " + opNo + ": " + o.abstractPatternOperation.name + " to "+ o.path)
                opNo++
            }
        }
    }
}

fun printInstructions() {
    println("Commands:")
    println("  del <patternname> - deletes pattern with <patternname> from mapping")
    println("  tog <patternname> <sequenceNo> - toggles enabled for pattern with <patternname> and interaction sequence <sequenceNo>")
    println("                                   (and updates request numbers of sibling sequences based on patten request number)")
    println("  setreq <patternname> <number> - sets number of requests for pattern with <patternname>")
    println("                                  (and updates request numbers of interaction sequences for this pattern)")
    println("  setreq <patternname> <sequenceNo> <number> - sets number of requests for pattern with <patternname> and interaction sequence <sequenceNo>")
    println("                                               (no update)")
    println("  cancel - ends tool without saving")
    println("  quit <fileName> - saves adjusted mapping named <fileName>, (or overwrites same file with -o argument)")
}