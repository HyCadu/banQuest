# PLAN.md — App Android de estudo gamificado (concurso Banestes, Analista de TI)

Documento de referência para o Claude Code. Leia junto com `CLAUDE.md`. Onde houver ★, é a decisão padrão adotada; o dono do projeto pode mudá-la depois.

> **Versão social (0.4):** login, nuvem, amigos e ranking real entraram no escopo a pedido do dono. O desenho completo
> está em `docs/PLANO_SOCIAL.md`; onde este documento falar em "offline", "sem login" ou "ligas com bots", vale o plano social.

## 1. Contexto e objetivo

Um app Android de estudo, offline-first e com amigos, no estilo de trilha gamificada de aprendizado de idiomas, para estudar o concurso do Banestes (banca FCC), cargo **Analista de TI – Desenvolvimento de Sistemas**.

Fatos do concurso que guiam o produto (do edital nº 01/2027):

| Item | Valor |
|---|---|
| Cargo / código de opção | Analista de TI – Desenvolvimento de Sistemas (04) |
| Vagas (cadastro de reserva) | 440 (242 ampla concorrência) |
| Prova | 21/02/2027, manhã, Vitória/São Mateus/Cachoeiro de Itapemirim |
| Estrutura | 80 questões em 4 h: 30 Gerais (peso 1) e **50 Específicos (peso 2)**, 5 alternativas, uma correta |
| Ritmo | 4 h ÷ 80 = **3:00 por questão em média** (sair antes de 3 h de prova elimina o candidato, item 10.19-f) |
| Regras de prova | sem relógio, calculadora, lápis ou borracha; caneta transparente preta/azul (itens 10.12, 10.18.2, 10.19) |
| Fonte | Edital nº 01/2027 revisado em 10/11/2026: cap. 2 (vagas), cap. 9 (provas), Anexo IV (conteúdo), Anexo V (cronograma). Conferido contra o PDF |
| Aprovação | média ponderada ≥ 6,0; classificação por posição (top 242 na ampla) |
| Estilo FCC (síntese de fontes) | enunciados diretos, alternativa errada com troca de uma palavra/termo, atenção a exceções e quantificadores; tendência a casos práticos |

**Objetivo da v1:** dar uma rotina diária viciante e eficiente para treinar os **Conhecimentos Específicos de TI** (Gerais ficam para a v2), com cronômetro no ritmo de prova.

**Data de referência:** hoje é 15/11/2026, faltam 98 dias para a prova. A v1 deve ser usável em poucos dias, com conteúdo chegando em pacotes.

## 2. Escopo

### Dentro da v1
- Onboarding com escolha de personagem, nome e meta diária.
- Trilha em caminho (unidades → lições) só com **Conhecimentos Específicos de TI**.
- Lição de 8 exercícios (12 no formato por conceitos), corações, XP, combo, tela de resultado.
- Ofensiva com "congelar", meta diária, missões diárias, conquistas.
- Modo "Praticar erros" (revisão espaçada simples).
- Gemas, loja (congelar ofensiva, recarregar corações) e baús.
- Ranking semanal real (geral e só de amigos), amizade por pedido ou código, ofensiva em dupla, progresso salvo na nuvem
  (Supabase; ver `docs/PLANO_SOCIAL.md`). Substitui as ligas contra rivais fictícios da 0.3.
- Cronômetro de ritmo de prova (3:00 por questão), configurável.
- Notificações locais (lembrete diário e "ofensiva em risco").
- Sons, vibração, animações, modo escuro, acessibilidade básica.
- Pacotes de conteúdo em JSON (embutidos e importáveis).
- Backup/exportação do progresso em JSON.

### Fora da v1 (não implementar agora)
- ~~Conhecimentos Gerais~~: **entraram na v1** em 15/11/2026 (G1–G4, ver `docs/CONTENT_PLAN.md`).
- Simulado de 80 questões / 4 h (planejado para v1.1; ver Fase 10).
- Anúncios, compras, analytics, Firebase, "cutucar" amigos, chat.
- Outros cargos (Suporte, Segurança) além de reaproveitar o conteúdo comum.

