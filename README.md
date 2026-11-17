# BanQuest

App Android de estudo gamificado para o concurso do **Banestes** (FCC), com duas provas à escolha no topo da Trilha:
**Analista de TI – Desenvolvimento de Sistemas** (prova em 21/02/2027) e **Técnico Bancário** (prova em 24/01/2027).
Lições curtas em trilha, XP, ofensiva, ranking com amigos e um diferencial: o **ritmo de prova**, com cronômetro de 3:00
por questão no Analista e 3:25 no Técnico.

- **Baixar e conhecer:** https://hycadu.github.io/banquest/
- **Estado atual:**
  - Unidade 1 (Banco de Dados), Unidade 2 (Programação e OO) e G1 (Língua Portuguesa) completas, no formato por
    conceitos;
  - questões de provas anteriores reais (FGV, FCC, IDECAN e Instituto Access) dentro das lições e inteiras na aba
    Praticar;
  - as unidades 3 a 10, os Gerais G2 a G5 e os Específicos do Técnico Bancário aguardam conteúdo
    ([`docs/CONTENT_PLAN.md`](docs/CONTENT_PLAN.md)).

## Como colaborar

- **Conteúdo (o que mais falta):** use qualquer IA com o guia
  [`docs/GUIA_TRILHA_COM_IA.md`](docs/GUIA_TRILHA_COM_IA.md). Ele ensina a IA a montar lições com a mesma lógica de
  aprendizado da Unidade 1 e a transcrever provas anteriores no formato do app. O passo a passo está em
  [`CONTRIBUTING.md`](CONTRIBUTING.md).
- **Código:** leia o [`CLAUDE.md`](CLAUDE.md) (regras e comandos) e o [`PLAN.md`](PLAN.md) (especificação). Todo
  trabalho entra por pull request, e quem aprova é o dono do projeto.

O que entra no app e quando é decisão do dono (veja o [`CONTRIBUTING.md`](CONTRIBUTING.md)).

## Rodar o projeto

```bash
./gradlew assembleDebug                                    # APK de debug
./gradlew check                                            # testes, lint e validação de conteúdo
python3 tools/validate_content.py app/src/main/assets/content/
```

Stack: Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore e WorkManager. Backend social no Supabase.
