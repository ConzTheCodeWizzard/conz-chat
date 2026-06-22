plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}
android {
    namespace = "com.conzchat.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.conzchat.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 408
        versionName = "4.0.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GIPHY_API_KEY", "\"gvnL7xPoArGXv6249XCJl87Hto1qv9wa\"")
        buildConfigField("String", "DEV_UID", "\"GAEtvdjvwla73GscQWnGthTPG6f1\"")
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
    flavorDimensions += "variant"
    productFlavors {
        create("original") {
            dimension = "variant"
            applicationId = "com.conzchat.app"
            resValue("string", "app_name", "ConzChat")
        }
        create("clone") {
            dimension = "variant"
            applicationId = "com.conzchat.app.clone"
            resValue("string", "app_name", "ConzChat Clone")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        abortOnError = false
    }
}
dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    // Media
    implementation("androidx.media:media:1.7.0")
    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    // Room (for Vault, Scheduler, Saved Accounts)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    // WorkManager (for scheduled messages)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // CameraX (for ConzLenses)
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    // ML Kit Face Detection (for filters)
    implementation("com.google.mlkit:face-detection:16.1.6")
    // Biometric (for vault fingerprint)
    implementation("androidx.biometric:biometric:1.1.0")
    // ExoPlayer (for video in feed)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    // OneSignal Push Notifications
    implementation("com.onesignal:OneSignal:5.1.31")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
