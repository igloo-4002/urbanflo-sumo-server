package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.*
import app.urbanflo.urbanflosumoserver.model.output.SumoSimulationOutput
import app.urbanflo.urbanflosumoserver.model.output.SumoTripInfo
import app.urbanflo.urbanflosumoserver.model.output.SumoTripInfoXml
import app.urbanflo.urbanflosumoserver.model.sumocfg.SumoCfg
import app.urbanflo.urbanflosumoserver.netconvert.NetconvertException
import app.urbanflo.urbanflosumoserver.netconvert.runNetconvert
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

private val logger = KotlinLogging.logger {}

@Service
class FilesystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    private lateinit var uploadsDir: Path
    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
        this.uploadsDir = Paths.get(properties.location)
        try {
            Files.createDirectories(uploadsDir)
        } catch (e: IOException) {
            throw StorageException("Cannot create uploads directory", e)
        }
    }

    override fun store(network: SumoNetwork): SimulationInfo {
        var id: SimulationId
        var simulationDir: Path
        do {
            id = UUID.randomUUID().toString()
            simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath()
        } while (simulationDir.exists())
        simulationDir.createDirectory()

        val now = currentTime()
        val info = SimulationInfo(id, now, now)
        try {
            writeFiles(info, network, simulationDir)
        } catch (e: Exception) {
            delete(id) // perform cleanup
            throw e
        }

        return info
    }

    override fun store(simulationId: SimulationId, network: SumoNetwork): SimulationInfo {
        val now = currentTime()
        val info = this.info(simulationId)
        assert(info.id == simulationId)
        val newInfo = SimulationInfo(info.id, info.createdAt, now)
        val simulationDir = getSimulationDir(simulationId)
        writeFiles(newInfo, network, simulationDir)
        return newInfo
    }

    private fun writeFiles(simulationInfo: SimulationInfo, network: SumoNetwork, simulationDir: Path) {
        if (!simulationDir.exists()) {
            throw IllegalStateException("simulationDir does not exist")
        }
        val simulationId = simulationInfo.id

        // save network as XML
        val nod = network.nodesXml()
        val nodPath = SumoNodesXml.filePath(simulationId, simulationDir)
        val edg = network.edgesXml()
        val edgPath = SumoEdgesXml.filePath(simulationId, simulationDir)
        val con = network.connectionsXml()
        val conPath = SumoConnectionsXml.filePath(simulationId, simulationDir)
        val rou = network.routesXml()
        val rouPath = SumoRoutesXml.filePath(simulationId, simulationDir)
        try {
            nodPath.toFile().writeText(xmlMapper.writeValueAsString(nod))
            edgPath.toFile().writeText(xmlMapper.writeValueAsString(edg))
            conPath.toFile().writeText(xmlMapper.writeValueAsString(con))
            rouPath.toFile().writeText(xmlMapper.writeValueAsString(rou))
        } catch (e: JsonProcessingException) {
            logger.error(e) { "Invalid network data" }
            throw StorageBadRequestException("Invalid network data", e)
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            throw StorageException("Cannot save files", e)
        }

        // run netconvert
        try {
            val netPath = runNetconvert(simulationId, simulationDir, nodPath, edgPath, conPath)
            assert(netPath.exists())
            // create sumocfg
            val sumocfg = SumoCfg(simulationId, netPath, rouPath)
            val sumocfgPath = SumoCfg.filePath(simulationId, simulationDir)
            sumocfgPath.toFile().writeText(xmlMapper.writeValueAsString(sumocfg))
        } catch (e: NetconvertException) {
            logger.error(e) { "Cannot convert XML files" }
            throw StorageException("Cannot convert XML files", e)
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            throw StorageException("Cannot save files", e)
        }

        // save info file
        val infoPath = SimulationInfo.filePath(simulationDir)
        try {
            infoPath.toFile().writeText(jsonMapper.writeValueAsString(simulationInfo))
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            throw StorageException("Cannot save files", e)
        }
    }

    override fun load(id: SimulationId, label: String): SimulationInstance {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize())
        val cfgPath = simulationDir.resolve("$id.sumocfg").normalize().toAbsolutePath()
        if (simulationDir.exists()) {
            return SimulationInstance(id, label, cfgPath)
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
        if (!simulationDir.exists()) {
            throw StorageSimulationNotFoundException("No such simulation with ID $id")
        }
        val infoFile = simulationDir.resolve("info.json").normalize().toAbsolutePath().toFile()
        assert(infoFile.exists())
        return jsonMapper.readValue(infoFile)
    }

    override fun export(simulationId: SimulationId): SumoNetwork {
        val simulationDir = getSimulationDir(simulationId)
        if (simulationDir.exists()) {
            try {
                val nodPath = SumoNodesXml.filePath(simulationId, simulationDir)
                val edgPath = SumoEdgesXml.filePath(simulationId, simulationDir)
                val conPath = SumoConnectionsXml.filePath(simulationId, simulationDir)
                val rouPath = SumoRoutesXml.filePath(simulationId, simulationDir)
                val nod: SumoNodesXml = xmlMapper.readValue(nodPath.toFile())
                val edg: SumoEdgesXml = xmlMapper.readValue(edgPath.toFile())
                val con: SumoConnectionsXml = xmlMapper.readValue(conPath.toFile())
                val rou: SumoRoutesXml = xmlMapper.readValue(rouPath.toFile())
                return SumoNetwork(nod, edg, con, rou)
            } catch (e: IOException) {
                logger.error(e) { "Cannot export network" }
                throw StorageException("Cannot export network", e)
            }
        } else {
            throw StorageSimulationNotFoundException("No such simulation with ID $simulationId")
        }
    }

    override fun listAll(): List<SimulationInfo> {
        val simulationDirs = uploadsDir.listDirectoryEntries()
            return simulationDirs.map<Path, SimulationInfo> { simulation ->
                val infoFile = simulation.resolve("info.json").normalize().toAbsolutePath().toFile()
                jsonMapper.readValue(infoFile)
            }.sortedByDescending { it.lastModifiedAt }
    }

    override fun getSimulationOutput(simulationId: SimulationId): SumoSimulationOutput {
        val tripInfoPath = SumoTripInfo.filePath(simulationId, getSimulationDir(simulationId))
        return try {
            val tripInfo: SumoTripInfoXml = xmlMapper.readValue(tripInfoPath.toFile())
            SumoSimulationOutput(tripInfo.tripInfos)
        } catch (e: IOException) {
            logger.error(e) { "Cannot read simulation output. Either simulation hasn't started or simulation wasn't closed properly" }
            throw StorageSimulationNotFoundException("Cannot read simulation output. Either simulation hasn't started or simulation wasn't closed properly")
        }
    }

    override fun deleteSimulationOutput(simulationId: SimulationId) {
        val tripInfoFile = SumoTripInfo.filePath(simulationId, getSimulationDir(simulationId)).toFile()
        tripInfoFile.delete()
    }

    private fun currentTime() = OffsetDateTime.now(ZoneOffset.UTC)

    private fun getSimulationDir(simulationId: SimulationId) = uploadsDir.resolve(Paths.get(simulationId).normalize())
}