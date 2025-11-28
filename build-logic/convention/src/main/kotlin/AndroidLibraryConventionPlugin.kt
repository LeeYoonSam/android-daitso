import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

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
                // Set namespace based on project path
                // e.g., :core:data -> com.bup.ys.daitso.core.data
                namespace = "com.bup.ys.daitso" + target.path.replace(":", ".").replace("-", ".")

                compileSdk = 35

                defaultConfig {
                    minSdk = 26
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                @Suppress("UnstableApiUsage")
                testOptions {
                    animationsDisabled = true
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            // Add common test dependencies
            dependencies {
                add("testImplementation", "org.jetbrains.kotlin:kotlin-test:2.1.0")
            }
        }
    }
}
