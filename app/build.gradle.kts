plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

android {
    namespace = "com.example.appcars"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.appcars"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.room:room-runtime:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    // Modificacion: Dependencia de Google Play Services para obtener la ubicación GPS (FusedLocationProviderClient)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Nuevo Proyecto V2: Retrofit nos permite hacer peticiones HTTP/REST a la API pública de vehículos
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Nuevo Proyecto V2: Gson traduce automáticamente el texto JSON que devuelve la API a clases de Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Nuevo Proyecto V2: Soporte de corrutinas para Room, ViewModel y ciclo de vida (así no congelamos la pantalla)
    implementation("androidx.room:room-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}


