package environment.api.parser.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import environment.api.parser.waste.DataPointApi
import environment.api.parser.waste.DataType

open class DyanmoDbUploader {

    val dynamodbClient: AmazonDynamoDB by lazy {
        AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .build()
    }

    fun upload(tableName: String, item: Map<String, AttributeValue>) {
        PutItemRequest()
                .withTableName(tableName)
                .withItem(item)
                .let {
                    dynamodbClient.putItem(it)
                }

    }

    fun download(tableName: String): List<DataPointApi> =
            DynamoDB(dynamodbClient).getTable(tableName)
                    .scan()
                    .toList()
                    .map {
                        DataPointApi(it.getString("name"),
                                it.getString("description"),
                                it.getString("type"),
                                it.getDouble("latitude"),
                                it.getDouble("longitude"),
                                DataType.valueOf(it.getString("dataType").toUpperCase())
                        )
                    }

}