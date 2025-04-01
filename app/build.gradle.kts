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
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // Firebase BoM (Bill of Materials) - Keeps all Firebase dependencies at the same version
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

    // Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // AndroidX and Material components
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.material)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.work:work-runtime:2.9.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CircleImageView for profile images
    implementation("de.hdodenhof:circleimageview:3.1.0")
}

