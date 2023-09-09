package app.urbanflo.urbanflosumoserver.model.output

data class SumoSimulationOutput(
    val tripInfo: List<SumoTripInfo>,
    val netstate: List<SumoNetstateTimestep>
)
