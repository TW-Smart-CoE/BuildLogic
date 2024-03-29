@file:Suppress("UnstableApiUsage")

fun readConfig(name: String): String {
    return settings.extensions.extraProperties.properties[name] as String?
        ?: System.getenv(name) ?: ""
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()

        if (readConfig("MAVEN_REPO").isNotEmpty()) {
            maven {
                url = uri(readConfig("MAVEN_REPO"))
                isAllowInsecureProtocol = true
                credentials {
                    username = readConfig("MAVEN_USER")
                    password = readConfig(("MAVEN_PWD"))
                }
            }
        }
    }

    versionCatalogs {
        create("libs") {
            from("io.github.ssseasonnn:VersionCatalog:0.0.4")
        }
    }
}
