plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {

    namespace = "com.example.offlinenotepad"   // keep this package everywhere
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.offlinenotepad"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("sapphire-key.jks") // Key store file name
            storePassword = "sapphire!@#$%*&^123" // Your keystore password
            keyAlias = "sapphireKey0" // Key alias
            keyPassword = "sapphire!@#$%*&^123" // Key password
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // Keep false for now to avoid shrinking issues
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            // Debug config stays as it is
        }
    }


    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    kotlinOptions { jvmTarget = "17" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(bom)
    androidTestImplementation(bom)


    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.material:material:1.12.0")


}
