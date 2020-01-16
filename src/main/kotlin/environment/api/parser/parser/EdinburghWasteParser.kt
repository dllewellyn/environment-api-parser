package environment.api.parser.parser

import com.environment.app.DataType
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import kotlinx.coroutines.flow.*
import javax.inject.Singleton

@Singleton
class EdinburghWasteParser(@Client("https://data.edinburghopendata.info") val httpClient: RxHttpClient) {

    @ExperimentalStdlibApi
    suspend fun parse(): Flow<DataPointApi> {

        val uriBuilder: HttpRequest<Any> = UriBuilder.of("/dataset/e8c41d88-612a-47a1-b51b-9cf04e46d14f/resource/4cfb5177-d3db-4efc-ac6f-351af75f9f92/download/recyclingpoints.csv")
                .build()
                .let {
                    HttpRequest.GET(it)
                }

        return httpClient.exchange(uriBuilder, String::class.java)
                .blockingLast()
                .body()
                .let {
                    it?.split("\n")?.let { all -> all.subList(1, all.size) } ?: listOf()
                }
                .asFlow()
                .map {
                    with(it.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())) {
                        if (it.length > 4) {
                            DataPointApi(this[0].strip(), this[1].strip(), this[2].strip(), this[3].strip().toDouble(), this[4].strip().toDouble(), DataType.RECYCLING)
                        } else {
                            null
                        }
                    }
                }
                .filterNotNull()
    }
}

fun String.strip() = replace("\"", "").replace("'", "").replace("\n", "")