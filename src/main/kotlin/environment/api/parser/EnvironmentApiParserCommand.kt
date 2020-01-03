package environment.api.parser

import environment.api.parser.interfaces.Downloader
import environment.api.parser.interfaces.ListUploader
import environment.api.parser.interfaces.Uploader
import environment.api.parser.waste.EdinburghWasteParser
import environment.api.parser.waste.RecyclingPoint
import environment.api.parser.waste.RecyclingPointApi
import io.micronaut.configuration.picocli.PicocliRunner
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

import picocli.CommandLine.Command
import picocli.CommandLine.Option
import javax.inject.Inject

@Command(name = "environment-api-parser", description = ["..."],
        mixinStandardHelpOptions = true)
class EnvironmentApiParserCommand : Runnable {

    @Option(names = ["-s", "--synchronise"], description = ["..."])
    private var synchronise: Boolean = false

    @Option(names = ["-r", "--recyclingPoints"], description = ["..."])
    private var recyclingPoints: Boolean = false

    @Option(names = ["-c", "--clear"], description = ["..."])
    private var clear: Boolean = false

    @Option(names = ["--list-categories"], description = ["List all categories"])
    private var listCategories: Boolean = false

    @Inject
    private lateinit var wasteParser: EdinburghWasteParser

    @Inject
    private lateinit var uploader: ListUploader<RecyclingPoint>

    @Inject
    private lateinit var dynamoDbUploader: Uploader<RecyclingPointApi>

    @Inject
    private lateinit var dynamoDbDownloader: Downloader<RecyclingPointApi>

    @ExperimentalStdlibApi
    override fun run() {
        runBlocking {


            if (recyclingPoints) {

                wasteParser.parse().toList().asFlow()
                        .onEach {
                            dynamoDbUploader.uploadSingleItem(it)
                        }
                        .collect()
            }

            if (synchronise) {
                val mappedData = mutableMapOf<String, RecyclingPoint>()
                dynamoDbDownloader.download().forEach { recyclingPoint ->

                    mappedData[recyclingPoint.locationUid()] = if (mappedData.containsKey(recyclingPoint.locationUid())) {
                        mappedData[recyclingPoint.locationUid()]?.let {
                            it.copy(type = it.type.toMutableList().also { list ->
                                list.add(recyclingPoint.type)
                            })
                        } ?: throw IllegalAccessError()
                    } else {
                        RecyclingPoint(recyclingPoint.name,
                                recyclingPoint.description,
                                listOf(recyclingPoint.type),
                                recyclingPoint.latitude,
                                recyclingPoint.longitude)
                    }
                }
                uploader.upload("environment-app", mappedData.values.toList())
            }

            if (listCategories) {
                dynamoDbDownloader.download().map {
                    it.type
                }.toSet()
                        .forEach(::println)
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PicocliRunner.run(EnvironmentApiParserCommand::class.java, *args)
        }
    }
}
