package app.urbanflo.urbanflosumoserver.model

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import java.time.OffsetDateTime

data class SimulationInfo(
    val id: String,
    val createdAt: OffsetDateTime,
    val lastModifiedAt: OffsetDateTime
): SumoXml {
    override fun fileName(simulationId: SimulationId) = "info.json"
}
