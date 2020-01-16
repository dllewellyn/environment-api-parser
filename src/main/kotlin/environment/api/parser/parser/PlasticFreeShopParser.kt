package environment.api.parser.parser

import com.environment.app.DataType
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Singleton

@Singleton
class PlasticFreeShopParser(@Client("https://docs.google.com/") val client: RxHttpClient) {

    val uriBuilder: HttpRequest<Any> = UriBuilder.of("spreadsheets/d/e/2PACX-1vQ3JXTeXyjI1gz6F1fYgcUcxuPR3ZfGHNdTsadJal1P0Y-90UT1HaDu8F-xqUHg-vXNW7i6jsN4nqTM/pub?output=csv")
            .build()
            .let {
                HttpRequest.GET(it)
            }

    suspend fun parse(): Flow<DataPointApi> =
            client.exchange(uriBuilder, String::class.java)
                    .blockingLast()
                    .body()
                    .let {
                        it?.split("\n")?.let { all -> all.subList(1, all.size) } ?: listOf()
                    }
                    .map {
                        with(it.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())) {
                            val items = mutableListOf<DataPointApi>()
                            this[4].split(",")
                                    .forEach {
                                        items.add(DataPointApi(this[1].strip(), if (this[2].isBlank()) {
                                            "-"
                                        } else {
                                            this[2].strip()
                                        }, it, this[5].strip().toDouble(), this[6].strip().toDouble(), DataType.SHOP))
                                    }
                            items

                        }
                    }
                    .flatten()
                    .asFlow()


}