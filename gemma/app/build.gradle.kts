plugins {
 alias(libs.plugins.android.application)
 alias(libs.plugins.compose.compiler)
 alias(libs.plugins.kotlin.serialization)
 alias(libs.plugins.ksp)
}
android {
 namespace = "com.androidengineers.pocketstories"
 compileSdk = 36
 defaultConfig {
  applicationId = "com.androidengineers.pocketstories"
  minSdk = 31
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
 implementation(libs.okhttp)
 testImplementation(libs.mockwebserver)
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
 implementation(libs.litert.lm)
 implementation(libs.camera.view)
 implementation(libs.camera.camera2)
 implementation(libs.camera.lifecycle)
 implementation(libs.androidx.lifecycle.process)
 implementation(libs.room.runtime)
 implementation(libs.room.ktx)
 ksp(libs.room.compiler)

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
