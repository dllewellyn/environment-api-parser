package environment.api.parser.waste

enum class DataType {
    RECYCLING,
    SHOP
}

data class DataPointApi(val name: String, val description: String, val type: String, val latitude: Double, val longitude: Double, val dataType: DataType) {
    fun locationUid() = "$latitude,$longitude"
}

data class DataPoint(val name: String, val description: String, val type: List<String>, val latitude: Double, val longitude: Double, val dataType: DataType)