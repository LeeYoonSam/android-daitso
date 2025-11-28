import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention Plugin for pure Kotlin JVM modules.
 *
 * Provides standardized configuration for Kotlin JVM modules without Android dependencies.
 * This plugin is used for core:model and core:common modules.
 */
class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }
        }
    }
}
