import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention Plugin for Android Application modules.
 *
 * Provides standardized configuration for Android application modules including:
 * - Compilation SDK version
 * - Minimum/Target SDK versions
 * - Java/Kotlin compiler options
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<BaseAppModuleExtension> {
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

    // Extension for accessing kotlinOptions from BaseAppModuleExtension
    private fun BaseAppModuleExtension.kotlinOptions(block: org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        (this as? org.gradle.api.plugins.ExtensionAware)?.extensions?.configure(
            "kotlinOptions",
            block as (Any) -> Unit
        )
    }
}
