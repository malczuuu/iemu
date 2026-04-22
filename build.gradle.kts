import com.diffplug.spotless.LineEnding
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
    id("internal.common-convention")
    id("internal.idea-convention")
    id("internal.jacoco-convention")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
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
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    implementation(libs.javalin)
    implementation(libs.jetty.client)

    implementation(libs.leshan.client.cf)

    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)

    implementation(libs.problem4j.core)
    implementation(libs.problem4j.jackson3)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.mockito.core)
}

application {
    mainClass = "io.github.malczuuu.iemu.AppKt"
}

spotless {
    kotlin {
        target("src/**/*.kt", "buildSrc/src/**/*.kt")

        ktfmt("0.62").metaStyle()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.gradle.kts", "buildSrc/src/**/*.gradle.kts")

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

defaultTasks("spotlessApply", "build")
