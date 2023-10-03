package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoPersonTeleports(
    @field:JacksonXmlProperty(isAttribute = true)
    val total: Int,
    val abortWait: Int,
    val wrongDest: Int
)
