package tools.evaluation

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.mainBody
import measurement.BoxPlotValues
import measurement.MeasurementContainer
import measurement.PatternMeasurement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.loadMeasurements
import util.readFile
import java.util.*
import kotlin.collections.ArrayList

val  log: Logger = LoggerFactory.getLogger("BoxPlot")

var sequenceHelperList = ArrayList<String>()

/**
 * Sample calls:
 * -o -r results.json
 */
fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::BoxPlotArguments).run {

        // load masurements
        val measurements = loadMeasurements(readFile(measurementFile)) ?: throw InvalidArgumentException("Could not parse measurments")
        log.info("Imported Measurments")

        //Trim path in all measurements
        for (m in measurements) {
            for (arm in m.apiRequestMeasurements) {
                arm.path = trimPath(arm.path)
            }
        }

        log.info("Fill container...")
        val container = MeasurementContainer()
        for (m in measurements) {

            //Determine sequence
            var seq = m.patternName
            for (arm in m.apiRequestMeasurements) {
                seq += arm.path
            }
            if (!sequenceHelperList.contains(seq)) {
                sequenceHelperList.add(seq)
            }

            val pattern = m.patternName
            if (!container.isPattern(pattern)) {
                container.addPattern(pattern)
            }

            val sequenceID = "Seq. No. ${sequenceHelperList.indexOf(seq)}"
            if (!container.isSequence(sequenceID, pattern)) {
                container.addSeqence(sequenceID, pattern)
            }

            for (n in m.apiRequestMeasurements) {
                val operation = n.abstractOperation
                if (!container.isOperation(pattern, sequenceID, operation)) {
                    container.addOperation(pattern, sequenceID, operation, n.index)
                }
                val method = n.path
                if (!container.isMethod(pattern, sequenceID, operation, method)) {
                    container.addMethod(pattern, sequenceID, operation, method, BoxPlotValues(getDurations(measurements, pattern, sequenceID, operation, method)))
                }
            }
        }
        log.info("filled.")

        log.info("generate boxplot table...")
        var fileContent = getTableHeader() + "\n"
        var lastSequenceID = ""
        var lastPattern = ""
        var lastOperation = ""

        for (pattern in container.measurements.keys) {
            for (sequenceID in container.measurements.getValue(pattern).keys) {
                for (o in container.measurements.getValue(pattern).getValue(sequenceID)) {
                    if (pattern!= lastPattern || sequenceID != lastSequenceID) {
                        //Insert Total pattern line
                        val total = BoxPlotValues(getDurations(measurements, pattern, sequenceID, "total", ""))
                        val totalrow = getTableRow(pattern, sequenceID, "total", "", total)
                        fileContent += totalrow + "\n"
                        lastPattern = pattern
                        lastSequenceID = sequenceID
                    }
                    val operation = o.first
                    if (o.second.entries.size > 1) {
                        //Multiple methods, insert total line
                        val total = BoxPlotValues(getDurations(measurements, pattern, sequenceID, operation, "total"))
                        val cPattern = if (pattern != lastPattern) pattern else ""
                        val cSequenceID = if (sequenceID != lastSequenceID) sequenceID else ""
                        val totalrow = getTableRow(cPattern, cSequenceID, operation, "total", total)
                        fileContent += totalrow + "\n"
                        lastOperation = operation
                    }
                    for (method in o.second.entries) {
                        val cPattern = if (pattern != lastPattern) pattern else ""
                        val cSequenceID = if (sequenceID != lastSequenceID) sequenceID else ""
                        val cOperation = if (operation != lastOperation) operation else ""
                        val cMethod = method.key
                        fileContent += getTableRow(cPattern, cSequenceID, cOperation, cMethod, method.value) + "\n"
                        lastPattern = pattern
                        lastSequenceID = sequenceID
                        lastOperation = operation
                    }
                }
            }
        }
        log.info("done.")

        log.info("write table to file")
        csvFile.writeText(fileContent)
        log.info("done.")

        return@mainBody
    }
}

fun getDurations(measurements: Array<PatternMeasurement>, pattern:String, sequenceID : String, operation:String, method:String) : Array<Int> {
    val values: ArrayList<Int> = ArrayList()

    log.debug("Call with: $pattern, $sequenceID, $operation, $method")

    for (m in measurements) {

        //Determine sequence
        var seq = m.patternName
        for (arm in m.apiRequestMeasurements) {
            seq += arm.path
        }
        if (!sequenceHelperList.contains(seq)) {
            sequenceHelperList.add(seq)
        }
        val seqNameForThisMeasurment = "Seq. No. ${sequenceHelperList.indexOf(seq)}"


        if (pattern == m.patternName) {
            if (sequenceID == seqNameForThisMeasurment) {
                if (operation == "total") {
                    values.add(getDuration(m.start, m.end))
                } else {
                    for (n in m.apiRequestMeasurements) {
                        if (operation == n.abstractOperation) {
                            if (method == "total") {
                                values.add(getDuration(n.start, n.end))
                            } else {
                                if (method == n.path) {
                                    values.add(getDuration(n.start, n.end))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (values.size == 0) log.debug("EMPTY ARRAY FOR: $pattern, $sequenceID, $operation, $method")
    return values.toTypedArray()
}

fun getDuration(start : Long, end : Long) : Int{
    val s = Date(start)
    val e = Date(end)
    val d = e.time - s.time
    return d.toInt()
}

fun getTableHeader() : String {
    var header = "Pattern,"
    header += " Sequence,"
    header += " Operation,"
    header += " Path,"
    header += " Min,"
    header += " Q1,"
    header += " Med,"
    header += " Q3,"
    header += " Max,"
    header += " n"
    return header
}

fun getTableRow(pattern: String, sequenceID: String, operation: String, method: String, boxPlotValues: BoxPlotValues) : String {
    var row = pattern
    row += ", $sequenceID"
    row += ", $operation"
    row += ", $method"
    row += ", ${boxPlotValues.min}"
    row += ", ${boxPlotValues.lowerQuartile}"
    row += ", ${boxPlotValues.median}"
    row += ", ${boxPlotValues.upperQuartile}"
    row += ", ${boxPlotValues.max}"
    row += ", ${boxPlotValues.size}"
    return row
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