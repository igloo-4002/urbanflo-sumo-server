package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.responses.SimulationInfo
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface StorageService {
    fun store(network: SumoNetwork): SimulationId

    fun store(simulationId: SimulationId, network: SumoNetwork)

    fun load(id: SimulationId, label: String): SimulationInstance

    fun delete(id: SimulationId)

    fun info(id: SimulationId): SimulationInfo

    fun listAll(): List<Resource>

    fun deleteAll()
}