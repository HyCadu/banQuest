# CLAUDE.md — Trilha TI (app Android de estudo, concurso Banestes)

Leia `PLAN.md` antes de qualquer trabalho. Ele é a especificação; este arquivo são as regras de trabalho.

## Projeto
App Android pessoal e offline, estilo trilha gamificada, para estudar os **Conhecimentos Específicos** do cargo Analista de TI – Desenvolvimento de Sistemas do Banestes (banca FCC). Prova em 21/02/2027. Idioma do app e do código de UI: **português do Brasil**. Nomes de classes e funções em inglês.

## Stack
Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, WorkManager, kotlinx.serialization. `minSdk 26`. Módulo único `app`. Confirme as versões atuais das dependências antes de fixá-las no `libs.versions.toml`.

## Como trabalhar
- Siga as **fases do PLAN.md na ordem**. Ao fim de cada fase: compile, rode `./gradlew test lint`, faça commit com mensagem clara e **pare para o dono revisar** antes da fase seguinte.
- Antes de codar uma fase, escreva em poucas linhas o que vai fazer e quais arquivos vai tocar.
- Lógica de domínio (`domain/`) fica em Kotlin puro, sem Android, e **toda regra tem teste unitário**. Data e hora sempre via `Clock` injetável, nunca `System.currentTimeMillis()` direto.
- Prefira funções pequenas, estado imutável e `StateFlow`. Sem `GlobalScope`. Sem `!!` fora de testes.
- Se o PLAN.md for ambíguo ou contraditório, escolha a opção marcada com ★ e anote a decisão em `docs/DECISIONS.md`. Pergunte ao dono só quando a decisão for cara de desfazer.
- Não adicione dependências, permissões, rede ou analytics fora do que o PLAN.md prevê, sem pedir.

## Regras que não podem ser quebradas
1. **Nada copiado de apps existentes:** nome, logo, mascote, sons, ícones, textos e paleta exata. Replique só as mecânicas. Os personagens (Guaiamum, Tartaruga-marinha, Tamanduá, Preguiça) são originais e desenhados em código.
2. **Offline e privado:** sem login, sem nuvem, sem anúncios. Permissões mínimas: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`.
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
```

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
