package app.urbanflo.urbanflosumoserver.model

data class ErrorResponse(val error: String, val errorFields: Map<String, String?>? = null)