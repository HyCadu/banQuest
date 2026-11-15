# QA — passo a passo para testar no celular

## Fase 0 — Esqueleto do projeto

**Gerar o APK** (na máquina com Android SDK e JDK 17+):

```bash
./gradlew assembleDebug
```

O arquivo sai em `app/build/outputs/apk/debug/app-debug.apk`.

**Instalar no celular:**
1. Ative "Opções do desenvolvedor" e "Depuração USB" no aparelho e conecte por USB (ou copie o APK e permita "instalar apps desconhecidos").
2. Com ADB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

**Checklist**
- [ ] O ícone "Trilha TI" (triângulo âmbar sobre azul-petróleo, provisório) aparece na lista de apps.
- [ ] Ao abrir, o app mostra a **tela de diagnóstico**: título "Trilha TI", "Fase 0 — esqueleto funcionando", a data de hoje (Brasília) e "Banco de dados: aberto, sem perfil ainda". Se aparecer isso, Compose, Hilt, Clock e Room estão funcionando. Vale para modo claro e escuro.
- [ ] Se o app **fechar sozinho ao abrir**, anote o modelo do celular e a versão do Android e me avise.
- [ ] Girar a tela não fecha o app.
- [ ] Em Configurações do Android > Apps > Trilha TI > Permissões: **nenhuma permissão** listada.
- [ ] O app funciona com o modo avião ligado (não usa rede).

**Verificações automáticas (já executadas na entrega):**

```bash
./gradlew assembleDebug test lint
./gradlew assembleRelease lintRelease
```

> Observação: a Fase 0 foi verificada por compilação, testes unitários e lint. O app **não foi aberto em um aparelho/emulador** no ambiente de desenvolvimento (nuvem, sem emulador), então o checklist acima é a primeira execução real.
