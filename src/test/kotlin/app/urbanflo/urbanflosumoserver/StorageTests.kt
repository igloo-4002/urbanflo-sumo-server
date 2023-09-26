package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.storage.FilesystemStorageService
import app.urbanflo.urbanflosumoserver.storage.StorageException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.util.AssertionErrors.*

@SpringBootTest
class StorageTests(@Autowired private val storageService: FilesystemStorageService) {
    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
    }

    @Test
    fun testStore() {
        val network: SumoNetwork = jsonMapper.readValue(ClassPathResource("simple-network.json").file)
        val info = storageService.store(network)
        assertEquals("Document name should be equal", network.documentName, info.documentName)
    }

    @Test
    fun testStoreNoEdges() {
        val network: SumoNetwork = jsonMapper.readValue(ClassPathResource("no-edges.json").file)
        assertThrows<StorageException> {
            storageService.store(network)
        }
    }

    @Test
    fun testModify() {
        val initialNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("simple-network.json").file)
        val initialInfo = storageService.store(initialNetwork)
        val initialStoredNetwork = storageService.export(initialInfo.id)
        assertEquals("Document name should be equal", initialNetwork.documentName, initialNetwork.documentName)
        assertEquals("Input and output network must be the same", initialNetwork, initialStoredNetwork)

        val modifiedNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("4-way-intersection.json").file)
        val modifiedInfo = storageService.store(initialInfo.id, modifiedNetwork)
        val modifiedStoredNetwork = storageService.export(modifiedInfo.id)
        assertEquals("Simulation ID must be the same", initialInfo.id, modifiedInfo.id)
        assertNotEquals("Network must be different", initialStoredNetwork, modifiedStoredNetwork)
    }
}