package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoNodesXml]
 */
data class SumoNode(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val x: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val y: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val type: String
) {
    init {
        type.let {
            require(
                it.lowercase() in listOf(
                    "priority",
                    "traffic_light",
                    "right_before_left",
                    "left_before_right",
                    "unregulated",
                    "priority_stop",
                    "traffic_light_unregulated",
                    "allway_stop",
                    "rail_signal",
                    "zipper",
                    "traffic_light_right_on_red",
                    "rail_crossing",
                    "dead_end"
                )
            ) {
                "Invalid value for node type: $it."
            }
        }
    }
}
