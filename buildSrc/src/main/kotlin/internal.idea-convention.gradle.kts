import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.JUnit
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext")
}

idea {
    project {
        settings {
            runConfigurations {
                create<Application>("Run [iemu|main]") {
                    moduleName = "iemu.test"
                    mainClass = "io.github.malczuuu.iemu.AppKt"
                }
                create<Application>("Run [iemu|demo]") {
                    moduleName = "iemu.test"
                    mainClass = "io.github.malczuuu.iemu.AppKt"
                    programParameters = "--profile demo"
                }
                create<Gradle>("Clean [iemu]") {
                    taskNames = listOf("clean")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Build [iemu]") {
                    taskNames = listOf("spotlessApply build")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Format Code [iemu]") {
                    taskNames = listOf("spotlessApply")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<JUnit>("JUnit [iemu]") {
                    moduleName = "iemu.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.malczuuu.iemu"
                }
            }
        }
    }
}
