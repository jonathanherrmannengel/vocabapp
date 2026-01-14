plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    compileSdk = 36
    defaultConfig {
        applicationId = "de.herrmann_engel.rbv"
        minSdk = 25
        targetSdk = 36
        versionCode = 82
        versionName = "3.4.8"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".test"
        }
    }
    productFlavors {
        flavorDimensions += listOf("branding")
        create("rbv") {
            dimension = "branding"
            isDefault = true
            buildConfigField("String", "DB_NAME", "\"rbv_db\"")
            buildConfigField("String", "EXPORT_FILE_NAME", "\"rbv_backup\"")
        }
        create("av") {
            dimension = "branding"
            applicationId = "de.herrmann_engel.anjasvocab"
            buildConfigField("String", "DB_NAME", "\"anja\"")
            buildConfigField("String", "EXPORT_FILE_NAME", "\"anjasvocab\"")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    namespace = "de.herrmann_engel.rbv"
}
dependencies {
    val kotlinVersion = rootProject.extra.get("kotlinVersion") as String
    val roomVersion = "2.8.4"
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("com.google.android.material:material:1.13.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.opencsv:opencsv:5.12.0")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    implementation("me.saket:better-link-movement-method:2.2.0")
    implementation("com.vanniktech:emoji-twitter:0.23.0")
    implementation("io.coil-kt.coil3:coil:3.3.0")
    implementation("com.github.rtugeek:colorseekbar:2.1.0")
    implementation("com.atlassian.commonmark:commonmark:0.13.0")
    //match Markwon version of commonmark
}
