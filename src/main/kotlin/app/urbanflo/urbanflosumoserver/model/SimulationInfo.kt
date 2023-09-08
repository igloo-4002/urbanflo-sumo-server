package app.urbanflo.urbanflosumoserver.model

import java.nio.file.Path
import java.time.OffsetDateTime

data class SimulationInfo(
    val id: String,
    val documentName: String,
    val createdAt: OffsetDateTime,
    val lastModifiedAt: OffsetDateTime
) {
    companion object {
        fun filePath(simulationDir: Path): Path =
            simulationDir.resolve(fileName()).normalize().toAbsolutePath()

        fun fileName() = "info.json"
    }
}