## 3. Identidade visual e personagens (originais)

**Não copiar** nome, logo, mascote, sons, cores exatas, ilustrações ou textos de nenhum app existente. Replicar apenas as **mecânicas** (trilha, corações, ofensiva, XP, ligas, missões). Nome do app: **"BanQuest"** (antes "Trilha TI"; editável em `strings.xml`).

- Personagens ★ bichos capixabas originais, desenhados em código (Compose `Canvas`/`Path` ou `VectorDrawable`), estilo geométrico e fofo:
  1. **Guaiamum** (caranguejo azul-arroxeado)
  2. **Tartaruga-marinha** (verde)
  3. **Tamanduá** (cinza com faixa)
  4. **Preguiça** (marrom claro)
- Cada personagem tem poses/estados: `idle`, `happy`, `sad`, `celebrate`, `sleepy` (lembrete). Transições com animação Compose (`animate*AsState`, `Animatable`).
- Paleta própria: base azul-petróleo + acento âmbar + verde/vermelho de acerto/erro. Tokens em `ui/theme`. Suporte a modo escuro.
- **Temas (0.8.0):** Claro, Escuro, Elétrico e Chiclete. Os dois novos seguem as referências do dono em 60/30/10, com o
  fundo sempre na cor clara. O tema é escolhido na primeira entrada e trocado em Configurações › Tema (ver
  `docs/DECISIONS.md`).
- Tipografia: uma família arredondada (Nunito ou equivalente via Google Fonts downloadable) e uma monoespaçada para código.
- Sons: gerados por script (ondas simples), originais, em `res/raw`: acerto, erro, combo, lição concluída, conquista, clique.

## 4. Stack e arquitetura

- **Kotlin + Jetpack Compose + Material 3**, `minSdk 26`, `targetSdk` = a mais recente estável. Use o catálogo de versões (`libs.versions.toml`) e confirme versões atuais antes de fixar.
- **MVVM + fluxo unidirecional** (`ViewModel` + `StateFlow`), **Hilt** para DI, **Navigation Compose**.
- **Room** (progresso), **DataStore** (preferências), **kotlinx.serialization** (conteúdo JSON), **WorkManager** (notificações), **SoundPool** (sons), **Vibrator/HapticFeedback**.
- **Relógio injetável** (`Clock`) em toda lógica de datas para permitir testes.
- Fuso: `America/Sao_Paulo` do dispositivo; virada de dia à meia-noite local.
- Módulo único `app`, pacotes:

```
app/src/main/java/.../
  ui/theme, ui/components, ui/mascot
  feature/onboarding, path, lesson, results, practice, quests, leagues, shop, profile, settings
  domain/ (engines puros e testáveis)
     StreakEngine, XpEngine, HeartsEngine, QuestEngine, AchievementEngine, LeagueEngine, SrsEngine
  data/db (Room), data/content (loader/validator), data/prefs, data/backup
  notify/ (canais, agendamento, receivers)
```

## 5. Modelo de dados

### 5.1 Pacote de conteúdo (JSON em `assets/content/` e importável)

```json
{
  "schema": 1,
  "pack": "u1-banco-de-dados",
  "version": 1,
  "unit": { "id": "u1", "order": 1, "title": "Banco de dados", "editalTopics": ["SQL", "Normalização"] },
  "lessons": [
    { "id": "u1l1", "order": 1, "title": "Modelo ER e normalização",
      "questions": [
        { "id": "u1l1q01", "type": "mcq5", "topic": "BD", "subtopic": "normalizacao",
          "difficulty": 2, "style": "conceitual",
          "stem": "…", "options": ["A","B","C","D","E"], "answer": 2,
          "explanation": "…", "source": "autoral", "reviewed": false }
      ] } ]
}
```

