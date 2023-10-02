package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoSafetyStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val collisions: Int,
    val emergencyStops: Int,
    val emergencyBraking: Int,
)
