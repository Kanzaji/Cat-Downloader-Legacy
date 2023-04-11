#!/bin/sh

ARGUMENTS_SETUP=
ARGUMENTS_LAUNCH=

VERSION="1.1"
echo "#!/bin/sh" > .git/hooks/post-merge
echo "java -jar Cat-Downloader-Legacy-${version}.jar ${arguments_launch}" >> .git/hooks/post-merge

echo Git Hooks have been set up! Running Cat Downloader...

java -jar Cat-Downloader-Legacy-${version}.jar ${arguments_setup}
