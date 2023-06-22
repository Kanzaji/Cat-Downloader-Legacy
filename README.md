# [Cat-Downloader Legacy](https://kanzaji.github.io/Cat-Downloader-Legacy/)
Cat-Downloader Legacy is an app that is meant to allow for easy synchronization of minecraft mods between modpack developers, with use of, for example, Git hooks.

It supports CurseForge `minecraftinstance.json` format, and has **Experimental** support for CurseForge site format `manifest.json`. *Modrinth support is Work in Progress!*

It also features file verification with Checksum (SHA-256), File Size Check and automatic updates for the app!

## Configuration
Cat-Downloader Legacy can be configured in two ways, to customize the behaviour of the app.<br>
You can configure it either by the Configuration file, or with the Arguments.

### Configuration keys

- `Mode` String // **Default: "Instance"**
  <br>Determines the mode of the app. Available modes are: "Instance" / "Pack"
  <br>**Argument representation:** `-Mode:`<br><br>

- `WorkingDirectory` String // **Default: "."**
  <br>Determines working directory of the app, so where mods are getting downloaded and log file is getting generated.
  <br>Empty string will result in working directory being set to place of execution of the app!
  <br>Accepts both relative and absolute paths.
  <br>**Argument representation:** `-WorkingDirectory:`<br><br>

- `ThreadCount` Integer // **Default: "16"**
  <br>Amount of threads an app is going to create for Data gathering, Verification and Downloading processes.
  <br>Accepts any Integer Value above 1!
  <br>**Argument representation:** `-ThreadCount:`<br><br>

- `DownloadAttempts` Integer // **Default: "5"**
  <br>Amount of tries the app will take before giving up on re-downloading a corrupted mod.
  <br>Accepts any Integer Value above 1!
  <br>**Argument representation:** `-DownloadAttempts:`<br><br>

- `isFileSizeVerificationActive` Boolean // **Default: "True"**
  <br>Determines if file size verification is turned on. Barely-Visible performance gain and will result in corrupted mods if disabled.
  <br>Accepts Boolean Values (Example: True)
  <br>**Argument representation:** `-SizeVerification:`<br><br>

- `isHashVerificationActive` Boolean // **Default: "True"**
  <br>Determines if Hash verification is turned on. Huge Performance gain but can result in corrupted mods if disabled!
  <br>Accepts Boolean Values (Example: True)
  <br>**Argument representation:** `-HashVerification:`<br><br>

- `isLoggerActive` Boolean // **Default: "True"**
  <br>Determines if the logger is enabled. *It is not recommended to disable Logger service. Entire Logger output will be printed to the console!*
  <br> Accepts Boolean Value (Example: True)
  <br> **Argument representation:** `-Logger:`<br><br>

- `-Settings:` Boolean or Integer (0/1) // **Default: "True"**
  <br>Determines if app generates and uses Configuration File (Argument only!)
  <br> Accepts Boolean Value (Example: True)<br><br>

- `-SettingsPath:` String // **Default: "."**
  <br>Determines directory where is located Configuration file.
  <br>Empty string will result in Settings path being set to place of execution of the app!
  <br>Accepts both relative and absolute paths.<br><br>

- `-DefaultSettings:` Boolean or Integer (0/1) // **Default: "True"**
  <br>Determines if app generates a template of the Settings file, or is going to override / generate settings with values from the Arguments. Useful for setting up the app in your repo without shipping the actual config file.
  <br> Accepts Boolean Value (Example: True)

## License
This project is under a MIT License, what you can find in the LICENSE file of this Repo. I of course don't have anything against you using/including this app in your modpack repo :D If you would mention that you are using this project in your Repo Readme file tho, I would be happy!

## FAQ
- **Why Legacy in the name?<br>**
  [Cat-Downloader](https://github.com/Kanzaji/Cat-Downloader) is actually a different project of mine that is meant to be full Minecraft Launcher! It is, however, in very early stages of the development, so for now it has more or less the same functionality as this app (Okay at the point of writing this ReadMe, it has way less features).
- **Why don't just, use CurseForge way of updating mods?**<br>
  CurseForge is known for screwing up downloads for some reason, not updating when you want to and other weird shenanigans. Cat-Downloader-Legacy allows for easy and automatic synchronization of the mods between modpack developers, additionally giving access for the same functionality for non-CurseForge launchers!

## Contributions

For Contribution Guide-Lines and other stuff, check out `Contributing` branch!