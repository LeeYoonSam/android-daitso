plugins {
    alias(libs.plugins.daitso.kotlin.jvm)
}

dependencies {
    // Coroutines for async/threading utilities
    implementation(libs.kotlinx.coroutines.android)
}
