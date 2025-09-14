#!/bin/bash
# Script to deploy Javadocs to gh-pages
#
# Run this script from the <repo>/jneuroformats/ sub directory while on branch `main`.
#

# Ensure we are on the main branch
if [ "$(git rev-parse --abbrev-ref HEAD)" != "main" ]; then
  echo "Error: You must be on the main branch to run this script."
  exit 1
fi

# Ensure we are in the jneuroformats/ sub directory of the repo
if [ ! -f pom.xml ]; then
  echo "Error: pom.xml not found. Please run this script from the jneuroformats/ sub directory."
  exit 1
fi

# Generate docs
mvn javadoc:javadoc || { echo "Maven Javadoc generation failed"; exit 1; }

echo "Now in directory $(pwd) on brain $(git rev-parse --abbrev-ref HEAD). Generating Javadocs..."

# ensure we are in the correct directory
if [ ! -d target/site/apidocs ]; then
  echo "Error: target/site/apidocs directory does not exist. Please run this script from the jneuroformats/ directory after building the project."
  exit 1
fi

# Copy docs to temporary location
TMP_DIR=$(mktemp -d)
cp -r target/site/apidocs/* "$TMP_DIR"

# Deploy to gh-pages
git stash && { git checkout gh-pages 2>/dev/null || git checkout --orphan gh-pages; } && echo "Switched to gh-pages branch. Current directory: $(pwd), current branch $(git rev-parse --abbrev-ref HEAD)"

cd .. && echo "Changed to repo root. Current directory: $(pwd), current branch $(git rev-parse --abbrev-ref HEAD)"

# Ensure once more we are in the correct directory: it should be the root of the repo now.
# Because we are on the gh-pages branch (should be), we should NOT see pom.xml or jneuroformats/ here, but we should see search.js from the last deployment of java docs.

if [ ! -f search.js ]; then
  echo "Error: file 'search.js' NOT found in current dir. Please run this script from the jneuroformats/ sub directory of the repo. Maybe changing to the root of the repo failed, or changing branch to gh-pages failed, check errors above."
  exit 1
fi

if [ -f pom.xml ]; then
  echo "Error: pom.xml found in current dir. We are most likely on incorrect branch (not gh-pages). Please run this script from the jneuroformats/ sub directory of the repo. Maybe changing to the root of the repo failed, or changing branch to gh-pages failed, check errors above."
  exit 1
fi

if [ -d jneuroformats ]; then
  echo "Error: directory 'jneuroformats/' found in current dir. We are most likely on incorrect branch (not gh-pages). Please run this script from the jneuroformats/ sub directory of the repo. Maybe changing to the root of the repo failed, or changing branch to gh-pages failed, check errors above."
  exit 1
fi

# Verify the name of the directory is correct - we should be in the root of the repo now, which is most likely called neuroformats. We will run 'rm -rf ./*' so we want to be sure.
if [ "$(basename "$(pwd)")" != "jneuroformats" ]; then
  echo "Error: You must be in the jneuroformats/ directory of the repo. Please run this script from the jneuroformats/ sub directory of the repo."
  exit 1
fi

rm -rf ./* && cp -r "$TMP_DIR"/* . && git add . && git commit -m "Update Javadocs $(date)" && git push origin gh-pages
git checkout main && git stash pop
rm -rf "$TMP_DIR"
echo "Javadocs deployed to gh-pages branch."