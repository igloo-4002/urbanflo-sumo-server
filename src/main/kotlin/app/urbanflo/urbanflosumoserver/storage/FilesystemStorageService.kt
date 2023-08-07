package app.urbanflo.urbanflosumoserver.storage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

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
            simulationDir = uploadsDir.resolve(Paths.get(id.toString()).normalize().toAbsolutePath())
        } while (simulationDir.exists())
        // TODO: save each file
        return id
    }

    override fun load(id: String): Map<String, Resource> {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }
}