package app.urbanflo.urbanflosumoserver.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
class StorageProperties {
    val location = "uploads"
}