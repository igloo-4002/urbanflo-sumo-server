package app.urbanflo.urbanflosumoserver.model.output.statistics

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path


@JacksonXmlRootElement(localName = "statistics")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoStatisticsXml(
    val performance: SumoPerformanceStatistics
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.statistics.xml"
    }
}