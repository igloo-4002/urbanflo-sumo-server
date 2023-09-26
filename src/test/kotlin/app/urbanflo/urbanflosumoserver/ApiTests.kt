package app.urbanflo.urbanflosumoserver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
        val response: ResponseEntity<String> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        assertEquals(response.statusCode, HttpStatus.CREATED)
    }

    companion object {
        @JvmStatic
        private lateinit var httpHeaders: HttpHeaders

        @JvmStatic
        @BeforeAll
        fun initHttpHeaders(): Unit {
            httpHeaders = HttpHeaders()
            httpHeaders.contentType = MediaType.APPLICATION_JSON
        }
    }
}