package io.github.malczuuu.iemu.infra.http

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.malczuuu.iemu.core.ConnectionState
import io.github.malczuuu.iemu.core.DeviceError
import io.github.malczuuu.iemu.core.DeviceState
import io.github.malczuuu.iemu.core.DeviceStateUpdate
import io.github.malczuuu.iemu.core.FirmwareState
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateState
import java.time.Instant

data class ConnectionStateDto(
    @JsonProperty("connected") val connected: Boolean,
    @JsonProperty("endpoint") val endpoint: String?,
    @JsonProperty("upstream") val upstream: String?,
    @JsonProperty("localPort") val localPort: Int,
    @JsonProperty("bootstrap") val bootstrap: Boolean,
    @JsonProperty("secureMode") val secureMode: Boolean,
)

fun ConnectionState.toDto(): ConnectionStateDto =
    ConnectionStateDto(
        connected = connected,
        endpoint = endpoint,
        upstream = upstream,
        localPort = localPort,
        bootstrap = bootstrap,
        secureMode = secureMode,
    )

data class DeviceStateDto(
    @JsonProperty("deviceType") val deviceType: String? = null,
    @JsonProperty("currentTime") val currentTime: String? = null,
    @JsonProperty("timeZone") val timeZone: String? = null,
    @JsonProperty("utcOffset") val utcOffset: String? = null,
    @JsonProperty("errors") val errors: List<ErrorDto>? = null,
    @JsonProperty("on") val on: Boolean? = null,
    @JsonProperty("onTime") val onTime: Long? = null,
    @JsonProperty("dimmer") val dimmer: Int? = null,
)

fun DeviceState.toDto(): DeviceStateDto =
    DeviceStateDto(
        deviceType = deviceType,
        currentTime = currentTime.toString(),
        timeZone = timeZone,
        utcOffset = utcOffset,
        errors = errors.map { it.toDto() },
        on = on,
        onTime = onTime,
        dimmer = dimmer,
    )

fun DeviceStateDto.toUpdate(): DeviceStateUpdate =
    DeviceStateUpdate(
        currentTime = currentTime?.let { Instant.parse(it) },
        timeZone = timeZone,
        utcOffset = utcOffset,
        on = on,
        onTime = onTime,
        dimmer = dimmer,
    )

data class ErrorDto(
    @JsonProperty("code") val code: Int?,
    @JsonProperty("message") val message: String?,
)

fun DeviceError.toDto(): ErrorDto = ErrorDto(code = code, message = message)

data class FirmwareDto(
    @JsonProperty("fileChecksum") val fileChecksum: String?,
    @JsonProperty("packageUri") val packageUri: String?,
    @JsonProperty("state") val state: FirmwareUpdateState?,
    @JsonProperty("result") val result: FirmwareUpdateResult?,
    @JsonProperty("pkgVersion") val pkgVersion: String?,
    @JsonProperty("deliveryMethod") val deliveryMethod: FirmwareUpdateDeliveryMethod?,
    @JsonProperty("progress") val progress: Int?,
) {

  @get:JsonProperty("stateValue")
  val stateValue: Int?
    get() = state?.value

  @get:JsonProperty("resultValue")
  val resultValue: Int?
    get() = result?.value

  @get:JsonProperty("deliveryMethodValue")
  val deliveryMethodValue: Int?
    get() = deliveryMethod?.value
}

fun FirmwareState.toDto(): FirmwareDto =
    FirmwareDto(
        fileChecksum = fileChecksum,
        packageUri = packageUri,
        state = state,
        result = result,
        pkgVersion = packageVersion,
        deliveryMethod = deliveryMethod,
        progress = progress,
    )
