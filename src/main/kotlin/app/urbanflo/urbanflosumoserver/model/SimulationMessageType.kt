package app.urbanflo.urbanflosumoserver.model

enum class SimulationMessageType {
    START, STOP
}

data class SimulationMessageRequest(var status: SimulationMessageType)