import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id( "java")
    id ("com.gradleup.shadow") .version( "9.3.0")
}

group = "io.github.malczuuu"
version = "1.1.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.slf4j:slf4j-api:1.7.32")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

    implementation("io.javalin:javalin:4.1.1")

    implementation("org.eclipse.leshan:leshan-client-cf:1.3.1")
    implementation("javax.xml.ws:jaxws-api:2.3.1")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")

    implementation("io.github.problem4j:problem4j-core:1.3.0")
    implementation("io.github.problem4j:problem4j-jackson2:1.3.0")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:3.8.0")
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

tasks.withType<Jar>().configureEach  {
    manifest {
        attributes("Main-Class" to "io.github.malczuuu.iemu.App")
    }
}
