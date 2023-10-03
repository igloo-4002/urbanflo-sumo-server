package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoRideStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val number: Int
)
