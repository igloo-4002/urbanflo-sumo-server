package app.urbanflo.urbanflosumoserver.responses

data class SumoNetwork(
    val nodes: Map<SumoEntityId, SumoNode>,
    val edges: Map<SumoEntityId, SumoEdge>
)
