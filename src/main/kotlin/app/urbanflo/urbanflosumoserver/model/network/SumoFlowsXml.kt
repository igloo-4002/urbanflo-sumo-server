package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

// TODO: not sure about the structure of this one
@JacksonXmlRootElement(localName = "flows")
data class SumoFlowsXml(
    @field:JacksonXmlProperty(localName = "flow")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val flows: List<SumoFlow>
)
