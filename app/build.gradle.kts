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
        versionCode = 25
        versionName = "1.0.24"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mapOf(
            "clearPackageData" to "true",
        )
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    productFlavors {
        create("github") {
            isDefault = true
        }
        create("free") {

        }
        create("pro") {
            applicationIdSuffix = ".pro"
        }
    }

    sourceSets {
        // Configure the github flavor to use the shared source set
        named("github") {
            java.srcDirs("src/freeGithub/java")
            kotlin.srcDirs("src/freeGithub/kotlin")
            res.srcDirs("src/freeGithub/res")
        }

        // Configure the pro flavor to use the shared source set
        named("pro") {
            java.srcDirs("src/proFree/java")
            kotlin.srcDirs("src/proFree/kotlin")
            res.srcDirs("src/proFree/res")
        }

        // Configure the free flavor to use the shared source set
        named("free") {
            java.srcDirs("src/proFree/java", "src/freeGithub/java")
            kotlin.srcDirs("src/proFree/kotlin", "src/freeGithub/kotlin")
            res.srcDirs("src/proFree/res", "src/freeGithub/res")
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

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
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
    androidTestUtil(libs.orchestrator)
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
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.ktor.client.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.documentfile)

    "proImplementation"(platform(libs.firebase.bom))
    "proImplementation"(libs.firebase.analytics)
    "proImplementation"(libs.firebase.crashlytics.ndk)
    "proImplementation"(libs.play.review)
    "proImplementation"("com.google.android.gms:play-services-auth:21.5.0")
    "proImplementation"("com.google.api-client:google-api-client-android:2.8.1")
    "proImplementation"("com.google.apis:google-api-services-drive:v3-rev20230815-2.0.0")

    "freeImplementation"(platform(libs.firebase.bom))
    "freeImplementation"(libs.firebase.analytics)
    "freeImplementation"(libs.firebase.crashlytics.ndk)
    "freeImplementation"(libs.play.review)
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
