package app.urbanflo.urbanflosumoserver.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "edge")
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