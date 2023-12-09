plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "de.herrmann_engel.rbv"
        minSdk = 25
        targetSdk = 34
        versionCode = 72
        versionName = "3.3.4"
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "de.herrmann_engel.rbv"
}
dependencies {
    val kotlinVersion = rootProject.extra.get("kotlinVersion") as String
    val roomVersion = "2.6.1"
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.opencsv:opencsv:5.9")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    implementation("me.saket:better-link-movement-method:2.2.0")
    implementation("com.vanniktech:emoji-twitter:0.17.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.github.rtugeek:colorseekbar:2.0.3")
    implementation("com.atlassian.commonmark:commonmark:0.13.0")
    //match Markwon version of commonmark
}
