package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.ErrorResponse
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import java.io.File

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTests(@Autowired private val restTemplate: TestRestTemplate) {
    @Value(value = "\${local.server.port}")
    private val port = 0
    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var uploadsLocation: String

    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    val simpleNetwork = ClassPathResource("simple-network.json").file.readText()
    val fourWayIntersection = ClassPathResource("4-way-intersection.json").file.readText()
    val noEdgeNetwork = ClassPathResource("no-edges.json").file.readText()
    val missingFields = ClassPathResource("missing-fields.json").file.readText()

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
    }

    @BeforeEach
    fun deleteAllUploads() {
        File(uploadsLocation).listFiles()?.forEach { simulation ->
            simulation.listFiles()?.forEach { file ->
                file.delete()
            }
            simulation.delete()
        }
    }

    @Test
    fun testUploadNetwork() {
        val request = HttpEntity(simpleNetwork, httpHeaders)
        val response: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        assertEquals(HttpStatus.CREATED, response.statusCode)

        val info = response.body!!
        val network: SumoNetwork = jsonMapper.readValue(simpleNetwork)
        assertEquals(network.documentName, info.documentName)

        // test simulation info
        val infoRequest: HttpEntity<SimulationInfo> = HttpEntity(httpHeaders)
        val infoResponse: ResponseEntity<SimulationInfo> = restTemplate.getForEntity("http://localhost:${port}/simulation/${info.id}", infoRequest)
        assertEquals(HttpStatus.OK, infoResponse.statusCode)
        assertEquals(info.id, infoResponse.body!!.id)
        assertEquals(info.documentName, infoResponse.body!!.documentName)
    }

    @Test
    fun testMissingFields() {
        val request = HttpEntity(missingFields, httpHeaders)
        val response: ResponseEntity<ErrorResponse> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun testNetconvertError() {
        val request = HttpEntity(noEdgeNetwork, httpHeaders)
        val response: ResponseEntity<ErrorResponse> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        // at the moment, we can't distinguish between bad networks and other netconvert errors
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }


    @Test
    fun testModifyNetwork() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<SimulationInfo> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.OK, modifyResponse.statusCode)
        assertEquals(id, modifyResponse.body!!.id)
    }


    @Test
    fun testModifyMissingFields() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val modifyRequest = HttpEntity(missingFields, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.BAD_REQUEST, modifyResponse.statusCode)
    }

    @Test
    fun testModifyNetconvertError() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val modifyRequest = HttpEntity(noEdgeNetwork, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, modifyResponse.statusCode)
    }

    @Test
    fun testModifyNotFound() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> = restTemplate.exchange("http://localhost:${port}/simulation/12345", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.NOT_FOUND, modifyResponse.statusCode)
    }

    @Test
    fun testModifyEmptyId() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val id = ""
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        // this is a 404 since there are no PUT endpoints in /simulation/
        assertEquals(HttpStatus.NOT_FOUND, modifyResponse.statusCode)
    }

    @Test
    fun testDeleteNetwork() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val deleteRequest: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.DELETE, deleteRequest)
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        // any subsequent deletion should return a 404
        val deleteResponse2: ResponseEntity<Any> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.DELETE, deleteRequest)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse2.statusCode)
    }

    @Test
    fun testDeleteEmptyId() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val id = ""
        val deleteRequest: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> = restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.DELETE, deleteRequest)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.statusCode)
    }

    @Test
    fun testInfoNotFound() {
        val id = "12345"
        val request: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> = restTemplate.getForEntity("http://localhost:${port}/simulation/$id", request)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.statusCode)
    }

    @Test
    fun testInfoEmptyId() {
        val id = ""
        val request: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> = restTemplate.getForEntity("http://localhost:${port}/simulation/$id", request)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.statusCode)
    }


    companion object {
        @JvmStatic
        private lateinit var httpHeaders: HttpHeaders

        @JvmStatic
        @BeforeAll
        fun initHttpHeaders() {
            httpHeaders = HttpHeaders()
            httpHeaders.contentType = MediaType.APPLICATION_JSON
        }
    }
}