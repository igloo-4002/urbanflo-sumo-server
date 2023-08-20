package app.urbanflo.urbanflosumoserver.model

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import java.nio.file.Path

interface SumoXml {
    fun fileName(simulationId: SimulationId): String
    fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
        simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()
}