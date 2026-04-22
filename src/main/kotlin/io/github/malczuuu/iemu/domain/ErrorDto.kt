package io.github.malczuuu.iemu.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorDto(
    @JsonProperty("code") val code: Int?,
    @JsonProperty("message") val message: String?,
)
