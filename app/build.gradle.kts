plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("androidx.navigation.safeargs.kotlin") version "2.9.1"
    id("kotlin-kapt")
}

// id("androidx.navigation.safeargs") version "2.9.1"

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
//        this.dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.glide)
//    annotationProcessor (libs.compiler)
    kapt(libs.compiler)
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
//    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    implementation (libs.androidx.viewpager2)

//    implementation("io.insert-koin:koin-android:3.5.3")
    implementation (libs.koin.android)

    // drunk author sprint 18 so we do double steps
//    val fragment_version = "1.5.5"
//    implementation ("androidx.fragment:fragment-ktx:$fragment_version")
//    после обновить -> в toml fragmentKtx = "1.7.1"

    implementation (libs.androidx.fragment.ktx)

//    // Подключаем основную библиотеку
//    implementation 'com.hannesdorfmann:adapterdelegates4-kotlin-dsl:4.3.2'
//    implementation ("com.hannesdorfmann:adapterdelegates4-kotlin-dsl:4.3.2")

    implementation (libs.adapterdelegates4.kotlin.dsl)

//    // Подключаем модуль для работы с ViewBinding
//    implementation 'com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:4.3.2'
//    implementation ("com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:4.3.2")

    implementation (libs.adapterdelegates4.kotlin.dsl.viewbinding)

    // sprint 20 option 1
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation(libs.kotlinx.coroutines.android)

    // coroutines sprint 20 optoin 2 to replace debounce
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

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

    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
//    implementation (libs.androidx.fragment.ktx.v156) // doubling -> implementation(libs.koin.androidx.fragment)

//    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")
//    implementation ("androidx.navigation:navigation-ui-ktx:2.5.3")
//    implementation ("androidx.fragment:fragment-ktx:1.5.6")

//    implementation(libs.koin.androidx.fragment) -> создает лишний  конфликт
//    Но! 💡 Даже если implementation(libs.koin.android) подключен, тебе нужно убедиться,
//    что модуль для fragment тоже включён — потому что by viewModel() в фрагменте требует
//    отдельного артефакта, а koin-android по умолчанию не содержит расширения для фрагментов.
//    implementation("io.insert-koin:koin-androidx-fragment:3.5.3") // или актуальная версия

    //    val roomVersion = "2.5.1"
//
//    implementation("androidx.room:room-runtime:$roomVersion")
//    kapt("androidx.room:room-compiler:$roomVersion")
//
//    // Опционально: ktx для корутин
//    implementation("androidx.room:room-ktx:$roomVersion")

//        val roomVersion = "2.5.1"

    implementation(libs.androidx.room.runtime)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)

    // Опционально: ktx для корутин
    implementation(libs.androidx.room.ktx)

}