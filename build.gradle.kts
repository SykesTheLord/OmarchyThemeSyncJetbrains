fun prop(name: String): String = providers.gradleProperty(name).get()

plugins {
    id("java")
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = prop("pluginGroup")
version = prop("pluginVersion")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(prop("platformType"), prop("platformVersion"))
        pluginVerifier()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = prop("pluginSinceBuild")
            val until = providers.gradleProperty("pluginUntilBuild").orNull
            // Blank property => open-ended upper bound so newer IDEs (2026.x) still load it.
            if (until.isNullOrBlank()) untilBuild = provider { null } else untilBuild = until
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    // The bundled sandbox IDE is used by runIde for manual verification.
    // Optionally auto-open a project: ./gradlew runIde -PrunIdeProject=/path/to/project
    runIde {
        providers.gradleProperty("runIdeProject").orNull?.let { args(it) }
    }
}
