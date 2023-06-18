package app.urbanflo.urbanflosumoserver


typealias SimulationStep = Map<String, VehiclePosition>

class SimulationInstance(val cfgPath: String = "demo.sumocfg") : Iterable<SimulationStep> {
    override fun iterator(): Iterator<SimulationStep> {
        return SimulationInstanceIterator(this)
    }
}