package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoRoute(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val edges: SumoEntityId
)
