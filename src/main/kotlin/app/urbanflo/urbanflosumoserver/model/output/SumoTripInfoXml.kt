package app.urbanflo.urbanflosumoserver.model.output

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "tripinfos")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoTripInfoXml(
    @field:JacksonXmlProperty(localName = "tripinfo")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val tripInfos: List<SumoTripInfo>
)
