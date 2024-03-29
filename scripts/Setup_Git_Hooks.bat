@echo off

REM You set your arguments here
set arguments_setup=
set arguments_launch=

set version=2.1.6
type NUL > .git/hooks/post-merge
echo #!/bin/sh > .git/hooks/post-merge
echo java -jar Cat-Downloader-Legacy-%version%.jar %arguments_launch% >> .git/hooks/post-merge

echo Git Hooks have been set up! Running Cat Downloader %version%...

java -jar Cat-Downloader-Legacy-%version%.jar %arguments_setup%

pause
