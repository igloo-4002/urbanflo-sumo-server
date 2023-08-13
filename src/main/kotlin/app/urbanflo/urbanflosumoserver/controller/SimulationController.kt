package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.SimulationInstanceIterator
import app.urbanflo.urbanflosumoserver.SimulationStep
import app.urbanflo.urbanflosumoserver.model.InvalidSimulationMessageTypeException
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
import reactor.core.publisher.Sinks


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


    @MessageMapping("/simulation-socket") // this is to receive messages
    fun simulationSocket(status: SimulationMessageType) { // status expected as subscribe or unsubscribe
        logger.info("Status from react is {}", status)

        when (status) {
            SimulationMessageType.SUBSCRIBE -> {
                logger.info("subbing to the simulation")
                // receive the subscribe message here, add start the simulation and keep sending data
                // stop simulation when you receive unsubscribe
                val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"

                val simulationInstance = SimulationInstance(cfgPath)
                val simulationIterator = SimulationInstanceIterator(simulationInstance)
                simulationInstanceIterator = simulationIterator

                val flux = Flux.fromIterable(simulationInstance)
                flux.doOnTerminate { simulationIterator.stopSimulation() }
                    .doOnCancel { simulationIterator.stopSimulation() }.doOnError { e ->
                        logger.error("Error occurred during simulation", e)
                        simpMessagingTemplate.convertAndSend(
                            "/topic/simulation-socket",
                            mapOf("error" to "Error occurred during simulation: ${e.message}")
                        )
                    }.subscribe { simulationStep ->
                        logger.info("simulation step data {}", simulationStep)
                        simpMessagingTemplate.convertAndSend("/topic/simulation-socket", simulationStep)
                    }
            }

            SimulationMessageType.UNSUBSCRIBE -> {
                logger.info("unsubbing from the simulation")
                simulationInstanceIterator?.stopSimulation()
                simulationDisposable?.dispose()
                simulationDisposable = null
            }

            else -> throw InvalidSimulationMessageTypeException(status.name)
        }
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