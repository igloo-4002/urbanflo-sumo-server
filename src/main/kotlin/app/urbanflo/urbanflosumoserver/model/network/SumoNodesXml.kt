package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "nodes")
data class SumoNodesXml(
    @field:JacksonXmlProperty(localName = "node")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val nodes: List<SumoNode>
)