package app.urbanflo.urbanflosumoserver.simulation

import app.urbanflo.urbanflosumoserver.model.SimulationException
import app.urbanflo.urbanflosumoserver.model.VehicleData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.sumo.libtraci.Simulation
import org.eclipse.sumo.libtraci.StringVector
import org.eclipse.sumo.libtraci.Vehicle
import reactor.core.Disposable
import reactor.core.publisher.Flux
import java.net.ServerSocket
import java.nio.file.Path


typealias SimulationStep = Map<String, VehicleData>
typealias SimulationId = String

// taken from traci python code
private const val DEFAULT_NUM_RETRIES = 60
private val logger = KotlinLogging.logger {}

class SimulationInstance(
    val label: SimulationId,
    cfgPath: Path
) : Iterator<SimulationStep> {
    private val vehicleColors: MutableMap<String, String> = mutableMapOf()
    private val port: Int = getNextAvailablePort()
    var flux = Flux.create<SimulationStep> { sink ->
        while (hasNext()) {
            sink.next(next())
        }
        sink.complete()
    }
    @Volatile
    private var shouldStop = false


    init {
        logger.info { "Connecting to SUMO with port ${port} and label ${label}" }
        Simulation.start(
            StringVector(arrayOf("sumo", "-c", cfgPath.toString())),
            port,
            DEFAULT_NUM_RETRIES,
            label
        )
    }

    override fun hasNext(): Boolean {
        if (shouldStop) {
            closeSimulation()
            return false
        }
        logger.info { label }
        Simulation.switchConnection(label)

        val expected = Simulation.getMinExpectedNumber() > 0
        if (!expected) {
            closeSimulation()
        }

        return expected
    }

    override fun next(): SimulationStep {
        try {
            Simulation.switchConnection(label)
            Simulation.step()

            val pairs = Vehicle.getIDList().map { vehicleId ->
                val position = Vehicle.getPosition(vehicleId)
                val acceleration = Vehicle.getAcceleration(vehicleId)
                val speed = Vehicle.getSpeed(vehicleId)
                // TODO: sumo has color functionality built in, check how that works
                val color = getVehicleColor(vehicleId)
                val laneIndex = Vehicle.getLaneIndex(vehicleId)
                val laneId = Vehicle.getLaneID(vehicleId)
                vehicleId to VehicleData(
                    vehicleId,
                    Pair(position.x, position.y),
                    color,
                    acceleration,
                    speed,
                    Pair(laneIndex, laneId)
                )
            }

            return pairs.toMap()
        } catch (e: Exception) {
            logger.error(e) { "Error in advancing simulation step" }
            throw SimulationException("Error in advancing simulation step: ${e.message}")
        }
    }

    private fun getVehicleColor(vehicleId: String): String {
        // if let color = vehicleColors[vehicleId] { return color } else { assign random colour to vehicle and return }
        val color = vehicleColors[vehicleId] ?: run {
            val newColor = "#ffff00"
            vehicleColors[vehicleId] = newColor
            newColor
        }
        return color
    }

    fun stopSimulation() {
        shouldStop = true
    }

    private fun closeSimulation() {
        logger.info { "Closing connection with label: ${Simulation.getLabel()}" }
        Simulation.close()
    }

    companion object {
        private fun getNextAvailablePort(): Int {
            // https://stackoverflow.com/questions/2675362/how-to-find-an-available-port
            val socket = ServerSocket(0)
            val socketPort = socket.localPort
            socket.close()
            return socketPort
        }
    }
}