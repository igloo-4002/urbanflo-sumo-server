package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance

interface StorageService {
    fun store(network: SumoNetwork): SimulationInfo

    fun store(simulationId: SimulationId, network: SumoNetwork)

    fun load(id: SimulationId, label: String): SimulationInstance

    fun delete(id: SimulationId)

    fun info(id: SimulationId): SimulationInfo

    fun listAll(): List<SimulationInfo>

    fun deleteAll()
}