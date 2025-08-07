// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.sqldelight) apply false
    id("org.jmailen.kotlinter") version "5.2.0" apply false
}

buildscript {
    dependencies {
//        classpath(libs.ktlint.compose)
//        classpath("io.nlopez.compose.rules:ktlint:0.4.27")
    }
}
