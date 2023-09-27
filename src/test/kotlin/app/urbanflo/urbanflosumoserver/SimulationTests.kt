package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.SimulationAnalytics
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.storage.StorageService
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.Async
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

@SpringBootTest
class SimulationTests(@Autowired private val storageService: StorageService) {
    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var uploadsLocation: String

    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    val simpleNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("simple-network.json").file)
    val fourWayIntersection: SumoNetwork = jsonMapper.readValue(ClassPathResource("4-way-intersection.json").file)

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
        val simulation = storageService.load(info.id, generateSimulationLabel())
        // TODO: learn how to test the flux

        assertTrue(simulation.hasNext())
        val future = runSimulation(simulation)
        val analytics = future.get()
        logger.info { "Analytics: $analytics" }
        assertFalse(simulation.hasNext())
    }

    @Test
    fun testConcurrentSimulations() {
        val simpleInfo = storageService.store(simpleNetwork)
        val fourWayInfo = storageService.store(fourWayIntersection)
        val simpleSimulation = storageService.load(simpleInfo.id, generateSimulationLabel())
        val fourWaySimulation = storageService.load(fourWayInfo.id, generateSimulationLabel())

        val simpleFuture = runSimulation(simpleSimulation)
        val fourWayFuture = runSimulation(fourWaySimulation)
        val simpleAnalytics = simpleFuture.get()
        val fourWayAnalytics = fourWayFuture.get()
        logger.info { "Analytics: $simpleAnalytics\n$fourWayAnalytics" }

        assertFalse(simpleSimulation.hasNext())
        assertFalse(fourWaySimulation.hasNext())
    }

    @Test
    fun testNoSimulationOutput() {
        val info = storageService.store(simpleNetwork)
        assertThrows<StorageSimulationNotFoundException> {
            storageService.getSimulationOutput(info.id)
        }
        assertThrows<StorageSimulationNotFoundException> {
            storageService.getSimulationAnalytics(info.id)
        }
    }

    @Async
    fun runSimulation(simulation: SimulationInstance): Future<SimulationAnalytics> {
        var i = 0
        while (true) {
            if (!simulation.hasNext()) { // use the side effect of hasNext() to actually close the connection
                break
            } else if (i < 100) {
                simulation.next()
            } else {
                simulation.stopSimulation()
            }
            i++
        }
        val analytics = storageService.getSimulationAnalytics(simulation.simulationId)
        return CompletableFuture.completedFuture(analytics)
    }

    private fun generateSimulationLabel() = UUID.randomUUID().toString()

    companion object {
        @JvmStatic
        @BeforeAll
        fun loadTraCI() {
            System.loadLibrary("libtracijni")
        }
    }
}