package app.urbanflo.urbanflosumoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.sumo.libtraci.Simulation
import org.eclipse.sumo.libtraci.StringVector
import org.eclipse.sumo.libtraci.Vehicle
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

// taken from traci python code
private const val DEFAULT_NUM_RETRIES = 60

class SimulationInstanceIterator
    (private val simulationInstance: SimulationInstance) : Iterator<SimulationStep> {
    private val vehicleColors: MutableMap<String, String> = mutableMapOf()


    init {
        System.loadLibrary("libtracijni")
        logger.info { "Connecting to SUMO with port ${simulationInstance.port} and label ${simulationInstance.label}" }
        Simulation.start(
            StringVector(arrayOf("sumo", "-c", simulationInstance.cfgPath)),
            simulationInstance.port,
            DEFAULT_NUM_RETRIES,
            simulationInstance.label
        )
    }

    override fun hasNext(): Boolean {
        Simulation.switchConnection(simulationInstance.label)
        return if (Simulation.getMinExpectedNumber() > 0) {
            true
        } else {
            logger.info { "Closing connection with label: ${Simulation.getLabel()}" }
            Simulation.close()
            false
        }
    }

    override fun next(): SimulationStep {
        Simulation.switchConnection(simulationInstance.label)
        Simulation.step()
        val pairs = Vehicle.getIDList().map { vehicleId ->
            val position = Vehicle.getPosition(vehicleId)
            val color = getVehicleColor(vehicleId)
            vehicleId to VehiclePosition(position.x, position.y, color)
        }
        return pairs.toMap()
    }

    private fun getVehicleColor(vehicleId: String): String {
        // if let color = vehicleColors[vehicleId] { return color } else { assign random colour to vehicle and return }
        val color = vehicleColors[vehicleId] ?: run {
            val newColor = randomColor()
            vehicleColors[vehicleId] = randomColor()
            newColor
        }
        return color
    }

    private fun randomColor(): String {
        val randBytes = Random.Default.nextBytes(3)
        return "#${"%02X".format(randBytes[0])}${"%02X".format(randBytes[1])}${"%02X".format(randBytes[2])}"
    }
}