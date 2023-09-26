package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.ErrorResponse
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
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

    val simpleNetwork = ClassPathResource("simple-network.json").file.readText()
    val fourWayIntersection = ClassPathResource("4-way-intersection.json").file.readText()
    val noEdgeNetwork = ClassPathResource("no-edges.json").file.readText()
    val missingFields = ClassPathResource("missing-fields.json").file.readText()

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
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> = restTemplate.exchange("http://localhost:${port}/simulation/", HttpMethod.PUT, modifyRequest)
        // this is a 404 since there are no PUT endpoints in /simulation/
        assertEquals(HttpStatus.NOT_FOUND, modifyResponse.statusCode)
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