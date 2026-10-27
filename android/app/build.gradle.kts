plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.nearhelp"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.nearhelp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        val backendUrl = providers.gradleProperty("nearhelpBaseUrl").orElse("https://nearhelp-backend-7sfj.onrender.com/").get().trimEnd('/') + "/"
        require(backendUrl.startsWith("https://") || backendUrl.startsWith("http://")) { "nearhelpBaseUrl must be an HTTP(S) URL" }
        buildConfigField("String", "API_BASE_URL", "\"$backendUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    testOptions {
      unitTests.isReturnDefaultValues = true
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  implementation("com.google.firebase:firebase-messaging:25.0.1")
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Material Icons Extended
  implementation(libs.androidx.compose.material.icons.extended)

  // Security Crypto (EncryptedSharedPreferences)
  implementation(libs.androidx.security.crypto)

  // Networking (Retrofit & OkHttp)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.gson)

  // Serialization
  implementation(libs.kotlinx.serialization.json)

  // Google Maps SDK & Compose
  implementation(libs.maps.compose)
  implementation(libs.play.services.maps)
}
