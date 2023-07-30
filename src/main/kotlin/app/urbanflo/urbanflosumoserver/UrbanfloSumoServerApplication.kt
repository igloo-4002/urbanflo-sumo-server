package app.urbanflo.urbanflosumoserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UrbanfloSumoServerApplication

fun main(args: Array<String>) {
    System.loadLibrary("libtracijni")
    runApplication<UrbanfloSumoServerApplication>(*args)
}
