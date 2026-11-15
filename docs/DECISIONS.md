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

## Fase 1 (15/11/2026)

Decisões tomadas **com o dono** (perguntas respondidas antes de desenhar):

1. **Paleta roxo + turquesa.** Roxo `#6B3FD6` (primária) e turquesa `#19D3C5` (destaque); verde/vermelho só para acerto/erro; âmbar para ofensiva, cronômetro em alerta e baú. Substitui o "azul-petróleo + âmbar" do PLAN.md §3 (que era só sugestão).
2. **Visual cartoon e gamificado, mas útil.** Contorno escuro grosso, botões 3D que afundam ao toque, brilhos de "adesivo", cantos bem arredondados.
3. **Mascotes mantidos:** Guaiamum, Tartaruga-marinha, Tamanduá e Preguiça (PLAN §3), em estilo geométrico e fofo, desenhados em `Canvas`.
4. **Navegação por barra inferior com 5 abas:** Trilha · Praticar · Ligas · Loja · Perfil. (Missões ficam dentro de Ligas ou da Trilha; decidir na Fase 5.)
5. **Tom dos textos: animado e bem-humorado.** Fica ajustável nas configurações só se o dono pedir depois.
6. **Nível de acabamento:** o dono pediu visual "quase pronto", não MVP. Por isso a Fase 1 inclui estados, animações, tema escuro e acessibilidade desde já.

Decisões técnicas:

7. **Cores:** tokens `TrilhaColors` (claro/escuro) em `ui/theme`; cada cor cheia tem `*Edge` (lombada 3D) e `on*`. **Teste de contraste WCAG AA (4,5:1)** em `ContrastTest` cobre todos os pares texto/fundo nos dois temas. Um par falhou na primeira execução (`onLocked`, 3,52) e foi corrigido.
8. **Fontes embutidas no APK** (o app é offline): Nunito e JetBrains Mono, variáveis, licença OFL (`docs/licenses/`). Não usa Google Fonts por download.
9. **Ícones e mascotes 100% em código** (`ui/icons`, `ui/mascot`): nada de imagens, emojis ou `material-icons`; consistente, original e sem dependência nova.
10. **Zero dependências novas no app** nesta fase.
11. **`ThemeMode` (Sistema/Claro/Escuro)** já existe no tema; a persistência (DataStore) entra com as Configurações.
12. **`animated = false`** nos mascotes congela a animação; será ligado ao ajuste de acessibilidade "remover animações" na Fase 8.
13. **Strings dos componentes em `strings.xml`** (com `plurals` onde há número). Os textos da galeria ficam inline porque ela é vitrine, não produto, e será removida/escondida da build final.
14. **Verificação visual:** como não há emulador na nuvem, os componentes foram renderizados como imagem com Robolectric num projeto **fora do repositório** (pasta temporária), só para eu enxergar o resultado. Nada disso entra no app nem nas dependências.
15. **Ambiente de desenvolvimento:** além de `dl.google.com`, foi liberado `gcp.mirrors.maven.org` (espelho do Maven Central, necessário para o renderizador de prévia).

Pontos fracos conhecidos (para decidir com o dono):
- O **Tamanduá** é o mascote menos convincente (a cauda lembra um pão). Candidato a retoque.
- O contador "dias para a prova" usa `EXAM_DATE = 21/02/2027` fixo (`domain/time/ExamCountdown.kt`).
