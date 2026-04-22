package io.github.malczuuu.iemu.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class StateDto(
    @JsonProperty("deviceType") val deviceType: String? = null,
    @JsonProperty("currentTime") val currentTime: String? = null,
    @JsonProperty("timeZone") val timeZone: String? = null,
    @JsonProperty("utcOffset") val utcOffset: String? = null,
    @JsonProperty("errors") val errors: List<ErrorDto>? = null,
    @JsonProperty("on") val on: Boolean? = null,
    @JsonProperty("onTime") val onTime: Long? = null,
    @JsonProperty("dimmer") val dimmer: Int? = null,
)
