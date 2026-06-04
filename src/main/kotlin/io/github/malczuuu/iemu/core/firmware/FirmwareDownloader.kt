package io.github.malczuuu.iemu.core.firmware

import java.net.URI

fun interface FirmwareDownloader {

  fun download(uri: URI): ByteArray
}
