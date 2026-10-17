<div align="center">

# EssentialsC

**An all-in-one essentials plugin for Paper servers that stays out of your way.**

Homes, warps, kits, a shop, an auction house and 90+ commands. Almost everything can be turned off if you don't need it, and every player can pick their own language.

[![Modrinth](https://img.shields.io/modrinth/dt/essentialsc?label=Modrinth&logo=modrinth&color=1B9100)](https://modrinth.com/plugin/essentialsc)
[![JitPack](https://img.shields.io/jitpack/version/GodlyCow203/EssentialsC?label=JitPack&logo=jitpack)](https://jitpack.io/#GodlyCow203/EssentialsC/)
[![License](https://img.shields.io/badge/License-Apache%202.0-F54927)](https://github.com/GodlyCow203/EssentialsC/blob/HEAD/LICENSE)
[![Discord](https://img.shields.io/discord/1348765054983737467?label=Discord&logo=discord&color=276CF5)](https://discord.gg/8MAuyrkZHf)
![Last Commit](https://img.shields.io/github/last-commit/GodlyCow203/EssentialsC?color=white)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.6%2B-brightgreen)

![Paper](https://img.shields.io/badge/Paper-Supported-75B9FF)
![Purpur](https://img.shields.io/badge/Purpur-Supported-E64FFF)
![Folia](https://img.shields.io/badge/Folia-Supported-27F549)
![Spigot](https://img.shields.io/badge/Spigot-Not%20Supported-red)

[Download](https://modrinth.com/plugin/essentialsc) · [Wiki](https://wiki.godlycow.org/essc) · [Issues](https://github.com/GodlyCow203/EssentialsC/issues)

</div>

---

<div align="center">

# EssentialsC vs EssentialsX

EssentialsC started as an alternative to EssentialsX, so here's a straight comparison.

</div>

| | EssentialsC | EssentialsX |
| :--- | :--- | :--- |
| **Vanilla commands** | Leaves them alone, everything runs at `normal` priority | Takes over vanilla commands by default |
| **Turning stuff off** | Almost everything can be switched off completely | Core features load together, plus optional add-on modules |
| **Storage** | One SQLite database, or MySQL if you want | One YAML file per player |
| **Languages** | 29 built in, each player picks theirs with `/language` | One language for the whole server, per-player is opt-in |
| **Menus** | Shop, auction house, kits and RTP all open real menus | Mostly chat and commands, a shop or AH means another plugin |
| **Switching over** | `/migration essentialsx` with a dry-run mode | No importer |
| **Mail** | No `/mail` | Has `/mail` |
| **Older servers** | Paper and 1.20.6+ only | Works on Spigot and versions going back years |
| **Track record** | Newer project | Over a decade, half the plugin world builds on it |

<details>
<summary><b>Want the longer version? Click here</b></summary>

<br>

* **Vanilla commands.** Everything ships at `normal` priority, so vanilla `/gamemode`, `/give` and friends keep working no matter what. You can still reach everything through `/essentialsc:<command>`, and every command can be turned off, reprioritized, aliased, put on a cooldown or given custom help text in `commands.yml`. EssentialsX takes over vanilla commands by default, and you have to dig through `overridden-commands` and `disabled-commands` to stop it.
* **Turning stuff off.** Don't use homes, kits, TPA or punishments? Turn them off and they're really gone: no background tasks, no listeners, and the commands don't even get registered. EssentialsX loads its core features together and has optional add-on modules for stuff like chat, spawn and protection.
* **Storage.** One SQLite database (or MySQL if you want) holding players, homes, kits, punishments and economy. No pile of per-player files. EssentialsX keeps one YAML file per player on disk.
* **Languages.** 29 languages are built in, and each player picks their own with `/language`. EssentialsX uses one language for the whole server by default. Matching each player's client language is an opt-in setting (`per-player-locale`).
* **Menus.** Shop, auction house, kits and RTP open real inventories you can click through. No walls of chat, and no extra plugins needed for a shop or an AH. EssentialsX is mostly chat and commands.
* **Switching over.** `/migration essentialsx` pulls over users, homes, warps, bans, mutes and economy, and dry-run mode shows you what would happen first. EssentialsX has no importer, so you start from zero.
* **Mail.** EssentialsX has `/mail`, EssentialsC doesn't. For offline messages you're stuck with `/msg` history or Discord.
* **Older servers.** EssentialsX runs on Spigot and on versions going back years. EssentialsC needs Paper and 1.20.6+, so on an old setup EssentialsX is your only option.
* **Track record.** EssentialsX has been around for over a decade and half the plugin world builds on it. EssentialsC is younger, so check the [issue tracker](https://github.com/GodlyCow203/EssentialsC/issues) before moving a big network over.

</details>

---

<details>
<summary><b>Click to see everything it can do</b></summary>

### Money
* **Auction House:** click through it in a menu, no extra plugin needed
* **Shop:** a shop menu built right in
* **Economy:** balances live in the same database as everything else

### Getting around
* **Homes** and **Warps**
* **TPA** requests between players
* **Wild Teleport (RTP):** opens as a menu

### Running your server
* **Moderation tools:** bans, mutes and other punishments
* **Kits:** claim them from a menu
* **Nicknames** and **Scoreboard**
* **Discord notifications**

### Also included
* **90+ commands:** turn off, reprioritize, alias, add cooldowns to or change the help text of any of them in `commands.yml`
* **Turn stuff off:** disable a feature and it's really gone, with no background tasks, no listeners and no registered commands
* **One database:** SQLite by default, MySQL if you want it
* **29 languages:** each player picks theirs with `/language`
* **MiniMessage:** gradients, hover text and RGB just work in every message

*...and more!*

</details>

<details>
<summary><b>Click to see how to get started</b></summary>

1. Make sure you're on Paper (or a supported fork) with Minecraft 1.20.6+ and Java 21+.
2. Grab EssentialsC from [Modrinth](https://modrinth.com/plugin/essentialsc) and drop it in your `plugins` folder.
3. Start the server, then tweak the generated configs. The [wiki](https://wiki.godlycow.org/essc) explains every option.
4. Coming from EssentialsX? Run `/migration essentialsx` to bring over users, homes, warps, bans, mutes and economy. Try dry-run mode first so you can see what would happen.

</details>

<details>
<summary><b>Click to see what it runs on</b></summary>

EssentialsC is built for Paper and its forks and works on **Minecraft 1.20.6 - 26.2+**.

| Software | Status |
| :--- | :--- |
| **Paper** | Supported |
| **Purpur** | Supported |
| **Folia** | Supported |
| **Other Forks** | Supported* |
| **Spigot** | Not Supported |
| **Bukkit** | Not Supported |

*\*Forks that don't change the core Bukkit API work too.*

**You'll need:** Java 21+, Minecraft 1.20.6+, and one of the server types above.

</details>

<details>
<summary><b>Use of AI</b></summary>

I use AI as a tool while building EssentialsC.

* A small part of the core code was written with AI, then I reviewed, tested and adapted it.
* I also used AI for the language files and a bigger part of the configs, mainly the shop sections.
* I still review everything and handle the final implementation myself.

</details>

<details>
<summary><b>Contributing</b></summary>

Contributions are always welcome! Got an idea, found a bug, or want to improve something? Open an issue or send a pull request on GitHub.

Just make sure your changes fit the project and are tested before you do.

</details>

---

Thank you to everyone using EssentialsC. I appreciate every single one of you.

> EssentialsC is not affiliated with or endorsed by EssentialsX or Mojang Studios.
> EssentialsC is developed by me in my free time.

---

![bStats](https://bstats.org/signatures/bukkit/EssentialsC.svg)