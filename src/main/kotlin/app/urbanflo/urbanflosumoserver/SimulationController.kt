package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.responses.NewSimulationResponse
import app.urbanflo.urbanflosumoserver.storage.StorageBadRequestException
import app.urbanflo.urbanflosumoserver.storage.StorageService
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import java.io.FileNotFoundException
import java.util.UUID

@Controller
class SimulationController(private val storageService: StorageService) {
    @GetMapping("/start-simulation", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun startSimulation(): Flux<SimulationStep> {
        val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"
        return Flux.fromIterable(SimulationInstance(cfgPath))
    }

    @PostMapping("/simulation")
    @ResponseBody
    fun newSimulation(@RequestBody files: Array<MultipartFile>): NewSimulationResponse {
        val id = storageService.store(files)
        return NewSimulationResponse(id.toString())
    }

    @GetMapping("/simulations")
    fun getSimulations() {
        TODO()
    }

    // exception handlers
    @ExceptionHandler(StorageBadRequestException::class)
    fun handleBadRequest(e: StorageBadRequestException): ResponseEntity<Any> {
        return ResponseEntity.badRequest().body(e)
    }

    @ExceptionHandler(FileNotFoundException::class)
    fun handleFileNotFound(e: FileNotFoundException): ResponseEntity<Any> {
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(NotImplementedError::class)
    fun handleNotImplemented(): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }
}