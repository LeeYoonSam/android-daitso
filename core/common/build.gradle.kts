plugins {
    alias(libs.plugins.daitso.kotlin.jvm)
}

dependencies {
    // Coroutines for async/threading utilities
    implementation(libs.kotlinx.coroutines.core)

    // javax.inject for @Qualifier annotation (pure JVM, no Android dependencies)
    implementation(libs.javax.inject)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
