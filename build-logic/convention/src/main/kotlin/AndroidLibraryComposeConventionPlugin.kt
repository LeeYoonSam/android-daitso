import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention Plugin for Android Library modules with Jetpack Compose support.
 *
 * Provides configuration for Compose including:
 * - Compose build feature enabled
 * - Compose compiler options
 *
 * Note: This plugin expects the compose-compiler plugin to be applied in the module's build.gradle.kts
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // First apply the android library plugin
            pluginManager.apply("daitso.android.library")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }
        }
    }
}
