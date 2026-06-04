package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import java.time.Instant

class DeviceError(val code: Int, val message: String)

class State(
    val deviceType: String,
    val currentTime: Instant,
    val timeZone: String,
    val utcOffset: String,
    val errors: List<DeviceError>,
    val on: Boolean,
    val onTime: Long,
    val dimmer: Int,
)

class StateUpdate(
    val currentTime: Instant? = null,
    val timeZone: String? = null,
    val utcOffset: String? = null,
    val on: Boolean? = null,
    val onTime: Long? = null,
    val dimmer: Int? = null,
)

class Firmware(
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
