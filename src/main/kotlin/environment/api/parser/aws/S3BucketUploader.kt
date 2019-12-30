package environment.api.parser.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import environment.api.parser.interfaces.ListUploader
import environment.api.parser.waste.RecyclingPoint
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import javax.inject.Singleton


@Singleton
class S3BucketUploader : ListUploader<RecyclingPoint> {
    private val s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.EU_WEST_1).build()

    override fun upload(name: String, items: List<RecyclingPoint>) {
        val baos = ByteArrayOutputStream()
        val writer = OutputStreamWriter(baos)
        ObjectMapper().writeValue(writer, items)
        baos.close()

        s3Client.putObject(name, "edi-recycling.json", baos.toByteArray().toString(Charset.defaultCharset()))
    }

}