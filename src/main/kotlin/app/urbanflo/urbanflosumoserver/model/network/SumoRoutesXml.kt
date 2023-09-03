package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path


@JacksonXmlRootElement(localName = "routes")
data class SumoRoutesXml(
    @field:JacksonXmlProperty(localName = "vType")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val vehicleTypes: List<SumoVehicleType> = listOf(),
    @field:JacksonXmlProperty(localName = "route")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val routes: List<SumoRoute> = listOf(),
    @field:JacksonXmlProperty(localName = "flow")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val flows: List<SumoFlow> = listOf()
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.rou.xml"
    }
}
