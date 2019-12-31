package environment.api.parser.waste

data class RecyclingPointApi(val name: String, val description: String, val type: String, val latitude: Double, val longitude: Double) {
    fun locationUid() = "$latitude,$longitude"
}

data class RecyclingPoint(val name: String, val description: String, val type: List<String>, val latitude: Double, val longitude: Double)