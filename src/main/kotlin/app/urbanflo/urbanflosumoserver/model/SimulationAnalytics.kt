package app.urbanflo.urbanflosumoserver.model

import app.urbanflo.urbanflosumoserver.model.output.statistics.SumoStatisticsXml

data class SimulationAnalytics(
    val averageDuration: Double,
    val averageWaiting: Double,
    val averageTimeLoss: Double,
    val totalNumberOfCarsThatCompleted: Int,
    val simulationLength: Double,
    val sumoStatistics: SumoStatisticsXml
)
