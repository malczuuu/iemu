import com.diffplug.spotless.LineEnding
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
    id("application")
    id("com.diffplug.spotless").version("8.1.0")
}

group = "io.github.malczuuu"
version = "1.1.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.23")

    implementation("io.javalin:javalin:6.7.0")
    implementation("org.eclipse.jetty:jetty-client:11.0.26")

    implementation("org.eclipse.leshan:leshan-client-cf:1.5.0")
    implementation("javax.xml.ws:jaxws-api:2.3.1")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")

    implementation("io.github.problem4j:problem4j-core:1.3.0")
    implementation("io.github.problem4j:problem4j-jackson2:1.3.0")

    testImplementation(platform("org.junit:junit-bom:5.14.1"))

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:3.8.0")
}

application {
    mainClass = "io.github.malczuuu.iemu.App"
}

spotless {
    java {
        target("**/src/**/*.java")

        // NOTE: decided not to upgrade Google Java Format, as versions 1.29+ require running it on Java 21
        googleJavaFormat("1.28.0")
        forbidWildcardImports()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    kotlin {
        target("**/src/**/*.kt")

        ktfmt("0.60").metaStyle()
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

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("misc") {
        target("**/.gitattributes", "**/.gitignore")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

// Utility to clean up old jars as they can clutter due to versioning by Git commit hashes.
// Usage:
//   ./gradlew cleanLibs
tasks.register<Delete>("cleanLibs") {
    description = "Deletes build/libs directory."
    group = "build"

    delete(layout.buildDirectory.dir("libs"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = true
    }

    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
}

tasks.withType<Jar>().configureEach {
    dependsOn("cleanLibs")

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Build-Jdk-Spec" to java.toolchain.languageVersion.get().toString(),
            "Created-By" to "Gradle ${gradle.gradleVersion}",
        )
    }

    from("LICENSE") {
        into("META-INF/")
        rename { "LICENSE.txt" }
    }
}
