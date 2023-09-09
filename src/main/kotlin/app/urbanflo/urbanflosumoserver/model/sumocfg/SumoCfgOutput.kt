package app.urbanflo.urbanflosumoserver.model.sumocfg

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoCfgOutput(
    @field:JacksonXmlProperty(localName = "tripinfo-output")
    val tripInfoOutput: SumoCfgTripInfoOutput,
    @field:JacksonXmlProperty(localName = "netstate-output")
    val netstateOutput: SumoCfgNetstateOutput
)