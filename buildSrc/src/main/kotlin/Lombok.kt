import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Adds Lombok to all required configurations for both main and test source sets.
 *
 * This helper mirrors the standard Lombok setup recommended by Gradle:
 * - `compileOnly` - makes Lombok annotations visible to main sources.
 * - `annotationProcessor` - enables Lombok's annotation processor for main sources.
 * - `testCompileOnly` - makes Lombok annotations visible in test sources.
 * - `testAnnotationProcessor` - enables Lombok's annotation processor for test sources.
 *
 * Typical usage (with Version Catalogs):
 * ```
 * dependencies {
 *     lombok(libs.lombok)
 * }
 * ```
 *
 * Lombok's Gradle integration can also be achieved by using 3rd-party plugins, but this project
 * intentionally avoids adding such plugins to keep the build small, transparent, and
 * dependency-free. This helper function provides the same Lombok setup without relying on any
 * external plugin.
 *
 * @param dependencyNotation A provider of a `MinimalExternalModuleDependency`, usually coming from
 *   a version catalog (e.g., `libs.lombok`). This matches Gradle's built-in dependency notation
 *   type for external module dependencies.
 */
fun DependencyHandler.lombok(dependencyNotation: Any) {
  add("compileOnly", dependencyNotation)
  add("annotationProcessor", dependencyNotation)

  add("testCompileOnly", dependencyNotation)
  add("testAnnotationProcessor", dependencyNotation)
}
