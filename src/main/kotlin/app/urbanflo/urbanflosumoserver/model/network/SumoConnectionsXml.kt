package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "connections")
data class SumoConnectionsXml(
    @field:JacksonXmlProperty(localName = "connection")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val connections: List<SumoConnection>
)
