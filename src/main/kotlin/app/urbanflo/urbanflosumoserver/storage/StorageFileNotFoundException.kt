package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.storage.StorageException

class StorageFileNotFoundException : StorageException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}