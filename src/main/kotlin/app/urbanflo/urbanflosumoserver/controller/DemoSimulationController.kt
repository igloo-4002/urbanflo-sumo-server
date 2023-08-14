package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.SimulationInstanceIterator
import app.urbanflo.urbanflosumoserver.SimulationStep
import app.urbanflo.urbanflosumoserver.model.SimulationMessageRequest
import app.urbanflo.urbanflosumoserver.model.SimulationMessageType
import app.urbanflo.urbanflosumoserver.storage.StorageService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import reactor.core.Disposable
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

@Controller
class DemoSimulationController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val storageService: StorageService
) {
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
                        logger.error(e) { "Error occurred during simulation" }
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
}