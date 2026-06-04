package io.github.malczuuu.iemu.core.firmware

data class FirmwareSnapshot(
    val color: String,
    val version: String,
    val stagedColor: String? = null,
    val packageUri: String? = null,
)

val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}$")
