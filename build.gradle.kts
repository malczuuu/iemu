import com.diffplug.spotless.LineEnding
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.JUnit
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
    id("jacoco")
    alias(libs.plugins.idea.ext)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation(libs.commons.cli)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    implementation(libs.javalin)
    implementation(libs.jetty.client)

    implementation(libs.leshan.client.cf)

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.snakeyaml)

    implementation(libs.problem4j.core)
    implementation(libs.problem4j.jackson3)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockk)
}

application {
    mainClass = "io.github.malczuuu.iemu.AppKt"
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

spotless {
    kotlin {
        target("src/**/*.kt")

        ktfmt("0.63").metaStyle()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    kotlinGradle {
        target("*.gradle.kts")

        ktlint("1.8.0").editorConfigOverride(mapOf("max_line_length" to "120"))
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("yaml") {
        target("**/*.yml", "**/*.yaml")
        targetExclude("webapp/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("misc") {
        target("**/.gitattributes", "**/.gitignore")
        targetExclude("webapp/node_modules/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

tasks.register<DefaultTask>("printVersion") {
    description = "Prints the current project version to the console."
    group = "help"

    val projectName = project.name
    val projectVersion = project.version.toString()

    doLast {
        println("$projectName version: $projectVersion")
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

tasks.named<Task>("check") {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = true
    }

    // For resolving warnings from mockito.
    jvmArgs("-XX:+EnableDynamicAgentLoading")

    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Created-By" to "Gradle ${gradle.gradleVersion}",
        )
    }

    from("${rootProject.rootDir}/LICENSE") {
        into("META-INF/")
        rename { "LICENSE.txt" }
    }
}

defaultTasks("spotlessApply", "build", "install")
