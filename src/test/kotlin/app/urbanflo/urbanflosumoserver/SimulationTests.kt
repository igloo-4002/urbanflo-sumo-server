package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.storage.FilesystemStorageService
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.util.*

@SpringBootTest
class SimulationTests(@Autowired private val storageService: FilesystemStorageService) {
    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var uploadsLocation: String

    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    val simpleNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("simple-network.json").file)

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
    fun startSimpleNetworkSimulation() {
        val info = storageService.store(simpleNetwork)
        val simulation = storageService.load(info.id, UUID.randomUUID().toString())
        // TODO: learn how to test the flux

        assertTrue(simulation.hasNext())
        for (i in 0..<100) {
            if (simulation.hasNext()) {
                simulation.next()
            } else {
                break
            }
        }
        simulation.stopSimulation()
        assertFalse(simulation.hasNext())

    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun loadTraCI() {
            System.loadLibrary("libtracijni")
        }
    }
}