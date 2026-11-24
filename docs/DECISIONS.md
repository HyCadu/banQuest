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

## Fases 2 a 5 e 7 integradas (15/11/2026) — a pedido do dono

O dono viu o catálogo da Fase 1 e disse que "parecia uma tela só" e que o jogo não estava integrado. Pediu a trilha como
primeira tela, a escolha de personagem só no perfil e um app "quase pronto", não um MVP. Por isso, em vez de parar em cada
fase, foram feitas juntas as fases 2 (conteúdo), 3 (lição), 4 (trilha), 5 (ofensiva, missões, loja), 7 (revisão de erros e
ligas) e as partes de perfil/configurações. Ficam para depois: notificações (fase 6), sons e acessibilidade fina (fase 8),
mais conteúdo (fase 9), simulado (fase 10).

1. **Sem onboarding.** O app abre direto na Trilha. Padrões: nome "Estudante", personagem Guaiamum, meta de 100 XP. Tudo se
   troca no Perfil. (O PLAN.md previa onboarding com escolha de personagem; o dono quer o personagem só no Perfil.)
2. **Abas:** Trilha · Praticar · Ligas · Loja · Perfil. As **missões diárias ficam na aba Ligas**, acima da classificação.
3. **Trilha com as 10 unidades e 48 lições** desde já (`domain/model/Curriculum.kt`). Só a Unidade 1 tem questões; as demais
   aparecem cinzas ("Conteúdo chegando em breve") e travam o avanço, porque o app não deixa pular conteúdo inexistente.
4. **Corações:** só o erro **de primeira** em uma **lição** custa 1 coração. A repetição do erro, o treino de erros e a revisão
   livre não custam. Ao zerar, a lição termina e abre a tela "sem corações" (praticar erros dá 1 coração, recarregar custa
   350 gemas, ou esperar 30 min por coração). O "modo sem vidas" desliga tudo isso.
5. **Nível** exibido = XP total ÷ 500 + 1 (começa no nível 1).
6. **Associação de pares:** qualquer toque errado marca a questão como errada, mas o aluno continua até casar tudo.
7. **Revisão espaçada:** só a **primeira** resposta de cada questão mexe no cartão. Uma repetição certa, na mesma lição, não
   avança o degrau. Quatro acertos seguidos nos intervalos 1, 3, 7 e 14 dias "formam" a questão e ela sai do banco de erros.
8. **Gemas:** lição +5 (+5 se perfeita), meta diária +20 (uma vez por dia), missões 20 a 30, conquistas 20 a 250, baú diário
   10 a 40, baú de unidade 60. Congelar ofensiva custa 200 (máx. 2), recarregar corações 350.
9. **Ofensiva:** reconciliada ao abrir o app e ao concluir lição. Cada dia perdido consome 1 congelamento; sem o suficiente,
   a ofensiva zera e nenhum congelamento é gasto. Treino de erros não conta para a ofensiva.
10. **Liga:** semana de segunda a segunda no fuso de Brasília; quem faz 0 XP não sobe; ninguém desce da Bronze. Os 29 rivais e
    seus nomes são fictícios e determinísticos (semente = semana + índice, SplitMix64 próprio).
11. **Cronômetro:** o tempo é **sempre medido**, mesmo no modo "Desligado", porque o resumo final sempre mostra o tempo médio
    por questão (regra do projeto). O modo só controla o que aparece durante a questão.
12. **Banco de dados:** Room versão 2 com todas as entidades do PLAN §5.2, com `fallbackToDestructiveMigration` **apenas
    enquanto não há dados reais**. A versão 1 só tinha uma tabela de perfil vazia. A partir do primeiro uso real, só
    migrações explícitas.
13. **Dependências novas (todas previstas no PLAN.md):** Navigation Compose 2.10.2, DataStore 1.3.0, kotlinx.serialization
    1.13.0 (+ plugin 2.4.0).
14. **Textos das telas inline no Kotlin** (app só em português do Brasil); `strings.xml` fica com os textos dos componentes
    básicos. Se um dia houver outro idioma, extrair tudo.
15. **Catálogo de design da Fase 1 removido do app** (continua no histórico do git).
16. **Teste de integração fora do app:** `tools/preview-harness/` (Robolectric, projeto separado) joga lições completas sobre
    um Room em memória. Ele achou um bug grave antes da entrega: na **última** questão da lição o painel de feedback nunca
    aparecia. Robolectric e `androidx.test` não estão no PLAN.md; **peço autorização** para promover esses testes a
    `app/src/test` e rodá-los no `./gradlew check`.
17. **Conteúdo da Unidade 1:** 50 questões (`source: "ia"`, `reviewed: false`), validadas por `tools/validate_content.py` e
    revisadas em segunda passada (`content/review/u1-banco-de-dados.md`). A revisão achou e corrigiu 3 ambiguidades.
    A posição da resposta correta está balanceada (A a E: 9, 9, 9, 9, 8).

Pontos fracos conhecidos:
- Sem sons, sem notificações, sem exportar/importar progresso nem importar pacotes de conteúdo (carregador de pacotes
  importados já existe em `ContentRepository`, falta a tela).
- Acessibilidade: rótulos e papéis de TalkBack estão nos componentes, mas ainda não houve passada com TalkBack real.
- Tamanduá segue sendo o mascote menos convincente.

## Varredura de funções incompletas (15/11/2026) — a pedido do dono

O dono pediu uma varredura de tudo que estava faltando ou não se completava, inclusive o ícone. Achados e decisões:

1. **Ícone:** era o triângulo provisório da Fase 0, na paleta antiga. Agora é um ícone adaptativo com o Guaiamum sorrindo
   sobre turquesa, com versão redonda, monocromática (ícones temáticos do Android 13) e ícone pequeno de notificação.
   Gerado por `tools/generate_icon.py` a partir da mesma geometria do mascote. Em `mipmap-anydpi`, porque o minSdk é 26.
2. **Notificações (Fase 6):** lembrete diário no horário escolhido (★ 19:00) se nenhuma lição foi feita, e aviso às 21:30
   se a ofensiva está em risco. A regra é pura e testada (`domain/notify/ReminderPolicy.kt`); o WorkManager agenda um
   `OneTimeWork` recalculado a cada disparo, sem alarme exato. Avisos atrasados mais de 90 min (aparelho desligado,
   economia de bateria) são descartados. Concluir uma lição apaga o lembrete pendente.
   - **Permissão:** não há onboarding, então o pedido de notificação aparece como um cartão na Trilha depois da primeira
     lição concluída ("Quer que o Guaiamum te lembre…?"), e em Configurações > Lembretes. Se negada, o app explica e leva
     à tela do sistema.
   - **Reinício do aparelho:** o próprio WorkManager reagenda (ele traz o receptor de `BOOT_COMPLETED`), então não há
     receptor próprio.
   - **Permissões:** o WorkManager adiciona `ACCESS_NETWORK_STATE`, `FOREGROUND_SERVICE` e `WAKE_LOCK`. As duas primeiras
     foram **removidas** no manifesto (o app não usa rede nem serviço em primeiro plano). **`WAKE_LOCK` ficou**: é uma
     permissão "normal", sem pergunta ao usuário, usada pelo WorkManager para não perder o disparo. Ela não está na lista
     do CLAUDE.md, então **o dono decide**; removê-la pode fazer lembretes falharem em alguns aparelhos.
3. **Sons (Fase 8):** acerto, erro, combo, lição concluída, conquista e clique, sintetizados por `tools/generate_sounds.py`
   (WAV curtos, ~110 KB no total). Tocados com SoundPool, respeitando o volume de mídia; opção "Sons" em Configurações.
4. **Backup do progresso:** Configurações > Dados > Exportar/Importar progresso (JSON, pelo seletor de arquivos, sem
   permissão). A importação valida formato e versão e substitui tudo numa única transação. O backup automático do Android
   agora também inclui preferências e pacotes importados.
5. **Importar pacote de conteúdo:** Configurações > Conteúdo. O pacote passa pelo mesmo validador do app e entra na trilha
   na hora; pode ser removido depois. Para cada unidade vale o pacote de versão mais alta (no empate, o importado).
6. **Interações que não faziam nada:** chip de gemas abre a Loja; chip de dias para a prova abre o planejamento
   (lições por semana até 21/02/2027); o livro do banner da unidade abre o **guia da unidade** (lições, status e tópicos
   do edital). A chama da ofensiva fica cinza enquanto você não estudou no dia.
