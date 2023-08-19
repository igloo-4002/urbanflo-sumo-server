package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "routes")
data class SumoRoutesXml(
    @field:JacksonXmlProperty(localName = "route")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val routes: List<SumoRoute>
)
