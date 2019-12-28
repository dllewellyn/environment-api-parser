package environment.api.parser.waste

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import javax.inject.Singleton

data class RecyclingPoint(val name: String, val description: String, val type: String, val latitude: Double, val longitude: Double)
@Singleton
class EdinburghWasteParser(@Client("https://data.edinburghopendata.info") val httpClient: RxHttpClient) {

    @ExperimentalStdlibApi
    fun parse() {

        val uriBuilder: HttpRequest<Any> = UriBuilder.of("/dataset/e8c41d88-612a-47a1-b51b-9cf04e46d14f/resource/4cfb5177-d3db-4efc-ac6f-351af75f9f92/download/recyclingpoints.csv")
                .build()
                .let {
                    HttpRequest.GET(it)
                }

        httpClient.exchange(uriBuilder, String::class.java)
                .blockingLast()
                .body()
                .let {
                    it?.split("\n")?.let { all -> all.subList(1, all.size) } ?: listOf()
                }
                .forEach {
                    with(it.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())) {
                    if (it.length > 4) {
                            println(RecyclingPoint(this[0], this[1], this[2], this[3].toDouble(), this[4].toDouble()))
                        }
                    }
                }
    }

}