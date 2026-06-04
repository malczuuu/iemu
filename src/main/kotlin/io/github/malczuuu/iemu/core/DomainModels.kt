package io.github.malczuuu.iemu.core

import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateState
import java.time.Instant

class ConnectionState(
    val connected: Boolean,
    val endpoint: String?,
    val upstream: String?,
    val localPort: Int,
    val bootstrap: Boolean,
    val secureMode: Boolean,
)

class DeviceError(val code: Int, val message: String)

class DeviceState(
    val deviceType: String,
    val currentTime: Instant,
    val timeZone: String,
    val utcOffset: String,
    val errors: List<DeviceError>,
    val on: Boolean,
    val onTime: Long,
    val dimmer: Int,
)

class DeviceStateUpdate(
    val currentTime: Instant? = null,
    val timeZone: String? = null,
    val utcOffset: String? = null,
    val on: Boolean? = null,
    val onTime: Long? = null,
    val dimmer: Int? = null,
)

class FirmwareState(
    val file: ByteArray,
    val fileChecksum: String,
    val packageUri: String?,
    val state: FirmwareUpdateState,
    val result: FirmwareUpdateResult,
    val packageVersion: String?,
    val deliveryMethod: FirmwareUpdateDeliveryMethod,
    val progress: Int,
)

class FirmwareUpdate(val file: ByteArray? = null, val packageUri: String? = null)
