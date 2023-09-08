package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import com.fasterxml.jackson.annotation.JsonProperty

data class SumoNetwork(
    val documentName: String,
    val nodes: List<SumoNode>,
    val edges: List<SumoEdge>,
    val connections: List<SumoConnection>,
    @JsonProperty("vType")
    val vehicleType: List<SumoVehicleType>,
    val route: List<SumoRoute>,
    val flow: List<SumoFlow>
) {
    constructor(
        simulationInfo: SimulationInfo,
        nodesXml: SumoNodesXml,
        edgesXml: SumoEdgesXml,
        connectionsXml: SumoConnectionsXml,
        routesXml: SumoRoutesXml
    ) : this(
        simulationInfo.documentName,
        nodesXml.nodes,
        edgesXml.edges,
        connectionsXml.connections,
        routesXml.vehicleTypes,
        routesXml.routes,
        routesXml.flows
    )

    fun nodesXml() = SumoNodesXml(this.nodes)
    fun edgesXml() = SumoEdgesXml(this.edges)
    fun connectionsXml() = SumoConnectionsXml(this.connections)
    fun routesXml() = SumoRoutesXml(
        this.vehicleType,
        this.route,
        this.flow
    )
}
