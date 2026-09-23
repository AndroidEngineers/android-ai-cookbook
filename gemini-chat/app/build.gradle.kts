plugins {
 alias(libs.plugins.android.application)
 alias(libs.plugins.compose.compiler)
 alias(libs.plugins.kotlin.serialization)
 alias(libs.plugins.ksp)
}
android {
 namespace = "com.androidengineers.pocketchat"
 compileSdk = 36
 defaultConfig {
  applicationId = "com.androidengineers.pocketchat"
  minSdk = 26
  targetSdk = 36
  versionCode = 1
  versionName = "0.1.0"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
 buildTypes {
  release {
   isMinifyEnabled = true
   proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
  }
 }
 compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
 }
 buildFeatures { compose = true; buildConfig = true }
 packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
kotlin { jvmToolchain(17) }
dependencies {
 implementation(platform(libs.androidx.compose.bom))
 androidTestImplementation(platform(libs.androidx.compose.bom))
 implementation(libs.androidx.core.ktx)
 implementation(libs.androidx.activity.compose)
 implementation(libs.androidx.lifecycle.runtime.compose)
 implementation(libs.androidx.lifecycle.viewmodel.compose)
 implementation(libs.androidx.compose.ui)
 implementation(libs.androidx.compose.ui.tooling.preview)
 implementation(libs.androidx.compose.material3)
 implementation(libs.compose.icons)
 implementation(libs.kotlinx.serialization.json)
 debugImplementation(libs.okhttp)
 implementation(libs.androidx.lifecycle.process)
 implementation(libs.room.runtime)
 implementation(libs.room.ktx)
 ksp(libs.room.compiler)
 testImplementation(libs.mockwebserver)
 debugImplementation(libs.androidx.compose.ui.tooling)
 testImplementation(libs.junit)
 testImplementation(libs.kotlinx.coroutines.test)
 androidTestImplementation(libs.androidx.compose.ui.test.junit4)
 androidTestImplementation(libs.androidx.test.ext.junit)
 androidTestImplementation(libs.androidx.test.runner)
 androidTestImplementation(libs.androidx.test.espresso.core)
 androidTestImplementation(libs.androidx.test.core)
 debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }
