package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoXml
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "nodes")
data class SumoNodesXml(
    @field:JacksonXmlProperty(localName = "node")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val nodes: List<SumoNode>
) : SumoXml {
    override fun fileName(simulationId: SimulationId) = "$simulationId.nod.xml"
}