package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.annotation.JsonValue

enum class SumoSpreadType(@JsonValue val spreadType: String) {
    RIGHT("right"),
    CENTER("center"),
    ROAD_CENTER("roadCenter")
}