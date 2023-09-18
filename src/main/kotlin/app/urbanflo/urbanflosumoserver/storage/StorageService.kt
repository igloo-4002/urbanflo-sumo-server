package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.SimulationAnalytics
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.model.output.SumoSimulationOutput
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance

interface StorageService {
    fun store(network: SumoNetwork): SimulationInfo

    fun store(simulationId: SimulationId, network: SumoNetwork): SimulationInfo

    fun load(id: SimulationId, label: String): SimulationInstance

    fun delete(id: SimulationId)

    fun info(id: SimulationId): SimulationInfo

    fun export(simulationId: SimulationId): SumoNetwork

    fun listAll(): List<SimulationInfo>

    fun getSimulationOutput(simulationId: SimulationId): SumoSimulationOutput

    fun deleteSimulationOutput(simulationId: SimulationId)

    fun getSimulationAnalytics(simulationId: SimulationId): SimulationAnalytics
}