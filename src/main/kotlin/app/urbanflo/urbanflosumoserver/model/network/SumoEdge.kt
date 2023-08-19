package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoEdge(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val from: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val to: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val priority: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val numLanes: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val speed: Double
)