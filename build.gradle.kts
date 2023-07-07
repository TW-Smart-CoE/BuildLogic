@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)

    detektPlugins(libs.detekt.formatting)
}

gradlePlugin {
    plugins.register("buildLogicPlugin") {
        id = "build.logic"
        implementationClass = "BuildLogicPlugin"
    }

    plugins.register("buildSettingPlugin") {
        id = "build.setting"
        implementationClass = "BuildSettingPlugin"
    }
}
