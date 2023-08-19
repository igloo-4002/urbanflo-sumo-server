package app.urbanflo.urbanflosumoserver.netconvert

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import java.io.IOException
import java.nio.file.Path

fun runNetconvert(simulationId: SimulationId, simulationDir: Path, nodPath: Path, edgPath: Path, conPath: Path): Path {
    val netPath = simulationDir.resolve("$simulationId.net.xml").normalize().toAbsolutePath()
    val netconvertCmd = "netconvert --node-files=$nodPath --edge-files=$edgPath --connection-files=$conPath --output-file=$netPath"
    val command = (if (System.getProperty("os.name").lowercase().startsWith("windows")) {
        arrayOf("cmd.exe", "/c", netconvertCmd)
    } else {
        arrayOf("sh", "-c", netconvertCmd)
    })
   val process = ProcessBuilder()
        .directory(simulationDir.toFile())
        .command(*command)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    val statusCode = process.waitFor()
    if (statusCode != 0) {
        val output = try {
            process.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            ""
        }
        throw NetconvertException("netconvert exited with status code $statusCode\nOutput:${output}")
    } else {
        return netPath
    }
}