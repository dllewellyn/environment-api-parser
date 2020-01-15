package environment.api.parser.waste

import com.environment.app.DataType

data class DataPointApi(val name: String, val description: String, val type: String, val latitude: Double, val longitude: Double, val dataType: DataType) {
    fun locationUid() = "$latitude,$longitude"
}

