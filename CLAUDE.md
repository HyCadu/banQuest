# CLAUDE.md — BanQuest (app Android de estudo, concurso Banestes)

Leia `PLAN.md` antes de qualquer trabalho. Ele é a especificação; este arquivo são as regras de trabalho.

## Projeto
**BanQuest** (antes "Trilha TI"; o pacote continua `br.com.trilhati`): app Android de estudo, offline-first e com amigos (versão social: `docs/PLANO_SOCIAL.md`), estilo trilha gamificada, para estudar os **Conhecimentos Específicos** do cargo Analista de TI – Desenvolvimento de Sistemas do Banestes (banca FCC). Prova em 21/02/2027. Idioma do app e do código de UI: **português do Brasil**. Nomes de classes e funções em inglês.

## Stack
Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, WorkManager, kotlinx.serialization. `minSdk 26`.
Backend social: Supabase (Auth por código de e-mail + Postgres com RLS e funções), acessado pela API REST com um cliente
HTTP próprio (`data/remote/`), sem SDK. O SQL fica em `supabase/migrations/` e é testado em `tools/sql-tests/`. Módulo único `app`. Confirme as versões atuais das dependências antes de fixá-las no `libs.versions.toml`.

## Como trabalhar
- Siga as **fases do PLAN.md na ordem**. Ao fim de cada fase: compile, rode `./gradlew test lint`, faça commit com mensagem clara e **pare para o dono revisar** antes da fase seguinte.
- Antes de codar uma fase, escreva em poucas linhas o que vai fazer e quais arquivos vai tocar.
- Lógica de domínio (`domain/`) fica em Kotlin puro, sem Android, e **toda regra tem teste unitário**. Data e hora sempre via `Clock` injetável, nunca `System.currentTimeMillis()` direto.
- Prefira funções pequenas, estado imutável e `StateFlow`. Sem `GlobalScope`. Sem `!!` fora de testes.
- Se o PLAN.md for ambíguo ou contraditório, escolha a opção marcada com ★ e anote a decisão em `docs/DECISIONS.md`. Pergunte ao dono só quando a decisão for cara de desfazer.
- Não adicione dependências, permissões, rede ou analytics fora do que o PLAN.md prevê, sem pedir.

## Regras que não podem ser quebradas
1. **Nada copiado de apps existentes:** nome, logo, mascote, sons, ícones, textos e paleta exata. Replique só as mecânicas. Os personagens (Guaiamum, Tartaruga-marinha, Tamanduá, Preguiça) são originais e desenhados em código.
2. **Privado e offline-first:** conta obrigatória com login por código de e-mail (Supabase Auth). O estudo funciona sem
   internet e sincroniza depois. Sem anúncios, sem analytics, sem Firebase. Os outros veem só apelido, personagem, XP da
   semana e ofensiva — nunca o e-mail. A chave `service_role`/secret do Supabase **nunca** entra no app, no repositório
   ou no chat (só a publishable, em `gradle.properties`). Toda tabela tem RLS; escrita só por funções `security definer`.
   Permissões: `POST_NOTIFICATIONS`, `INTERNET`, `ACCESS_NETWORK_STATE`, e as que o WorkManager traz
   (`RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`).
3. **Conteúdo de estudo é sensível a erros.** Toda questão gerada por IA entra com `source: "ia"` e `reviewed: false`, passa pelo validador e pela revisão técnica separada. O app mostra um selo "não revisada". Nunca invente norma, versão ou número de artigo; se não tiver certeza, reformule a questão para algo conceitual ou descarte.
4. **Não afirme que uma questão é "da FCC"** a menos que o dono a tenha fornecido (`source: "fcc_real"`).
5. **Escopo da v1:** só Conhecimentos Específicos de TI. Português, Raciocínio, Atualidades e Ética ficam para depois. O simulado de 80 questões é a fase 10.
6. **O cronômetro é o diferencial:** 3:00 por questão (configurável), nunca reprova a resposta, e o resumo final sempre mostra tempo médio por questão.
7. **Datas:** fuso `America/Sao_Paulo`, virada do dia à meia-noite local. A ofensiva conta ao concluir pelo menos uma lição.

## Convenções de conteúdo
- Formato e schema: seção 5.1 do PLAN.md. Pacotes em `app/src/main/assets/content/`, um arquivo por unidade.
- Questões estilo FCC: cinco alternativas de tamanho parecido, uma correta, sem "todas as anteriores", mistura de enunciado literal, exceção ("NÃO é") e ~40% de casos práticos.
- Gere **uma unidade por vez**, valide com `tools/validate_content.py`, faça a segunda revisão e grave o relatório em `content/review/`. Distribua a posição da resposta correta de forma uniforme entre as 5 alternativas.
- Questões reais coladas pelo dono em `content/inbox/` servem para calibrar estilo. Transcreva-as com `source: "fcc_real"` só se o dono pedir.
- Mantenha cada lição dentro dos tópicos que o Anexo IV do edital cita para o cargo 04.

## Comandos
```
./gradlew assembleDebug          # APK de debug
./gradlew test                   # testes unitários
./gradlew lint                   # lint
./gradlew check                  # testes + lint + validação de conteúdo
python3 tools/validate_content.py app/src/main/assets/content/
tools/build_apk.sh               # APK assinado para distribuir (precisa da chave; ver docs/ASSINATURA.md)
tools/sql-tests/run.sh           # SQL do Supabase num Postgres local (precisa do postgresql-16)
./gradlew -p tools/preview-harness :shot:testDebugUnitTest   # renders, integração (inclui social) e acessibilidade
```

## Distribuição
- O APK distribuído sai **só** por `tools/build_apk.sh`, assinado com a chave fixa do BanQuest. A chave fica com o
  dono, nunca no repositório (`docs/ASSINATURA.md`). Sem a chave, não entregue APK.
- Cada versão nova sobe o `versionCode`. O aviso de versão nova é publicado no Supabase (`docs/PUBLICAR_VERSAO.md`).
- Não mude o `applicationId` (`br.com.trilhati`): seria outro app, e ninguém conseguiria atualizar por cima.

## Definição de pronto (cada fase)
Compila sem warnings novos · testes e lint passam · critério de aceite da fase no PLAN.md cumprido · `docs/QA.md` atualizado com o passo a passo para o dono testar no celular · commit feito.

---

## Prompt inicial (cole no Claude Code ao abrir a pasta do projeto)

```
Leia CLAUDE.md e PLAN.md por inteiro. Resuma em 10 linhas o que entendeu do projeto
e liste quaisquer ambiguidades que realmente bloqueiam a Fase 0. Se não houver, siga.
Execute apenas a Fase 0 do PLAN.md (projeto Gradle, Compose, Hilt, Room, Clock injetável,
catálogo de versões, teste e lint passando), faça o commit e pare para minha revisão.
```
