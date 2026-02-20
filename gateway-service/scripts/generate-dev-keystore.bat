@echo off
REM Generate a self-signed PKCS12 keystore for gateway HTTPS in dev (T1.2).
REM Run from gateway-service directory: scripts\generate-dev-keystore.bat
REM Then set SERVER_SSL_ENABLED=true and SERVER_SSL_KEY_STORE to the path of build\gateway-dev.p12.

set OUTPUT=gateway-dev.p12
if not "%~1"=="" set OUTPUT=%~1
set PASSWORD=changeit
if not "%GATEWAY_DEV_KEYSTORE_PASSWORD%"=="" set PASSWORD=%GATEWAY_DEV_KEYSTORE_PASSWORD%

set DIR=%~dp0..
cd /d "%DIR%"
if not exist build mkdir build
set KEYSTORE=%DIR%\build\%OUTPUT%

keytool -genkeypair ^
  -alias gateway ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 365 ^
  -storetype PKCS12 ^
  -keystore "%KEYSTORE%" ^
  -storepass "%PASSWORD%" ^
  -dname "CN=localhost, OU=Dev, O=Hospital, L=City, ST=State, C=XX"

echo Created: %KEYSTORE%
echo Use: set SERVER_SSL_ENABLED=true
echo      set SERVER_SSL_KEY_STORE=%KEYSTORE%
echo      set SERVER_SSL_KEY_STORE_PASSWORD=%PASSWORD%
echo      set SERVER_SSL_KEY_STORE_TYPE=PKCS12
echo      set SERVER_SSL_KEY_ALIAS=gateway
