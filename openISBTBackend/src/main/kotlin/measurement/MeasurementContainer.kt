package measurement

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MeasurementContainer {

    val  log: Logger = LoggerFactory.getLogger("MeasurmentContainer")

    // pattern -> sequence -> operation -> method -> Boxplot
    var measurements: MutableMap<String, //pattern ->
            MutableMap<String, // sequenceID ->
                    ArrayList<Pair<String, //operation ->
                            MutableMap<String, BoxPlotValues>>>>> = HashMap() //path -> Boxplot

    fun isPattern(pattern: String): Boolean {
        return measurements.contains(pattern)
    }

    fun addPattern(pattern: String) {
        measurements[pattern] = HashMap()
    }

    fun isSequence(pattern: String, sequenceID: String): Boolean {
        return measurements.getValue(pattern).containsKey(sequenceID)
    }

    fun addSeqence(pattern: String, sequence: String) {
        measurements.getValue(pattern)[sequence] = ArrayList()
    }


    fun isOperation(pattern: String, sequence: String, operation: String, index: Int): Boolean {
        if (measurements.getValue(pattern).getValue(sequence).size > index) {
            val op = (measurements.getValue(pattern).getValue(sequence)[index])
            if (op.first == operation) {
                return true
            }
        }
        return false
    }

    fun addOperation(pattern: String, sequence: String, operation: String, index: Int) {
        val operations = measurements.getValue(pattern).getValue(sequence)
        //create empty Pairs if previous measurements are not added to the list until this point
        while (operations.size < index + 1) operations.add(Pair("bla", HashMap()))
        operations[index] = Pair(operation, HashMap())
    }

    fun isPath(pattern: String, sequence: String, operation: String, index: Int, path: String): Boolean {
        if (measurements.getValue(pattern).getValue(sequence).size > index) {
            val op = (measurements.getValue(pattern).getValue(sequence)[index])
            if (op.first == operation) {
                return op.second.containsKey(path)
            }
        }
        return false
    }

    fun addPath(pattern: String, sequence: String, operation: String, index: Int, path: String, boxPlotValues: BoxPlotValues) {
        if (measurements.getValue(pattern).getValue(sequence).size > index) {
            val op = (measurements.getValue(pattern).getValue(sequence)[index])
            if (op.first == operation) {
                op.second[path] = boxPlotValues
                return
            }
        }
        log.error("Critical error while adding path, some boxplot values might not be added")
    }
}