7. **Lição:** novo chip "Lição mm:ss" (PLAN §7, "tempo restante da lição"). Os chips do cabeçalho quebram linha com fonte
   grande, e o número do anel não cresce com a fonte do sistema.
8. **Resultado:** aviso de "Subiu para o nível N".
9. **Acabamento:** fundo da janela no tema do app (sem tela branca ao abrir no escuro); barras do sistema acompanham o tema
   escolhido; mascotes param de animar quando "Remover animações" está ligado no Android; versão do app em Configurações;
   aviso sobre otimização de bateria (PLAN §8).
10. **Acessibilidade automática:** `tools/preview-harness` agora verifica que **todo elemento tocável tem rótulo** para o
    TalkBack (5 abas e 3 tipos de exercício) e renderiza Trilha e lição com fonte 1,5×.
11. **Versão:** 0.3.0 (versionCode 3).

Continua incompleto (fora do alcance de uma varredura de código):
- **Conteúdo das Unidades 2 a 10** (Fase 9): é o maior buraco do app. Exige geração e revisão unidade por unidade.
- **v1.1 do PLAN:** simulado (20q/60min e 80q/4h), teste de nivelamento para pular unidade, missão mensal.
- **Marcar questão como revisada:** hoje só editando o JSON (`reviewed: true`). Não há botão no app de propósito: o
  pacote é a fonte da verdade.
- **Validação em aparelho real:** TalkBack, vibração, sons, horário de notificação e virada de dia/semana reais.

## Versão social com Supabase (0.4.0, 15/11/2026) — a pedido do dono

Plano completo em `docs/PLANO_SOCIAL.md`. As perguntas em aberto ficaram com o padrão sugerido, porque o dono pediu para
construir sem responder antes:

1. **Ranking só semanal**, de segunda 00:00 a domingo 23:59 (Brasília), calculado no servidor a partir das sessões
   enviadas, e não de um total vindo do app.
2. **Amizade:** um pedido feito pelo ranking geral precisa ser aceito; o código `TRI-XXXXX` vale na hora. O código
   funciona mesmo com quem está oculto do ranking geral. Se os dois se pedirem, a amizade sai na hora.
3. **"Liga Ouro" virou "Pódio da semana":** top 3 do ranking geral na semana anterior, com pelo menos 3 pessoas que
   estudaram. Recompensa de 100 gemas, concedida uma vez por semana (chave `podium:<semana>`). A aba "Ligas" virou
   **"Ranking"**, e as missões do dia passaram a um cartão compacto no topo dela.
4. **Liga simulada removida** (`LeagueEngine`, tabela `league_week`). A coluna `stats.bestLeagueTier` continua no banco
   sem uso, porque o SQLite do minSdk 26 não apaga colunas com facilidade.
5. **Cliente REST próprio em vez do `supabase-kt`.** O login é o do **Supabase Auth**: as contas ficam em
   Authentication → Users. O app chama direto os mesmos endpoints públicos que o SDK usaria (`/auth/v1/otp`,
   `/auth/v1/verify`, `/auth/v1/token` e `/rest/v1/rpc/*`), com `HttpURLConnection` atrás da interface `HttpTransport`.
   Motivo: não adicionar `supabase-kt` + Ktor (CLAUDE.md pede para não incluir dependências sem combinar). A troca pelo
   SDK é possível depois, sem mudar o servidor.
6. **Escrita só por funções.** Toda tabela tem RLS; `insert/update/delete` são revogados para `anon` e
   `authenticated`. O app só lê o próprio perfil e o próprio retrato, e todo o resto passa por funções
   `security definer` com `search_path` fixo. As funções internas (`are_friends`, `pair_streak`…) não podem ser
   chamadas pelo app.
7. **Anti-trapaça "confiança + limites"**, iguais no app (`SessionLimits`) e no servidor: até 210 XP por lição (150 até a
   0.9.0, ver "Limite de XP da lição no ranking") e 120 por treino, no mínimo 2 s por resposta, data entre hoje − 7 e hoje, e 40 sessões por dia. O id da sessão é um UUID
   gerado no app, então reenviar não duplica. Uma sessão que o app já sabe que será recusada sai da fila, para não
   travar as seguintes.
8. **Progresso na nuvem = retrato JSON** no mesmo formato do backup, com versão otimista (`expected_version`). Se outro
   aparelho gravou antes, o app pergunta qual progresso fica (diálogo obrigatório). O ranking não muda, porque vem das
   sessões. Na primeira entrada: sem nuvem, sobe o do aparelho; sem progresso local, baixa a nuvem; com os dois iguais
   (por exemplo, backup do Android restaurado), baixa sem perguntar; com os dois diferentes, pergunta.
9. **Offline-first:** o Room continua sendo a fonte do estudo. As sessões vão para uma fila (`outbox_session`), e a
   sincronização roda ao abrir o app, ao voltar para ele (se a última rodada tiver mais de 5 min), depois de cada sessão
   e de hora em hora pelo WorkManager, só com rede. O ranking fica em cache para ser visto offline.
10. **Conta obrigatória:** sem sessão ou sem perfil, o app abre na tela de entrada. Por isso, o primeiro acesso exige
    internet; depois, estudar funciona offline.
11. **Sair da conta limpa o aparelho** (o progresso está na nuvem). Antes de sair, o app avisa se ainda há algo não
    enviado. **Excluir a conta** apaga em cascata no servidor (`auth.users` → perfil, sessões, amizades, retrato).
12. **Backup automático do Android** exclui `datastore/auth.preferences_pb` (o token não viaja para outro aparelho) e
    `datastore/sync.preferences_pb` (o id do aparelho precisa ser único).
13. **Ofensiva em dupla** conta só dias com **lição concluída** pelos dois, a mesma regra da ofensiva individual
    (CLAUDE.md, regra 7). O aviso das 21:30 menciona as duplas em risco, e a Trilha mostra um banner.
14. **Permissões:** entram `INTERNET` e `ACCESS_NETWORK_STATE`, esta última usada pelo WorkManager para só sincronizar
    com rede. Continuam `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` e `WAKE_LOCK`, que vêm do WorkManager.
    `FOREGROUND_SERVICE` continua removida.
15. **Room v3 com migração escrita à mão** (`MIGRATION_2_3`): cria `outbox_session`, adiciona `stats.podiums` e apaga
    `league_week`. A destruição automática do banco ficou só para quem vem da v1. Teste: `MigrationTest` na harness,
    que cria um banco v2 a partir de `app/schemas/.../2.json` e abre com a 0.4.
16. **SMTP:** o padrão do plano era o Resend, mas ele exige um domínio verificado para mandar e-mail a terceiros. O guia
    (`docs/SUPABASE_SETUP.md`) recomenda o **Gmail com senha de app** para quem não tem domínio, e o Resend para quem tem.
17. **Testes da harness:** `androidx.work:work-testing` entrou **só** na harness (`tools/preview-harness`), não no app.
18. **"Confirm email" desligado no Supabase** (descoberto no primeiro teste real): com ele ligado, um e-mail que recebeu
    o link de confirmação fica preso como "não confirmado", e o Supabase manda o template de confirmação, com link,
    em vez do código. O código de 6 dígitos já prova a posse do e-mail. O passo está no `docs/SUPABASE_SETUP.md`.

## Associar pares: erro corrigido (0.4.2, 15/11/2026) — decisão do dono

Antes, qualquer par errado marcava a questão como erro, mesmo depois de o aluno completar todos os pares: custava
coração, a questão repetia no fim e o resultado saía como erro, embora a tela deixasse continuar. O dono escolheu
**"corrigiu vale, com custo menor"**:
- Se o aluno completa os pares sem errar nenhum, é acerto normal: 10 XP, ou 15 com o bônus de velocidade.
- Se errou algum par no caminho, a questão é **corrigida**:
  - vale 5 XP, zera o combo e não conta para a lição perfeita;
  - **não** custa coração e **não** repete no fim;
  - entra na revisão (Praticar erros) como um erro;
  - feedback âmbar "Pares completos!", e no resumo aparece como "Corrigida no caminho".
- Regra pura em `LessonRun.answer(..., corrected = true)` e `XpEngine.answerXp(..., corrected)`, com testes unitários e
  um teste de integração na harness.

## Personagens: desenho e animação (0.4.3, 15/11/2026) — a pedido do dono (itens 1 a 4 da proposta)

