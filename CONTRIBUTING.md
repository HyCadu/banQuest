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

- `./gradlew test lint` passando e, se mexeu em conteúdo, o validador sem erros.
- `docs/QA.md` atualizado com o passo a passo para testar no celular, se mudou comportamento do app.
- `docs/DECISIONS.md` atualizado, se houve decisão (com o link da issue).
- Para conteúdo: o relatório da segunda revisão em `content/review/`.

## Agentes de IA (Claude Code e outros)

Seguem o `CLAUDE.md` e este arquivo: trabalham numa branch, fazem commit nela, abrem o PR e **param**. Não fazem merge,
não aprovam o próprio PR e não mudam as configurações do repositório.

## Configuração única do dono (proteção da `main`)

Só o dono pode fazer isto, em GitHub > Settings > Branches > Add branch ruleset (ou "branch protection rule") para `main`:

- Exigir pull request antes do merge, com **1 aprovação** e **revisão dos Code Owners**.
- Bloquear force push e exclusão da branch.
- Exigir que a branch esteja atualizada antes do merge.
- Deixar o dono fora da regra (bypass), senão ele não consegue fazer merge dos próprios PRs, porque o GitHub não deixa
  ninguém aprovar o próprio PR.
