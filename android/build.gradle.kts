plugins {
 id("com.android.application") version "9.1.1" apply false
 kotlin("jvm") version "2.2.10" apply false
 id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
 id("com.google.devtools.ksp") version "2.3.12" apply false
}
providers.environmentVariable("SPROUT_BUILD_ROOT").orNull?.let { root ->
 allprojects { layout.buildDirectory.set(file("$root/$name")) }
}
