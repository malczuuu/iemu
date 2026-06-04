package io.github.malczuuu.iemu.settings

import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

class CommandLine(private val args: Array<String>) {

  var profile: String = ""
    private set

  fun init() {
    val options =
        Options()
            .addOption(
                Option.builder("p")
                    .longOpt("profile")
                    .hasArg()
                    .argName("name")
                    .desc("Configuration profile name")
                    .build(),
            )
            .addOption(
                Option.builder("h").longOpt("help").desc("Print this help message").build(),
            )

    try {
      val cmd = DefaultParser().parse(options, args)
      if (cmd.hasOption("help")) {
        HelpFormatter().printHelp("iemu [options]", options)
        exitProcess(0)
      }
      profile = cmd.getOptionValue("profile") ?: ""
    } catch (e: ParseException) {
      throw SettingsException(e.message ?: "Invalid program arguments", e)
    }
  }
}
