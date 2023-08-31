package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

typealias SumoEntityId = String

@JacksonXmlRootElement(localName = "edges")
data class SumoEdgesXml(
    @field:JacksonXmlProperty(localName = "edge")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val edges: List<SumoEdge>
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.edg.xml"
    }
}
