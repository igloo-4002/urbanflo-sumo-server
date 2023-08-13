package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.SimulationInstanceIterator
import app.urbanflo.urbanflosumoserver.SimulationStep
import app.urbanflo.urbanflosumoserver.model.SimulationMessageRequest
import app.urbanflo.urbanflosumoserver.model.SimulationMessageType
import jakarta.annotation.PreDestroy
import org.springframework.messaging.handler.annotation.MessageMapping
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import reactor.core.Disposable
import app.urbanflo.urbanflosumoserver.responses.NewSimulationResponse
import app.urbanflo.urbanflosumoserver.responses.SimulationInfo
import app.urbanflo.urbanflosumoserver.storage.StorageBadRequestException
import app.urbanflo.urbanflosumoserver.storage.StorageException
import app.urbanflo.urbanflosumoserver.storage.StorageService
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux


@Controller
class SimulationController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val storageService: StorageService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(SimulationController::class.java)
    }

    private var simulationDisposable: Disposable? = null
    private var simulationInstanceIterator: SimulationInstanceIterator? = null


    @MessageMapping("/simulation-data")
    fun simulationSocket(request: SimulationMessageRequest) { // status expected as subscribe or unsubscribe

        when (request.status) {
            SimulationMessageType.SUBSCRIBE -> {
                val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"

                val simulationInstance = SimulationInstance(cfgPath)
                val simulationIterator = SimulationInstanceIterator(simulationInstance)
                simulationInstanceIterator = simulationIterator

                val flux = Flux.create<SimulationStep> { sink ->
                    while (simulationIterator.hasNext()) {
                        sink.next(simulationIterator.next())
                    }

                    sink.complete()
                }
                simulationDisposable = flux.doOnTerminate { simulationIterator.stopSimulation() }
                    .doOnCancel { simulationIterator.stopSimulation() }
                    .doOnError { e ->
                        logger.error("Error occurred during simulation", e)
                        simpMessagingTemplate.convertAndSend(
                            "/topic/simulation-data",
                            mapOf("error" to "Error occurred during simulation: ${e.message}")
                        )
                    }
                    .subscribe { simulationStep ->
                        simpMessagingTemplate.convertAndSend("/topic/simulation-data", simulationStep)
                    }
            }

            SimulationMessageType.UNSUBSCRIBE -> {
                simulationInstanceIterator?.stopSimulation()
                simulationDisposable?.dispose()
                simulationDisposable = null
            }
        }

        // TODO: not fully done; needs better error handling, and perhaps rework it?
        // TODO: this is currently broadcast; make the connections private using /queue/ instead of topic
    }

    @PreDestroy
    fun cleanup() {
        simulationDisposable?.dispose()
    }

    @PostMapping("/simulation")
    @ResponseBody
    fun newSimulation(@RequestBody files: Array<MultipartFile>): NewSimulationResponse {
        try {
            val id = storageService.store(files)
            return NewSimulationResponse(id.toString())
        } catch (e: StorageBadRequestException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
        } catch (e: StorageException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
        }
    }

    @DeleteMapping("/simulation/{id:.+}")
    @ResponseBody
    fun deleteSimulation(@PathVariable id: String) {
        try {
            storageService.delete(id.trim())
        } catch (e: StorageSimulationNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
        }
    }

    @GetMapping("/simulation/{id:.+}")
    @ResponseBody
    fun getSimulationInfo(@PathVariable id: String): SimulationInfo {
        try {
            return storageService.info(id.trim())
        } catch (e: StorageSimulationNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
        }
    }

    @GetMapping("/simulations")
    fun getSimulations() {
        TODO()
    }
}