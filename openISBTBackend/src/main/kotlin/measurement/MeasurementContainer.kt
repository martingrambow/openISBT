package measurement

class MeasurementContainer {

    //resource -> pattern -> operation -> method -> Boxplot
    var measurements : MutableMap<String, //resource ->
            MutableMap<String, // pattern ->
                    ArrayList<Pair<String, //operation ->
                            MutableMap<String, BoxPlotValues>>>>> = HashMap() //method -> Boxplot

    fun isResource (resource : String) : Boolean{
        return measurements.containsKey(resource)
    }

    fun addResource (resource: String) {
        measurements[resource] = HashMap()
    }

    fun isPattern(resource : String, pattern: String) : Boolean {
        return measurements.getValue(resource).contains(pattern)
    }

    fun addPattern(resource: String, pattern: String) {
        measurements.getValue(resource)[pattern] = ArrayList()
    }

    fun isOperation(resource: String, pattern: String, operation: String) : Boolean {
        for (op in measurements.getValue(resource).getValue(pattern)) {
            if (op.first == operation) {
                return true
            }
        }
        return false
    }

    fun addOperation(resource: String, pattern: String, operation: String, index : Int) {
        val operations = measurements.getValue(resource).getValue(pattern)
        while (operations.size < index+1) operations.add(Pair("bla", HashMap()))
        operations[index] = Pair(operation, HashMap())
    }

    fun isMethod(resource: String, pattern: String, operation: String, method:String) : Boolean {
        val operations = measurements.getValue(resource).getValue(pattern)
        for (o in operations) {
            if (o.first == operation) {
                return o.second.containsKey(method)
            }
        }
        return false
    }

    fun addMethod(resource: String, pattern: String, operation: String, method: String, boxPlotValues: BoxPlotValues) {
        val operations = measurements.getValue(resource).getValue(pattern)
        for (o in operations) {
            if (o.first == operation) {
                o.second[method] = boxPlotValues
            }
        }
    }
}