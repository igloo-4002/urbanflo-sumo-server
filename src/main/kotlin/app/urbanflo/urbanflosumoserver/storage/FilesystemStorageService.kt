package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.SimulationId
import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.netconvert.NetconvertException
import app.urbanflo.urbanflosumoserver.netconvert.runNetconvert
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.SumoNetwork
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
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
        val nodeXml = xmlMapper.writeValueAsString(network.nodesXml())
        val edgXml = xmlMapper.writeValueAsString(network.edgesXml())
        val nodeFileName = "$id-nod.xml"
        val edgeFileName = "$id-edg.xml"
        val nodeFilePath = simulationDir.resolve(nodeFileName).normalize().toAbsolutePath()
        val edgeFilePath = simulationDir.resolve(edgeFileName).normalize().toAbsolutePath()
        val nodeFile = nodeFilePath.toFile()
        val edgeFile = edgeFilePath.toFile()
        try {
            logger.info { "Saving nod file to $nodeFilePath" }
            nodeFile.writeText(nodeXml)
            logger.info { "Saving edg file to $edgeFilePath" }
            edgeFile.writeText(edgXml)
        } catch (e: IOException) {
            delete(id.toString())
            throw StorageException("Cannot save files", e)
        }

        // run netconvert
        try {
            runNetconvert(id, simulationDir, nodeFileName, edgeFileName)
        } catch (e: NetconvertException) {
            delete(id.toString())
            throw StorageException("Cannot convert edge and node files", e)
        }
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