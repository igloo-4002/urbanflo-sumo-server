package app.urbanflo.urbanflosumoserver

import org.eclipse.sumo.libtraci.Simulation
import org.eclipse.sumo.libtraci.StringVector
import org.eclipse.sumo.libtraci.Vehicle
import kotlin.random.Random

class SimulationInstanceIterator
    (simulationInstance: SimulationInstance) : Iterator<SimulationStep> {
    private val vehicleColors: MutableMap<String, String> = mutableMapOf()

    init {
        System.loadLibrary("libtracijni")
        Simulation.start(StringVector(arrayOf("sumo", "-c", simulationInstance.cfgPath)))
    }

    override fun hasNext(): Boolean {
        return Simulation.getMinExpectedNumber() > 0
    }

    override fun next(): SimulationStep {
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