1. **Desenho:**
   - sombra no chão, que encolhe quando o personagem pula;
   - meia-lua de sombra embaixo de corpo e cabeça (`fillOvalShaded`/`shadeInside`), para dar volume;
   - brilho duplo nos olhos.
   - **Tamanduá-bandeira redesenhado** de perfil: focinho cônico curvado para baixo, que se funde com a cabeça; cauda
     em bandeira com mechas de pelo; patas com garras grandes.
   - **Preguiça** com mãos grandes e três garras curvas escuras.
   - Tudo continua desenhado em código (regra 1), sem biblioteca nova.
2. **Animação:**
   - "squash & stretch" na troca de pose: encolhe, estica e assenta com mola;
   - física de pulo: achata ao tocar o chão e estica no alto;
   - piscada dupla de vez em quando;
   - menos confete na comemoração.
3. **Gestos de quem está parado:** um a cada 4 a 8 s. A agenda é pura e testada (`FidgetSchedule`), em O(1) e
   determinística pela semente do personagem.
   - Gestos comuns: olhar em volta e pulinho.
   - **Jeito próprio de cada personagem:**
     - Guaiamum anda de lado estalando as garras;
     - Tartaruga recolhe a cabeça por trás da borda do casco e volta;
     - Tamanduá fareja e mostra a língua três vezes;
     - Preguiça boceja espreguiçando.
4. **Poses novas** e onde aparecem:

   | Pose | Onde |
   |---|---|
   | Pensando (balão de pensamento) | personagem pequeno na lição, enquanto você responde |
   | Nervoso (gota de suor e boca tremida) | na lição, quando o cronômetro visível passa do âmbar |
   | Surpreso ("!") | no feedback, a cada 5 de combo |
   | Acenando | tela de entrada (e-mail) |
   | Orgulhoso | resultado com ofensiva ou conquista nova; perfil com meta do dia batida, no lugar da comemoração contínua |

   A escolha da pose na lição é pura e testada (`LessonMascot`).
5. **"Remover animações" do Android e as prévias** continuam respeitados. O personagem fica parado, e nos primeiros
   6 s na tela nunca faz gesto.

## BanQuest 0.5.0 (15/11/2026) — a pedido do dono

1. **Nome "BanQuest"** no ícone, na entrada, nas notificações, no compartilhamento do código, nas Configurações e nos
   textos de backup.
   - O `applicationId`/pacote continua `br.com.trilhati`: mudar faria do BanQuest um app novo, sem atualizar por cima.
   - Também continuam o identificador do formato de backup (`trilha-ti-backup`, para os backups antigos continuarem
     válidos), os nomes internos de código (`TrilhaTheme`, `Trilha.colors`) e o nome do repositório.
   - Os textos do e-mail do código ficam no painel do Supabase: o guia agora diz "BanQuest".
2. **Assinatura fixa — chave nova, guardada pelo dono** (escolhas do dono):
   - RSA 4096, válida por 30 anos, certificado "CN=BanQuest", sem dados pessoais.
   - O Gradle lê o caminho e a senha de variáveis de ambiente (`BANQUEST_KEYSTORE_FILE`, `BANQUEST_KEYSTORE_PASSWORD`)
     ou de `~/.gradle/gradle.properties`.
   - `tools/build_apk.sh` gera o APK e confere o certificado contra `tools/signing/banquest-cert-sha256.txt`. Ele recusa
     APK sem a chave ou com outra chave.
   - O APK distribuído continua sendo a variante debug (sem R8, que é a coberta pelos testes), agora com a chave fixa.
     Passar para a variante release fica para quando der para testá-la num aparelho.
   - Consequência aceita pelo dono: a passagem da 0.4.x para a 0.5.0 exige desinstalar e instalar de novo uma vez,
     depois de sincronizar (passo a passo em `docs/ASSINATURA.md`).
3. **Aviso de versão nova:**
   - Fonte: tabela `app_releases` (`0002_releases.sql`), com RLS e sem acesso direto.
   - O app lê pela função `latest_release()`, liberada também para quem não está logado (papel `anon`), para a tela de
     entrada e um app desatualizado também receberem o aviso. A função não expõe nada pessoal.
   - Só o dono publica, pelo painel. O `0001` devolve essa permissão se for rodado de novo depois do `0002` (testado em
     `tools/sql-tests`).
   - O app confere ao abrir, ao voltar (no máximo a cada 30 min) e no trabalho de sincronização de hora em hora.
   - Avisos:
     - na Trilha, com "Agora não" por versão;
     - uma notificação por versão, no canal novo "Atualizações do app";
     - em Configurações → Sobre o app, com "Verificar atualização".
   - Versão abaixo de `min_version_code` → diálogo que não fecha.
   - Regra pura e testada (`UpdateRule`). O link precisa ser https.
   - O download é só um link aberto no navegador. O app não instala nada sozinho, então nenhuma permissão nova
     (`REQUEST_INSTALL_PACKAGES` não entrou).
   - APKs ficam num bucket público `apk` do Supabase Storage (guia em `docs/PUBLICAR_VERSAO.md`). Qualquer link https
     serve.


## Provas anteriores na trilha (16/11/2026) — a pedido do dono
1. **Quais provas entram:** as questões de TI de provas reais que o dono enviou em PDF, de qualquer banca, cada uma com
   a banca na etiqueta. Hoje são FGV · Banestes 2018 e 2021, FCC · DPE-RR 2015 e FCC · Prefeitura de Teresina 2016.
   - Fonte da verdade: `content/provas/*.json`, um arquivo por prova, com a lição de destino (`lesson`), a prova de
     origem (`ref`), a situação do gabarito (`gabarito`) e marcações (`flags`). O validador é `tools/validate_provas.py`.
   - O texto foi conferido contra o PDF; figuras e tabelas viraram texto. A resposta segue o gabarito oficial e foi
     conferida resolvendo cada questão. Só a explicação é escrita por IA (`explanation_source: "ia"`, `reviewed: false`).
2. **Origem nova `prova_real`** (em vez de criar `fgv_real` etc.): vale para qualquer banca, e a banca fica em
   `ref.banca`. `fcc_real` continua aceito. A regra 4 do CLAUDE.md vale assim: só questão com `ref.banca` FCC (ou
   `fcc_real`) pode ser chamada de "da FCC", e o validador recusa "FCC" no texto de questão de outra banca.
3. **Fora por padrão (escolha do dono):** questões que só valem para versões antigas (`versao_antiga`: PMBOK 4/5,
   COBIT 4.1/5, CMMI 1.3, MPS.BR antigo) e as fora do edital (`fora_do_edital`). Elas ficam guardadas em
   `content/provas` mas não vão para o app. Das 135, entram 123. Questões que citam uma versão anterior da norma mas
   cuja resposta não mudou (`versao_citada_anterior`) entram.
4. **Onde aparecem (escolha do dono): dentro das lições da trilha**, não numa área separada. Cada questão vai para a
   lição do tópico (ex.: subconsultas → `u1l3`). O TDD e o Mercurial foram para "Testes, TDD e Git" (`u2l5`), porque é
   lá que o PLAN põe esses temas.
   - Elas **somam** ao banco da lição, que passa de 10 questões. A lição continua sorteando 8, priorizando erros.
   - ★ Só entram em lições que já têm pacote de conteúdo. A trilha libera uma lição por vez e a primeira lição vazia
     trava o resto; abrir lições só com 1 ou 2 questões de prova daria lições curtas demais. Hoje entram as 26 da
     unidade 1. As outras 97 entram sozinhas quando o pacote de cada unidade chegar, sem mexer em nada.
5. **Pacote do app gerado, não editado à mão:** `tools/build_provas.py` monta `assets/provas/provas-anteriores.json` a
   partir de `content/provas`, sem as questões escondidas e sem o ";" que a FGV põe no fim de cada alternativa. O
   `./gradlew check` roda o validador e falha se o pacote estiver desatualizado (`--check`).
6. **Na lição:** etiqueta "FGV · Banestes 2021 · questão 48" (com "gabarito preliminar" ou "gabarito não confirmado"
   quando for o caso) e o selo "explicação não revisada" no lugar de "não revisada", porque o enunciado é o original.

## Provas anteriores: Conhecimentos Gerais (16/11/2026) — a pedido do dono

