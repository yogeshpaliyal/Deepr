plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    id("org.jmailen.kotlinter")
}

android {
    namespace = "com.yogeshpaliyal.deepr"
    compileSdk = 36
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.yogeshpaliyal.deepr"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "1.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    productFlavors {
        create("free") {
            isDefault = true
        }
        create("pro") {
            applicationIdSuffix = ".pro"
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
    testImplementation(libs.junit)
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
    implementation("dev.chrisbanes.haze:haze:1.6.9")
    implementation("dev.chrisbanes.haze:haze-materials:1.6.9")
    implementation(libs.jsoup)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.ktor.client.android)
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
