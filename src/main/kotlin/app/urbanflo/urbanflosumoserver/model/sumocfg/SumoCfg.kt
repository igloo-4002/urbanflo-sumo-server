package app.urbanflo.urbanflosumoserver.model.sumocfg

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

@JacksonXmlRootElement(localName = "configuration")
class SumoCfg(
    netPath: Path,
    rouPath: Path
) {
    val input: SumoCfgInput

    init {
        input = SumoCfgInput(
            SumoCfgNetFile(netPath.fileName.toString()),
            SumoCfgRouteFiles(rouPath.fileName.toString())
        )
    }
}
