package environment.api.parser.waste

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import environment.api.parser.aws.DyanmoDbUploader
import environment.api.parser.interfaces.Uploader
import io.micronaut.context.annotation.Value
import javax.inject.Singleton

@Singleton
class WasteUploaderDynamoDb(@Value("recycling_table_name") val recyclingTableName : String) : DyanmoDbUploader(), Uploader<RecyclingPoint> {
    override fun uploadSingleItem(item: RecyclingPoint) {
        upload("edi-recycling", item.toMap())
    }
}

fun RecyclingPoint.toMap() : Map<String, AttributeValue> {
    val returnValue = mutableMapOf<String, AttributeValue>()
    returnValue["name"] = AttributeValue(name)
    returnValue["description"] = AttributeValue(description)
    returnValue["type"] = AttributeValue(type)
    returnValue["latitude"] = AttributeValue(latitude.toString())
    returnValue["longitude"] = AttributeValue(longitude.toString())
    returnValue["id"] = AttributeValue("$name-$type-$latitude-$longitude")
    return returnValue
}