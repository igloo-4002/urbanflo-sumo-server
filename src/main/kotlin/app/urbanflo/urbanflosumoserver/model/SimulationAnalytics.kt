package app.urbanflo.urbanflosumoserver.model

import app.urbanflo.urbanflosumoserver.model.output.statistics.SumoPerformanceStatistics

data class SimulationAnalytics(
    val averageDuration: Double,
    val averageWaiting: Double,
    val averageTimeLoss: Double,
    val totalNumberOfCarsThatCompleted: Int,
    val simulationLength: Double,
    val performance: SumoPerformanceStatistics
)