- `type`: `mcq5` (padrão FCC), `truefalse`, `match` (pares termo↔definição), `order_code` (ordenar linhas), `fill_blank` (completar SQL/código com lacuna e opções), `code_output` (qual a saída?), `case` (caso prático com cenário + mcq5).
- `style`: `conceitual`, `literal` (troca de termo), `caso_pratico`, `excecao` (quantificadores/NÃO é).
- `source`: `autoral`, `ia`, `fcc_real`, `prova_real` (prova anterior enviada pelo dono, de qualquer banca, com `ref` e
  vinda de `assets/provas`; ver `docs/DECISIONS.md`). `reviewed`: se o dono conferiu.
- Cada lição tem **10 questões no banco**; a lição sorteia 8 (prioriza as que o usuário errou). Erros são repetidos uma vez no fim da lição.
- **Formato por conceitos** (piloto na `u1l1`; ver `docs/DECISIONS.md`): a lição lista `concepts` (`id` `uXlYcN`,
  `title`, `summary`) e cada questão traz `concept` e `level` (`aprender`, `praticar` ou `prova`; sem `level` vale
  `prova`). Tipo novo `quick` (escolha rápida, 3 alternativas curtas). Exercício feito a partir de uma questão oficial
  leva `adaptedFrom` (`id`, `banca`, `orgao`, `ano`, `numero`). Exercício de ensino leva `step` (1 = exemplo resolvido,
  2 = aplicar, 3 = variar; variantes repetem o passo). O banco da lição fica entre 24 e 80 exercícios; cada conceito tem
  os passos 1, 2 e 3, ao menos 2 de aprender, 2 de praticar e 1 de prova (do pacote ou oficial). A sessão tem 12: 3
  conceitos na ordem da lição (os não vistos primeiro), um bloco de 3 passos para cada, e 3 de prova no fim.

### 5.2 Entidades Room (mínimo)
`UserProfile` (nome, personagem, meta diária XP, criado em), `Stats` (XP total, gemas, corações, último regen, ofensiva atual, recorde, último dia concluído, congelamentos), `LessonProgress` (lessonId, estrelas/coroa, melhor nota, concluída em), `QuestionStat` (questionId, vezes, acertos, último erro, próxima revisão, intervalo), `Attempt` (questionId, correta, tempo, data), `DailyLog` (data, XP, lições, meta batida, congelou?), `QuestState`, `Achievement`, `LeagueWeek` (semana, tier, XP do usuário, bots seed), `ShopState`, `PackMeta` (pack, versão importada).

## 6. Conteúdo: trilha de TI (Específicos – cargo 04)

Ordem ★ (do cerne de desenvolvimento para a infraestrutura). O Anexo IV do cargo 04 é muito largo e inclui **redes, sistemas operacionais, servidores, monitoramento e segurança em profundidade**, não só desenvolvimento. Por isso são **10 unidades e 48 lições × 10 questões = ~480 questões**. Mínimo para lançar: unidades 1–4. Cada lição lista os tópicos que o edital cita, e o conteúdo gerado deve ficar dentro deles.

