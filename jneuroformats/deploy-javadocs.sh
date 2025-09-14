#!/bin/bash
# Script to deploy Javadocs to gh-pages
#
# Run this script from the <repo>/jneuroformats/ sub directory while on branch `main`.
#


# Generate docs
mvn javadoc:javadoc

# Copy docs to temporary location
TMP_DIR=$(mktemp -d)
cp -r target/site/apidocs/* "$TMP_DIR"

# Deploy to gh-pages
git stash && git checkout gh-pages 2>/dev/null || git checkout --orphan gh-pages && rm -rf ./* && cp -r "$TMP_DIR"/* . && git add . && git commit -m "Update Javadocs $(date)" && git push origin gh-pages
git checkout main && git stash pop
rm -rf "$TMP_DIR"
echo "Javadocs deployed to gh-pages branch."