package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.netconvert.NetconvertException
import app.urbanflo.urbanflosumoserver.netconvert.runNetconvert
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
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
        var id: SimulationId
        var simulationDir: Path
        do {
            id = UUID.randomUUID().toString()
            simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath()
        } while (simulationDir.exists())
        logger.info { "Saving file at $simulationDir" }
        simulationDir.createDirectory()

        // save network as XML
        val nod = network.nodesXml()
        val nodPath = nod.filePath(id, simulationDir)
        val edg = network.edgesXml()
        val edgPath = edg.filePath(id, simulationDir)
        try {
            logger.info { "Saving nod file to $nodPath" }
            nodPath.toFile().writeText(xmlMapper.writeValueAsString(nod))
            logger.info { "Saving edg file to $edgPath" }
            edgPath.toFile().writeText(xmlMapper.writeValueAsString(edg))
        } catch (e: IOException) {
            delete(id) // perform cleanup
            throw StorageException("Cannot save files", e)
        }

        // run netconvert
        try {
            runNetconvert(id, simulationDir, nodPath, edgPath)
        } catch (e: NetconvertException) {
            delete(id)
            throw StorageException("Cannot convert edge and node files", e)
        }
        val netPath = simulationDir.resolve("$id.net.xml").normalize().toAbsolutePath()
        assert(netPath.exists())
        return id
    }

    override fun store(simulationId: SimulationId, network: SumoNetwork) {
        TODO("modify existing simulation")
    }

    override fun load(id: SimulationId, label: String): SimulationInstance {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize())
        val cfgPath = simulationDir.resolve("$id.sumocfg").normalize().toAbsolutePath()
        if (simulationDir.exists()) {
            return SimulationInstance(label, cfgPath)
        } else {
            throw StorageSimulationNotFoundException("No such simulation with ID $id")
        }
    }

    override fun delete(id: SimulationId) {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath().toFile()
        simulationDir.listFiles()?.forEach { file -> file.delete() }
        if (!simulationDir.delete()) {
            throw StorageSimulationNotFoundException("No such simulation with ID $id")
        }
    }

    override fun info(id: SimulationId): SimulationInfo {
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