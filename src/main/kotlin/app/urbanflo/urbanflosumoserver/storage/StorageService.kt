package app.urbanflo.urbanflosumoserver.storage

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface StorageService {
    fun store(file: MultipartFile): UUID

    fun load(id: String): Map<String, Resource>

    fun deleteAll()
}