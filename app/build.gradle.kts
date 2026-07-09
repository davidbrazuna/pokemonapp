import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Loaded from keystore.properties (gitignored, see keystore.properties.example)
// rather than hardcoded, so signing credentials never enter version control.
// Absent entirely on a fresh clone or CI without it — assembleDebug still
// works either way; only a signed assembleRelease needs this to be present.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    } else {
        logger.warn(
            "keystore.properties not found — assembleRelease will produce an " +
                "UNSIGNED APK/AAB, not an installable release build. See " +
                "keystore.properties.example."
        )
    }
}

android {
    namespace = "com.davidbrazuna.pokemonapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.davidbrazuna.pokemonapp"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                fun requiredProperty(key: String): String =
                    keystoreProperties.getProperty(key)
                        ?: throw GradleException(
                            "keystore.properties is missing '$key' — check it against " +
                                "keystore.properties.example."
                        )
                storeFile = file(requiredProperty("storeFile"))
                storePassword = requiredProperty("storePassword")
                keyAlias = requiredProperty("keyAlias")
                keyPassword = requiredProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Safe now that Gson (reflection-based, needed hand-written keep
            // rules) is gone — Room, Hilt, Retrofit/OkHttp, Coil, Paging, and
            // kotlinx.serialization all ship their own consumer R8 rules.
            isMinifyEnabled = true
            isShrinkResources = true
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    buildFeatures {
        // No View-based screens remain — the app is Compose end to end.
        buildConfig = true
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    // Interceptor is added only when BuildConfig.DEBUG is true (see NetworkModule)
    implementation(libs.okhttp.logging.interceptor)
    // Compose — the BOM aligns all Compose artifact versions.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Paging 3 for the list (runtime + Compose integration).
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Coil 3 for image loading in Compose; coil-network-okhttp reuses our OkHttp.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Room for offline caching. room-paging bridges Room's generated PagingSource
    // to Paging 3; room-compiler runs through KSP (no kapt in this project).
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Hilt for DI; the compiler runs through KSP. hilt-navigation-compose wires
    // hiltViewModel() into the Compose NavHost.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Splash screen: compat shim so the same behavior (icon + exit animation)
    // works below API 31 too, since minSdk is 28.
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}