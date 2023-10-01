package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.model.ErrorResponse
import app.urbanflo.urbanflosumoserver.model.SimulationMessageRequest
import app.urbanflo.urbanflosumoserver.model.SimulationMessageType
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.storage.StorageBadRequestException
import app.urbanflo.urbanflosumoserver.storage.StorageException
import app.urbanflo.urbanflosumoserver.storage.StorageService
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import com.fasterxml.jackson.core.JsonProcessingException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.annotation.PreDestroy
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.validation.FieldError
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
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

    @Operation(summary = "Create a new simulation.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "New simulation saved"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid network data",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "An internal error occurred during saving",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/simulation", consumes = ["application/json"], produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun newSimulation(@Validated @RequestBody network: SumoNetwork) = storageService.store(network)

    @Operation(summary = "Delete a simulation.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Simulation deleted"),
            ApiResponse(
                responseCode = "404",
                description = "Simulation not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @DeleteMapping("/simulation/{id:.+}", produces = ["application/json"])
    @ResponseBody
    fun deleteSimulation(@PathVariable id: SimulationId) = storageService.delete(id.trim())

    @Operation(summary = "Modify a simulation.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Simulation modified"),
            ApiResponse(
                responseCode = "404",
                description = "Simulation not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid network data",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "An internal error occurred during saving",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PutMapping("/simulation/{id:.+}", consumes = ["application/json"], produces = ["application/json"])
    @ResponseBody
    fun modifySimulation(@PathVariable id: SimulationId, @RequestBody network: SumoNetwork) = storageService.store(id, network)

    @Operation(summary = "Get simulation information.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Simulation found"),
            ApiResponse(
                responseCode = "404",
                description = "Simulation not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/simulation/{id:.+}", produces = ["application/json"])
    @ResponseBody
    fun getSimulationInfo(@PathVariable id: SimulationId) = storageService.info(id.trim())

    @Operation(summary = "Get information of all simulations.")
    @GetMapping("/simulations", produces = ["application/json"])
    @ResponseBody
    fun getAllSimulationInfo() = storageService.listAll()

    @Operation(summary = "Get simulation network in the same format as the uploaded network data.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Simulation found"),
            ApiResponse(
                responseCode = "404",
                description = "Simulation not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/simulation/{id:.+}/network", produces = ["application/json"])
    @ResponseBody
    fun exportSimulationNetwork(@PathVariable id: SimulationId) = storageService.export(id.trim())

    @Operation(summary = "Get simulation output data.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Simulation found and finished"),
            ApiResponse(
                responseCode = "404",
                description = "Simulation not found, not started or not closed properly",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/simulation/{id:.+}/output", produces = ["application/json"])
    @ResponseBody
    fun getSimulationOutput(@PathVariable id: SimulationId) = storageService.getSimulationOutput(id.trim())

    @Operation(summary = "Get simulation analytics.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Simulation found and finished"),
            ApiResponse(
                responseCode = "404",
                description = "Simulation not found, not started or not closed properly",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/simulation/{id:.+}/analytics", produces = ["application/json"])
    @ResponseBody
    fun getSimulationAnalytics(@PathVariable id: SimulationId) = storageService.getSimulationAnalytics(id.trim())

    @ExceptionHandler(StorageSimulationNotFoundException::class)
    fun handleStorageNotFound(e: StorageSimulationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "No such simulation"), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(StorageBadRequestException::class)
    fun handleStorageBadRequest(e: StorageBadRequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Invalid request"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(JsonProcessingException::class)
    fun handleJsonError(e: JsonProcessingException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse("Invalid JSON body"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(StorageException::class)
    fun handleStorageException(e: StorageException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse("An internal error occurred"), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fields = e.allErrors.associate{ err -> (err as FieldError).field to err.defaultMessage}.toMap()
        return ResponseEntity(ErrorResponse("Invalid JSON body", fields), HttpStatus.BAD_REQUEST)
    }

    @PreDestroy
    fun stopAllSimulations() {
        logger.info { "Server is shutting down. Stopping all simulations" }
        instances.values.forEach { instance ->
            instance.forceCloseConnectionOnServerShutdown()
        }
    }
}