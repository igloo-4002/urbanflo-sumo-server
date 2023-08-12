package app.urbanflo.urbanflosumoserver.storage

class StorageSimulationNotFoundException : StorageException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}