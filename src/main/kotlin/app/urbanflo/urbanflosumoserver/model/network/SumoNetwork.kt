package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.annotation.JsonProperty

data class SumoNetwork(
    val nodes: Map<SumoEntityId, SumoNode>,
    val edges: Map<SumoEntityId, SumoEdge>,
    val connections: Map<SumoEntityId, SumoConnection>,
    @JsonProperty("vType")
    val vehicleType: Map<SumoEntityId, SumoVehicleType>,
    val route: Map<SumoEntityId, SumoRoute>,
    val flow: Map<SumoEntityId, SumoFlow>
) {
    fun nodesXml() = SumoNodesXml(this.nodes.values.toList())
    fun edgesXml() = SumoEdgesXml(this.edges.values.toList())
    fun connectionsXml() = SumoConnectionsXml(this.connections.values.toList())
    fun routesXml() = SumoRoutesXml(
        this.vehicleType.values.toList(),
        this.route.values.toList(),
        this.flow.values.toList()
    )
}
