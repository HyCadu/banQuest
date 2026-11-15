# Assinatura do APK (chave fixa do BanQuest)

O Android só deixa instalar uma versão nova **por cima** da antiga se as duas tiverem a **mesma assinatura**. Até a
0.4.x, cada APK saía assinado com uma chave de teste do ambiente onde foi gerado. Como esse ambiente na nuvem é
descartado, a chave mudaria sem aviso. Desde a 0.5.0, todo APK distribuído é assinado com a **chave fixa do BanQuest**.

## Onde está a chave

- **Com o dono do app, e só com ele.** São dois arquivos:
  - `banquest-release.jks`: a chave;
  - `banquest-chave-LEIA.txt`: a senha e estas instruções.
- **Nunca** vai para o GitHub. O `.gitignore` já bloqueia `*.jks` e `*.keystore`.
- Guarde **duas cópias** em lugares diferentes (ex.: Google Drive e pendrive) e não mande para ninguém. Quem tiver a
  chave e a senha consegue assinar um APK que se passa pelo BanQuest.
- No repositório fica só a impressão digital pública do certificado (`tools/signing/banquest-cert-sha256.txt`), usada
  para conferir que o APK foi assinado com a chave certa.

## Como gerar um APK numa sessão nova

1. Anexe `banquest-release.jks` e `banquest-chave-LEIA.txt` na conversa e peça o APK.
2. O APK sai por `tools/build_apk.sh`, que exige a chave e **recusa** qualquer APK assinado com outra:
   ```bash
   BANQUEST_KEYSTORE_FILE=/caminho/banquest-release.jks BANQUEST_KEYSTORE_PASSWORD='senha' tools/build_apk.sh
   ```
   Resultado: `dist/banquest-<versão>.apk`.

As variáveis também podem ficar em `~/.gradle/gradle.properties`, fora do projeto: `banquest.signing.storeFile` e
`banquest.signing.storePassword`. O alias da chave é `banquest` (padrão).

## Se a chave se perder

Não há como recuperar. Seria preciso criar uma chave nova, e todo mundo teria que desinstalar e instalar de novo uma
vez, como na passagem da 0.4.x para a 0.5.0. O progresso volta pela conta, mas o que não estiver sincronizado se perde.

## Passagem da 0.4.x para a 0.5.0 (uma vez só)

A 0.5.0 é a primeira com a chave fixa, então **não instala por cima** da 0.4.x ("App não instalado"). Para cada pessoa:

1. Na 0.4.x, abra o app com internet e vá em **Configurações → Conta → Sincronizar**. Espere aparecer "Tudo sincronizado".
2. Desinstale o app antigo.
3. Instale o `banquest-0.5.0.apk` e entre com o mesmo e-mail: o progresso volta da nuvem.

Da 0.5.0 em diante, as versões novas instalam por cima, e o próprio app avisa quando sai uma (`docs/PUBLICAR_VERSAO.md`).
