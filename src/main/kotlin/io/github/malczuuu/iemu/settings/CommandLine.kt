package io.github.malczuuu.iemu.settings

import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.commons.cli.help.HelpFormatter

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
                    .get(),
            )
            .addOption(
                Option.builder("h").longOpt("help").desc("Print this help message").get(),
            )

    try {
      val cmd = DefaultParser().parse(options, args)
      if (cmd.hasOption("help")) {
        HelpFormatter.builder().get().printHelp("iemu [options]", "", options, "", false)
        exitProcess(0)
      }
      profile = cmd.getOptionValue("profile") ?: ""
    } catch (e: ParseException) {
      throw SettingsException(e.message ?: "Invalid program arguments", e)
    }
  }
}
