plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "be.ap.student.mobiledev_rently"
    compileSdk = 34

    defaultConfig {
        applicationId = "be.ap.student.mobiledev_rently"
        minSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        viewBinding.enable = true
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation ("org.osmdroid:osmdroid-android:6.1.14")
    implementation ("javax.xml.stream:stax-api:1.0-2")
    implementation ("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.0")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation ("com.google.firebase:firebase-auth:22.1.1")
    implementation (platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation ("androidx.core:core-ktx:1.9.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.20")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("androidx.activity:activity:1.9.3")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
}
