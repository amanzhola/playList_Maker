plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("androidx.navigation.safeargs.kotlin") version "2.9.1"
    id("kotlin-kapt")

    // Google services / Crashlytics
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

    // Kotlin Compose plugin под 2.0.21
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {

    // --- Compose BOM ---
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))

    // --- Базовые Compose модули ---
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Activity/Lifecycle/Nav для Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Инструменты для превью/тестов Compose
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

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
    kapt(libs.glide.compiler)
    implementation(libs.gson)

    // --- Arch ---
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // coroutines под Kotlin 2.0.x
    implementation(libs.kotlinx.coroutines.android)

    // --- Koin: ОДНА версия везде (3.5.6) ---
    // (берётся из catalog; см. libs.versions.toml ниже)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // --- Room ---
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // --- Navigation (Views) ---
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
    implementation(libs.newpipeextractor) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.protobuf", module = "protobuf-kotlin-lite")
    }
    implementation(libs.protobuf.javalite.v3253)
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.25.3")

    // --- Media3 ---
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // --- Desugaring Java 8/Time ---
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // --- Network misc ---
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.conscrypt.android)

    // Для AndroidViewBinding внутри Compose
    implementation(libs.androidx.ui.viewbinding)

    // Иконки
    implementation(libs.androidx.material.icons.extended)

    // --- Kotlin BOM + constraints: жёстко фиксируем 2.0.21 на всё kotlin-* ---
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.21"))
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common:2.0.21")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
        implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
    }

    implementation(libs.adapterdelegates4.kotlin.dsl)
    implementation(libs.adapterdelegates4.kotlin.dsl.viewbinding)

    implementation("io.coil-kt.coil3:coil:3.0.0")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")

    implementation(libs.androidx.constraintlayout.compose)
}
