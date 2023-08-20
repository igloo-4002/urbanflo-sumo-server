package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

typealias SumoEntityId = String

@JacksonXmlRootElement(localName = "edges")
data class SumoEdgesXml(
    @field:JacksonXmlProperty(localName = "edge")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val edges: List<SumoEdge>
) : SumoXml {
    override fun fileName(simulationId: SimulationId) = "$simulationId.edg.xml"
}
