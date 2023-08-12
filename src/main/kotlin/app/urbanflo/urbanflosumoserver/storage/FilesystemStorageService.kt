package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.SimulationId
import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.responses.SimulationInfo
import app.urbanflo.urbanflosumoserver.responses.SumoNetwork
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

private val logger = KotlinLogging.logger {}

@Service
class FilesystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    private lateinit var uploadsDir: Path
    private val xmlMapper = XmlMapper()

    init {
        this.uploadsDir = Paths.get(properties.location)
        try {
            Files.createDirectories(uploadsDir)
        } catch (e: IOException) {
            throw StorageException("Cannot create uploads directory", e)
        }
    }

    override fun store(network: SumoNetwork): SimulationId {
        var id: UUID
        var simulationDir: Path
        do {
            id = UUID.randomUUID()
            simulationDir = uploadsDir.resolve(Paths.get(id.toString()).normalize()).toAbsolutePath()
        } while (simulationDir.exists())
        logger.info {"Saving file at $simulationDir"}
        simulationDir.createDirectory()

        // save network as XML
        val nodeXml = xmlMapper.writeValueAsString(network.nodes.values.toList())
        val edgXml = xmlMapper.writeValueAsString(network.edges.values.toList())
        val nodeFileName = "$id-nod.xml"
        val edgeFileName = "$id-edg.xml"
        val nodeFilePath = simulationDir.resolve(nodeFileName).normalize().toAbsolutePath()
        val edgeFilePath = simulationDir.resolve(edgeFileName).normalize().toAbsolutePath()
        logger.info { "Saving nod file to $nodeFilePath" }
        logger.info { "Saving edg file to $edgeFilePath" }
        return id
    }

    override fun store(simulationId: SimulationId, network: SumoNetwork) {
        TODO("modify existing simulation")
    }

    override fun load(id: String): SimulationInstance {
        TODO("load simulation")
    }

    override fun delete(id: String) {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath().toFile()
        simulationDir.listFiles()?.forEach { file -> file.delete() }
        if (!simulationDir.delete()) {
            throw StorageSimulationNotFoundException("No such simulation with ID $id")
        }
    }

    override fun info(id: String): SimulationInfo {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath()
        if (simulationDir.exists()) {
            // TODO: fetch simulation metadata
            return SimulationInfo(id)
        } else {
            throw StorageSimulationNotFoundException("No such simulation with ID $id")
        }
    }

    override fun listAll(): List<Resource> {
        TODO("list all simulations")
    }

    override fun deleteAll() {
        TODO("delete all simulations")
    }
}