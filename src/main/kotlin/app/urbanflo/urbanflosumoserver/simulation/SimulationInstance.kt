package app.urbanflo.urbanflosumoserver.simulation

import app.urbanflo.urbanflosumoserver.model.SimulationException
import app.urbanflo.urbanflosumoserver.model.VehicleData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.sumo.libtraci.Simulation
import org.eclipse.sumo.libtraci.StringVector
import org.eclipse.sumo.libtraci.Vehicle
import reactor.core.publisher.Flux
import java.net.ServerSocket
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max


typealias SimulationStep = Map<String, VehicleData>
typealias SimulationId = String

// taken from traci python code
private const val DEFAULT_NUM_RETRIES = 60
private val logger = KotlinLogging.logger {}

class SimulationInstance(
    val simulationId: SimulationId,
    val label: String,
    cfgPath: Path
) : Iterator<SimulationStep> {
    private val vehicleColors: MutableMap<String, String> = mutableMapOf()
    private val port: Int = getNextAvailablePort()
    private var frameTime = setSimulationSpeed(1)
    var flux = Flux.create<SimulationStep> { sink ->
        try {
            while (hasNext()) {
                sink.next(next())
            }
        } catch (_: UnknownError) {
            logger.warn { "Simulation $simulationId with label $label forcibly closed. Ignore if server is shutting down" }
        }
        sink.complete()
    }

    @Volatile
    private var shouldStop = false

    fun setSimulationSpeed(speed: Long): Duration = Duration.ofMillis(1000 / (60 * speed))

    init {
        try {
            logger.info { "Connecting to SUMO with port $port and label $label" }
            lock.lock()
            logger.info { "$label: lock acquired" }
            Simulation.start(
                StringVector(arrayOf("sumo", "-c", cfgPath.toString())),
                port,
                DEFAULT_NUM_RETRIES,
                label
            )

        } finally {
            logger.info { "$label: releasing lock" }
            lock.unlock()
        }
    }

    override fun hasNext(): Boolean {
        try {
            lock.lock()
            logger.info { "$label: lock acquired" }
            Simulation.switchConnection(label)
            if (shouldStop) {
                closeSimulation()
                return false
            }

            val expected = Simulation.getMinExpectedNumber() > 0
            if (!expected) {
                closeSimulation()
            }

            return expected
        } catch (e: Exception) {
            logger.error(e) { "Error in advancing simulation step" }
            throw SimulationException("Error in advancing simulation step: ${e.message}")
        } finally {
            logger.info { "$label: releasing lock" }
            lock.unlock()
        }
    }

    override fun next(): SimulationStep {
        val start = Instant.now()
        val pairs: Map<String, VehicleData>
        try {
            lock.lock()
            logger.info { "$label: lock acquired" }

            Simulation.switchConnection(label)
            Simulation.step()

            pairs = Vehicle.getIDList().associateWith { vehicleId ->
                val rawPosition = Vehicle.getPosition(vehicleId)
                val position = Simulation.convertGeo(rawPosition.x, rawPosition.y, false)
                val acceleration = Vehicle.getAcceleration(vehicleId)
                val speed = Vehicle.getSpeed(vehicleId)
                val color = getVehicleColor(vehicleId)
                val laneIndex = Vehicle.getLaneIndex(vehicleId)
                val laneId = Vehicle.getLaneID(vehicleId)
                VehicleData(
                    vehicleId,
                    Pair(position.x, position.y),
                    color,
                    acceleration,
                    speed,
                    Pair(laneIndex, laneId)
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error in advancing simulation step" }
            throw SimulationException("Error in advancing simulation step: ${e.message}")
        } finally {
            logger.info { "$label: releasing lock" }
            lock.unlock()
        }
        // we've left the critical section here, so the sleep can be done asynchronously
        val end = Instant.now()
        val delay = max(frameTime.toMillis() - Duration.between(start, end).toMillis(), 0)
        logger.info { "$label: sleeping for $delay ms" }
        Thread.sleep(delay)
        return pairs
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
        logger.info { "Closing connection with ID $simulationId and label: ${Simulation.getLabel()}" }
        Simulation.close()
    }

    companion object {
        private val lock = ReentrantLock()
        private fun getNextAvailablePort(): Int {
            // https://stackoverflow.com/questions/2675362/how-to-find-an-available-port
            val socket = ServerSocket(0)
            val socketPort = socket.localPort
            socket.close()
            return socketPort
        }
    }
}