plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.finalcalcihide"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.finalcalcihide"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.airbnb.android:lottie:6.4.1")

    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.balysv:material-ripple:1.0.2")


    implementation ("androidx.media3:media3-exoplayer:1.3.1")
    implementation ("androidx.media3:media3-ui:1.3.1")

    implementation("net.objecthunter:exp4j:0.4.8")

    implementation("com.google.android.material:material:1.8.0")
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")


    implementation("com.github.abhishekti7:UnicornFilePicker:1.0.2")





    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}