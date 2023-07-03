package app.urbanflo.urbanflosumoserver

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Flux

@Controller
class SimulationController {
    @GetMapping("/start-simulation", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun startSimulation(): Flux<SimulationStep> {
        val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"
        return Flux.fromIterable(SimulationInstance(cfgPath))
    }
}