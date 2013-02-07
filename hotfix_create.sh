#!/bin/sh

targetversion=$1
snapshotversion="$targetversion-SNAPSHOT"

# Add validater here.

echo "============ Hotfix branch generator ===============\n"

echo "Current branch is \n$(git branch -v)"
echo "Updating pom version numbers to $targetversion"

git checkout master
git flow hotfix start $targetversion

## Need to change impl-parent version ONLY, not children.
#mvn versions:set -DnewVersion=$snapshotversion

mvn -N versions:update-child-modules
mvn versions:commit

echo "Done! Do not forget to commit & push the changes."
