plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "br.com.trilhati"
    compileSdk = 37

    defaultConfig {
        applicationId = "br.com.trilhati"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        // Avisos de "há versão nova" quebrariam o build sem motivo; as versões são revisadas à mão no catálogo.
        disable += setOf("NewerVersionAvailable", "GradleDependency")
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}

// `./gradlew check` também valida os pacotes de conteúdo (CLAUDE.md).
val validateContent = tasks.register<Exec>("validateContent") {
    group = "verification"
    description = "Valida os pacotes de conteúdo com tools/validate_content.py"
    val contentDir = layout.projectDirectory.dir("src/main/assets/content")
    val script = rootProject.layout.projectDirectory.file("tools/validate_content.py")
    inputs.dir(contentDir)
    inputs.file(script)
    commandLine("python3", "-I", script.asFile.absolutePath, contentDir.asFile.absolutePath)
}

tasks.matching { it.name == "check" }.configureEach { dependsOn(validateContent) }
