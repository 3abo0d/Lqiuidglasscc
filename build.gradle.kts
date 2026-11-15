plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.liquidglass.cc"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.liquidglass.cc"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-alpha"
    }
}
