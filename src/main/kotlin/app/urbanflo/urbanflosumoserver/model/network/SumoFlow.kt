package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoFlow(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val type: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val route: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val begin: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val end: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val vehsPerHour: Double
)
