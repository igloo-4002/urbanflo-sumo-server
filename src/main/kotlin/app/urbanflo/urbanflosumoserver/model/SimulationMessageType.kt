package app.urbanflo.urbanflosumoserver.model

enum class SimulationMessageType {
    SUBSCRIBE, UNSUBSCRIBE
}

data class SimulationMessageRequest(var status: SimulationMessageType)