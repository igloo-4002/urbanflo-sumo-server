package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoVehiclesStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val loaded: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val inserted: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val running: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val waiting: Int
)
