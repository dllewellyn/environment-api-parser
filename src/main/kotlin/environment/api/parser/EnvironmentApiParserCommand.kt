package environment.api.parser

import environment.api.parser.interfaces.ListUploader
import environment.api.parser.waste.EdinburghWasteParser
import environment.api.parser.waste.RecyclingPoint
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.ResourceLoader
import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import io.micronaut.http.client.RxHttpClient
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import javax.inject.Inject
import javax.xml.bind.JAXBElement

@Command(name = "environment-api-parser", description = ["..."],
        mixinStandardHelpOptions = true)
class EnvironmentApiParserCommand : Runnable {

    @Option(names = ["-v", "--verbose"], description = ["..."])
    private var verbose: Boolean = false

    @Inject
    private lateinit var wasteParser: EdinburghWasteParser

    @Inject
    private lateinit var uploader: ListUploader<RecyclingPoint>

    @ExperimentalStdlibApi
    override fun run() {
        runBlocking {
            uploader.upload("environment-app", wasteParser.parse().toList())
        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PicocliRunner.run(EnvironmentApiParserCommand::class.java, *args)
        }
    }
}
