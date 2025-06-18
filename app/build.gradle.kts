plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.example.playlistmaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.playlistmaker"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8

    kotlinOptions {
        jvmTarget = "17"
    }
//        jvmTarget = "1.8"

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation(libs.gson)
    implementation(libs.androidx.recyclerview)

//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation("com.github.bumptech.glide:glide:4.12.0")
//    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
//    implementation("com.google.code.gson:gson:2.8.8")
//    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

//    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
//    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")

    // sprint 13
//    implementation ("androidx.constraintlayout:constraintlayout:2.0.0")

    // ViewPager2
//    implementation "androidx.viewpager2:viewpager2:1.0.0"

    implementation (libs.androidx.viewpager2)

    implementation (libs.koin.android)

    // drunk author sprint 18 so we do double steps
//    val fragment_version = "1.5.5"
//    implementation ("androidx.fragment:fragment-ktx:$fragment_version")
//    после обновить -> в toml fragmentKtx = "1.7.1"

    implementation (libs.androidx.fragment.ktx)

    // previous mess for info


//    implementation (libs.material.v161)

//    implementation(libs.glide)
//    annotationProcessor (libs.compiler)
//    implementation(libs.androidx.recyclerview)
//    implementation (libs.gson)
//    implementation (libs.retrofit)
//    implementation (libs.converter.gson)

//    implementation (libs.androidx.appcompat.v131)
//    implementation (libs.material.v140)

// удаленные из-за дублирования
//    implementation (libs.retrofit)
//    implementation (libs.converter.gson)

//    implementation (libs.glide.v4120)
//    annotationProcessor (libs.compiler.v4120)

//    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation ("com.github.bumptech.glide:glide:4.12.0")
//    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

//    implementation ("com.github.bumptech.glide:glide:4.14.2")
//    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")

}