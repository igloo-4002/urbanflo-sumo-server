package app.urbanflo.urbanflosumoserver.responses


data class SumoEdge(
    val id: SumoEntityId,
    val from: SumoEntityId,
    val to: SumoEntityId,
    val priority: Int,
    val numLanes: Int,
    val speed: Double
)