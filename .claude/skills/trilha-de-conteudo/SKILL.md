---
name: trilha-de-conteudo
description: Monta conteúdo do BanQuest seguindo docs/GUIA_TRILHA_COM_IA.md. Cobre transcrever uma prova anterior ou simulado (PDF e gabarito) para content/provas, gerar uma lição ou unidade no formato por conceitos (aprender, praticar e prova, passos 1-2-3) e revisar um pacote. Use quando receber uma prova, um gabarito ou um pedido de questões, lições ou unidades.
---

# Trilha de conteúdo do BanQuest

A fonte da verdade é `docs/GUIA_TRILHA_COM_IA.md`. Leia por inteiro antes de começar e siga as Partes 3 a 8. Esta skill
só organiza o trabalho dentro do repositório.

## Antes de escrever

1. Leia `docs/GUIA_TRILHA_COM_IA.md`, as regras de conteúdo do `CLAUDE.md` e o trecho da unidade em
   `docs/CONTENT_PLAN.md` (tópicos do edital, densidade e risco de cada lição).
2. Abra o exemplo vivo, `app/src/main/assets/content/u1-banco-de-dados.json`: conceitos, passos e níveis seguem esse
   padrão.
3. Conte as questões de prova que já esperam pela lição (`content/provas/*.json`, campo `lesson`). Elas calibram o
   estilo e viram as questões de prova da lição, ligadas a um conceito (`concept`).

## Fluxo

1. **Prova nova (PDF):** transcreva em `content/provas/<banca>-<orgao>-<ano>.json` (Parte 5.2 do guia). Rode
   `python3 tools/validate_provas.py` e `python3 tools/build_provas.py`. Nunca edite `app/src/main/assets/provas/` à mão.
2. **Lição:**
   - defina e registre o plano antes do JSON: conceitos em ordem, cenário e quantidade por nível; o Contributor pode decidir sem aprovação prévia do fundador;
   - gere no pacote da unidade (`app/src/main/assets/content/uN-<nome>.json`, Parte 5.1);
   - uma unidade por vez, lição por lição.
3. **Valide:** `python3 tools/validate_content.py app/src/main/assets/content/`, até zerar os erros. Leia os avisos.
4. **Segunda revisão:**
   - resolva cada questão de prova;
   - confira as normas contra a Parte 7;
   - grave o relatório em `content/review/<pack>.md` (Parte 8.2).
5. **Testes:** rode `./gradlew test lint`. Se a contagem de questões mudou, ajuste `ContentTest` e a harness
   (`tools/preview-harness`).
6. **PR:** branch `content/<unidade>`, um PR por lição ou prova, e pare. Só o dono faz o merge (`CONTRIBUTING.md`).

## Lembretes que mais escapam

- `"source": "ia"` e `"reviewed": false` em tudo que a IA escreve. Nada de norma, versão ou artigo fora da Parte 7.
- Exercício de ensino nunca cobra o que não mostrou. Um cenário por conceito, passos 1 (exemplo resolvido), 2 (aplicar)
  e 3 (variar).
- Questão de prova: 5 alternativas parecidas, resposta espalhada entre A e E, ~40% de casos práticos.
- "FCC" só aparece em questão real de prova da FCC.
