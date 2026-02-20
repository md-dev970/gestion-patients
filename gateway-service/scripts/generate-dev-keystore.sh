#!/usr/bin/env sh
# Generate a self-signed PKCS12 keystore for gateway HTTPS in dev (T1.2).
# Run from gateway-service directory: ./scripts/generate-dev-keystore.sh
# Then set SERVER_SSL_ENABLED=true and SERVER_SSL_KEY_STORE to the path of gateway-dev.p12.

set -e
OUTPUT="${1:-gateway-dev.p12}"
PASSWORD="${GATEWAY_DEV_KEYSTORE_PASSWORD:-changeit}"
DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$DIR"
mkdir -p build
KEYSTORE="$DIR/build/$OUTPUT"

keytool -genkeypair \
  -alias gateway \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE" \
  -storepass "$PASSWORD" \
  -dname "CN=localhost, OU=Dev, O=Hospital, L=City, ST=State, C=XX"

echo "Created: $KEYSTORE"
echo "Use: SERVER_SSL_ENABLED=true SERVER_SSL_KEY_STORE=$KEYSTORE SERVER_SSL_KEY_STORE_PASSWORD=$PASSWORD SERVER_SSL_KEY_STORE_TYPE=PKCS12 SERVER_SSL_KEY_ALIAS=gateway"
