package app.urbanflo.urbanflosumoserver.netconvert

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import java.io.IOException
import java.nio.file.Path

/**
 * Converts the node, edge and connection files to a network XML file using `netconvert`.
 *
 * @param simulationId Simulation ID
 * @param simulationDir path to root directory where all simulation files are stored
 * @param nodPath path to nodes XMl file
 * @param edgPath path to edges XMl file
 * @param conPath path to connections XMl file
 * @return A path to the network XML file
 * @throws NetconvertException if `netconvert` returns a non-zero exit status
 */
fun runNetconvert(simulationId: SimulationId, simulationDir: Path, nodPath: Path, edgPath: Path, conPath: Path): Path {
    val netPath = simulationDir.resolve("$simulationId.net.xml").normalize().toAbsolutePath()
    val netconvertCmd =
        "netconvert --node-files=$nodPath --edge-files=$edgPath --connection-files=$conPath --output-file=$netPath"
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
    if (statusCode == 0) {
        return netPath
    } else {
        val stdout = try {
            process.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            ""
        }
        val stderr = try {
            process.errorStream.bufferedReader().readText()
        } catch (e: IOException) {
            ""
        }
        throw NetconvertException("netconvert exited with status code $statusCode\nstdout:\n$stdout\nstderr:\n$stderr")
    }
}