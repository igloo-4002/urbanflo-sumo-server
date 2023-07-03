package app.urbanflo.urbanflosumoserver

import java.net.ServerSocket
import java.util.*


typealias SimulationStep = Map<String, VehiclePosition>

class SimulationInstance(val cfgPath: String = "demo.sumocfg") : Iterable<SimulationStep> {
    val port: Int = getNextAvailablePort()
    val label: String = UUID.randomUUID().toString()

    override fun iterator(): Iterator<SimulationStep> {
        return SimulationInstanceIterator(this)
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