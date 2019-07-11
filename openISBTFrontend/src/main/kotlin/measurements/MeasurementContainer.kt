package measurements

class MeasurementContainer {

    //resource -> pattern -> operation -> method -> Boxplot
    var measuremnts : MutableMap<String, //resource ->
            MutableMap<String, // pattern ->
                    ArrayList<Pair<String, //operation ->
                            MutableMap<String, BoxPlotValues>>>>> = HashMap() //method -> Boxplot

    fun isResource (resource : String) : Boolean{
        return measuremnts.containsKey(resource)
    }

    fun addResource (resource: String) {
        measuremnts.put(resource, HashMap())
    }

    fun isPattern(resource : String, pattern: String) : Boolean {
        return measuremnts.getValue(resource).contains(pattern)
    }

    fun addPattern(resource: String, pattern: String) {
        measuremnts.getValue(resource).put(pattern, ArrayList())
    }

    fun isOperation(resource: String, pattern: String, operation: String) : Boolean {
        for (op in measuremnts.getValue(resource).getValue(pattern)) {
            if (op.first == operation) {
                return true
            }
        }
        return false
    }

    fun addOperation(resource: String, pattern: String, operation: String, index : Int) {
        var operations = measuremnts.getValue(resource).getValue(pattern)
        while (operations.size < index+1) {
            operations.add(Pair("bla", HashMap()))
        }
        operations.set(index, Pair(operation, HashMap()))
    }

    fun isMethod(resource: String, pattern: String, operation: String, method:String) : Boolean {
        var operations = measuremnts.getValue(resource).getValue(pattern)
        for (o in operations) {
            if (o.first == operation) {
                return o.second.containsKey(method)
            }
        }
        return false
    }

    fun addMethod(resource: String, pattern: String, operation: String, method: String, boxPlotValues: BoxPlotValues) {
        var operations = measuremnts.getValue(resource).getValue(pattern)
        for (o in operations) {
            if (o.first == operation) {
                o.second.put(method, boxPlotValues)
            }
        }
    }


}