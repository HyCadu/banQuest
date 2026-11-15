#!/usr/bin/env bash
# Gera o APK para distribuir (dist/banquest-<versão>.apk), assinado com a chave fixa do BanQuest.
#
# Precisa de:
#   BANQUEST_KEYSTORE_FILE      caminho do banquest-release.jks (fica com o dono, nunca no repositório)
#   BANQUEST_KEYSTORE_PASSWORD  senha da chave
# Recusa entregar um APK assinado com outra chave: o certificado é conferido contra tools/signing/banquest-cert-sha256.txt
# (a impressão digital é pública; a chave e a senha não).
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT"
: "${BANQUEST_KEYSTORE_FILE:?Defina BANQUEST_KEYSTORE_FILE com o caminho do banquest-release.jks}"
: "${BANQUEST_KEYSTORE_PASSWORD:?Defina BANQUEST_KEYSTORE_PASSWORD com a senha da chave}"
[ -f "$BANQUEST_KEYSTORE_FILE" ] || { echo "Chave não encontrada: $BANQUEST_KEYSTORE_FILE"; exit 1; }

./gradlew -q assembleDebug
APK=app/build/outputs/apk/debug/app-debug.apk
SDK=${ANDROID_HOME:-$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null)}
APKSIGNER=$(ls -d "$SDK"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1)
[ -x "$APKSIGNER" ] || { echo "apksigner não encontrado no SDK ($SDK)"; exit 1; }

GOT=$("$APKSIGNER" verify --print-certs "$APK" | sed -n 's/.*certificate SHA-256 digest: //p' | head -1 | tr 'a-f' 'A-F')
WANT=$(tr -d '[:space:]' < tools/signing/banquest-cert-sha256.txt | tr 'a-f' 'A-F')
if [ "$GOT" != "$WANT" ]; then
  echo "ERRO: o APK não foi assinado com a chave do BanQuest."
  echo "  esperado: $WANT"
  echo "  obtido:   $GOT"
  exit 1
fi
VERSION=$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' app/build.gradle.kts | head -1)
mkdir -p dist
cp "$APK" "dist/banquest-$VERSION.apk"
echo "OK: dist/banquest-$VERSION.apk (assinatura conferida: ${GOT:0:16}…)"
