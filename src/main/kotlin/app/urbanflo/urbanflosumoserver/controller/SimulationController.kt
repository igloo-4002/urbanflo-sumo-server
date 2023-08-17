package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.model.NewSimulationResponse
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.SimulationMessageRequest
import app.urbanflo.urbanflosumoserver.model.SimulationMessageType
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.storage.StorageBadRequestException
import app.urbanflo.urbanflosumoserver.storage.StorageException
import app.urbanflo.urbanflosumoserver.storage.StorageService
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import reactor.core.Disposable

private val logger = KotlinLogging.logger {}

@Controller
class SimulationController(
    private val storageService: StorageService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private var instances: MutableMap<String, SimulationInstance> = mutableMapOf()
    private var disposables: MutableMap<String, Disposable> = mutableMapOf()

    @MessageMapping("/simulation/{id}")
    fun simulationSocket(
        @DestinationVariable id: SimulationId,
        request: SimulationMessageRequest,
        @Header("simpSessionId") sessionId: String
    ) {
        val idTrim = id.trim()
        when (request.status) {
            SimulationMessageType.START -> {
                logger.info { "Simulation $idTrim with session ID $sessionId started" }
                try {
                    val simulationInstance = instances[sessionId] ?: run {
                        val newSimulation = storageService.load(idTrim, sessionId)
                        instances[sessionId] = newSimulation
                        newSimulation
                    }
                    disposables[sessionId] =
                        simulationInstance.flux.doOnTerminate { simulationInstance.stopSimulation() }
                            .doOnCancel { simulationInstance.stopSimulation() }
                            .doOnError { e ->
                                logger.error(e) { "Error occurred during simulation $idTrim" }
                                simpMessagingTemplate.convertAndSend(
                                    "/topic/simulation/${idTrim}/error",
                                    mapOf("error" to "Error occurred during simulation: ${e.message}")
                                )
                            }
                            .subscribe { simulationStep ->
                                simpMessagingTemplate.convertAndSend("/topic/simulation/${idTrim}", simulationStep)
                            }
                } catch (e: StorageSimulationNotFoundException) {
                    logger.error(e) { "Error occurred during simulation $idTrim" }
                    simpMessagingTemplate.convertAndSend(
                        "/topic/simulation/${idTrim}/error",
                        mapOf("error" to "Error occurred during simulation: ${e.message}")
                    )
                }
            }

            SimulationMessageType.STOP -> {
                logger.info { "Simulation $id with session ID $sessionId stopped" }
                instances[sessionId]?.stopSimulation()
                disposables[sessionId]?.dispose()
                disposables.remove(sessionId)
                instances.remove(sessionId)
            }
        }
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
    fun deleteSimulation(@PathVariable id: SimulationId) {
        try {
            storageService.delete(id.trim())
        } catch (e: StorageSimulationNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
        }
    }

    @GetMapping("/simulation/{id:.+}")
    @ResponseBody
    fun getSimulationInfo(@PathVariable id: SimulationId): SimulationInfo {
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