| Un. | Tema | Lições |
|---|---|---|
| 1 | Banco de dados (5) | modelo ER e normalização · SQL DML/DDL/DCL e transações · subconsultas, CTEs e funções analíticas (window functions) · PL/SQL, PL/pgSQL, PostgreSQL 14/15, Oracle 19c, H2 · modelagem dimensional, DW, data mart, OLAP, data lake, NoSQL (documentos, chave-valor, colunas, grafos), governança de dados |
| 2 | Programação e OO (5) | fundamentos (tipos, operadores, estruturas, compilação/execução, exceções, acesso a BD) · OO (classes, interfaces, encapsulamento, herança, polimorfismo, packages, sobrecarga/sobrescrita) · princípios SOLID · PHP, Python, C, Java, C# e arcabouço .NET · testes automatizados, TDD/BDD, Git (GitFlow, pull requests, code review), uso responsável de assistentes de código com IA |
| 3 | Engenharia de software e ágil (5) | processo (CMMI-DEV v2.0, ISO/IEC/IEEE 12207:2021, MR-MPS-SW 2023) · requisitos, UML 2.5, BPMN, modelagem de dados estruturados/semiestruturados (XML, JSON) · Scrum, Kanban, XP · tipos de teste, gestão de configuração, versionamento semântico, revisão de código · qualidade ISO/IEC 25010:2024, low-code/no-code, dicionário de dados, rastreabilidade |
| 4 | Web, APIs e arquitetura (5) | HTML5, CSS3, Bootstrap 5, JavaScript, TypeScript · React, React Native, Angular, Vue.js, Node.js, mobile-first · Web Services REST, SOAP/XML Schema, OpenAPI/Swagger, idempotência, paginação, versionamento · usabilidade, UX, WCAG 2.1/2.2, ABNT NBR 17225:2025 · arquitetura multicamadas, SOA, orientada a eventos/filas, MVC, DDD, hexagonal, cloud-native, microsserviços, API Gateway, Service Discovery, circuit breaker, retries e timeouts |
| 5 | DevOps e containers (4) | CI/CD e pipelines, IaC, automação de testes de regressão · Docker, imagens, Kubernetes, Rancher, ambientes em cluster · blue/green e canary · observabilidade (logs, métricas, traces), DevSecOps |
| 6 | Segurança da informação e LGPD (6) | conceitos (CID), riscos e ISO 27005:2023, ISO 27001:2024/27002:2022 · criptografia simétrica/assimétrica, hash, certificados e assinatura digital, SSL/TLS · controle de acesso, autenticação e auditoria, RBAC, MFA, IAM, SSO, OAuth 2.0, OIDC · proteção de redes: firewall, WAF, UTM, DMZ, proxy, NAC, IDS/IPS, VPN, ZTNA, ataques (malware, DoS/DDoS) · SIEM, EDR, SOC, resposta a incidentes (ISO 27035, NIST SP 800-61), Zero Trust, segurança em nuvem (ISO 27017), OWASP Top 10:2021 · LGPD (princípios, bases legais, direitos, agentes, ANPD, anonimização e pseudonimização) |
| 7 | Governança de TI (5) | PMBOK 8ª edição e projetos, portfólio · ITIL (Version 5), SLAs e OLAs, melhoria contínua · COBIT 2019, PETIC/PDTIC · gestão financeira de TI (TCO, ROI, CAPEX, OPEX, FinOps) e riscos (ISO 31000:2018) · Lei 14.133/2021 em contratações de TIC, arquitetura corporativa e TOGAF, redação técnica |
| 8 | Sistemas operacionais (4) | processos e threads, alocação de CPU, sincronização, deadlocks e starvation · memória (segmentação, virtual, paginação), E/S, armazenamento · Linux (Red Hat/Oracle Linux): instalação, administração, comandos · Windows 11/Server 2019/2022, Active Directory, PowerShell, virtualização, hardening |
| 9 | Redes de computadores (5) | tipos e meios de transmissão, comutação, PAN/LAN/MAN/WAN, elementos de interconexão · TCP/IP, IPv4/IPv6, subnetting, UDP/TCP, IPsec, ARP, SSH, SMTP, HTTP, FTP, LDAP, DNS, DHCP, POP/IMAP · roteamento (OSPF, BGP, MPLS), VLAN, VXLAN, SDN, NFV · cabeamento estruturado (ABNT NBR 14565:2019), fibras ópticas, padrões IEEE 802.x, Wi-Fi 802.11n/ac/ax · QoS, VoIP/ToIP, SIP, H.323, multicast e IGMP |
| 10 | Serviços, monitoramento e nuvem (4) | servidores de aplicação (WildFly, JBoss EAP, Apache HTTP Server, IIS), web proxy, e-mail, VPN corporativa, acesso remoto · gerência de redes: SNMP, MIBs, métricas de desempenho, Zabbix, ELK, Grafana, Prometheus, Fluentd · computação em nuvem (nuvem pública/privada/híbrida/multicloud, IaaS/PaaS/SaaS, migração, custos, alta disponibilidade) · inglês técnico |

