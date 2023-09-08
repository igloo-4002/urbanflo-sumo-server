package app.urbanflo.urbanflosumoserver.model.output

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoNetstateLane(
    val id: String,
    @field:JacksonXmlProperty(localName = "vehicle")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val vehicles: List<SumoNetstateVehicle>
)
