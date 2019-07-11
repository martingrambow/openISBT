package measurements

class BoxPlotValues (private val rawvalues : Array<Int>){

    var size = rawvalues.size
    private var sorted = rawvalues.sortedArray()
    var min = sorted[0]
    var median = measurements.median(sorted, 0, sorted.size -1)
    var max = sorted[sorted.lastIndex]

    private val m = sorted.size / 2
    private var lowerEnd = if (sorted.size % 2 == 1) m else m - 1
    var lowerQuartile = median(sorted, 0, lowerEnd)
    var upperQuartile = median(sorted, m, sorted.size - 1)
}

private fun median(x: Array<Int>, start: Int, endInclusive: Int): Double {
    val size = endInclusive - start + 1
    require (size > 0) { "Array slice cannot be empty" }
    val m = start + size / 2
    return if (size % 2 == 1) x[m].toDouble() else (x[m - 1] + x[m]) / 2.0
}