1. **Entram as questões gerais das provas FGV · Banestes 2018 (Q1–50) e 2021 (Q1–35)**, nos mesmos arquivos de
   `content/provas`, nas lições G1–G4 (`u11`–`u14`) do `docs/CONTENT_PLAN.md`: Português 35, Raciocínio 30, mercado
   financeiro 11, ética e lavagem de dinheiro 6. A proporção fica perto do peso de cada matéria na prova (10/10/5/5).
   - Ficam de fora as anuladas (2021 Q21 e Q22). A 2021 Q25 (criptomoedas, comunicado de 2017) entra com
     `versao_antiga`, porque o marco legal de 2022 mudou o papel do Banco Central.
   - Fidelidade: texto conferido contra o PDF e resposta do gabarito definitivo, conferida resolvendo a questão. Termos
     sublinhados aparecem entre colchetes, com aviso no enunciado. O Texto 1 e o Texto 2 de 2018 entram em cada questão
     que os usa. A charge de 2018 Q16 virou descrição em texto (`depende_de_figura`).
   - As questões sobre o Guia de Conduta Ética do Banestes (2018 Q28–30, 2021 Q16) foram conferidas contra o guia
     atual (PDF do site do banco, atualização de 2020): as respostas oficiais continuam valendo, e as explicações citam
     os itens atuais. As de 2018 levam `versao_citada_anterior` (a Q30 cita o Capítulo I; hoje a regra está no 8.9.1).
     O PDF do guia não entra no repositório.
   - 2018 Q25 diz que o Copom foi instituído em 1999. Mantido como na prova; a explicação corrige: 20/06/1996 (em 1999
     veio o regime de metas para a inflação, Decreto nº 3.088).
2. **Não atrapalham o fluxo de estudo:** o app recusa o pacote de provas inteiro se uma questão aponta para lição que
   não existe no `Curriculum.kt`. Por isso `tools/build_provas.py` só leva ao app as questões de lições que já existem
   no `Curriculum`. As 83 gerais ficam guardadas em `content/provas` e entram sozinhas quando a trilha dos Gerais for
   criada; o pacote do app continua com 123 questões, todas de Específicos.
3. **Peso na trilha (para quando os Gerais entrarem):** os Gerais valem 30 de 130 pontos (peso 1 contra peso 2 dos
   Específicos), cerca de 23% da nota. Sugestão: uma lição de Gerais para cada três de Específicos, para que a parte
   de TI continue sendo a maior parte do estudo. A forma de mostrar os Gerais na trilha segue aberta (CONTENT_PLAN,
   decisão 2).
4. Os PDFs das provas ficam em `docs/provas/`, fora do git (`.gitignore`), só para conferência.

## Lições por conceitos — piloto na u1l1 (16/11/2026) — a pedido do dono
Ideia do dono: ensinar com tarefas pequenas e repetidas, como nos apps de idiomas, sem abrir mão das questões no nível
da prova. Escolhas do dono: lição de 10 a 12 exercícios e exercícios adaptados das questões oficiais.
1. **Conceitos e níveis.** A lição lista de 4 a 7 conceitos; cada exercício aponta para um conceito e tem um nível:
   aprender (certo/errado e escolha rápida, o tipo novo `quick` com 3 alternativas), praticar (associar, completar,
   ordenar, cenário curto) ou prova (FCC de 5 alternativas e as oficiais). Lição sem conceitos continua no formato
   antigo (sorteio de 8), então a mudança é por lição e não quebra as outras.
2. **Montagem da sessão (`LessonPlanner`, puro e testado).** ★ 12 exercícios: 4 de aprender, um por conceito, na ordem
   dos conceitos; 5 de praticar, intercalando conceitos e evitando o mesmo tipo em sequência; 3 de prova no fim. No
   máximo 4 conceitos por sessão, para cada um aparecer 2 ou 3 vezes. Erros anteriores entram primeiro e puxam o
   conceito deles. Se faltar exercício num nível, completa com os outros. A repetição do erro no fim continua igual.
3. **Adaptadas.** Exercício feito a partir de uma questão oficial leva `adaptedFrom` e a etiqueta "adaptada de FCC ·
   Prefeitura de Teresina 2016 · questão 56", com o selo "não revisada" (é texto de IA). Ela nunca cai na mesma sessão
   que a original, senão a original vira decoreba. O validador confere que a original existe em `content/provas` com a
   mesma referência. Dizer "adaptada de FCC · …" não é afirmar que o exercício é da FCC (regra 4): ele aponta para a
   questão real que o dono forneceu.
4. **Cronômetro por nível.** ★ Prova usa o ritmo configurado (3:00, ou 2:30, com a opção de 4:00 para caso prático);
   praticar, metade (1:30); aprender, 2/9 (0:40). Continua nunca reprovando, e o chip "Lição" soma os alvos de cada
   exercício que falta.
5. **Dica do conceito.** Nos exercícios de aprender aparece um botão "Dica: <conceito>" que abre um resumo de 2 ou 3
   linhas. Fica fechado até o aluno pedir.
6. **Banco da lição.** Formato por conceitos: de 24 a 80 exercícios (a u1l1 tem 46, mais as 6 oficiais). A meta de
   ~40% de casos práticos e o balanço da posição da resposta entre A e E valem para as questões de prova; a escolha
   rápida tem o seu próprio balanço entre A, B e C.
7. **Versão do pacote.** `u1-banco-de-dados` foi para a versão 2, para ganhar de um pacote importado antigo.

## Aprender fazendo, meio coração e "Não sei" (0.7.1, 16/11/2026) — a pedido do dono
Retorno do dono sobre a 0.7.0: os exercícios novos pareciam "jogados" e chutáveis, e com vidas que demoram a voltar não
dá para aprender errando. Escolhas do dono: errar em exercício de ensino custa **meio coração**, e o botão **"Não sei"**
é um bônus que se ganha com o tempo e se compra na loja.
1. **Sessão em blocos (substitui o item 2 da decisão anterior).** ★ 3 conceitos por sessão, **na ordem da lição**:
   primeiro os que o aluno ainda não viu (descobertos pelas respostas já dadas naquela lição), depois os dos erros,
   depois os outros, sorteados. Cada conceito é um bloco de 3 exercícios em sequência (passo 1 exemplo resolvido, 2
   aplicar, 3 variar); a sessão fecha com 3 questões de prova dos conceitos dela. Total: 12. Prova só de conceito já
   ensinado; se faltar, a sessão completa com ensino.
2. **Exercícios que ensinam.** O enunciado traz a regra ou um exemplo resolvido, e a resposta sai do raciocínio. Um
   cenário por conceito. Campo novo `step` (1, 2, 3…; variantes com o mesmo passo).
3. **Conceito novo.** No primeiro exercício de um conceito que o aluno ainda não viu, a dica vem aberta com o rótulo
   "Novo conceito: …". Depois, continua opcional.
4. **Meio coração.** Corações passam a contar em metades (coluna `heartHalf`). Erro em aprender ou praticar custa ½;
   erro em questão de prova custa 1. A regeneração continua no mesmo ritmo: ½ a cada 15 min (1 a cada 30). O contador
   mostra "4½". A repetição de um erro no fim da lição continua sem custo.
5. **"Não sei".** Estoque (colunas `helps` e `helpsDay`): todos começam com 3; a cada dia novo ganha +1 até 3; na loja
   custa 50 gemas e dá para guardar até 9. Só aparece em aprender e praticar. Usar mostra a resposta com a explicação,
   não custa coração, não dá XP, e o exercício volta no fim da lição e vai para a revisão (conta como erro na precisão e
   tira o "perfeito"). Na questão de prova não há "Não sei", porque é a parte de avaliar.
6. **Banco v4.** Migração 3→4 só acrescenta colunas (ninguém perde progresso; testada na harness). O backup continua na
   versão 1: os campos novos têm valor padrão, e um app antigo ignora o que não conhece.

## Menos texto nas telas (0.7.2, 16/11/2026) — a pedido do dono
O dono achou o app "poluído de textos", principalmente o Ranking. Regras que passam a valer para toda tela:
1. **Uma linha por item.** Nada de parágrafo explicativo embaixo de opção, cartão ou lista.
2. **Explicação que importa vai para o ⓘ** (`InfoButton`): abre um diálogo curto. Usado em Ofensivas em dupla, Praticar
   (como funciona a revisão) e nas seções das Configurações (Conta, Lembretes, Cronômetro, Conteúdo, Dados, Sobre).
3. **Número e ícone no lugar de frase.** Ex.: ofensiva em dupla é a chama com o número; recompensas do resultado são
   fichas curtas ("+125", "Meta batida", "Baú liberado", "2 missões"); o tempo médio mostra "alvo 3:00" ao lado.
