package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.responses.NewSimulationResponse
import app.urbanflo.urbanflosumoserver.storage.StorageService
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import java.util.UUID

@Controller
class SimulationController(private val storageService: StorageService) {
    @GetMapping("/start-simulation", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun startSimulation(): Flux<SimulationStep> {
        val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"
        return Flux.fromIterable(SimulationInstance(cfgPath))
    }

    @PostMapping("/simulation/new")
    @ResponseBody
    fun newSimulation(@RequestBody files: Array<MultipartFile>): NewSimulationResponse {
        return NewSimulationResponse(UUID.randomUUID().toString())
    }
}