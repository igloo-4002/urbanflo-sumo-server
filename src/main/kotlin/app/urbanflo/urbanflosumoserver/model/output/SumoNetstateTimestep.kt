package app.urbanflo.urbanflosumoserver.model.output

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoNetstateTimestep(
    val time: Double,
    @field:JacksonXmlProperty(localName = "edge")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val edges: List<SumoNetstateEdge>
)
