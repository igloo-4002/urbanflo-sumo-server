package app.urbanflo.urbanflosumoserver.responses

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "nodes")
data class SumoNodes(
    @field:JacksonXmlProperty(localName = "node")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val nodes: List<SumoNode>
)