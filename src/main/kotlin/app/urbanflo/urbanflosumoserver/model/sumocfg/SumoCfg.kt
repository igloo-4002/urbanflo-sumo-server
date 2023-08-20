package app.urbanflo.urbanflosumoserver.model.sumocfg

import app.urbanflo.urbanflosumoserver.model.SumoXml
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

@JacksonXmlRootElement(localName = "configuration")
class SumoCfg(
    netPath: Path,
    rouPath: Path
): SumoXml {
    val input: SumoCfgInput

    init {
        input = SumoCfgInput(
            SumoCfgNetFile(netPath.fileName.toString()),
            SumoCfgRouteFiles(rouPath.fileName.toString())
        )
    }

    override fun fileName(simulationId: SimulationId) = "$simulationId.sumocfg"
}
