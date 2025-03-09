plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services") // Firebase Plugin
}

android {
    namespace = "com.example.scratch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.scratch"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.material)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database) // Firebase Analytics
}

