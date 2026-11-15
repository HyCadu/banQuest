# Harness de prévia e de integração (ferramenta de desenvolvimento)

Projeto Gradle **separado** do app: não entra no `./gradlew check` nem no APK. Compila as mesmas fontes de
`app/src/main` e roda, com Robolectric, dois tipos de teste que o app ainda não tem por conta própria:

- `FlowShots`: renderiza as telas (abas, exercícios, resultado) em PNG, nos temas claro e escuro, para conferir o visual
  sem emulador. Saída em `shot/build/shots/`.
- `IntegrationTest`: joga lições inteiras com o `SessionViewModel` sobre um Room em memória e confere progresso, corações,
  revisão espaçada, loja, missões, baús, ofensiva com congelamento e fechamento semanal da liga.

```bash
./gradlew -p tools/preview-harness :shot:testDebugUnitTest
```

Usa Robolectric e `androidx.test`, que **não estão no PLAN.md**. Por isso ficam só aqui. Se o dono autorizar, os testes de
integração podem ser promovidos para `app/src/test` (ver `docs/DECISIONS.md`).
