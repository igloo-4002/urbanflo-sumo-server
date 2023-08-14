package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface StorageService {
    fun store(files: Array<MultipartFile>): UUID

    fun load(id: String): SimulationInstance

    fun delete(id: String)

    fun info(id: String): SimulationInfo

    fun listAll(): List<Resource>

    fun deleteAll()
}