package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
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
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
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

private val logger = KotlinLogging.logger {}

@Service
class FilesystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    private lateinit var uploadsDir: Path
    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
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

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val info = SimulationInfo(id, now, now)
        writeFiles(info, network, simulationDir)

        return info
    }

    override fun store(simulationId: SimulationId, network: SumoNetwork) {
        TODO("modify existing simulation")
    }

    private fun writeFiles(simulationInfo: SimulationInfo, network: SumoNetwork, simulationDir: Path) {
        if (!simulationDir.exists()) {
            throw IllegalStateException("simulationDir does not exist")
        }
        val simulationId = simulationInfo.id

        // save network as XML
        val nod = network.nodesXml()
        val nodPath = nod.filePath(simulationId, simulationDir)
        val edg = network.edgesXml()
        val edgPath = edg.filePath(simulationId, simulationDir)
        val con = network.connectionsXml()
        val conPath = con.filePath(simulationId, simulationDir)
        val rou = network.routesXml()
        val rouPath = rou.filePath(simulationId, simulationDir)
        try {
            nodPath.toFile().writeText(xmlMapper.writeValueAsString(nod))
            edgPath.toFile().writeText(xmlMapper.writeValueAsString(edg))
            conPath.toFile().writeText(xmlMapper.writeValueAsString(con))
            rouPath.toFile().writeText(xmlMapper.writeValueAsString(rou))
        } catch (e: JsonProcessingException) {
            logger.error(e) { "Invalid network data" }
            delete(simulationId)
            throw StorageBadRequestException("Invalid network data", e)
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            delete(simulationId) // perform cleanup
            throw StorageException("Cannot save files", e)
        }

        // run netconvert
        try {
            val netPath = runNetconvert(simulationId, simulationDir, nodPath, edgPath, conPath)
            assert(netPath.exists())
            // create sumocfg
            val sumocfg = SumoCfg(netPath, rouPath)
            val sumocfgPath = sumocfg.filePath(simulationId, simulationDir)
            sumocfgPath.toFile().writeText(xmlMapper.writeValueAsString(sumocfg))
        } catch (e: NetconvertException) {
            logger.error(e) { "Cannot convert XML files" }
            delete(simulationId)
            throw StorageException("Cannot convert XML files", e)
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            delete(simulationId) // perform cleanup
            throw StorageException("Cannot save files", e)
        }

        // save info file
        val infoPath = simulationInfo.filePath(simulationId, simulationDir)
        try {
            infoPath.toFile().writeText(jsonMapper.writeValueAsString(simulationInfo))
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            delete(simulationId) // perform cleanup
            throw StorageException("Cannot save files", e)
        }
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
        return when (id) {
            "demo" -> {
                // for demo id, just make up some timestamps
                val now = OffsetDateTime.now(ZoneOffset.UTC)
                SimulationInfo("demo", now, now)
            }

            else -> {
                val simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath()
                val infoFile = simulationDir.resolve("info.json").normalize().toAbsolutePath().toFile()
                assert(infoFile.exists())
                jsonMapper.readValue(infoFile)
            }
        }
    }

    override fun listAll(): List<Resource> {
        TODO("list all simulations")
    }

    override fun deleteAll() {
        TODO("delete all simulations")
    }
}