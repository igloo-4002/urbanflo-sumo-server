package app.urbanflo.urbanflosumoserver.responses



data class SumoNode(
    val id: SumoEntityId,
    val x: Double,
    val y: Double,
    val type: String
)
