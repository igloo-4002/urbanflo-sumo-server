package app.urbanflo.urbanflosumoserver.controller

import app.urbanflo.urbanflosumoserver.responses.NewSimulationResponse
import app.urbanflo.urbanflosumoserver.responses.SimulationInfo
import app.urbanflo.urbanflosumoserver.storage.StorageBadRequestException
import app.urbanflo.urbanflosumoserver.storage.StorageException
import app.urbanflo.urbanflosumoserver.storage.StorageService
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

@Controller
class SimulationController(
    private val storageService: StorageService
) {
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