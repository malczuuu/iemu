package io.github.malczuuu.iemu.storage

import io.github.malczuuu.iemu.core.firmware.FirmwareProvider
import io.github.malczuuu.iemu.core.firmware.FirmwareSnapshot
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class FileFirmwareProvider(private val path: Path = Paths.get("data/firmware.yaml")) :
    FirmwareProvider {

  override fun load(): FirmwareSnapshot {
    try {
      val content = Files.readString(path)
      val map: Map<*, *> = Yaml().load(content) ?: return default()
      val color = map["color"] as? String ?: return default()
      val version = map["version"] as? String ?: return default()
      val stagedColor = map["stagedColor"] as? String
      val packageUri = map["packageUri"] as? String
      return FirmwareSnapshot(color, version, stagedColor, packageUri)
    } catch (_: IOException) {
      return default()
    }
  }

  override fun save(snapshot: FirmwareSnapshot) {
    try {
      val options = DumperOptions().also { it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }
      val map = linkedMapOf<String, Any?>("color" to snapshot.color, "version" to snapshot.version)
      snapshot.stagedColor?.let { map["stagedColor"] = it }
      snapshot.packageUri?.let { map["packageUri"] = it }
      Files.writeString(path, Yaml(options).dump(map))
      log.info("Saved firmware state to {}", path)
    } catch (e: IOException) {
      log.error("Failed to save firmware state to {}", path, e)
    }
  }

  private fun default() = FirmwareSnapshot(FirmwareProvider.DEFAULT_COLOR, FirmwareProvider.DEFAULT_VERSION)

  companion object {
    private val log = LoggerFactory.getLogger(FileFirmwareProvider::class.java)
  }
}
