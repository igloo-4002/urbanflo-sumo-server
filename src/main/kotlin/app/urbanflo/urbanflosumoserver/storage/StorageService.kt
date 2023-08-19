package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.SumoNetwork
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import org.springframework.core.io.Resource

interface StorageService {
    fun store(network: SumoNetwork): SimulationId

    fun store(simulationId: SimulationId, network: SumoNetwork)

    fun load(id: SimulationId, label: String): SimulationInstance

    fun delete(id: SimulationId)

    fun info(id: SimulationId): SimulationInfo

    fun listAll(): List<Resource>

    fun deleteAll()
}