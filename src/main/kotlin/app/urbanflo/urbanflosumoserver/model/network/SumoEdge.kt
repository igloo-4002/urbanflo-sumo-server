package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

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
    val speed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val width: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val name: String?,
    @field:JacksonXmlProperty(isAttribute = true)
    val spreadType: String?,
) {
    init {
        spreadType?.let {
            require(it.lowercase() in listOf("right", "center", "roadCenter")) {
                "Invalid value for spreadType: $it. Allowed values are 'right', 'center' and 'roadCenter'"
            }
        }
    }
}