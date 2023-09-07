package app.urbanflo.urbanflosumoserver.model.output

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.nio.file.Path

@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoTripInfo(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val depart: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departLane: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val departPos: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departSpeed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departDelay: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrival: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrivalLane: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrivalPos: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrivalSpeed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val duration: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val routeLength: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val waitingTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val waitingCount: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val stopTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val timeLoss: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val rerouteNo: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val devices: String,
    @field:JacksonXmlProperty(isAttribute = true, localName = "vType")
    val vehicleType: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val speedFactor: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val vaporized: Boolean?
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.tripinfo.xml"
    }
}
