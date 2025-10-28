plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.vcsd.leaselogic"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vcsd.leaselogic"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "11"
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("com.github.bumptech.glide:glide:5.0.5")
    kapt("com.github.bumptech.glide:compiler:5.0.5")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.airbnb.android:lottie:6.6.10")
    implementation("com.airbnb.android:lottie:6.3.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.tbuonomo:dotsindicator:5.1.0")
    implementation("com.tbuonomo:dotsindicator:5.0")

    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.android.material:material:1.11.0")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("androidx.gridlayout:gridlayout:1.1.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    implementation("com.facebook.shimmer:shimmer:0.5.0")







    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
// Firebase
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-firestore:26.0.1")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.squareup.picasso:picasso:2.8") // for image loading
    implementation("com.google.firebase:firebase-storage-ktx:21.0.2")
    implementation("com.squareup.picasso:picasso:2.71828") // for image loading

// Optional UI helpers
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.13.0")
}

private fun DependencyHandlerScope.kapt(string: String) {}
