# EssentialsC

[![Modrinth](https://img.shields.io/modrinth/dt/essentialsc?label=Modrinth&logo=modrinth&color=1B9100)](https://modrinth.com/plugin/essentialsc)
[![JitPack](https://img.shields.io/jitpack/version/GodlyCow203/EssentialsC?label=JitPack&logo=jitpack)](https://jitpack.io/#GodlyCow203/EssentialsC/)
![License](https://img.shields.io/github/license/GodlyCow203/EssentialsC?color=F54927)
![Discord](https://img.shields.io/discord/1348765054983737467?label=Discord&logo=discord&color=276CF5)
![Last Commit](https://img.shields.io/github/last-commit/GodlyCow203/EssentialsC?color=white)
![Repo Size](https://img.shields.io/github/repo-size/GodlyCow203/EssentialsC)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-brightgreen)
![Paper](https://img.shields.io/badge/Paper-Supported-75B9FF)
![Spigot](https://img.shields.io/badge/Spigot-Supported-FFDF75)
![Purpur](https://img.shields.io/badge/Purpur-Supported-E64FFF)

## Description

EssentialsC is a powerful and optimized Minecraft Bukkit plugin designed for modern servers. It adds a wide range of features including Kits, Homes, Warps, Shops, Wild Teleports, and much more, providing server owners with flexible tools to enhance gameplay.

The plugin is fully compatible with Minecraft versions `1.17–1.21+` and supports multiple server softwares, including [Purpur](https://purpurmc.org/download/purpur), [Paper](https://papermc.io/downloads/paper), [CraftBukkit](https://getbukkit.org/download/craftbukkit), and [Spigot](https://hub.spigotmc.org/jenkins/job/BuildTools/). Other forks such are also supported, provided they do not modify the Bukkit API. ( Example Forks: [Leaf](https://www.leafmc.one/download), [Leaves](https://leavesmc.org/downloads/leaves) and [Youer](https://mohistmc.com/downloadSoftware?project=youer&projectVersion=1.21.1) )

EssentialsC comes with over **70** fully configurable commands, each manageable through ``commands.yml``. It seamlessly integrates with popular plugins such as [TAB](https://www.spigotmc.org/resources/tab.57806/), [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/), [Floodgate](https://geysermc.org/download/?project=floodgate), [LuckPerms](https://luckperms.net/download), [Vault](https://www.spigotmc.org/resources/vault.34315/), and [DiscordSRV](https://www.spigotmc.org/resources/discordsrv.18494/), giving you extended functionality out-of-the-box.

For server migrations, EssentialsC includes a migration tool: `/migration essentialsx <flags>`, allowing easy transition from EssentialsX.

Actively maintained and regularly updated, I release weekly updates on Modrinth, ensuring you always have access to the latest features and improvements.

---

# A public API for integrating with the EssentialsC Minecraft plugin.

## Documentation

You can visit the whole [EssentialsCAPI documentation](https://api.godlycow.org/essc/index.html) at https://api.godlycow.org/essc/index.html

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





