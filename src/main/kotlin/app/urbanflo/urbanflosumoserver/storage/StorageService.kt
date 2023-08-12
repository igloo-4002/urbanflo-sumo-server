package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.SimulationId
import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.responses.SimulationInfo
import app.urbanflo.urbanflosumoserver.responses.SumoNetwork
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface StorageService {
    fun store(network: SumoNetwork): SimulationId

    fun store(simulationId: SimulationId, network: SumoNetwork)

    fun load(id: String): SimulationInstance

    fun delete(id: String)

    fun info(id: String): SimulationInfo

    fun listAll(): List<Resource>

    fun deleteAll()
}