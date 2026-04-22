package internal

// This file contains extension functions for Gradle's Project API.

import org.gradle.api.Project

/**
 * Retrieves a boolean property from the project, interpreting any value other than `"false"` as
 * `true`.
 *
 * @param name The name of the property to retrieve.
 * @param defaultValue The default boolean value to return if the property is not set. Defaults to
 *   `false`.
 * @return The boolean value of the property, or [defaultValue] if the property is not set.
 * @receiver Gradle [Project] from which the property is read.
 */
fun Project.getBooleanProperty(name: String, defaultValue: Boolean = false): Boolean {
  return if (hasProperty(name)) findProperty(name)?.toString() != "false" else defaultValue
}
