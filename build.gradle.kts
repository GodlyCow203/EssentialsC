plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.3" apply false
}

group = "net.godlycow.org"

allprojects {
    group = rootProject.group
    version = rootProject.version

    apply {
        plugin("java")
        plugin("java-library")
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    configurations.named("compileClasspath") {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }

    tasks.named<ProcessResources>("processResources") {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand("project" to mapOf("version" to project.version))
        }
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    }
}
