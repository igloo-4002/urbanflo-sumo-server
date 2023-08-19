package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

// TODO: not sure about the structure of this one
@JacksonXmlRootElement(localName = "vTypes")
data class SumoVehicleTypesXml(
    @field:JacksonXmlProperty(localName = "vType")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val vTypes: List<SumoVehicleType>
)
