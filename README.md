# Cat-Downloader Legacy
[![Discord](https://img.shields.io/discord/1146365683576356884?style=for-the-badge&logo=discord&label=Questions%3F%20Join%20my%20discord!&labelColor=%23333333&color=%235865f2&cacheSeconds=30)](https://discord.gg/ktCFeKECvH)
[![Static Badge](https://img.shields.io/badge/CDL_Configuration_Website-%5BWIP%5D-FF2222?style=for-the-badge&labelColor=333333)](https://kanzaji.github.io/Cat-Downloader-Legacy/)

Cat-Downloader Legacy is an app that is meant to allow for easy synchronization of minecraft mods between modpack developers, with use of, for example, Git hooks.

It Supports Modrinth index (`.mrpack` and modrinth index format), Curseforge Site Format (`manifest.json` format), and CurseForge Instance Format (`minecraftinstance.json` format, from the CF Launcher)!

It also features file verification with Checksum (SHA-512), File Size Check, and automatic updates for the app!

## Setup
The app requires Java 17 with SHA-512 Digest module (However, this should be in most of the common distributions), and can be launched manually or by the automation like Git Hooks.<br>
The app at first launch will generate Settings file (if not disabled) with default settings and documentation. The file extension is json5, that allows to put comments into json!

## Configuration
Cat-Downloader Legacy can be configured in two ways, to customize the behaviour of the app.<br>
You can configure it either by the Configuration file, or with the Arguments.
Note: Here are only shown options that are argument only! For full configuration, check out the website (WIP) or the [configuration file](https://github.com/Kanzaji/Cat-Downloader-Legacy/blob/main/src/catdownloaderlegacy/src/main/resources/assets/templates/settings.json5).

### Configuration keys
`-Settings:`        -> Boolean // Determines if Settings are enabled or disabled. (Default: true)<br>
`-SettingsPath:`    -> String // Specifies a directory where configuration file is. Can be absolute/relative.<br>
`-DefaultSettings:` -> Boolean // Determines if generated Settings file should have values from the arguments of the app. Useful for setup scripts (Default: false)<br>
`-BypassNetworkCheck` -> Null // If this argument is present, the Network Connection check will be by-passed. (The host for testing connection is github.com)<br>

## License
This project is under a MIT License, what you can find in the LICENSE file of this Repo and each Source File. I of course don't have anything against you using/including this app in your modpack repo :D If you would mention that you are using this project in your Repo Readme file tho, I would be happy!

## FAQ
- **Why Legacy in the name?<br>**
  [Cat-Downloader](https://github.com/Kanzaji/Cat-Downloader) is actually a different project of mine that is meant to be full Minecraft Launcher! It is, however, in very early stages of the development, so for now it has more or less the same functionality as this app (Okay at the point of writing this ReadMe, it has way less features).
- **Why don't just, use CurseForge way of updating mods for modpack developers?**<br>
  CurseForge launcher MinecraftInstance is created for a specified user on a specified computer. It doesn't always work when you import it from GitHub repository, and it doesn't completely work on non-CurseForge launcher. That is why InstanceSync was used for modpack development. Due to InstanceSync lacking file verification and being archived by Vazkii, Cat-Downloader-Legacy was created.

## Contributions

For Contribution Guide-Lines and other stuff, check out `Contributing` branch!
(This is also WIP and will be on the website 😅)
