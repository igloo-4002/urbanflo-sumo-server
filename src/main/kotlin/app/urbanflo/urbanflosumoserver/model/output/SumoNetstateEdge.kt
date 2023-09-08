package app.urbanflo.urbanflosumoserver.model.output

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoNetstateEdge(
    val id: String,
    @field:JacksonXmlProperty(localName = "lane")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val lanes: List<SumoNetstateLane>
)
