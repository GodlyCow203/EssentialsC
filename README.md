<div align="center">

# Comparison

</div>

EssentialsC started as an alternative to EssentialsX, so here is a straight comparison. I appreciate every single one of you using EssentialsC

### Where EssentialsC differs

| Area | EssentialsC | EssentialsX |
| :--- | :--- | :--- |
| **Vanilla commands** | Every command ships at `normal` priority, so vanilla `/gamemode`, `/give` and friends keep working no matter what. You can still reach everything through `/essentialsc:<command>`, and each command can be turned off, reprioritized, aliased, cooled down or given custom help text in `commands.yml` | Takes over vanilla commands by default, and you have to dig through `overridden-commands` and `disabled-commands` to stop it |
| **Modularity** | Almost everything can be switched off completely. Don't use homes, kits, TPA or punishments? Turn them off and they are really gone: no background tasks, no listeners, commands unregistered like the feature never existed | Mostly everything loads whether you use it or not |
| **Storage** | One SQLite database (or MySQL if you want) holding players, homes, kits, punishments and economy. No pile of per-player files | One YAML file per player on disk |
| **Languages & formatting** | 29 languages built in, and each player picks their own with `/language`. Messages use MiniMessage, so gradients, hover text and RGB just work | One language for the whole server, and old school `&` color codes |
| **Player-facing GUIs** | Shop, auction house, kits and RTP open real inventories you can click through. No chat walls, and no extra plugins needed for a shop or an AH | Mostly chat and commands. A shop or an auction house means installing something else |
| **Switching over** | Coming from EssentialsX? `/migration essentialsx` pulls over users, homes, warps, bans, mutes and economy, and dry-run mode shows what would happen first | No importer, you start from zero |

### Where EssentialsX still wins

* **Older setups.** EssentialsX runs on Spigot and on versions going back years. EssentialsC needs Paper and 1.20.6+, so on old setups EssentialsX is your only pick of the two.
* **Mail.** EssentialsX has `/mail`. EssentialsC doesn't, so for offline messages you are stuck with `/msg` history or Discord.
* **Track record.** EssentialsX has been around for over a decade and half the plugin world builds on it. EssentialsC is younger, so check the [issue tracker](https://github.com/GodlyCow203/EssentialsC/issues) before moving a big network over.

---

<div align="center">

# About

</div>

EssentialsC is a simple, lightweight, and modern all-in-one plugin made to give your Minecraft server everything it needs.

---

<div align="center">

# Features

</div>

<details>
<summary><b>Click to expand full feature list</b></summary>

### Economy & Commerce
* **Auction House**
* **Shop**
* **Economy**

### Navigation & Teleportation
* **Homes**
* **Warps**
* **Wild Teleport (RTP)**

### Administration & Management
* **Moderation Tools**
* **Scoreboard**
* **Nicknames**
* **Discord Notifications**
* **Kits**
* **90+ Commands**

*...and more!*

</details>

---

<div align="center">

# Compatibility

</div>

EssentialsC is built for Bukkit-based servers and supports **Minecraft 1.20.6 - 26.2+**

<details>
<summary><b>Supported Software & System Requirements</b></summary>

### Software Support


| Software | Status |
| :--- | :--- |
| **Paper** | Supported |
| **Purpur** | Supported |
| **Folia** | Supported |
| **Other Forks** | Supported* |
| **Spigot** | Not Supported |
| **Bukkit** | Not Supported |

*\*Forks that do not modify the core Bukkit API are supported.*

### Requirements

* **Java Version:** 21+
* **Minecraft Version:** 1.20.6+
* **Server Platform:** A supported server software listed above

</details>

---

<div align="center">

# Project Information

</div>

<details>
<summary><b>Use of AI</b></summary>

I use AI as a tool while developing EssentialsC.

* A small part of the core code was generated with AI, but was reviewed, tested, and adapted by me.
* AI was also used for the language files and a larger part of the configs, mainly the shop sections.
* I still review and handle the final implementation myself.

</details>

<details>
<summary><b>Contributing</b></summary>

Contributions are always welcome! If you have an idea, find a bug, or want to improve something, feel free to open an issue or submit a pull request on GitHub.

Before contributing, please make sure your changes fit the project and are properly tested.

</details>

---

> EssentialsC is not affiliated with or endorsed by EssentialsX or Mojang Studios.
> EssentialsC is developed by me in my free time.

---

<div align="center">

[![Modrinth](https://img.shields.io/modrinth/dt/essentialsc?label=Modrinth&logo=modrinth&color=1B9100)](https://modrinth.com/plugin/essentialsc)
[![JitPack](https://img.shields.io/jitpack/version/GodlyCow203/EssentialsC?label=JitPack&logo=jitpack)](https://jitpack.io/#GodlyCow203/EssentialsC/)
![License](https://img.shields.io/github/license/GodlyCow203/EssentialsC?color=F54927)
[![Discord](https://img.shields.io/discord/1348765054983737467?label=Discord&logo=discord&color=276CF5)](https://discord.gg/...)
![Last Commit](https://img.shields.io/github/last-commit/GodlyCow203/EssentialsC?color=white)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-brightgreen)

![Paper](https://img.shields.io/badge/Paper-Supported-75B9FF)
![Spigot](https://img.shields.io/badge/Spigot-Supported-FFDF75)
![Purpur](https://img.shields.io/badge/Purpur-Supported-E64FFF)
![Folia](https://img.shields.io/badge/Folia-Supported-27F549)

</div>

---

![bStats](https://bstats.org/signatures/bukkit/EssentialsC.svg)