**Regras de qualidade das questões (FCC):**
- Cinco alternativas parecidas e de mesmo tamanho; só uma correta. Sem "todas as anteriores".
- Misturar `literal` (uma palavra trocada), `excecao` ("NÃO é", "exceto") e **~40% casos práticos** (cenário curto de banco/sistema, "qual abordagem aplicar").
- Explicação curta dizendo por que cada distrator está errado quando útil.
- Sem lixo de versão: citar a versão só quando o edital cita (PostgreSQL 14/15, Oracle 19c, WCAG 2.1/2.2, OWASP Top 10:2021).
- Marcar `source: "ia"` e `reviewed: false`; o app mostra um selo discreto "não revisada".

**Pipeline de geração (rodar com o Claude Code, em lotes de 1 unidade):**
1. Gerar JSON da unidade seguindo o schema.
2. `tools/validate_content.py`: valida schema, 5 opções, `answer` entre 0–4, ids únicos, sem duplicatas (similaridade), tamanho de enunciado, explicação preenchida, balanceamento das posições corretas.
3. Segunda passada de revisão técnica (outro prompt: "ache erros factuais e alternativas ambíguas") e relatório em `content/review/`.
4. Questões reais da FCC que o dono colar em `content/inbox/` servem de **calibração de estilo**; se transcritas, marcar `fcc_real`.

## 7. Funcionalidades (comportamento exato)

**Onboarding:** boas-vindas → escolher personagem (pré-visualização animada) → nome → meta diária (Leve 50 / Regular 100 / Séria 200 / Intensa 300 XP) → horário do lembrete e permissão de notificação (Android 13+) → primeira lição guiada.

**Trilha (home):** caminho em zigue-zague com nós por lição, cabeçalho fixo da unidade, nó atual pulsando, baú ao final de cada unidade, personagem ao lado do nó atual. Unidade seguinte desbloqueia ao concluir a anterior; ★ permitir "pular" com um teste de nivelamento (v1.1). Topo: ofensiva, gemas, corações, contagem regressiva para 21/02/2027. **Duas trilhas (0.8.0):** seletor "Específicos | Gerais" logo abaixo do topo; cada trilha desbloqueia na sua ordem e tem as cores próprias, com troca animada.

**Lição:** 8 exercícios (12 no formato por conceitos: blocos por conceito do exemplo resolvido à variação, e prova no fim; "Novo conceito" aberto na primeira vez; botão "Não sei" nos de ensino). Barra de progresso; botão Verificar; feedback em painel inferior (verde/vermelho) com explicação; Continuar. Erro custa 1 coração (½ nos exercícios de aprender e praticar do formato por conceitos; se o modo corações estiver ligado); erros voltam no fim da lição (+5 XP em vez de +10). Combo visual a partir de 3 acertos seguidos.

**XP:** +10 por acerto de primeira, +5 de bônus se ≤ 2:00, +20 por concluir, +10 por lição perfeita. Nível = XP total ÷ 500.

**Cronômetro de ritmo (diferencial do app):** anel/barra de 3:00 por questão, cor âmbar aos 2:15 e vermelha ao estourar (não reprova a resposta). Durante a questão aparece só o anel e, quando o aluno está atrasado, a ficha de atraso = (questões feitas × alvo) − tempo usado; a folga aparece no resumo (0.7.3; antes havia também as fichas "Lição" e "Folga"). Resumo final mostra tempo médio por questão e compara com 3:00. Configurações: `Desligado`, `Discreto` (só ao final), `Ritmo` ★ (visível). Ritmo-alvo ajustável: **3:00** ★ (média simples da prova) ou **2:30**, para sobrar tempo de revisão no fim. Opção de "caso prático = 4:00". No formato por conceitos, o alvo vale para as questões de prova; praticar usa metade dele (1:30) e aprender 2/9 (0:40).

**Ofensiva:** um dia conta ao concluir ≥ 1 lição (não precisa bater a meta). Virada à meia-noite local. Perdeu 1 dia → consome **congelamento** automaticamente (máx. 2 estocados). Mostrar calendário de dias estudados e recorde.

