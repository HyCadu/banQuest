plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Assinatura fixa do BanQuest. A chave NUNCA fica no repositório: o caminho e a senha vêm de variáveis de ambiente
 * (ou de propriedades do Gradle fora do projeto, em ~/.gradle/gradle.properties). Sem elas, o build usa a chave de debug
 * da máquina, e o APK resultante NÃO serve para distribuir (tools/build_apk.sh confere isso). Ver docs/ASSINATURA.md.
 */
fun signingValue(env: String, prop: String): String? =
    providers.environmentVariable(env).orElse(providers.gradleProperty(prop)).orNull?.takeIf { it.isNotBlank() }

val banquestKeystore = signingValue("BANQUEST_KEYSTORE_FILE", "banquest.signing.storeFile")
val banquestPassword = signingValue("BANQUEST_KEYSTORE_PASSWORD", "banquest.signing.storePassword")
val banquestSigned = banquestKeystore != null && banquestPassword != null

android {
    namespace = "br.com.trilhati"
    compileSdk = 37

    defaultConfig {
        applicationId = "br.com.trilhati"
        minSdk = 26
        targetSdk = 37
        versionCode = 22
        versionName = "0.13.0"
        // Projeto Supabase (chave pública/publishable: pode ir no app; as regras RLS protegem os dados).
        buildConfigField("String", "SUPABASE_URL", "\"${providers.gradleProperty("trilha.supabaseUrl").getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${providers.gradleProperty("trilha.supabaseKey").getOrElse("")}\"")
    }

    signingConfigs {
        if (banquestSigned) {
            create("banquest") {
                storeFile = file(checkNotNull(banquestKeystore))
                storePassword = banquestPassword
                keyAlias = signingValue("BANQUEST_KEY_ALIAS", "banquest.signing.keyAlias") ?: "banquest"
                keyPassword = signingValue("BANQUEST_KEY_PASSWORD", "banquest.signing.keyPassword") ?: banquestPassword
            }
        }
    }

    buildTypes {
        // Com a chave disponível, os dois tipos de build saem com a mesma assinatura (o APK distribuído é o debug,
        // sem R8, que é o que os testes cobrem; a assinatura é o que importa para atualizar por cima).
        debug {
            if (banquestSigned) signingConfig = signingConfigs.getByName("banquest")
        }
        release {
            if (banquestSigned) signingConfig = signingConfigs.getByName("banquest")
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
        buildConfig = true
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
    implementation(libs.androidx.work.runtime.ktx)

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

// Questões de provas anteriores: valida a fonte (content/provas) e confere se o pacote do app foi regerado.
val validateProvas = tasks.register<Exec>("validateProvas") {
    group = "verification"
    description = "Valida content/provas com tools/validate_provas.py"
    val sourceDir = rootProject.layout.projectDirectory.dir("content/provas")
    val script = rootProject.layout.projectDirectory.file("tools/validate_provas.py")
    inputs.dir(sourceDir)
    inputs.file(script)
    commandLine("python3", "-I", script.asFile.absolutePath, sourceDir.asFile.absolutePath)
}
val checkProvasPack = tasks.register<Exec>("checkProvasPack") {
    group = "verification"
    description = "Confere se assets/provas está em dia com content/provas (tools/build_provas.py --check)"
    val script = rootProject.layout.projectDirectory.file("tools/build_provas.py")
    inputs.dir(rootProject.layout.projectDirectory.dir("content/provas"))
    inputs.dir(layout.projectDirectory.dir("src/main/assets/provas"))
    inputs.file(script)
    commandLine("python3", "-I", script.asFile.absolutePath, "--check")
}

tasks.matching { it.name == "check" }.configureEach { dependsOn(validateContent, validateProvas, checkProvasPack) }
