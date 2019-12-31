package environment.api.parser.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import environment.api.parser.interfaces.ListUploader
import environment.api.parser.waste.RecyclingPoint
import environment.api.parser.waste.RecyclingPointApi
import java.io.ByteArrayInputStream
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

        val putObject = PutObjectRequest(name, "edi-recycling.json", ByteArrayInputStream(baos.toByteArray()), ObjectMetadata().also { it.setHeader("content-type", "application/json") })
                .withCannedAcl(CannedAccessControlList.PublicRead)

        s3Client.putObject(putObject)
    }
}