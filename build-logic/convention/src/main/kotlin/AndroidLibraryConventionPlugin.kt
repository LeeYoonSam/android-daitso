import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention Plugin for Android Library modules.
 *
 * Provides standardized configuration for Android library modules including:
 * - Compilation SDK version
 * - Minimum SDK version
 * - Java/Kotlin compiler options
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 35

                defaultConfig {
                    minSdk = 26
                    targetSdk = 35
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                kotlinOptions {
                    jvmTarget = "17"
                }
            }
        }
    }

    // Extension for accessing kotlinOptions from LibraryExtension
    private fun LibraryExtension.kotlinOptions(block: org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        (this as? org.gradle.api.plugins.ExtensionAware)?.extensions?.configure(
            "kotlinOptions",
            block as (Any) -> Unit
        )
    }
}
