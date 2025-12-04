@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "com.yogeshpaliyal.shared"
        compileSdk = 36
        minSdk = 24

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
//    val xcfName = "sharedKit"
//
//    iosX64 {
//        binaries.framework {
//            baseName = xcfName
//        }
//    }
//
//    iosArm64 {
//        binaries.framework {
//            baseName = xcfName
//        }
//    }
//
//    iosSimulatorArm64 {
//        binaries.framework {
//            baseName = xcfName
//        }
//    }
    wasmJs {
        browser()
        binaries.executable()
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(libs.tabler.icons)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.navigation3.runtime)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.haze)
                implementation(libs.haze.materials)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)
            //                implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-json:1.6.0") // Or the latest version
                // Add KMP dependencies here
            }
        }



        wasmJsMain {
            dependencies {
                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

//        iosMain {
//            dependencies {
//                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
//                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
//                // part of KMP’s default source set hierarchy. Note that this source set depends
//                // on common by default and will correctly pull the iOS artifacts of any
//                // KMP dependencies declared in commonMain.
//            }
//        }
    }

}

compose.resources {
    publicResClass = true
}