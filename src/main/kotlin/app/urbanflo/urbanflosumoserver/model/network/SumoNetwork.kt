package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class SumoNetwork(
    @field:NotBlank(message = "Document name cannot be blank")
    val documentName: String,
    @field:NotEmpty(message = "Nodes must not be empty")
    val nodes: List<SumoNode>,
    @field:NotEmpty(message = "Edges must not be empty")
    @field:Valid
    val edges: List<SumoEdge>,
    @field:NotEmpty(message = "Connections must not be empty")
    val connections: List<SumoConnection>,
    @field:JsonProperty("vType")
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
