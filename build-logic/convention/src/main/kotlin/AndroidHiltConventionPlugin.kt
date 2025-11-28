import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention Plugin for Hilt dependency injection setup.
 *
 * Provides standardized configuration for Hilt including:
 * - Hilt Android plugin
 * - KSP compiler for code generation
 * - Hilt dependencies
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            dependencies {
                "implementation"("com.google.dagger:hilt-android:2.54")
                "ksp"("com.google.dagger:hilt-compiler:2.54")
            }
        }
    }
}
