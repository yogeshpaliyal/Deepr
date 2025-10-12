plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    id("org.jmailen.kotlinter")
//     id("com.google.gms.google-services")
//     id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.yogeshpaliyal.deepr"
    compileSdk = 36
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.yogeshpaliyal.deepr"
        minSdk = 24
        targetSdk = 36
        versionCode = 17
        versionName = "1.0.16"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    productFlavors {
        create("free") {
            isDefault = true
        }
        create("freePlaystore") {

        }
        create("pro") {
            applicationIdSuffix = ".pro"

        }
    }

    sourceSets {
        // Configure the pro flavor to use the shared source set
        named("pro") {
            java.srcDirs("src/proFreePlaystore/java")
            kotlin.srcDirs("src/proFreePlaystore/kotlin")
            res.srcDirs("src/proFreePlaystore/res")
        }

        // Configure the freePlayStore flavor to use the shared source set
        named("freePlaystore") {
            java.srcDirs("src/proFreePlaystore/java")
            kotlin.srcDirs("src/proFreePlaystore/kotlin")
            res.srcDirs("src/proFreePlaystore/res")
        }
    }


    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions("default")

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystores/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
}

sqldelight {
    databases {
        create("DeeprDB") {
            packageName.set("com.yogeshpaliyal.deepr")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.android.driver)
    implementation(libs.coroutines.extensions)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.tabler.icons)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.compose.qr.code)
    implementation(libs.accompanist)
    implementation(libs.opencsv)
    implementation(libs.koin.compose)
    implementation(libs.zxing.scanner)
    ktlint(libs.ktlint)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.jsoup)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.ktor.client.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.documentfile)

    // Add Firebase dependencies to pro and freePlayStore flavors specifically
    "proImplementation"(platform(libs.firebase.bom))
    "proImplementation"(libs.firebase.analytics)
    "proImplementation"(libs.firebase.crashlytics.ndk)

    "freePlaystoreImplementation"(platform(libs.firebase.bom))
    "freePlaystoreImplementation"(libs.firebase.analytics)
    "freePlaystoreImplementation"(libs.firebase.crashlytics.ndk)
}

kotlinter {
    ktlintVersion = "1.5.0"
    ignoreFormatFailures = true
    ignoreLintFailures = false
    reporters = arrayOf("checkstyle")
}

tasks.check {
    dependsOn("installKotlinterPrePushHook")
}
