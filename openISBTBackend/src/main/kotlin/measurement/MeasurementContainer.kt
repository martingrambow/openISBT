package measurement

class MeasurementContainer {

    //sequence -> pattern -> operation -> method -> Boxplot
    var measurements : MutableMap<String, //sequence ->
            MutableMap<String, // pattern ->
                    ArrayList<Pair<String, //operation ->
                            MutableMap<String, BoxPlotValues>>>>> = HashMap() //method -> Boxplot

    fun isSequence (sequenceID : String) : Boolean{
        return measurements.containsKey(sequenceID)
    }

    fun addSeqence (sequence: String) {
        measurements[sequence] = HashMap()
    }

    fun isPattern(sequence : String, pattern: String) : Boolean {
        return measurements.getValue(sequence).contains(pattern)
    }

    fun addPattern(sequence: String, pattern: String) {
        measurements.getValue(sequence)[pattern] = ArrayList()
    }

    fun isOperation(sequence: String, pattern: String, operation: String) : Boolean {
        for (op in measurements.getValue(sequence).getValue(pattern)) {
            if (op.first == operation) {
                return true
            }
        }
        return false
    }

    fun addOperation(sequence: String, pattern: String, operation: String, index : Int) {
        val operations = measurements.getValue(sequence).getValue(pattern)
        while (operations.size < index+1) operations.add(Pair("bla", HashMap()))
        operations[index] = Pair(operation, HashMap())
    }

    fun isMethod(sequence: String, pattern: String, operation: String, method:String) : Boolean {
        val operations = measurements.getValue(sequence).getValue(pattern)
        for (o in operations) {
            if (o.first == operation) {
                return o.second.containsKey(method)
            }
        }
        return false
    }

    fun addMethod(sequence: String, pattern: String, operation: String, method: String, boxPlotValues: BoxPlotValues) {
        val operations = measurements.getValue(sequence).getValue(pattern)
        for (o in operations) {
            if (o.first == operation) {
                o.second[method] = boxPlotValues
            }
        }
    }
}