package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface StorageService {
    fun store(files: Array<MultipartFile>): UUID

    fun load(id: SimulationId, label: String): SimulationInstance

    fun delete(id: SimulationId)

    fun info(id: SimulationId): SimulationInfo

    fun listAll(): List<Resource>

    fun deleteAll()
}