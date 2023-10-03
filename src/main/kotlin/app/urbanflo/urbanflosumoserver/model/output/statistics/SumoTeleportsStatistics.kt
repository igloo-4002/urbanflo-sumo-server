package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoTeleportsStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val total: Int,
    val jam: Int,
    val yield: Int,
    val wrongLane: Int
)