#!/bin/sh

targetversion=$1
snapshotversion="$targetversion-SNAPSHOT"

# Add validater here.

echo "============ Hotfix branch generator ===============\n"

echo "Current branch is \n$(git branch -v)"
echo "Updating pom version numbers to $targetversion"

#git checkout master
#git flow hotfix start $targetversion

## Need to change impl-parent version ONLY, not children.
#mvn versions:set -DnewVersion=$snapshotversion

targetdir=(
	"ding-impl"
	"event-impl"
	"io-impl"
	"model-impl"
	"psi-mi-impl"
	"session-impl"
	"viewmodel-impl"
	"vizmap-impl"
	"work-swing-impl"
)

for item in ${targetdir[@]}
do
	echo "Fixing: $item"
	cd $item
	mvn -N versions:update-child-modules
	cd ..
done

#mvn -N versions:update-child-modules
mvn versions:commit

echo "\nDone! Do not forget to commit & push the changes."