4. **Ação rara ou destrutiva sai da frente.** "Desfazer amizade" foi para o menu ⋯ de cada amigo; "Atualizar" virou o
   botão ↻ no título do ranking, e o "Atualizado há…" só aparece quando há problema (sem conexão, erro ou lições
   esperando internet).
5. **Loja em linhas** (ícone, nome, uma linha e o preço à direita), sem a fala do personagem.
6. **Acessibilidade continua completa:** o que saiu da tela continua no rótulo do TalkBack (ex.: "Ofensiva da dupla:
   5 dias", "Missões cumpridas: …", "Saiba mais: Ofensivas em dupla").
O resumo do resultado continua mostrando o tempo médio por questão (regra 6).

## Tela de exercício enxuta e coração rachado (0.7.3, 16/11/2026) — a pedido do dono
1. **"4,5" com o coração rachado, no lugar de "4½".** Com 4 inteiros e meio, o contador mostra **4,5** (vírgula, padrão
   do português) e o ícone do coração aparece rachado (metade inteira, metade desbotada, rachadura em zigue-zague). O
   próximo erro de ensino quebra esse coração e o contador vai a 4. O contador treme quando um coração racha ou quebra
   (sem animação se o aparelho pedir para remover animações). No diálogo de corações da Trilha, o coração da vez aparece
   rachado. O TalkBack lê "4 corações e meio". A regra (½ por erro de ensino, 1 por erro de prova) não mudou; o máximo
   continua 5.
2. **Tempo: só o anel.** Saem da questão as fichas "Folga" e "Lição"; a folga continua no resumo final. Durante a lição
   só aparece a ficha de **atraso**, quando o aluno está abaixo do ritmo (folga negativa). O combo continua a partir de 3.
3. **Uma faixa curta antes do enunciado.** Origem em uma linha ("FCC · Teresina 2016 · Q22", "Adaptada · FCC · …"), o
   selo "não revisada" pequeno (regra 3 continua: o selo está sempre visível), a ficha "Repetição · 5 XP" e a dica do
   conceito **fechada** ("Novo: …" no primeiro exercício de um conceito novo, "Dica: …" depois). Tocar na origem ou no
   selo abre "Sobre esta questão", com a prova completa, a situação do gabarito e o que não foi revisado.
4. **Detalhes.** A instrução do associar ficou "Toque num termo e na definição". O botão "Não sei" some quando o estoque
   chega a zero.


## Questões oficiais ligadas aos conceitos das lições 2 a 5 (0.8.2, 17/11/2026)
Com as lições 2 a 5 da U1 no formato por conceitos (pacote v4), o planejador só sorteia questão de prova que tenha um
conceito da lição (`LessonPlanner.exam`). As 20 questões oficiais dessas lições (FCC e FGV) estavam sem `concept` e, na
0.8.1, nunca apareciam. O validador acusava isso, mas não tinha rodado no ambiente em que o pacote v4 foi gerado.
1. **Cada questão ganhou o conceito mais próximo do assunto que ela cobra** (lista em
   `content/review/u1-provas-conceitos.md`). Questões sobre recursos do Oracle e do DB2 que não são multitenant (IOT,
   ROWNUM, DUAL, tabelas temporárias) ficaram em "Oracle multitenant e H2", o conceito de SGBDs específicos da lição 4.
2. **Validador:** as alternativas são comparadas sem descartar símbolos ("x = NULL" e "x <> NULL" são diferentes), e
   a comparação de enunciados inclui os pares do associar e as linhas do ordenar. Antes, a instrução padrão do
   associar ("Retome o cenário da lição e associe…") gerava 2 erros e 174 avisos falsos.
3. **Testes:** o teste de integração confere que toda questão de prova de uma lição por conceitos tem conceito da
   lição. Os testes que contavam 90 exercícios ou supunham "a u1l2 só tem prova" foram atualizados para o pacote v4.

## Temas e trilha dos Gerais (0.9.0; feito em 16/11/2026 e lançado em 17/11/2026) — a pedido do dono
O dono enviou duas paletas de referência e pediu: cores em 60/30/10, as paletas numa aba de temas, a escolha do tema na
primeira entrada (avisando que dá para trocar nas Configurações) e a trilha dos Gerais com um botão simples de troca e
animação fluida.
1. **Quatro temas:** os dois que já existiam, **Claro** e **Escuro** (roxo e turquesa), e dois novos, **Elétrico** e
   **Chiclete**. O antigo "Sistema" deixou de ser opção: antes da primeira escolha o app segue o claro/escuro do
   aparelho, e quem tinha Claro ou Escuro fixo continua no mesmo tema.
2. **60/30/10 com o fundo sempre na cor clara** (correção do dono: "o fundo deve ser sempre a cor clara"):
   - Elétrico: 60% branco #FFFFFF no fundo e nos cartões, 30% azul #0131CD na cor principal (botões, lição atual,
     faixa da unidade, seletor) e 10% verde-limão #B9FC01 no destaque (lições concluídas). Texto azul-marinho.
   - Chiclete: 60% creme #FEF1CF no fundo e nos cartões, 30% rosa #FC3B7F na cor principal e 10% azul #0032A8 no
     destaque. O texto sobre o rosa é quase preto: branco sobre esse rosa fica em 3,5:1, abaixo do mínimo de 4,5:1.
   As cores de estado (acerto, erro, alerta, chama, gema, coração) não vêm da referência: são as do tema Claro.
3. **Contraste testado.** O `ContrastTest` cobre todo par texto/fundo dos quatro temas e das trilhas dos Gerais, com
   mínimo de 4,5:1. Agora inclui os textos de acerto, erro e alerta sobre o fundo e os cartões (ex.: "Meta batida!").
4. **Escolha na primeira entrada.** Depois do login e antes da Trilha aparece "Escolha o seu tema", com os quatro
   cartões (miniatura e faixa 60/30/10) e o aviso "Dá para trocar quando quiser em Configurações › Tema." A escolha
   fica no DataStore (`palette`, `theme_chosen`). Quem atualizar de uma versão anterior também vê essa tela uma vez. Nas
   Configurações, a seção **Tema** mostra os quatro em duas colunas.
5. **Gerais: duas trilhas independentes** (★ da decisão 2 do `docs/CONTENT_PLAN.md`). O seletor "Específicos | Gerais"
   fica no topo da Trilha e a escolha é lembrada (`trail_track`). Cada trilha desbloqueia na sua própria ordem. As
   unidades G1 a G4 (`u11`–`u14`) estão no `Curriculum` com os títulos de lição do plano de conteúdo. Ainda não têm
   pacote, então aparecem como "Conteúdo chegando em breve". As 82 questões de prova dos Gerais já estão no pacote do
   app (u11: 35, u12: 30, u13: 11, u14: 6) e entram sozinhas quando cada lição ganhar conteúdo.
6. **Cores da trilha dos Gerais:** a cor principal e a de destaque do tema trocam de papel. Claro e Escuro: roxo vira
   verde-água. Elétrico: azul vira limão. Chiclete: rosa vira azul. O botão selecionado do seletor tem borda e base em
   relevo, para continuar visível quando a cor da trilha é clara.
7. **Animação da troca.** A trilha desliza para o lado da trilha escolhida, com esmaecimento, e as cores se misturam em
   0,5 s. O botão do seletor desliza com efeito de mola. Se o aparelho pedir para remover animações, a troca é imediata.

## Limite de XP da lição no ranking (0.9.1, 17/11/2026) — a pedido do dono
O dono perguntou se o ranking estava atualizando. Não estava: as lições boas no formato por conceitos não chegavam ao
servidor.
1. **Causa.** O limite anti-trapaça da lição (decisão 7 do social) era 150 XP, a lição perfeita no formato antigo
   (8 questões × 15 + 20 de conclusão + 10 de perfeita). A lição por conceitos tem 12 exercícios
   (`LessonPlanner.SIZE`) e chega a 210 XP. Acima de 150, o app tirava a sessão da fila sem enviar, e o servidor
   recusaria do mesmo jeito. Num teste com 4 lições da U1 (12, 10, 8 e 6 acertos de primeira), o app somou 690 XP e
   o ranking recebeu 140. Isso afetava também a ofensiva em dupla (o servidor só conta o dia com lição recebida) e o
   "Pódio da semana". XP, nível e ofensiva individual não eram afetados.
2. **Correção.** O limite da lição passou para **210** no app (`SessionLimits.LESSON_MAX_XP`), no `submit_session` e
   na restrição da tabela `study_sessions`. A mudança fica no próprio `0001_social.sql`, que pode ser reaplicado: uma
   migration separada seria desfeita por quem rodasse o 0001 de novo. O 0001 refaz a restrição da tabela, então
   rodá-lo de novo atualiza um banco criado antes.
