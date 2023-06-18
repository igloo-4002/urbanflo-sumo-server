package app.urbanflo.urbanflosumoserver

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime

@Controller
class SimulationController {
    @GetMapping("/start-simulation", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun startSimulation(): Flux<String> {
        val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"

        return Flux.interval(Duration.ofSeconds(1)).map { seq ->
            "Current time: ${LocalDateTime.now()}"
        }
    }
}