#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$ROOT_DIR"

if [ ! -f .env ]; then
  echo "ERROR: .env file not found in $ROOT_DIR"
  exit 1
fi

USER=$(grep MAVEN_CENTRAL_USERNAME .env | cut -d= -f2 | tr -d '\r\n')
PASS=$(grep MAVEN_CENTRAL_PASSWORD .env | cut -d= -f2 | tr -d '\r\n')

if [ -z "$USER" ] || [ -z "$PASS" ]; then
  echo "ERROR: MAVEN_CENTRAL_USERNAME or MAVEN_CENTRAL_PASSWORD not set in .env"
  exit 1
fi

echo "==> 1. Compiling, signing and publishing to local repository..."
./gradlew publishToMavenLocal --no-daemon

echo "==> 2. Preparing Maven Central Portal Bundle..."
BUNDLE_DIR="build/bundle/com/sxnnysideproject/pollux-polyglot-jvm/0.1.0"
rm -rf build/bundle build/bundle-*.zip
mkdir -p "$BUNDLE_DIR"
cp ~/.m2/repository/com/sxnnysideproject/pollux-polyglot-jvm/0.1.0/* "$BUNDLE_DIR/"

echo "==> 3. Generating md5 and sha1 checksums..."
cd "$BUNDLE_DIR"
for f in $(ls | grep -v '\.asc$' | grep -v '\.md5$' | grep -v '\.sha1$'); do
  md5 -q "$f" > "$f.md5"
  shasum -a 1 "$f" | cut -d' ' -f1 > "$f.sha1"
done
cd "$ROOT_DIR"

echo "==> 4. Creating ZIP bundle..."
cd build/bundle
zip -r ../bundle-pollux-polyglot-jvm-0.1.0.zip . > /dev/null
cd "$ROOT_DIR"

echo "==> 5. Uploading bundle to Sonatype Central Publisher Portal API..."
TOKEN=$(echo -n "$USER:$PASS" | base64)
DEPLOY_ID=$(curl -s -f -H "Authorization: Bearer $TOKEN" \
  -F "bundle=@build/bundle-pollux-polyglot-jvm-0.1.0.zip" \
  "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC&name=pollux-polyglot-jvm-0.1.0")

echo "Deployment submitted successfully!"
echo "Deployment ID: $DEPLOY_ID"

echo "==> 6. Checking deployment status..."
sleep 3
STATUS_JSON=$(curl -s -X POST -H "Authorization: Bearer $TOKEN" "https://central.sonatype.com/api/v1/publisher/status?id=$DEPLOY_ID")
echo "$STATUS_JSON"
