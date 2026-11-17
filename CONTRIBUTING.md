# Como colaborar

O projeto é colaborativo, mas **quem decide é o dono** (@HyCadu). Este fluxo garante duas coisas: a `main` sempre
compila e passa nos testes, e nada entra nela sem a aprovação do dono.

## Regras

1. **Ninguém faz push direto na `main`.** Nem pessoas, nem agentes de IA. Tudo entra por pull request.
2. **Só o dono aprova e faz o merge.** O arquivo `.github/CODEOWNERS` pede a revisão dele em todo PR.
3. **Decisão vira issue.** Se uma escolha é cara de desfazer (escopo, dependência, permissão, mudança de dados, nome do
   app), abra uma issue com o título começando por `[Decisão]`, liste as opções e marque a recomendada com ★. O dono
   responde na issue. Depois, a decisão é registrada em `docs/DECISIONS.md` com o link da issue.
4. **Um assunto por PR.** Uma unidade de conteúdo, uma fase ou uma correção. PR pequeno é revisado mais rápido.

## Passo a passo

```bash
git switch main && git pull
git switch -c <tipo>/<assunto>     # ex.: content/u2-programacao, feat/trilha-gerais, fix/cronometro
# ... trabalho ...
./gradlew test lint
python3 tools/validate_content.py app/src/main/assets/content/
git push -u origin <tipo>/<assunto>
gh pr create --base main           # o modelo de PR abre com o checklist
```

Tipos de branch: `feat/` (função nova), `fix/` (correção), `content/` (questões), `docs/` (documentação),
`chore/` (build, dependências).

Quem não tem permissão de escrita no repositório trabalha num fork e abre o PR a partir dele.

## O que o PR precisa ter

- A CI verde (check **CI ok** no PR). Ela roda sozinha em todo PR: `./gradlew check assembleDebug` (testes, lint e
  validadores de conteúdo), `tools/sql-tests/run.sh` e o preview-harness. Jobs que não têm a ver com o que o PR muda
  (ex.: SQL num PR de conteúdo) são pulados. As telas renderizadas pelo harness ficam no artefato `telas` da execução.
  Rodar `./gradlew test lint` antes do push continua valendo: é mais rápido que esperar a CI.
- `docs/QA.md` atualizado com o passo a passo para testar no celular, se mudou comportamento do app.
- `docs/DECISIONS.md` atualizado, se houve decisão (com o link da issue).
- Para conteúdo: o relatório da segunda revisão em `content/review/`.

## Conteúdo com IA (qualquer IA)

O jeito mais rápido de ajudar é transformar uma prova anterior ou uma lição do edital em conteúdo do app. Dá para usar
qualquer IA (ChatGPT, Claude, Gemini…), desde que ela siga a mesma lógica de aprendizado da Unidade 1:

1. Anexe à conversa o [`docs/GUIA_TRILHA_COM_IA.md`](docs/GUIA_TRILHA_COM_IA.md) e o PDF da prova com o gabarito (ou
   diga qual lição quer gerar).
2. Cole o prompt da Parte 9 do guia e faça uma etapa por vez: transcrever, classificar, gerar a lição.
3. Salve os arquivos nos caminhos que o guia indica e rode os validadores:
   `python3 tools/validate_content.py app/src/main/assets/content/` e `python3 tools/validate_provas.py`.
4. Leia e resolva as questões antes do PR: o validador pega erros de formato, não de conteúdo.
5. Abra um PR por lição ou por prova, com o relatório de revisão em `content/review/`.

Antes de começar, veja nas issues e PRs abertos se alguém já está na mesma lição ou prova.

## Agentes de IA (Claude Code e outros)

Seguem o `CLAUDE.md`, o `AGENTS.md` e este arquivo: trabalham numa branch, fazem commit nela, abrem o PR e **param**.
Não fazem merge, não aprovam o próprio PR e não mudam as configurações do repositório. Para conteúdo, leem o
`docs/GUIA_TRILHA_COM_IA.md` (no Claude Code, a skill `trilha-de-conteudo` faz isso).

## Configuração única do dono (proteção da `main`)

Só o dono pode fazer isto, em GitHub > Settings > Branches > Add branch ruleset (ou "branch protection rule") para `main`:

- Exigir pull request antes do merge, com **1 aprovação** e **revisão dos Code Owners**.
- Bloquear force push e exclusão da branch.
- Exigir que a branch esteja atualizada antes do merge.
- Exigir que os status checks passem ("Require status checks to pass") e adicionar só o check **CI ok** (do workflow
  `.github/workflows/ci.yml`). Ele só aparece na lista depois que o workflow rodou uma vez. Não adicione os outros
  jobs: eles são pulados quando o PR não mexe no que conferem, e um check obrigatório pulado trava o merge.
- Deixar o dono fora da regra (bypass), senão ele não consegue fazer merge dos próprios PRs, porque o GitHub não deixa
  ninguém aprovar o próprio PR.

## DCM — colaboração aberta

Leia [docs/DCM.md](docs/DCM.md). Comente `/participar` numa tarefa Available ou In progress; não há exclusividade nem prazo. Novas ideias aguardam aprovação do fundador. A revisão comunitária é opcional; a conferência do conteúdo e a CI continuam necessárias. O Contributor define o roteiro da lição. Sem Git? Anexe os arquivos e relatório na Issue para outro Contributor integrar. Apenas o fundador aprova e faz merge.
