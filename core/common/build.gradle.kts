plugins {
    alias(libs.plugins.daitso.kotlin.jvm)
}

dependencies {
    // Coroutines for async/threading utilities
    implementation(libs.kotlinx.coroutines.core)

    // Hilt for dependency injection (Qualifier)
    implementation(libs.hilt.android)
}
