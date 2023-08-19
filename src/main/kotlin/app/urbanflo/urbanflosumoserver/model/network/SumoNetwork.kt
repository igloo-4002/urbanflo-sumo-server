package app.urbanflo.urbanflosumoserver.model.network

data class SumoNetwork(
    val nodes: Map<SumoEntityId, SumoNode>,
    val edges: Map<SumoEntityId, SumoEdge>,
    val connections: Map<SumoEntityId, SumoConnection>,
    val vType: Map<SumoEntityId, SumoVehicleType>,
    val route: Map<SumoEntityId, SumoRoute>,
    val flow: Map<SumoEntityId, SumoFlow>
) {
    fun nodesXml() = SumoNodesXml(this.nodes.values.toList())
    fun edgesXml() = SumoEdgesXml(this.edges.values.toList())
    fun connectionsXml() = SumoConnectionsXml(this.connections.values.toList())
    fun vTypesXml() = SumoVehicleTypesXml(this.vType.values.toList())
    fun routesXml() = SumoRoutesXml(this.route.values.toList())
    fun flowsXml() = SumoFlowsXml(this.flow.values.toList())
}
