package environment.api.parser

import com.environment.app.DataPoint
import environment.api.parser.interfaces.Downloader
import environment.api.parser.interfaces.ListUploader
import environment.api.parser.interfaces.Uploader
import environment.api.parser.waste.DataPointApi
import environment.api.parser.waste.EdinburghWasteParser
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

    @Option(names = ["--list-categories"], description = ["List all categories"])
    private var listCategories: Boolean = false

    @Inject
    private lateinit var wasteParser: EdinburghWasteParser

    @Inject
    private lateinit var uploader: ListUploader<DataPoint>

    @Inject
    private lateinit var dynamoDbUploader: Uploader<DataPointApi>

    @Inject
    private lateinit var dynamoDbDownloader: Downloader<DataPointApi>

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
                val mappedData = mutableMapOf<String, DataPoint>()
                dynamoDbDownloader.download().forEach { recyclingPoint ->

                    mappedData[recyclingPoint.locationUid()] = if (mappedData.containsKey(recyclingPoint.locationUid())) {
                        mappedData[recyclingPoint.locationUid()]?.let {
                            it.copy(type = it.type.toMutableList().also { list ->
                                list.add(recyclingPoint.type)
                            })
                        } ?: throw IllegalAccessError()
                    } else {
                        DataPoint(recyclingPoint.name,
                                recyclingPoint.description,
                                listOf(recyclingPoint.type),
                                recyclingPoint.latitude,
                                recyclingPoint.longitude,
                                recyclingPoint.dataType)
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
