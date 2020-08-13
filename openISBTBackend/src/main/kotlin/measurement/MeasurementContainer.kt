package measurement

class MeasurementContainer {

    // pattern -> sequence -> operation -> method -> Boxplot
    var measurements: MutableMap<String, //pattern ->
            MutableMap<String, // sequence ->
                    ArrayList<Pair<String, //operation ->
                            MutableMap<String, BoxPlotValues>>>>> = HashMap() //method -> Boxplot

    fun isPattern(pattern: String): Boolean {
        return measurements.contains(pattern)
    }

    fun addPattern(pattern: String) {
        measurements[pattern] = HashMap()
    }

    fun isSequence(sequenceID: String, pattern: String): Boolean {
        return measurements.getValue(pattern).containsKey(sequenceID)
    }

    fun addSeqence(sequence: String, pattern: String) {
        measurements.getValue(pattern)[sequence] = ArrayList()
    }


    fun isOperation(pattern: String, sequence: String, operation: String): Boolean {
        for (op in measurements.getValue(pattern).getValue(sequence)) {
            if (op.first == operation) {
                return true
            }
        }
        return false
    }

    fun addOperation(pattern: String, sequence: String, operation: String, index: Int) {
        val operations = measurements.getValue(pattern).getValue(sequence)
        while (operations.size < index + 1) operations.add(Pair("bla", HashMap()))
        operations[index] = Pair(operation, HashMap())
    }

    fun isMethod(pattern: String, sequence: String, operation: String, method: String): Boolean {
        val operations = measurements.getValue(pattern).getValue(sequence)
        for (o in operations) {
            if (o.first == operation) {
                return o.second.containsKey(method)
            }
        }
        return false
    }

    fun addMethod(pattern: String, sequence: String, operation: String, method: String, boxPlotValues: BoxPlotValues) {
        val operations = measurements.getValue(pattern).getValue(sequence)
        for (o in operations) {
            if (o.first == operation) {
                o.second[method] = boxPlotValues
            }
        }
    }
}