package app.urbanflo.urbanflosumoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.sumo.libsumo.Simulation
import org.eclipse.sumo.libsumo.StringVector
import org.eclipse.sumo.libsumo.Vehicle

private val logger = KotlinLogging.logger {}

fun main() {
    System.loadLibrary("libsumojni")
    val sumoCmd = arrayOf("sumo", "-c", "demo/demo.sumocfg")
    logger.info { "Starting SUMO with commandline: ${sumoCmd.joinToString(" ")}" }
    try {
        Simulation.start(StringVector(sumoCmd))

        while (Simulation.getMinExpectedNumber() > 0) {
            Simulation.step()
            val activeVehicles = Vehicle.getIDList()

            activeVehicles.forEach { vehicleId ->
                val position = Vehicle.getPosition(vehicleId)
                logger.info { "$vehicleId: (${position.x}, ${position.y})" }
            }
        }
        logger.info {"Simulation finished"}
    } catch (e: Exception) {
        logger.error(e) { "Error while running simulation" }
    }
}