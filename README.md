# Cat-Downloader Legacy
Cat-Downloader Legacy is an app that is meant to allow for easy synchronization of minecraft mods between modpack developers!

It supports CurseForge mods only, and is compatible both with CurseForge Launcher `minecraftinstance.json` format and CurseForge site `manifest.json` format.

## Configuration
Cat-Downloader Legacy accepts some arguments to customize app behaviour to your needs. Here is the list and explanation for all of them:

- `-Mode:` String <br>
  **Default: "Instance"<br>**
  Determines the Mode the app is going to run in.<br>
  For `minecraftinstance.json` use `Instance`, for `manifest.json` use `Pack`.
- `-WorkingDirectory:` String <br>
  **Default: "."<br>**
  Path where Manifest File and `/mods` folder are. Main working directory for the program.
- `-ThreadCount:` Integer <br>
  **Default: 16<br>**
Amount of virtual threads Java is going to use for Verification and Download tasks.
- `-DownloadAttempts:` Integer <br>
  **Default: 5<br>**
Amount of tries Cat-Downloader will try to download a mod.
- `-SizeVerification:` Boolean or Integer (0/1) <br>
  **Default: True<br>**
Determines if Size Verification of installed/downloaded files is turned on/off.<br>
*Small Performance Penalty*
- `-SumCheckVerification:` Boolean or Integer (0/1) <br>
  **Default: True<br>**
Determines if Sum-Check Verification of installed/downloaded files is turned on/off.<br>
*Big Performance penalty and bigger usage of bandwidth*
- `-Logger:` Boolean or Integer (0/1) <br>
  **Default: True<br>**
Determines if Logger is turned on/off and if the log file is meant to be created.<br>
*IT IS NOT RECOMMENDED TO DISABLE LOGGER SERVICE*

## License
This project is under a MIT License, what you can find in the LICENSE file of this Repo. I of course don't have anything against you using/including this app in your modpack repo :D If you would mention that you are using this project in your Repo Readme file tho, I would be happy!

## FAQ
- Why Legacy in the name?<br>
  (Cat-Downloader)[https://github.com/Kanzaji/Cat-Downloader] is actually a different project of mine that is meant to be full Minecraft Launcher! It is, however, in very early stages of the development, so for now it has more or less the same functionality as this app.

## Contributions
For Contribution Guide-Lines and other stuff, check out Develop branch!