**Corações:** 5. Zerou → tela "sem corações": praticar erros para ganhar 1, recarregar com gemas ou esperar (1 coração a cada 30 min). Opção em Configurações: **"Modo sem vidas"** (estudo sem punição), desligado por padrão.

**Missões diárias (3 por dia, sorteadas):** ex.: ganhar 100 XP, concluir 2 lições, acertar 10 questões seguidas, terminar uma lição dentro do ritmo (≤ 3:00 de média), praticar erros. Recompensa em gemas. Missão mensal simples (v1.1).

**Gemas e loja:** ganhar em lições, baús, missões, conquistas. Loja: congelar ofensiva (200), recarregar corações (350). Baú diário.

**Conquistas (≈15):** ofensivas 3/7/14/30/60/100; XP total 1k/5k/20k; primeira lição perfeita; 10 lições perfeitas; unidade concluída; "no ritmo" (média ≤ 3:00 em 10 lições); revisão de 50 erros; Pódio da semana (top 3 do ranking geral com pelo menos 3 participantes).

**Praticar erros:** sessão de 8 questões escolhidas por `SrsEngine` (intervalos 1, 3, 7, 14 dias; erro zera). Mostra quantas estão "vencidas".

**Ranking semanal (0.4):** soma o XP das sessões enviadas ao servidor de segunda 00:00 a domingo 23:59 (Brasília). Lista
geral (quem deixou "aparecer no ranking geral" ligado) e lista só de amigos. Amizade por pedido a partir do ranking (o
outro aceita) ou por código `TRI-XXXXX` (vale na hora). Ofensiva em dupla: dias seguidos em que os dois concluíram ao
menos uma lição. Detalhes, regras anti-trapaça e esquema em `docs/PLANO_SOCIAL.md`.

**Perfil/estatísticas:** XP, ofensiva, precisão por tema (as 10 unidades), tempo médio por questão, histórico de 30 dias, conquistas.

**Configurações:** personagem, meta, horário do lembrete, cronômetro, sons, vibração, tema (Claro, Escuro, Elétrico ou Chiclete), modo sem vidas, importar pacote de conteúdo, exportar/importar progresso, apagar dados.

## 8. Notificações (somente locais)

- Canais: `lembrete_diario` e `ofensiva_em_risco`.
- Android 13+: pedir `POST_NOTIFICATIONS` no onboarding (e explicar se negada).
- Agendamento via **WorkManager** (`OneTimeWork` recalculado todo dia, com margem de alguns minutos). Evitar alarmes exatos.
- 1ª notificação no horário escolhido (★ 19:00) se nenhuma lição foi feita; 2ª às 21:30 apenas se há ofensiva ativa e nada foi feito.
- Reagendar após reinício (`BOOT_COMPLETED`) e ao mudar o horário. Cancelar se a meta do dia já foi cumprida.
- Texto na voz do personagem escolhido, variando (ex.: "O Guaiamum está de olho na sua ofensiva de 12 dias").
- Informar em Configurações: otimização de bateria do fabricante pode atrasar avisos.

## 9. Fases, entregáveis e critérios de aceite

Cada fase termina com app compilando, testes verdes e commit. **Pare ao fim de cada fase e peça revisão.**

