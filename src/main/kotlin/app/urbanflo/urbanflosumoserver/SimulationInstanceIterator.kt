package app.urbanflo.urbanflosumoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.sumo.libtraci.Simulation
import org.eclipse.sumo.libtraci.StringVector
import org.eclipse.sumo.libtraci.Vehicle
import kotlin.random.Random
import kotlin.random.nextInt

private val logger = KotlinLogging.logger {}

// taken from traci python code
private const val DEFAULT_NUM_RETRIES = 60

class SimulationInstanceIterator
    (private val simulationInstance: SimulationInstance) : Iterator<SimulationStep> {
    private val vehicleColors: MutableMap<String, String> = mutableMapOf()


    init {
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

    companion object {
        private val allowedVehicleColors = arrayOf(
            "#FF0000",
            "#00FF00",
            "#0000FF",
            "#FFFF00",
            "#FF00FF",
            "#00FFFF",
            "#FF8000",
            "#FF0080",
            "#80FF00",
            "#00FF80",
            "#0080FF",
            "#8000FF",
            "#FFC000",
            "#FF00C0",
            "#C0FF00",
            "#00FFC0",
            "#00C0FF",
            "#C000FF",
            "#FF4000",
            "#FF0040",
            "#40FF00",
            "#00FF40",
            "#0040FF",
            "#4000FF",
            "#FFA000",
            "#FF00A0",
            "#A0FF00",
            "#00FFA0",
            "#00A0FF",
            "#A000FF",
            "#FF2000",
            "#FF0020",
            "#20FF00",
            "#00FF20",
            "#0020FF",
            "#2000FF",
            "#FF9000",
            "#FF0090",
            "#90FF00",
            "#00FF90",
            "#0090FF",
            "#9000FF",
            "#FF6000",
            "#FF0060",
            "#60FF00",
            "#00FF60",
            "#0060FF",
            "#6000FF",
            "#FFD000",
            "#FF00D0",
            "#D0FF00",
            "#00FFD0",
            "#00D0FF",
            "#D000FF",
            "#FF1000",
            "#FF0010",
            "#10FF00",
            "#00FF10",
            "#0010FF",
            "#1000FF",
            "#FFB000",
            "#FF00B0",
            "#B0FF00",
            "#00FFB0",
            "#00B0FF",
            "#B000FF",
            "#FF3000",
            "#FF0030",
            "#30FF00",
            "#00FF30",
            "#0030FF",
            "#3000FF",
            "#FF8000",
            "#FF0080",
            "#80FF00",
            "#00FF80",
            "#0080FF",
            "#8000FF",
            "#FF5000",
            "#FF0050",
            "#50FF00",
            "#00FF50",
            "#0050FF",
            "#5000FF",
            "#FFC000",
            "#FF00C0",
            "#C0FF00",
            "#00FFC0",
            "#00C0FF",
            "#C000FF",
            "#FF7000",
            "#FF0070",
            "#70FF00",
            "#00FF70",
            "#0070FF",
            "#7000FF",
            "#FFE000",
            "#FF00E0",
            "#E0FF00",
            "#00FFE0",
            "#00E0FF",
            "#E000FF",
            "#FF9000",
            "#FF0090",
            "#90FF00",
            "#00FF90",
            "#0090FF",
            "#9000FF",
            "#FF4000",
            "#FF0040",
            "#40FF00",
            "#00FF40",
            "#0040FF",
            "#4000FF",
        )
        @OptIn(ExperimentalStdlibApi::class)
        private fun randomColor(): String {
            val index = Random.Default.nextInt(0..<allowedVehicleColors.count())
            return allowedVehicleColors[index]
        }
    }
}