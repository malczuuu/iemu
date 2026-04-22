package io.github.malczuuu.iemu.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState

data class FirmwareDto(
    @get:JsonIgnore val file: ByteArray?,
    val fileChecksum: String?,
    val packageUri: String?,
    val state: FirmwareUpdateState?,
    val result: FirmwareUpdateResult?,
    val pkgVersion: String?,
    val deliveryMethod: FirmwareUpdateDeliveryMethod?,
    val progress: Int?,
) {

  val stateValue: Int?
    get() = state?.value

  val resultValue: Int?
    get() = result?.value

  val deliveryMethodValue: Int?
    get() = deliveryMethod?.value

  // FIXME: remove file from DTO and drop equals and hashCode

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FirmwareDto

    if (progress != other.progress) return false
    if (!file.contentEquals(other.file)) return false
    if (fileChecksum != other.fileChecksum) return false
    if (packageUri != other.packageUri) return false
    if (state != other.state) return false
    if (result != other.result) return false
    if (pkgVersion != other.pkgVersion) return false
    if (deliveryMethod != other.deliveryMethod) return false
    if (stateValue != other.stateValue) return false
    if (resultValue != other.resultValue) return false
    if (deliveryMethodValue != other.deliveryMethodValue) return false

    return true
  }

  override fun hashCode(): Int {
    var result = progress ?: 0
    result = 31 * result + (file?.contentHashCode() ?: 0)
    result = 31 * result + (fileChecksum?.hashCode() ?: 0)
    result = 31 * result + (packageUri?.hashCode() ?: 0)
    result = 31 * result + (state?.hashCode() ?: 0)
    result = 31 * result + (this@FirmwareDto.result?.hashCode() ?: 0)
    result = 31 * result + (pkgVersion?.hashCode() ?: 0)
    result = 31 * result + (deliveryMethod?.hashCode() ?: 0)
    result = 31 * result + (stateValue ?: 0)
    result = 31 * result + (resultValue ?: 0)
    result = 31 * result + (deliveryMethodValue ?: 0)
    return result
  }
}
