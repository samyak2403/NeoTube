import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.samyak.neotube"
    compileSdk = 36

    val rootLocalProperties = Properties()
    val rootLocalPropertiesFile = rootProject.file("local.properties")
    if (rootLocalPropertiesFile.exists()) {
        rootLocalPropertiesFile.inputStream().use { rootLocalProperties.load(it) }
    }

    defaultConfig {
        applicationId = "com.samyak.neotube"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val poTokenGoogleApiKey = (project.findProperty("poTokenGoogleApiKey") as? String)
            ?: rootLocalProperties.getProperty("poTokenGoogleApiKey")
            ?: System.getenv("POTOKEN_GOOGLE_API_KEY")
            ?: ""
        val escapedPoTokenKey = poTokenGoogleApiKey
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        buildConfigField("String", "POTOKEN_GOOGLE_API_KEY", "\"$escapedPoTokenKey\"")

        testInstrumentationRunner = "com.samyak.neotube.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
        
        multiDexEnabled = true

        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    flavorDimensions += "version"
    productFlavors {
        create("foss") {
            dimension = "version"
            isDefault = true
            buildConfigField("Boolean", "UPDATER_ENABLED", "false")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    signingConfigs {
        create("release") {
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { localProperties.load(it) }
            }

            storeFile = rootProject.file("release.keystore")
            storePassword = (project.findProperty("storePassword") as? String)
                ?: localProperties.getProperty("storePassword") 
                ?: System.getenv("STORE_PASSWORD") 
                ?: ""
            keyAlias = (project.findProperty("keyAlias") as? String)
                ?: localProperties.getProperty("keyAlias") 
                ?: System.getenv("KEY_ALIAS") 
                ?: ""
            keyPassword = (project.findProperty("keyPassword") as? String)
                ?: localProperties.getProperty("keyPassword") 
                ?: System.getenv("KEY_PASSWORD") 
                ?: ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        create("nightly") {
            initWith(getByName("release"))
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            val releaseKeystore = try { signingConfigs.getByName("release").storeFile } catch (e: Exception) { null }
            if (releaseKeystore?.exists() == true) {
                signingConfig = signingConfigs.getByName("release")
                println("Using RELEASE signing config with keystore: ${releaseKeystore.absolutePath}")
            } else {
                signingConfig = null
                println("WARNING: Release keystore not found. Building UNSIGNED release APK.")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    
    // --- Compose (Using BOM) ---
    implementation(platform(libs.androidx.compose.bom)) 
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material) 
    implementation(libs.androidx.material.icons.extended)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose) 

    // --- Lifecycle & Architecture ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Layouts ---
    implementation(libs.androidx.constraintlayout.compose)

    // --- Image Loading ---
    implementation(libs.coil.compose) 
    implementation(libs.picasso)
    implementation("androidx.palette:palette-ktx:1.0.0")

    // --- Dependency Injection ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // --- Data & Network ---
    implementation(libs.newpipe.extractor) 
    
    // Networking
    implementation(libs.okhttp)
    
    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.encoding)

    // Serialization & JSON
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    // Conscrypt for OkHttp TLS support on older Android versions
    implementation(libs.conscrypt.android)

    // --- Media Playback ---
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.androidx.media)

    // --- Database & Storage ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    implementation(libs.androidx.datastore.preferences)

    // --- Async & Utils ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    
    // RxJava (Required for NewPipeExtractor)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.multidex)

    implementation(libs.brotli) 
    implementation(libs.re2j)

    // Desugaring for older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")

    // --- Testing ---
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspTest("com.google.dagger:hilt-android-compiler:2.51.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Allow references to generated code
ksp {
}

hilt {
    enableTransformForLocalTests = false
}