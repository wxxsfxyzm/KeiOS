import java.util.Properties

val localProps = Properties().apply {
    val localPropsFile = file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use(::load)
    }
}

fun propOrLocal(key: String): String? {
    return localProps.getProperty(key)
}

val gprUser = propOrLocal("gpr.user") ?: System.getenv("GITHUB_ACTOR")
val gprKey = propOrLocal("gpr.key") ?: System.getenv("GITHUB_TOKEN")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(url = "https://maven.pkg.github.com/compose-miuix-ui/miuix") {
            if (!gprUser.isNullOrBlank() && !gprKey.isNullOrBlank()) {
                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "KeiOSDemo"
include(":app")
