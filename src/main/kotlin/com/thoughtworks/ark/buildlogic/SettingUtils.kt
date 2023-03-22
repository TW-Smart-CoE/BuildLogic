@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

package com.thoughtworks.ark.buildlogic

import org.gradle.api.artifacts.DependencySubstitutions
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.DependencyResolutionManagement
import org.gradle.api.internal.ProcessOperations
import org.gradle.api.internal.file.FileOperations
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.ByteArrayOutputStream

val Settings.processService
    get() = serviceOf<ProcessOperations>()

val Settings.fileService
    get() = serviceOf<FileOperations>()

fun getEnv(name: String): String {
    return System.getenv(name) ?: ""
}

fun Settings.readConfig(name: String): String {
    return extensions.extraProperties.properties[name] as String? ?: getEnv(name)
}

fun Settings.configPrivateMaven(repositoryHandler: RepositoryHandler) {
    if (readConfig("MAVEN_REPO").isNotEmpty()) {
        repositoryHandler.maven {
            url = fileService.uri(readConfig("MAVEN_REPO"))
            isAllowInsecureProtocol = true
            credentials {
                username = readConfig("MAVEN_USER")
                password = readConfig(("MAVEN_PWD"))
            }
        }
    } else {
        System.err.println("Please config your private Maven repo!")
    }
}

fun DependencyResolutionManagement.configVersionCatalog() {
    versionCatalogs {
        create("libs") {
            from("io.github.ssseasonnn:VersionCatalog:0.0.3")
        }
    }
}

fun Settings.execCmd(workPath: String, cmd: String): String {
    val stdout = ByteArrayOutputStream()
    processService.exec {
        workingDir = fileService.file(workPath)
        commandLine(cmd.split(" "))
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

/**
 * Config Feature for local debug
 */
fun Settings.configFeature(
    featureName: String,
    featureGitUrl: String,
    featureModuleName: String = "",
    featureApiModuleName: String = "",
    enable: Boolean = true
) {
    if (enable) {
        val featureLocalPath = createFeaturePath(featureName)
        cloneFeature(featureName, featureGitUrl, featureLocalPath)

        includeBuild(featureLocalPath) {
            dependencySubstitution {
                replaceModule(featureModuleName, featureApiModuleName)
            }
        }
    }
}

private fun Settings.cloneFeature(featureName: String, featureGitUrl: String, featureLocalPath: String) {
    if (!fileService.file(featureLocalPath).exists()) {
        println("Init feature $featureName...")
        // clone build logic to BuildLogic dir
        val result = execCmd(
            ".",
            "git clone -b main $featureGitUrl $featureLocalPath"
        )
        if (result.isNotEmpty()) {
            println(result)
        }
        println("Feature $featureName init success")
    } else {
        println("Update feature $featureName...")
        val result = execCmd(featureLocalPath, "git pull")
        if (result.isNotEmpty()) {
            println(result)
        }
        println("Update feature $featureName success")
    }
}

private fun createFeaturePath(featureName: String): String {
    // If it is build by Jenkins, use the project directory to keep BuildLogic
    return if (getEnv("BUILD_ID").isEmpty()) {
        "../$featureName"
    } else {
        featureName
    }
}

private fun DependencySubstitutions.replaceModule(featureModuleName: String, featureApiModuleName: String) {
    if (featureModuleName.isNotEmpty()) {
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureModuleName-dev"))
            .using(project(":$featureModuleName"))
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureModuleName-uat"))
            .using(project(":$featureModuleName"))
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureModuleName-staging"))
            .using(project(":$featureModuleName"))
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureModuleName-prod"))
            .using(project(":$featureModuleName"))
    }

    if (featureApiModuleName.isNotEmpty()) {
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureApiModuleName-dev"))
            .using(project(":$featureApiModuleName"))
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureApiModuleName-uat"))
            .using(project(":$featureApiModuleName"))
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureApiModuleName-staging"))
            .using(project(":$featureApiModuleName"))
        substitute(module("${MavenConfig.MAVEN_GROUP_ID}:$featureApiModuleName-prod"))
            .using(project(":$featureApiModuleName"))
    }
}