3. **Para não voltar a acontecer:** o `SocialRulesTest` joga a melhor lição possível de `LessonPlanner.SIZE`
   exercícios e exige que o limite seja exatamente o XP dela. Se o tamanho da lição ou o XP mudarem, o teste falha. O
   pior caso de respostas (todas erradas e repetidas, 24) também cabe no limite de 30. A harness tem um teste de ponta
   a ponta com uma lição real de 12 exercícios, e o `tools/sql-tests/run.sh` simula um banco antigo antes de reaplicar
   o 0001.
4. **O que já se perdeu não volta.** As sessões recusadas saíram da fila do aparelho. O ranking das semanas passadas
   fica como está, e o desta semana passa a contar a partir da atualização.

## Provas anteriores na aba Praticar (0.10.0, 17/11/2026) — a pedido do dono
Pedido do dono: um modo "provas anteriores" na aba Praticar. Completar uma prova dá XP, mas só se o aluno completar
tudo. Cada vez que abre, gasta um coração, mas errar não tira coração. Isso precisa ser avisado antes de começar. Se
possível, uma opção para silenciar as notificações durante a prova.
1. **Uma prova = as questões dela que estão no pacote de provas** (`assets/provas`), agrupadas por banca, órgão, ano e
   cargo (`PastExams`). O pacote já tira as de versão antiga e as fora do edital, então a prova tem só o que vale para o
   edital atual: FGV Banestes 2021 (57), FGV Banestes 2018 (87), FCC Teresina 2016 (35) e FCC DPE-RR 2015 (26). As
   questões dos Gerais e das unidades sem conteúdo também entram: é o primeiro lugar do app onde elas aparecem.
2. **Regras pedidas pelo dono:**
   - antes de começar, uma tela de aviso com as regras, o tempo estimado e o gabarito (preliminar ou não confirmado);
   - "Começar" gasta 1 coração inteiro, e nada é gasto antes disso;
   - errar não tira coração (a sessão não usa corações);
   - o XP só é creditado quando todas as questões foram respondidas. Sair no meio não grava nada, e o coração gasto
     para abrir não volta.
3. **Escolhas feitas aqui (★), fáceis de mudar:**
   - **Partes:** nas provas com Específicos e Gerais dá para escolher prova inteira, só Específicos ou só Gerais. A
     de 2018 inteira tem 87 questões, cerca de 4h20 no ritmo de 3:00.
   - **Correção na hora**, com a explicação, como nas lições. A versão sem correção, com resultado só no fim, fica para
     o simulado (fase 10).
   - **O erro não volta no fim** (`LessonRun.start(repeatMistakes = false)`), como numa prova real, e vai para
     "Praticar erros". Para isso, o `ContentRepository` passou a achar por id também as questões de prova que ainda
     não estão numa lição.
   - **XP:** 10 por acerto, +5 se em até 2:00, +20 ao terminar e +10 se gabaritar. São as mesmas regras da lição.
   - **Conta como estudo do dia:** ofensiva (também a em dupla, no servidor), meta diária e as gemas de uma lição.
     Não conta como lição concluída nem como treino de erros.
   - **Não devolve coração** ao terminar, ao contrário do treino de erros. Senão, abrir sairia de graça.
   - **Precisa de 1 coração inteiro para abrir.** Com só meio coração, aparece a tela "sem corações". No modo sem
     vidas, abrir é grátis.
4. **Servidor:** o `submit_session` aceita o tipo `exam`, com até 100 respostas e XP até 15 por resposta + 30. As
   restrições da tabela foram refeitas no próprio `0001_social.sql`, como na 0.9.1. A ofensiva em dupla (`pair_streak` e
   `my_friends`) conta o dia com lição ou prova. Sem isso, uma prova de 87 questões cairia no mesmo problema da 0.9.1
   (limite de 30 respostas) e sumiria do ranking. O `SocialRulesTest` e a harness conferem a maior prova do pacote.
5. **Silenciar notificações:** um interruptor na tela de aviso, lembrado para as próximas provas.
   - **Como funciona:** liga o Não perturbe do Android (modo prioridade) ao começar e desliga ao terminar ou sair. Se
     o app for fechado no meio, desliga quando ele abrir de novo (`ExamFocus`, chamado no `TrilhaApp`).
   - **Quando não mexe:** se o Não perturbe já estava ligado, não mexe nele. Se o aluno mudou o modo durante a prova,
     não desfaz a mudança.
   - **Permissão:** precisa de `ACCESS_NOTIFICATION_POLICY` (permissão nova, pedida pelo dono) e de o aluno liberar o
     acesso uma vez nas configurações do Android. O interruptor abre essa tela. No Android 15 ou mais novo, o sistema
     mostra isso como um modo "BanQuest" nas configurações de Modos.
6. **Limite conhecido:** se o Android encerrar o app no meio de uma prova longa (por exemplo, com o app muito tempo em
   segundo plano), a prova recomeça do aviso e o coração gasto não volta. Guardar a prova em andamento fica para
   depois, se fizer falta.

## Treino de erros sem farm (0.10.1, 17/11/2026) — decisão do dono
Um amigo do dono achou o bug: na aba Praticar dava para repetir o treino sem fim. Ele também estranhou que os erros
continuavam lá depois de acertar.
1. **Por que acontecia:**
   - **Erro que não sai:** acertar uma vez só reagenda a questão. Ela sai dos "erros guardados" depois de acertar as 4
     revisões (1, 3, 7 e 14 dias). Isso é de propósito, mas a tela não explicava.
   - **Treinar os mais antigos:** sem nada vencido, o botão repetia as mesmas questões. Cada rodada dava até 120 XP e
     +1 coração, e cada acerto adiantava a revisão espaçada mesmo fora do prazo. Em 4 rodadas seguidas a questão "se
     formava" em minutos, em vez de 25 dias.
   - **Revisão livre:** também dava XP e +1 coração a cada rodada, sem limite.
   - **Efeito:** XP infinito no ranking e corações infinitos (lição de graça).
2. **Escolha do dono: "só vencidas valem".**
   - XP e o coração do treino vêm só da revisão das questões vencidas (`SessionKind.Practice`).
   - O treino sem nada vencido ("Treino livre") e a "Revisão livre" continuam liberados para estudar, mas viram
     `SessionKind.FreeReview`: não dão XP, coração, missão nem meta, e não vão para o ranking. As respostas valem 0
     XP já na hora (`LessonRun.start(awardsXp = false)`).
   - Errar no treino livre ainda volta o cartão para o começo.
3. **Revisão espaçada de verdade:** acertar antes do dia da revisão não adianta o cartão (`SrsEngine.onAnswer`). Vale
   em qualquer sessão (lição, treino e prova). A conquista de revisão conta só as revisões no dia certo.
4. **Telas:**
   - A aba Praticar explica que cada erro sai depois de 4 revisões certas, em dias diferentes.
   - Os botões dizem "(sem XP)" quando não há recompensa, e o resultado do treino livre diz por quê.
   - A tela "sem corações" só oferece "Revisar erros vencidos (ganhe 1 coração)" quando há erro vencido. Os erros de
     hoje só vencem amanhã, então, no dia em que os corações acabam, sobram esperar ou recarregar com gemas.

## U2 e G1 juntas, com as oficiais ligadas a conceitos (0.12.0, 17/11/2026) — a pedido do dono
O dono pediu a U2 (Programação e OO) e a G1 (Língua Portuguesa) na mesma rodada, validando tudo no fim, e enviou os PDFs
das provas do Banestes de 2012 e 2024.
1. **Uma branch e um PR para as duas unidades** (o CONTENT_PLAN sugere uma unidade por PR): foi o pedido do dono. Cada
   unidade tem o seu pacote e o seu relatório de revisão, então dá para revisar uma de cada vez.
2. **Mesmo formato da U1 (por conceitos):** 4 a 7 conceitos por lição, 2 exercícios de aprender e 2 de praticar por
   conceito (passos 1, 2 e 3, um cenário por conceito) e questões de prova estilo FCC. U2: 26 conceitos e 154
   exercícios. G1: 28 conceitos e 168 exercícios.
3. **As 57 oficiais que esperavam por essas lições ganharam `concept`** (22 da U2 e 35 da G1), escolhido pelo assunto
   que cada uma cobra. As que cobram temas fora do edital (ordenação, hash, complemento a dois, NetBeans, Mercurial,
   Factory Method) ficaram no conceito mais próximo, como as do Oracle na U1. Tabelas nos relatórios de revisão.
