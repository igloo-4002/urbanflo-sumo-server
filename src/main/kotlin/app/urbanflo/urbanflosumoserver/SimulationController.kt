package app.urbanflo.urbanflosumoserver

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import reactor.core.publisher.Flux

@Controller
class SimulationController {
    @GetMapping("/start-simulation", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @CrossOrigin(origins = ["http://localhost:5173"])
    @ResponseBody
    fun startSimulation(): Flux<SimulationStep> {
        val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"
        return Flux.fromIterable(SimulationInstance(cfgPath))
    }
}