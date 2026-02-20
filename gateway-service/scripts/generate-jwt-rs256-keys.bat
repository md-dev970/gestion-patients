@echo off
REM Generate RSA key pair for JWT RS256 (T1.3). Auth signs with private key;
REM gateway verifies with public key from Secrets.
REM Requires OpenSSL. Run from project root: gateway-service\scripts\generate-jwt-rs256-keys.bat

set DIR=%~dp0..
cd /d "%DIR%"
if not exist build\jwt-keys mkdir build\jwt-keys
cd build\jwt-keys

openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem

echo Generated private.pem and public.pem in %CD%
echo Auth: JWT_PRIVATE_KEY or JWT_PRIVATE_KEY_LOCATION
echo Gateway: JWT_PUBLIC_KEY or JWT_PUBLIC_KEY_LOCATION from Secrets