4. **Provas de 2012 (IDECAN) e 2024: só calibração, sem transcrição.** São do mesmo cargo, mas vieram sem gabarito, e a
   regra do CLAUDE.md exige gabarito para `prova_real`. A de 2024 tem 4 alternativas, e o app e o validador esperam 5.
   Transcrever as duas depende de o dono enviar os gabaritos e de decidir como tratar as questões de 4 alternativas.
5. **Português: só norma-padrão sem divergência.** Onde a gramática admite duas formas (crase facultativa, "comparecer
   a/em", "a maioria de" + verbo), a questão é montada para que a variação não decida a resposta.
6. **Testes:** um teste de unidade novo valida todos os pacotes embutidos e confere que toda questão da U2 e da G1
   (oficiais incluídas) tem conceito da lição. Os testes da harness que usavam a U2 como "unidade sem conteúdo" passaram
   a usar a U3.

## Provas Banestes 2012 (IDECAN) e 2024 (Instituto Access) (0.12.0, 17/11/2026) — a pedido do dono
O dono enviou os gabaritos das duas provas do cargo que, na geração da U2 e da G1, tinham servido só para calibrar o estilo (decisão
anterior, item 4), e depois o Guia de Conduta Ética do Banestes.
1. **Duas provas novas em `content/provas`**, no mesmo formato das outras. O texto foi conferido contra o PDF (figura e
   tabela viraram texto, com `depende_de_figura`); a resposta segue o gabarito e foi conferida resolvendo a questão. Um
   script confere cada letra contra o gabarito oficial.
   - **IDECAN · Banestes 2012** (`idecan-banestes-2012.json`, 60 questões). O arquivo do dono se chama "definitivo", mas
     o documento diz **"GABARITO PRELIMINAR"**: entra com `gabarito: "preliminar"`, e o app mostra a etiqueta. Ficam
     guardadas com `fora_do_edital`, sem ir ao app: COBOL (Q23–26 e Q37), tipos do Visual Basic clássico (Q30), banco
     hierárquico (Q33), fases e artefatos do RUP (Q50–51) e atualidades do Espírito Santo de 2012 (Q13–16). Entram 47.
   - **Instituto Access · Banestes 2024** (`access-banestes-2024.json`, 75 questões, gabarito definitivo retificado).
     Ficam de fora as anuladas (Q12, Q32, Q43), a Q7 (pede a ideia principal da letra inteira de uma canção, que não
     reproduzimos; as Q5 e Q6 entram só com o trecho citado) e a Q11 (depende da numeração de linhas do caderno). A Q63
     (ASP clássico) fica guardada com `fora_do_edital`. Entram 74.
2. **4 alternativas (escolha do dono): a questão fica como foi aplicada.** Os validadores (Python e Kotlin) passam a
   aceitar 4 ou 5 alternativas só em questão `prova_real`; exercício de IA continua com 5. A tela já desenhava qualquer
   número de alternativas até 5. Teste novo em `ContentTest`.
3. **Guia de Conduta Ética (o dono: "é pré-requisito saber")**: as Q48 (os "Rs" da consciência ambiental, item 4.27.2) e
   Q49 (composição do Conselho de Conduta Ética, item 8.3.1) foram conferidas contra o guia enviado (atualizado em 2020)
   e entram em `u14l1`. O PDF do guia continua fora do repositório.
4. **Onde entram:** Português em G1, Raciocínio em G2, Conhecimentos Bancários em G3 e G4, Inglês em `u10l4`, e TI nas
   lições do tópico. Nas lições que já têm pacote por conceitos (U1, U2 e G1), cada questão ganhou o conceito mais
   próximo. Pacote do app: de 205 para 326 questões.
5. Banca nova no validador: "Instituto Access".


## Escolha de prova: Analista de TI ou Técnico Bancário (0.13.0, 17/11/2026) — a pedido do dono
O dono quis que a namorada, que faz a prova de Técnico Bancário do Banestes (FCC, 24/01/2027), usasse o mesmo app. Ele
aprovou um mockup e respondeu as dúvidas antes do código.
1. **Um app só, com a prova escolhida num chip no topo da Trilha** (mecânica de troca de curso; visual nosso). O chip
   mostra o emblema da prova e os dias que faltam; tocar abre um painel só com os cartões das duas provas (data, dias,
   formato); o dono pediu para tirar os textos extras. Separar em dois apps foi descartado: dois APKs, dois avisos de versão, Gerais duplicados e
   ranking dividido.
2. **Cabeçalho mais limpo:** o chip absorveu o relógio da contagem regressiva, e as gemas saíram do topo (o saldo fica na
   Loja). Ficaram prova, ofensiva e corações.
3. **Primeira entrada:** a pergunta "Qual prova você vai fazer?" vem logo depois da escolha do tema, só para quem está
   começando (decisão do dono). Quem já usava o app (tema escolhido antes) continua no Analista, sem pergunta.
4. **Só o emblema muda** de uma prova para a outra (decisão do dono); as cores da trilha seguem o tema.
5. **Cronômetro segue a prova** (decisão do dono): 3:00 no Analista (4 h / 80) e 3:25 no Técnico (4 h / 70). O 2:30
   escolhido nas Configurações continua valendo nas duas. O 3:00 que as versões antigas gravavam passa a "segue a prova".
6. **Progresso único.** O currículo virou um catálogo (`u1`–`u19`) e cada prova escolhe as suas unidades
   (`TargetExam`/`ExamCourse`): uma lição feita vale em qualquer prova que a tenha. Os Gerais são os mesmos; a
   Matemática financeira é dos Gerais no Analista e dos Específicos no Técnico.
7. **G2 dividida** em Raciocínio (`u12`, 4 lições) e Matemática financeira (`u15`, 4 lições; o Técnico vê as 3 primeiras,
   sem VPL e TIR). Sem perda de progresso nem de questão oficial: a G2 não tinha conteúdo, e as 49 oficiais dela estavam
   nas lições 1–4. Os rótulos ("Unidade 3", "G2") passam a vir da posição na trilha da prova.
8. **Específicos do Técnico** (`u16`–`u19` e a `u15`) entram como "em preparação", com aviso e botão para os Gerais. O
   conteúdo vem em PRs próprias (decisão do dono: a 0.13.0 é só o seletor).
9. **Provas anteriores:** o Técnico vê só a parte de Conhecimentos Gerais das provas do Banestes (as do pacote são de TI);
   o cartão diz "Só Gerais". O id da prova não muda.
10. **A prova escolhida vai no retrato que sincroniza** (campo `targetExam` do backup, opcional): volta ao trocar de
    celular e no importar/exportar. Sem mudança no SQL do Supabase nem migração do Room. O ranking e os limites do
    servidor não mudam.
11. Conquistas de unidade concluída contam as unidades das duas trilhas, sem repetir (`TrailEngine.completedUnitCount`).

## Provas de Técnico Bancário: FGV · Banestes 2018 e 2023 (0.14.0, 17/11/2026) — a pedido do dono
O dono enviou as provas Tipo 1 e os gabaritos definitivos de Técnico Bancário do Banestes (FGV, 06/05/2018 e
05/03/2023, edital 01/2022).
1. **Transcrição** em `content/provas/fgv-banestes-2018-tecnico.json` (80 questões; entram 79, sem a Q70 anulada) e
   `fgv-banestes-2023-tecnico.json` (70; entram 64). Texto conferido contra o PDF, resposta conferida contra o gabarito
   (lido do PDF, não digitado) e resolvendo as questões de cálculo. Os termos sublinhados viraram colchetes, com o aviso
   de sempre no enunciado.
2. **Ficam de fora em 2023:** as anuladas (Q4, Q16, Q43, Q46) e duas que dependem de imagem impossível de descrever sem
   entregar a resposta (Q18, peças montadas; Q31, formatação de um trecho do Word). Q15 (hexágono), Q17 (quadro de peças)
   e Q25 (gráfico) entram com a figura descrita no enunciado (`depende_de_figura`); a Q49 de 2018 também.
3. **Guardadas sem ir ao app:** garantias civis (2018 Q66–69, aval, hipoteca, alienação fiduciária e fiança;
   `fora_do_edital`, o edital de 2026 não cita), Resolução 4.539/2016 (2018 Q73, substituída pela 4.949/2021) e
   criptomoedas pelo comunicado de 2017 (2018 Q63) com `versao_antiga`, e o operador “+” do Google (2023 Q35, que o
   Google abandonou). A 2018 Q13 é a mesma questão da prova de Analista de 2018: ganhou a marca nova **`repetida`**
   (validador e `build_provas.py`), para não aparecer duas vezes.
4. **Gabaritos discutíveis mantidos:** 2023 Q67 (metas como “planos de ação comercial”) e Q68 (só II). A explicação diz
   qual é o gabarito oficial e qual seria a leitura mais comum.
5. **Provas por cargo no Praticar:** cada prova anterior é do cargo dela (`TargetExam.forCargo`). Quem estuda para uma
   prova vê as do próprio cargo inteiras e as do outro só com os Gerais da sua prova; Gerais e Específicos seguem o cargo
   da prova (a Matemática financeira é Específicos no Técnico). O Analista ganha os Gerais das provas de Técnico, inclusive
   as 17 de Matemática financeira, as primeiras oficiais da G5.
6. **Ids:** duas provas do mesmo órgão e ano levam o cargo no id da segunda (`fgv-banestes-2018-tecnico-bancario`), em vez
   de um número.
7. Pacote do app: de 326 para 461 questões (135 de Técnico; 30 delas de Português, que já entram nas lições da G1).
8. **Unidade 1 do Técnico completa (pedido do dono: "pelo menos a 1 unidade por completo")**: as 7 lições de
   Conhecimentos Bancários (`u16`) entraram nesta mesma versão, no formato por conceitos (36 conceitos, 170 exercícios),
   com as 30 oficiais de bancários ligadas aos conceitos. O pacote é gerado por `tools/gen_u16.py` a partir de
   `content/source/u16_*.py`: escrever a alternativa certa primeiro e deixar o gerador espalhar a posição evita viés de
   posição. Revisão em `content/review/u16-conhecimentos-bancarios.md`.
9. **Correção: a Unidade 1 do Técnico abria travada.** O `TrailEngine` ordenava as unidades pelo número (`order`), e não
   pela ordem da trilha da prova. No Técnico, a Matemática financeira (`u15`, sem conteúdo) é a Unidade 3, mas pelo número
   vinha antes de `u16` e travava tudo depois dela. Agora a ordem é a da trilha (`ExamCourse`), com teste de domínio e de
   integração. O guia da unidade também passou a usar o rótulo da trilha ("Unidade 1", "G1") em vez do número interno.


## G2 Raciocínio lógico, lição 1: Lógica de relações (18/11/2026) — a pedido do dono

1. **Fonte de roteiro:** o dono pediu para usar a playlist "Bônus: Editora Opção" (Apostilas Opção, YouTube) nos Gerais.
   As aulas de lógica (vídeos 42 a 53) deram a ordem dos conceitos e os métodos de resolução; nenhum texto, exemplo ou
   número foi copiado (CLAUDE.md, regra 1). Os vídeos de Português (54 a 76) ficam para revisar a G1; os de probabilidade
   (39 a 41) entram na u12l4, porque as oficiais FGV 2018 Q50 e FGV 2021 Q35 já esperam lá, embora o edital não cite
   probabilidade. Informática e Direito Constitucional não são dos Gerais.
2. **Lógica proposicional na l1:** o edital fala em "processo lógico que, a partir de hipóteses, conduz de forma válida a
   conclusões", e 11 das 26 oficiais da lição são de negação, equivalência ou dedução com condicionais. A l1 ganhou 7
   conceitos (conectivos, negação, equivalências, quantificadores, dedução, conjuntos, associações), 44 exercícios e as 26
   oficiais ligadas a eles.
3. **Gerador:** `tools/gen_u16.py` passou a aceitar a unidade (`python3 tools/gen_u16.py u12`; sem argumento continua
   gerando a u16, idêntica). `pos` fixa a posição da correta quando as alternativas têm ordem natural, e a saída é
   gravada com fim de linha LF também no Windows.

## Sino de avisos e meta do dia em lições (0.14.1, 18/11/2026) — a pedido do dono
O dono pediu um botão de notificações minimalista que reúna a versão nova, o alerta de ofensiva e a meta do dia, e
apontou que 100 XP por dia não servia mais de meta.
1. **A faixa da meta saiu do topo da Trilha.** No lugar entrou um sino só de contorno depois dos corações
   (`feature/path/Notices.kt`). O ponto vermelho acende só para os avisos (versão nova ainda não dispensada e ofensiva em
   risco), não para a meta, que está sempre no painel.
2. **O painel desce do topo como o de provas** e mostra: meta do dia, "Ofensiva em risco" (a do aluno e as duplas, texto
   em `domain/streak/StreakAlert.kt`) e o cartão de versão nova (o mesmo `UpdateBanner` de antes). Os avisos de versão e
   de dupla que ficavam soltos na Trilha foram para dentro dele. O convite do lembrete diário continua na Trilha.
3. **Meta em lições pelo ritmo da prova** (escolha do dono entre três opções): as lições que faltavam no começo do dia na
   trilha da prova escolhida, Específicos e Gerais, inclusive as ainda em preparação, divididas pelos dias de estudo até a
   véspera da prova e arredondadas para cima, mínimo 1 (`domain/progress/DailyGoal.kt`). Em 18/11/2026 dá 1 lição por dia
   nas duas provas; a meta sobe sozinha se o aluno atrasar.
4. **A meta não cai durante o dia:** lição concluída hoje pela primeira vez volta para a conta de "faltavam". Se o aluno
   repetir no mesmo dia uma lição que concluiu pela primeira vez hoje, ela sai dessa conta (o registro guarda só a última
   conclusão); a meta pode cair uma lição nesse caso raro. Aceito para não criar tabela nem migração.
5. **Conta para a meta** o mesmo que conta para a ofensiva: lição concluída (inclusive repetida) e prova anterior inteira.
   As gemas da meta e o "Meta batida" do resultado continuam, uma vez por dia.
6. **A escolha de meta saiu do Perfil.** A coluna `dailyGoalXp` continua no banco, no retrato sincronizado e no
   exportar, sem uso, para não mexer no Room nem no formato do backup. Nada muda no SQL do Supabase.

## Hotfix: ofensiva quebrada na linha do ranking (0.14.2, 18/11/2026) — relatado pelo dono
Com a fonte do sistema aumentada, o botão "+ Adicionar" ou "Aceitar" ocupa a direita da linha do ranking e o texto da
ofensiva ("1 dia") era espremido até quebrar uma letra por linha. O XP e a ofensiva agora ficam num `FlowRow`: lado a
lado quando cabem e, sem espaço, a ofensiva (chama e dias, sempre juntos e numa linha só) desce para baixo do XP. Render
de regressão em tela de 360 dp com fonte a 130% (`r_global_fonte_grande.png`).

## G2 Raciocínio lógico, lições 2 a 4 (24/11/2026) — a pedido do dono

1. **Fonte de conferência:** a apostila do cargo enviada pelo dono (Apostilas Opção, OP-121NV-26) serviu para conferir
   definições e métodos de Aritmética, Proporção e porcentagem e Estatística. É material licenciado: fica fora do
   repositório, e nada dela foi copiado (CLAUDE.md, regra 1).
2. **Variância:** a apostila usa o denominador n − 1 (amostral); as bancas costumam pedir a populacional (÷ n). O dono
   decidiu seguir o padrão das bancas: a populacional é a regra do conceito, e a amostral entra só quando a questão a
   pedir (e como distrator).
3. **Probabilidade na u12l4:** o edital de Raciocínio não cita probabilidade, mas três oficiais (FGV 2018 Q50, FGV 2021
   Q35, Access 2024 Q20) já esperavam pela lição. O conceito "Casa dos pombos e chance" cobre só o necessário para elas
   (casos favoráveis ÷ possíveis, retiradas sem reposição); o resto fica com a `u18` do Técnico.
4. **Contas e ordem das alternativas:** cada arquivo de origem tem um `assert` por conta e por distrator justificado, e
   a geração para se um resultado não bater. Nas questões de prova numéricas, as alternativas ficam em ordem crescente,
   como nas provas; os distratores foram escolhidos para a resposta não ficar sempre no meio.
5. **Estilo das questões de prova:** nas lições de cálculo, a proporção de casos práticos fica acima dos ~40% do guia
   (27 de 34). O dono aceitou: o conteúdo segue o edital, e mais prática não atrapalha.
6. **Probabilidade na u12l4:** o dono confirmou o item 3.
