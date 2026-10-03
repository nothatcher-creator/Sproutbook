plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose"); id("com.google.devtools.ksp") }
android {
 namespace = "com.nothatcher.sproutbook"
 compileSdk { version = release(37) { minorApiLevel = 0 } }
 defaultConfig { applicationId = "com.nothatcher.sproutbook"; minSdk = 26; targetSdk = 37; versionCode = 30700; versionName = "3.7.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
 buildFeatures { compose = true; buildConfig = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 // An absent evaluation key fails signing; Gradle must never generate a replacement.
 val evaluationKey = providers.gradleProperty("sprout.debugKeystore").orElse(providers.environmentVariable("SPROUT_DEBUG_KEYSTORE"))
 signingConfigs.getByName("debug") { storeFile = rootProject.file(evaluationKey.orNull ?: "development-signing.keystore"); storePassword = "android"; keyAlias = "androiddebugkey"; keyPassword = "android" }
 buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17); optIn.addAll("androidx.compose.material3.ExperimentalMaterial3Api", "androidx.compose.foundation.ExperimentalFoundationApi") } }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
 implementation(project(":core"))
 implementation("com.airbnb.android:lottie-compose:6.7.1")
 implementation(platform("androidx.compose:compose-bom:2025.04.01"))
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-core")
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
 implementation("androidx.navigation:navigation-compose:2.8.9")
 implementation("androidx.datastore:datastore-preferences:1.1.4")
 implementation("androidx.work:work-runtime-ktx:2.10.1")
 implementation("androidx.room:room-runtime:2.7.1")
 implementation("androidx.room:room-ktx:2.7.1")
 ksp("androidx.room:room-compiler:2.7.1")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
 implementation("com.google.code.gson:gson:2.12.1")
 testImplementation("junit:junit:4.13.2")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test:runner:1.6.2")
 androidTestImplementation("androidx.test:core:1.6.1")
 androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
}
