import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention Plugin for Android Library modules with Jetpack Compose support.
 *
 * Provides configuration for Compose including:
 * - Compose build feature enabled
 * - Compose compiler options
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }

                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.15"
                }
            }
        }
    }
}
