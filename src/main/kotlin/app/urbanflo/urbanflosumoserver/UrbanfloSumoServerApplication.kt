package app.urbanflo.urbanflosumoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class UrbanfloSumoServerApplication

fun main(args: Array<String>) {
    logger.info { "Starting Spring Boot" }
    runApplication<UrbanfloSumoServerApplication>(*args)
}
