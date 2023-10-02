package app.urbanflo.urbanflosumoserver.model.output.statistics

data class SumoPersonsStatitsics(
    val loaded: Int,
    val running: Int,
    val jammed: Int
)