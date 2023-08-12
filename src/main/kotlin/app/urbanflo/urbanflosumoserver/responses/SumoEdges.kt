package app.urbanflo.urbanflosumoserver.responses

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "edges")
data class SumoEdges(
    @field:JacksonXmlProperty(localName = "edge")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val edges: List<SumoEdge>
)