| Fase | Entrega | Aceite |
|---|---|---|
| 0 | Projeto Gradle, Compose, Hilt, Room, CI local (`./gradlew test lint`), `Clock`, `libs.versions.toml` | App abre em tela vazia; testes e lint passam |
| 1 | Design system, tema claro/escuro, 4 mascotes animados, galeria de componentes | Tela de galeria mostra personagens em todas as poses |
| 2 | Motor de conteúdo: loader/validator, `tools/validate_content.py`, pacote da Unidade 1 (50 questões) | Validação falha em JSON inválido; unidade 1 carrega |
| 3 | Fluxo da lição: todos os tipos de exercício, feedback, corações, XP, combo, resultado, cronômetro | Lição completa jogável; testes de `XpEngine`/`HeartsEngine` |
| 4 | Onboarding + trilha + progressão e desbloqueio | Concluir lição desbloqueia a seguinte; progresso persiste |
| 5 | Ofensiva, congelar, meta diária, missões, conquistas, baús, gemas, loja | Testes de `StreakEngine` (virada, congelar, perda) passam |
| 6 | Notificações locais | Notificação chega no horário; reboot reagenda |
| 7 | Praticar erros (SRS) e ligas com bots (na 0.4 viraram o ranking real) | Sessão de erros usa itens vencidos; semana fecha e promove |
| 8 | Sons, vibração, acessibilidade (TalkBack, fonte grande, contraste), polimento | Checklist de acessibilidade ok; sem jank perceptível |
| 9 | Conteúdo: unidades 2–10 em lotes com validação e revisão | Todas as unidades validam; relatório de revisão por unidade |
| 10 (v1.1) | Simulado 20q/60min e 80q/4h (só Específicos), nivelamento | Resultado com tempo por questão e erros por tema |

**Cronograma sugerido (alvo):** fases 0–5 em ~1 semana de trabalho; 6–8 na semana seguinte; conteúdo (fase 9) em paralelo desde a fase 2, para o usuário já estudar a Unidade 1 cedo.

## 10. Testes e qualidade

- **Unitários:** `StreakEngine` (meia-noite, congelar, múltiplos dias), `XpEngine`, `HeartsEngine` (regeneração), `QuestEngine`, regras sociais (`SocialRules`, limites, semana do ranking), `SrsEngine`, parser/validador de conteúdo, cálculo de folga/atraso do cronômetro.
- **UI (Compose):** fluxo de uma lição com respostas certa/errada; navegação onboarding→trilha.
- **Conteúdo:** `validate_content.py` roda no `./gradlew check`.
- **Manual:** checklist por fase em `docs/QA.md`.
- **SQL:** `tools/sql-tests/run.sh` aplica a migração duas vezes num Postgres local e roda os testes de RLS e regras.
- **Integração social:** a harness testa o cliente inteiro contra um Supabase falso em memória (`SocialSyncTest`).
- Sem analytics. Permissões: `POST_NOTIFICATIONS`, `INTERNET`, `ACCESS_NETWORK_STATE` (+ `RECEIVE_BOOT_COMPLETED` e
  `WAKE_LOCK` do WorkManager).

## 11. Entrega e backup

- APK de debug por `./gradlew assembleDebug` e instalação via ADB/arquivo (sideload). Assinatura de release é opcional e fica fora do repositório.
- Progresso salvo na conta (Supabase) a cada lição; trocar de aparelho restaura tudo. O backup automático do Android
  continua para o banco local (sem a sessão de login), mais **exportar/importar JSON** nas Configurações.

## 12. Riscos e decisões em aberto

- **Qualidade factual das questões geradas por IA:** mitigada por validação, segunda revisão e selo "não revisada"; o dono deve revisar sobretudo normas e versões.
- **Estilo real da FCC em TI ainda não observado:** calibrar com questões reais coladas pelo dono (`content/inbox/`).
- **Notificações em segundo plano** dependem do fabricante do celular.
- **Pendências para o dono responder depois:** tema final dos personagens, linguagem de programação preferida (peso dos exemplos), meta diária padrão, uso de PWA/alerta externo, nome definitivo do app. (Ligas: decidido — amigos reais via Supabase.)
- **Cargo 04 cobra muito mais que programação:** redes, SO, servidores, monitoramento e segurança ocupam grande parte do Anexo IV. A ordem da trilha começa por desenvolvimento, mas o dono deve ajustá-la ao seu ponto fraco.
- **Versões muito recentes no edital** (ITIL Version 5, PMBOK 8ª ed., OWASP Top 10:2021 e 2025 em seções distintas): material de estudo pode ser escasso, e o conteúdo gerado nesses temas deve ser revisado com mais cuidado.
- Prioridade se o prazo apertar: fases 0–5 e a Unidade 1–4 de conteúdo valem mais que ranking, loja e sons.
