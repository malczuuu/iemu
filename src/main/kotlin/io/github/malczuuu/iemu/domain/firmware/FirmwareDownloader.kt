package io.github.malczuuu.iemu.domain.firmware

import java.net.URI

fun interface FirmwareDownloader {

  fun download(uri: URI): ByteArray
}
