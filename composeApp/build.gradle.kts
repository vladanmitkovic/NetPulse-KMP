import com.google.devtools.ksp.gradle.KspAATask
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ksp)
}

kotlin {

    // Suppress expect/actual classes Beta warning (applies globally)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(), // Intel Mac simulator
        iosArm64(), // Real iOS device
        iosSimulatorArm64(), // Apple Silicon Mac simulator
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            // Compose & UI
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // AndroidX & Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.composeVM)
            api(libs.koin.annotations)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            api(libs.sqldelight.coroutines)

            // Navigation
            implementation(libs.androidx.navigation.compose)

            // XML Serialization
            implementation(libs.xmlutil.serialization)
            implementation(libs.xmlutil.core)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.xml)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Icons
            implementation(compose.materialIconsExtended)
        }

        androidMain.dependencies {
            // Compose & UI
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Koin Android
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            // Logging
            implementation(libs.timber)

            // DataStore
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.datastore.core)

            // SQLDelight Android Driver
            implementation(libs.sqldelight.android.driver)

            // Ktor Engine
            implementation(libs.ktor.client.okhttp)

            // Ktor Serialization
            implementation(libs.ktor.serialization.kotlinx.xml)
        }

        iosMain.dependencies {
            // Koin
            implementation(libs.koin.core)

            // Ktor
            implementation(libs.ktor.client.core)

            // Ktor Engine for iOS
            implementation(libs.ktor.client.darwin)

            // SQLDelight Native Driver
            implementation(libs.sqldelight.native.driver)

            // Duplicate? Consider removing if not needed
            implementation(libs.ktor.client.darwin)
        }

        desktopMain.dependencies {
            // Compose Desktop
            implementation(compose.desktop.currentOs)

            // Coroutines Swing
            implementation(libs.kotlinx.coroutinesSwing)

            // Koin
            implementation(libs.koin.core)

            // Ktor Engine for Desktop
            implementation(libs.ktor.client.cio)

            // SQLDelight SQLite Driver
            implementation(libs.sqldelight.sqlite.driver)
        }

        // KSP generated sources
        sourceSets.named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }

        commonTest.dependencies {
            // Testing
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "me.mitkovic.kmp.netpulse"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "me.mitkovic.kmp.netpulse"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Debug Tools
    debugImplementation(compose.uiTooling)

    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
    add("kspDesktop", libs.koin.ksp.compiler)
}

compose.desktop {
    application {
        mainClass = "me.mitkovic.kmp.netpulse.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "me.mitkovic.kmp.netpulse"
            packageVersion = "1.0.0"

            modules("java.sql")
        }
    }
}

// SQLDelight configuration
sqldelight {
    databases {
        create("NetPulseDatabase") {
            packageName.set("me.mitkovic.kmp.netpulse.data.local.database")
        }
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

// Make Kotlin compilation depend on KSP metadata
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// KSP task dependencies - ensure proper ordering
tasks.withType<KspAATask>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
