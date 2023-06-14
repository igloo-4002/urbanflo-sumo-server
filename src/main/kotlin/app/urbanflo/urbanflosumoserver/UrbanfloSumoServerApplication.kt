package app.urbanflo.urbanflosumoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UrbanfloSumoServerApplication

fun main(args: Array<String>) {
    runApplication<UrbanfloSumoServerApplication>(*args)
}
