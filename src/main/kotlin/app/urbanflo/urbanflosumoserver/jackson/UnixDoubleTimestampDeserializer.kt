package app.urbanflo.urbanflosumoserver.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.google.gson.JsonParseException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.math.roundToLong

// https://stackoverflow.com/questions/20635698/how-do-i-deserialize-timestamps-that-are-in-seconds-with-jackson
class UnixDoubleTimestampDeserializer : StdDeserializer<OffsetDateTime>(OffsetDateTime::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): OffsetDateTime {
        val number = p?.valueAsString?.toDouble() ?: run { throw JsonParseException("Value is null") }
        val instant = Instant.ofEpochMilli((number * 1000).roundToLong())
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
}