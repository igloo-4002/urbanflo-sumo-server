package app.urbanflo.urbanflosumoserver.storage

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
    private val uploadsDir: Path

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
                    throw StorageException("Cannot store file outside uploads directory")
                }
                val inputStream = file.inputStream
                try {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    throw StorageException("Cannot store simulation file", e)
                }
            } else {
                throw StorageException("file name is not present or null")
            }
        }
        return id
    }

    override fun load(id: String): Map<String, Resource> {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }
}