package io.github.malczuuu.iemu.infra.http

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.malczuuu.iemu.common.Config
import io.github.malczuuu.iemu.domain.ConnectionService
import io.github.malczuuu.iemu.domain.DeviceError
import io.github.malczuuu.iemu.domain.Firmware
import io.github.malczuuu.iemu.domain.State
import io.github.malczuuu.iemu.domain.StateUpdate
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import java.time.Instant

data class ErrorDto(
    @JsonProperty("code") val code: Int?,
    @JsonProperty("message") val message: String?,
)

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

fun State.toDto(): StateDto =
    StateDto(
        deviceType = deviceType,
        currentTime = currentTime.toString(),
        timeZone = timeZone,
        utcOffset = utcOffset,
        errors = errors.map { it.toDto() },
        on = on,
        onTime = onTime,
        dimmer = dimmer,
    )

fun DeviceError.toDto(): ErrorDto = ErrorDto(code = code, message = message)

fun StateDto.toUpdate(): StateUpdate =
    StateUpdate(
        currentTime = currentTime?.let { Instant.parse(it) },
        timeZone = timeZone,
        utcOffset = utcOffset,
        on = on,
        onTime = onTime,
        dimmer = dimmer,
    )

fun Firmware.toDto(): FirmwareDto =
    FirmwareDto(
        fileChecksum = fileChecksum,
        packageUri = packageUri,
        state = state,
        result = result,
        pkgVersion = packageVersion,
        deliveryMethod = deliveryMethod,
        progress = progress,
    )

data class ConnectionDto(
    @JsonProperty("connected") val connected: Boolean,
    @JsonProperty("endpoint") val endpoint: String?,
    @JsonProperty("upstream") val upstream: String?,
    @JsonProperty("localPort") val localPort: Int,
    @JsonProperty("bootstrap") val bootstrap: Boolean,
    @JsonProperty("secureMode") val secureMode: Boolean,
)

fun ConnectionService.toDto(config: Config.LwM2m): ConnectionDto =
    ConnectionDto(
        connected = isStarted(),
        endpoint = config.endpoint,
        upstream = config.upstream,
        localPort = config.localPort,
        bootstrap = config.bootstrap,
        secureMode = config.useSecureMode(),
    )
