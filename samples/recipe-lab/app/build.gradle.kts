plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
// Offline recipes work without a Firebase project. Enable generated Firebase resources only
// when the learner supplies their own Android app configuration.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
android {
    namespace = "in.androidengineers.cookbook"
    compileSdk = 36
    defaultConfig {
        applicationId = "in.androidengineers.cookbook"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging { resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/INDEX.LIST", "META-INF/DEPENDENCIES") }
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-ai")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")
    releaseImplementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.adk:google-adk-kotlin-core-android:0.1.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
