# EssentialsC Public API

[![Modrinth](https://img.shields.io/modrinth/dt/essentialsc?label=Modrinth&logo=modrinth&color=1B9100)](https://modrinth.com/plugin/essentialsc)
[![JitPack](https://img.shields.io/jitpack/version/GodlyCow203/EssentialsC?label=JitPack&logo=jitpack)](https://jitpack.io/#GodlyCow203/EssentialsC/)
![License](https://img.shields.io/github/license/GodlyCow203/EssentialsC?color=F54927)
![Discord](https://img.shields.io/discord/1348765054983737467?label=Discord&logo=discord&color=276CF5)
![Last Commit](https://img.shields.io/github/last-commit/GodlyCow203/EssentialsC?color=white)
![Repo Size](https://img.shields.io/github/repo-size/GodlyCow203/EssentialsC)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-brightgreen)
![Paper](https://img.shields.io/badge/Paper-Supported-75B9FF)
![Spigot](https://img.shields.io/badge/Spigot-Supported-FFDF75)
![Spigot](https://img.shields.io/badge/Purpur-Supported-E64FFF)







A public API for integrating with the EssentialsC Minecraft plugin.

---

## Repository Setup

### Maven

Add the JitPack repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add the Dependency to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.GodlyCow203</groupId>
        <artifactId>EssentialsC</artifactId>
        <version>v0.0.5</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle

Add the JitPack repository to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
Add the Dependency to your `build.gradle`:

```gradle
dependencies {
    compileOnly 'com.github.GodlyCow203:EssentialsC:v0.0.5'
}
```

### Gradle Kotlin DSL

Add the JitPack repository to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```
Add the Dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    compileOnly("com.github.GodlyCow203:EssentialsC:v0.0.5")
}
```





