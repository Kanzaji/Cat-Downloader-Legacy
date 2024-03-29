#!/bin/sh

ARGUMENTS_SETUP=
ARGUMENTS_LAUNCH=

VERSION="2.1.6"
echo "#!/bin/sh" > .git/hooks/post-merge
echo "java -jar Cat-Downloader-Legacy-${VERSION}.jar ${ARGUMENTS_LAUNCH}" >> .git/hooks/post-merge

echo Git Hooks have been set up! Running Cat Downloader...

java -jar Cat-Downloader-Legacy-${VERSION}.jar ${ARGUMENTS_SETUP}
