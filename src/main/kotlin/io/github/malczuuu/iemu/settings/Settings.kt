package io.github.malczuuu.iemu.settings

data class Settings(
    val http: Http = Http(),
    val lwM2m: LwM2m = LwM2m(),
    val features: Features = Features(),
) {

  data class Http(val port: Int = 4500)

  data class Features(val firmwareUpdate: FirmwareUpdate = FirmwareUpdate()) {
    data class FirmwareUpdate(val enabled: Boolean = false)
  }

  data class LwM2m(
      val isEnabled: Boolean = false,
      val localPort: Int = 0,
      val endpoint: String? = "client",
      val bootstrap: Boolean = false,
      val upstream: String? = "localhost:5683",
      val security: Security = Security(),
  ) {

    fun useBootstrap(): Boolean = bootstrap

    fun useSecureMode(): Boolean = security.identity != null && security.psk != null

    data class Security(
        val identity: String? = null,
        val psk: String? = null,
    )
  }
}
