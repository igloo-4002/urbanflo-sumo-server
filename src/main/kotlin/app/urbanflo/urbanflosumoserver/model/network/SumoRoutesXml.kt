package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoXml
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "routes")
data class SumoRoutesXml(
    @field:JacksonXmlProperty(localName = "vType")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val vehicleTypes: List<SumoVehicleType>,
    @field:JacksonXmlProperty(localName = "route")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val routes: List<SumoRoute>,
    @field:JacksonXmlProperty(localName = "flow")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val flows: List<SumoFlow>
) : SumoXml {
    override fun fileName(simulationId: SimulationId) = "$simulationId.rou.xml"
}
