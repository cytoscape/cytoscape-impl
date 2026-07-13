#!/usr/bin/env bash
#
# Build the Cytoscape platform toolchain (parent + api) from source into the local Maven repo, then
# run the app-impl unit tests. Usable standalone by developers and invoked by CI (.github/workflows/ci.yml).
#
# The parent/api artifacts for this version are not published to any public Maven repo, so they are
# built from source (cytoscape-parent, cytoscape-api) at the tag matching this repo's api version.
#
# Env overrides:
#   CY_REF      git ref for the parent/api clones (default: derived from pom.xml)
#   LOCAL_REPO  Maven local repository (default: ~/.m2/repository; set to a mktemp dir for a clean run)
set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
cd "$REPO_ROOT"   # in-tree mvn (the app-impl test) then picks up .mvn/maven.config automatically
SETTINGS="$REPO_ROOT/.mvn/settings.xml"
LOCAL_REPO="${LOCAL_REPO:-$HOME/.m2/repository}"   # override (e.g. a mktemp dir) to force a clean bootstrap

# Toolchain ref from the impl pom (XML parse; Maven can't evaluate the pom until its parent is installed below).
if [ -z "${CY_REF:-}" ]; then
  VER="$(python3 -c "import xml.etree.ElementTree as ET; ns={'m':'http://maven.apache.org/POM/4.0.0'}; \
print(ET.parse('pom.xml').getroot().find('m:properties/m:cytoscape.api.version', ns).text)")"
  case "$VER" in *-SNAPSHOT) CY_REF=develop ;; *) CY_REF="$VER" ;; esac
fi
echo "Cytoscape toolchain ref: $CY_REF ; local repo: $LOCAL_REPO"

# Build parent + api into $LOCAL_REPO when absent (skipped once present). These use a pom OUTSIDE the
# impl repo (-f), so Maven derives the .mvn base dir from that pom's location, not cwd — .mvn/maven.config
# does NOT apply to them. Pass the settings explicitly with -s.
if [ ! -f "$LOCAL_REPO/org/cytoscape/api-bundle/$CY_REF/api-bundle-$CY_REF.jar" ]; then
  work="$(mktemp -d)"
  git clone --depth 1 --branch "$CY_REF" https://github.com/cytoscape/cytoscape-parent.git "$work/parent"
  mvn -B -s "$SETTINGS" -Dmaven.repo.local="$LOCAL_REPO" -f "$work/parent/pom.xml" install
  git clone --depth 1 --branch "$CY_REF" https://github.com/cytoscape/cytoscape-api.git "$work/api"
  mvn -B -s "$SETTINGS" -Dmaven.repo.local="$LOCAL_REPO" -f "$work/api/pom.xml" -pl event-api install -DskipTests  # event-api first
  mvn -B -s "$SETTINGS" -Dmaven.repo.local="$LOCAL_REPO" -f "$work/api/pom.xml" install -DskipTests                # then full api reactor
fi

# Run the app-impl tests (in-tree: .mvn/maven.config applies the settings; -s added for parity).
mvn -B -s "$SETTINGS" -Dmaven.repo.local="$LOCAL_REPO" -pl app-impl test
