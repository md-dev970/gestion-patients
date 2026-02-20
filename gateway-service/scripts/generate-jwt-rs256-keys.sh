#!/usr/bin/env sh
# Generate RSA key pair for JWT RS256 (T1.3). Auth-service signs with private key;
# gateway verifies with public key (load from Secrets: env JWT_PUBLIC_KEY or mounted file).
# Run from project root or gateway-service: ./gateway-service/scripts/generate-jwt-rs256-keys.sh
# Output: private.pem (auth), public.pem (gateway/Secrets). Do not commit private.pem.

set -e
DIR="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$DIR/build/jwt-keys"
cd "$DIR/build/jwt-keys"

openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem

echo "Generated: $DIR/build/jwt-keys/private.pem (auth-service: JWT_PRIVATE_KEY or JWT_PRIVATE_KEY_LOCATION)"
echo "Generated: $DIR/build/jwt-keys/public.pem (gateway from Secrets: JWT_PUBLIC_KEY or JWT_PUBLIC_KEY_LOCATION)"
