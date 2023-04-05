@echo off

set arguments = 
set version = 1.0

type NUL > .git/hooks/post-merge
echo #!/bin/sh > .git/hooks/post-merge
echo java -jar Cat-Downloader-Legacy-%version%.jar %arguments% >> .git/hooks/post-merge

echo Git Hooks have been setted up! Running Cat Downloader...

java -jar Cat-Downloader-Legacy-%version%.jar %arguments%

pause
