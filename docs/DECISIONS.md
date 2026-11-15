# Decisões de projeto

Formato: data · decisão · motivo. Decisões marcadas com ★ no PLAN.md não precisam ser registradas aqui, a menos que resolvam uma contradição.

## Fase 0 (15/11/2026)

1. **Fuso do `Clock`: `America/Sao_Paulo` fixo.** O PLAN.md §4 diz "fuso do dispositivo"; o CLAUDE.md (regra 7) diz `America/Sao_Paulo`. Prevaleceu o CLAUDE.md: a virada do dia fica previsível mesmo em viagem ou com fuso errado no aparelho. `Clock.today()` ignora o fuso do relógio recebido e usa `APP_ZONE`. Fácil de reverter (um único arquivo: `domain/time/AppClock.kt`).
2. **Pacote e applicationId: `br.com.trilhati`.** Nome de trabalho; o nome definitivo do app é pendência do dono (PLAN §12). Mudar depois exige renomear pacote e invalida o backup do Android, então vale decidir antes de instalar em uso real.
3. **`compileSdk`/`targetSdk` 37.** Android 37.0 consta como estável no repositório do SDK (sem codinome de prévia). `minSdk 26`.
4. **Kotlin 2.4.0 (não 2.4.21).** O AGP 9.5.1 traz Kotlin embutido na 2.4.0; o plugin do Compose precisa casar com ele. Atualizar o Kotlin sozinho exigiria forçar o plugin do Kotlin no classpath. Rever ao trocar o AGP.
5. **Sem o plugin `kotlin-android`.** O AGP 9 já compila Kotlin; aplicar o plugin separado é desnecessário.
6. **Lint com `warningsAsErrors`, exceto `NewerVersionAvailable` e `GradleDependency`.** Esses dois falhariam o build toda vez que saísse uma versão nova. Versões são revisadas à mão em `libs.versions.toml`.
7. **Kotlin com `allWarningsAsErrors`.** Garante o requisito "sem warnings novos" do CLAUDE.md.
8. **Catálogo só com o que a Fase 0 usa.** DataStore, WorkManager, Navigation e kotlinx.serialization entram na fase que os usa (planejado no PLAN.md; nenhuma dependência fora dele). `hilt-navigation-compose` já está no catálogo e no app por estar na stack, mas ainda sem uso.
9. **Sem testes instrumentados e sem dependências de teste extras** (mockk, turbine, Robolectric, compose-ui-test): não estão no PLAN.md. `AppDatabase` é verificado só pela compilação do KSP e pelo schema exportado em `app/schemas/`. Testes de UI (PLAN §10) entram na Fase 3/4 e pedirei autorização para `compose-ui-test`.
10. **Backup do Android ligado** (PLAN §11) com regras que incluem só o banco de dados (`xml/backup_rules.xml`, `xml/data_extraction_rules.xml`).
11. **Permissão extra no APK:** `br.com.trilhati.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` é injetada automaticamente pelo `androidx.core` (permissão interna do próprio app, protege receivers dinâmicos). Não é uma permissão pedida ao usuário. As permissões do CLAUDE.md entram nas fases 6 e 8.
12. **Ambiente de desenvolvimento (nuvem):** foi preciso liberar o host `dl.google.com` na rede do ambiente e instalar o Android SDK em `/opt/android-sdk` (`local.properties` aponta para lá e não vai para o git).
