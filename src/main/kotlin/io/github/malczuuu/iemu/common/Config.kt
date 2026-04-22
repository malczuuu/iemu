package io.github.malczuuu.iemu.common

import com.fasterxml.jackson.annotation.JsonProperty

data class Config(
    @JsonProperty("http") val http: Http = Http(),
    @JsonProperty("lwm2m") val lwM2m: LwM2m = LwM2m(),
) {

  data class Http(@JsonProperty("port") val port: Int = 4500)

  data class LwM2m(
      @JsonProperty("enabled") val isEnabled: Boolean = false,
      @JsonProperty("localPort") val localPort: Int = 0,
      @JsonProperty("endpoint") val endpoint: String? = "client",
      @JsonProperty("bootstrap") val bootstrap: Boolean = false,
      @JsonProperty("upstream") val upstream: String? = "localhost:5683",
      @JsonProperty("security") val security: Security = Security(),
  ) {

    fun useBootstrap(): Boolean = bootstrap

    fun useSecureMode(): Boolean = security.identity != null && security.psk != null

    data class Security(
        @JsonProperty("identity") val identity: String? = null,
        @JsonProperty("psk") val psk: String? = null,
    )
  }
}
