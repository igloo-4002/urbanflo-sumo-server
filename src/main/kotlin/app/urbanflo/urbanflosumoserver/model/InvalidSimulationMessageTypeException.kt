package app.urbanflo.urbanflosumoserver.model

class InvalidSimulationMessageTypeException(status: String) : RuntimeException("Invalid status: $status")