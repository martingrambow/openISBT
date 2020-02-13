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

/**
 * Sample calls:
 * -o -r results.json
 */
fun main(args: Array<String>) = mainBody  {
    ArgParser(args).parseInto(::BoxPlotArguments).run {

        // load masurements
        val measurements = loadMeasurements(readFile(measurementFile)) ?: throw InvalidArgumentException("Could not parse measurments")
        log.info("Imported Measurments")

        log.info("Fill container...")
        val container = MeasurementContainer()
        for (m in measurements) {
            val resource = m.resource
            if (!container.isResource(resource)) {
                container.addResource(resource)
            }
            val pattern = m.patternName
            if (!container.isPattern(resource, pattern)) {
                container.addPattern(resource, pattern)
            }
            for (n in m.apiRequestMeasurements) {
                val operation = n.abstractOperation
                if (!container.isOperation(resource, pattern, operation)) {
                    container.addOperation(resource, pattern, operation, n.index)
                }
                val method = n.path
                if (!container.isMethod(resource, pattern, operation, method)) {
                    container.addMethod(resource, pattern, operation, method, BoxPlotValues(getDurations(measurements, resource, pattern, operation, method)))
                }
            }
        }
        log.info("filled.")

        log.info("generate boxplot table...")
        var fileContent = getTableHeader() + "\n"
        var lastResource = ""
        var lastPattern = ""
        var lastOperation = ""

        for (resource in container.measurements.keys) {
            for (pattern in container.measurements.getValue(resource).keys) {
                for (o in container.measurements.getValue(resource).getValue(pattern)) {
                    if (pattern!= lastPattern || resource != lastResource) {
                        //Insert Total pattern line
                        val total = BoxPlotValues(getDurations(measurements, resource, pattern, "total", ""))
                        val totalrow = getTableRow(resource, pattern, "total", "", total)
                        fileContent += totalrow + "\n"
                        lastResource = resource
                        lastPattern = pattern
                    }
                    val operation = o.first
                    if (o.second.entries.size > 1) {
                        //Multiple methods, insert total line
                        val total = BoxPlotValues(getDurations(measurements, resource, pattern, operation, "total"))
                        val cResource = if (resource != lastResource) resource else ""
                        val cPattern = if (pattern != lastPattern) pattern else ""
                        val totalrow = getTableRow(cResource, cPattern, operation, "total", total)
                        fileContent += totalrow + "\n"
                        lastOperation = operation
                    }
                    for (method in o.second.entries) {
                        val cResource = if (resource != lastResource) resource else ""
                        val cPattern = if (pattern != lastPattern) pattern else ""
                        val cOperation = if (operation != lastOperation) operation else ""
                        val cMethod = method.key
                        fileContent += getTableRow(cResource, cPattern, cOperation, cMethod, method.value) + "\n"
                        lastResource = resource
                        lastPattern = pattern
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

fun getDurations(measurements: Array<PatternMeasurement>, resource : String, pattern:String, operation:String, method:String) : Array<Int> {
    val values: ArrayList<Int> = ArrayList()

    log.debug("Call with: $resource, $pattern, $operation, $method")

    for (m in measurements) {
        if (resource == m.resource) {
            if (pattern == m.patternName) {
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

    if (values.size == 0) log.debug("EMPTY ARRAY FOR: $resource, $pattern, $operation, $method")
    return values.toTypedArray()
}

fun getDuration(start : Long, end : Long) : Int{
    val s = Date(start)
    val e = Date(end)
    val d = e.time - s.time
    return d.toInt()
}

fun getTableHeader() : String {
    var header = "Resource,"
    header += " Pattern,"
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

fun getTableRow(resource: String, pattern: String, operation: String, method: String, boxPlotValues: BoxPlotValues) : String {
    var row = resource
    row += ", $pattern"
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