package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

private val logger = KotlinLogging.logger {}

@Service
class FilesystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    private lateinit var uploadsDir: Path

    init {
        this.uploadsDir = Paths.get(properties.location)
        try {
            Files.createDirectories(uploadsDir)
        } catch (e: IOException) {
            throw StorageException("Cannot create uploads directory", e)
        }
    }

    override fun store(files: Array<MultipartFile>): UUID {
        var id: UUID
        var simulationDir: Path
        do {
            id = UUID.randomUUID()
            simulationDir = uploadsDir.resolve(Paths.get(id.toString()).normalize()).toAbsolutePath()
        } while (simulationDir.exists())
        logger.info {"Saving file at $simulationDir"}
        simulationDir.createDirectory()
        // save each file
        logger
        files.forEach { file ->
            val filename = file.originalFilename
            if (filename != null) {
                val filePath = simulationDir.resolve(Paths.get(filename).normalize())
                // security check to prevent path traversal attack
                if (filePath.parent != simulationDir) {
                    delete(id.toString())
                    throw StorageBadRequestException("Invalid file name: $filename")
                }
                val inputStream = file.inputStream
                try {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    delete(id.toString())
                    throw StorageException("Cannot store simulation file", e)
                }
            } else {
                delete(id.toString())
                throw StorageBadRequestException("file name is not present or null")
            }
        }
        return id
    }

    override fun load(id: String): SimulationInstance {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize())
        val cfgPath = simulationDir.resolve("$id.sumocfg").normalize().toAbsolutePath()
        if (simulationDir.exists()) {
            return SimulationInstance(id, cfgPath)
        } else {
            throw StorageSimulationNotFoundException("No such simulation with ID $id")
        }
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
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }
}