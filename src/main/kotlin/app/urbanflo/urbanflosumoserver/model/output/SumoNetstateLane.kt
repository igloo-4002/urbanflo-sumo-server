package app.urbanflo.urbanflosumoserver.model.output

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoNetstateLane(
    val id: String,
    @field:JacksonXmlProperty(localName = "vehicle")
    val vehicles: List<SumoNetstateVehicle> = listOf()
)
