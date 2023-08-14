package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.SimulationId
import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.SumoNetwork
import org.springframework.core.io.Resource

interface StorageService {
    fun store(network: SumoNetwork): SimulationId

    fun store(simulationId: SimulationId, network: SumoNetwork)

    fun load(id: String): SimulationInstance

    fun delete(id: String)

    fun info(id: String): SimulationInfo

    fun listAll(): List<Resource>

    fun deleteAll()
}