plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("androidx.navigation.safeargs.kotlin") version "2.9.1"
    id("kotlin-kapt")

    // Google services / Crashlytics
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.playlistmaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.playlistmaker"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        isCoreLibraryDesugaringEnabled = true    // ✅ включён desugaring
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { viewBinding = true }
}

dependencies {
    // --- AndroidX / UI ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment.ktx)

    // --- Tests ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Networking / JSON / Images ---
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.glide)
    kapt(libs.compiler)
    implementation(libs.gson)

    // --- Arch ---
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.android)
    implementation(libs.adapterdelegates4.kotlin.dsl)
    implementation(libs.adapterdelegates4.kotlin.dsl.viewbinding)

    // --- Room ---
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler) // (ксп не используем)
    implementation(libs.androidx.room.ktx)

    // --- Navigation ---
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- Badges ---
    implementation(libs.shortcutbadger)

    // --- Firebase (BoM) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.functions.ktx)

    // --- NewPipeExtractor + protobuf-lite ---
    // ✅ v0.24.8 из JitPack; исключаем full protobuf
    implementation(libs.newpipeextractor) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    // ✅ один рантайм protobuf (Lite)
    implementation(libs.protobuf.javalite.v3253)

    // --- Media3 (воспроизведение/контролы) ---
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // --- Desugaring Java 8/Time/NIO (для старых устройств) ---
    // ❗ Заменили NIO-вариант на базовый пакет desugar_jdk_libs
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.conscrypt.android)
}

// Один рантайм protobuf (lite)
val protobufLite = "3.25.3"
val grpcLite = "1.62.2"
val commonProtosLite = "2.41.0"

configurations.configureEach {
    // не пропускаем full-артефакты
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "io.grpc", module = "grpc-protobuf")
}

configurations.all {
    resolutionStrategy {
        force(
            "com.google.protobuf:protobuf-javalite:$protobufLite",
            "io.grpc:grpc-protobuf-lite:$grpcLite",
            "io.grpc:grpc-stub:$grpcLite",
            "io.grpc:grpc-api:$grpcLite",
            "io.grpc:grpc-context:$grpcLite"
        )
        eachDependency {
            if (requested.group == "io.grpc" && requested.name == "grpc-protobuf") {
                useTarget("io.grpc:grpc-protobuf-lite:$grpcLite")
                because("на Android нужны lite-артефакты grpc")
            }
            if (requested.group == "com.google.protobuf" && requested.name == "protobuf-java") {
                useTarget("com.google.protobuf:protobuf-javalite:$protobufLite")
                because("один рантайм protobuf (lite)")
            }
            if (requested.group == "com.google.api.grpc" && requested.name == "proto-google-common-protos") {
                useTarget("com.google.api.grpc:proto-google-common-protos:$commonProtosLite:lite")
                because("исключаем full протосы")
            }
        }
    }
}
