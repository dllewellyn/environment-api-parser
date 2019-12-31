package environment.api.parser.waste

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import environment.api.parser.aws.DyanmoDbUploader
import environment.api.parser.interfaces.Downloader
import environment.api.parser.interfaces.Uploader
import io.micronaut.context.annotation.Value
import javax.inject.Singleton

@Singleton
class RecyclingUploaderAndDownloaderDynamoDb(@Value("\${recycling_table_name}") val recyclingTableName: String) : DyanmoDbUploader(), Uploader<RecyclingPointApi>, Downloader<RecyclingPointApi> {
    override fun uploadSingleItem(item: RecyclingPointApi) {
        upload(recyclingTableName, item.toMap())
    }

    override fun download() = download(recyclingTableName)
}

fun RecyclingPointApi.toMap(): Map<String, AttributeValue> {
    val returnValue = mutableMapOf<String, AttributeValue>()
    returnValue["name"] = AttributeValue(name)
    returnValue["description"] = AttributeValue(description)
    returnValue["type"] = AttributeValue(type)
    returnValue["latitude"] = AttributeValue(latitude.toString())
    returnValue["longitude"] = AttributeValue(longitude.toString())
    returnValue["id"] = AttributeValue("$name-$type-$latitude-$longitude")
    return returnValue
}