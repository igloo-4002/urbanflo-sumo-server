package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoVehicleType(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val accel: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val decel: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val sigma: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val length: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val minGap: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val maxSpeed: Double
)
