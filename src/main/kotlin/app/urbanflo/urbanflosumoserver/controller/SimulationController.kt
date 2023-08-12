package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.SimulationInstance
import app.urbanflo.urbanflosumoserver.SimulationStep
import app.urbanflo.urbanflosumoserver.model.InvalidSimulationMessageTypeException
import app.urbanflo.urbanflosumoserver.model.SimulationMessageType
import jakarta.annotation.PreDestroy
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Flux
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.messaging.simp.SimpMessagingTemplate
import reactor.core.Disposable


@Controller
class SimulationController(private val simpMessagingTemplate: SimpMessagingTemplate) { // this template is automatically injected

    companion object {
        private val logger = LoggerFactory.getLogger(SimulationController::class.java)
    }

    private var simulationDisposable: Disposable? = null

    @MessageMapping("/simulation-socket") // this is to receive messages
    fun simulationSocket(status: SimulationMessageType) { // status expected as subscribe or unsubscribe
        // listen for subscribe message here, then start the simulation

        when (status) {
            SimulationMessageType.SUBSCRIBE -> {
                logger.info("subbing to the simulation")
                // receive the subscribe message here, add start the simulation and keep sending data
                // stop simulation when you receive unsubscribe
                val cfgPath = System.getenv("SUMOCFG_PATH") ?: "demo/demo.sumocfg"
                val flux = Flux.fromIterable(SimulationInstance(cfgPath))
                flux.subscribe { simulationStep ->
                    logger.info("sending this to react {}", simulationStep)
                    simpMessagingTemplate.convertAndSend("/topic/simulation-socket", simulationStep)
                }
            }

            SimulationMessageType.UNSUBSCRIBE -> {
                logger.info("unsubbing from the simulation")
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
}