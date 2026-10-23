plugins { id("com.android.application") }
val releaseKey = providers.environmentVariable("GLASSHOME_KEYSTORE").orNull
android {
 namespace = "local.glasshome"
 compileSdk = 35
 signingConfigs {
  if (releaseKey != null) create("release") {
   storeFile = file(releaseKey)
   storePassword = providers.environmentVariable("GLASSHOME_STORE_PASSWORD").get()
   keyAlias = providers.environmentVariable("GLASSHOME_KEY_ALIAS").get()
   keyPassword = providers.environmentVariable("GLASSHOME_KEY_PASSWORD").get()
  }
 }
 defaultConfig { applicationId = "local.glasshome"; minSdk = 26; targetSdk = 35; versionCode = 6; versionName = "1.3.2-compact" }
 buildTypes { getByName("release") {
  isDebuggable = false
  isMinifyEnabled = false
  if (releaseKey != null) signingConfig = signingConfigs.getByName("release")
 } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies { implementation("androidx.recyclerview:recyclerview:1.3.2") }

