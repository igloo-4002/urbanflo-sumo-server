package app.urbanflo.urbanflosumoserver.model

data class SumoNetwork(
    val nodes: Map<SumoEntityId, SumoNode>,
    val edges: Map<SumoEntityId, SumoEdge>
) {
    fun nodesXml(): SumoNodes = SumoNodes(this.nodes.values.toList())
    fun edgesXml(): SumoEdges = SumoEdges(this.edges.values.toList())
}
