# Plano — Trilha TI social (Supabase)

Branch de trabalho: `feature/supabase-social`. A `main` continua sendo a versão offline (0.3.0) até o dono aprovar esta.

> **Situação (15/11/2026): S0 a S5 implementados na branch `feature/supabase-social` (versão 0.4.0).** As perguntas da
> seção 10 ficaram com o padrão sugerido; decisões e desvios estão em `docs/DECISIONS.md` ("Versão social"). Falta o
> dono executar `docs/SUPABASE_SETUP.md` (SQL, login por código e SMTP) e o roteiro de dois aparelhos do `docs/QA.md`.
> O cliente usa a API REST do Supabase Auth/PostgREST diretamente, sem o SDK `supabase-kt` (decisão 5).

## 1. Objetivo

Transformar o Trilha TI num app para um grupo fechado de amigos que estudam para o Banestes:
conta com login, progresso salvo na nuvem, **ranking real da semana** (geral e só de amigos), amizades e
**ofensiva em dupla** com cada amigo. O estudo continua funcionando sem internet depois do login.

## 2. Decisões do dono (15/11/2026)

| Tema | Decisão |
|---|---|
| Conta | **Obrigatória.** O app abre no login se não houver sessão. |
| Login | **Código por e-mail** (6 dígitos, sem senha), pelo Supabase Auth. |
| Progresso | **Tudo na nuvem**: trocar de celular e fazer login restaura lições, erros, XP, gemas e conquistas. |
| Ranking | **Lista com todos os usuários** (o app é só para amigos, sem filtro de apelido) **e** lista só de amigos. Só **ranking da semana** (zera na segunda 00:00 de Brasília). |
| Rivais simulados | **Removidos.** Sai a liga de 30 com divisões. |
| Ofensiva em conjunto | **Em dupla**: para cada amizade, conta os dias em que os dois concluíram ao menos 1 lição. |
| Amizade | Adicionar **pelo ranking** ou por **código de convite**. Quem não quer aparecer no ranking geral só é achado pelo código. |
| Visível para os outros | Só **nome, personagem, XP e ofensiva**. |
| Avisos | **Sem Firebase**: o app busca novidades ao abrir e em segundo plano (~1 h). Sem "cutucar". |
| Anti-trapaça | **Confiança + limites**: o servidor aceita o XP do app e recusa o impossível. |

## 3. Regras do projeto que mudam (CLAUDE.md e PLAN.md)

Só serão editadas quando o dono aprovar este plano:

- **Regra 2** passa de "offline e privado: sem login, sem nuvem" para:
  *"Conta obrigatória por código de e-mail. O estudo funciona offline depois do login e sincroniza com o Supabase.
  Para os outros usuários vai só nome, personagem, XP da semana e ofensiva. Sem anúncios e sem analytics."*
- **Permissões:** entram `INTERNET` e `ACCESS_NETWORK_STATE` (o worker de sincronização espera ter rede).
  `WAKE_LOCK` (do WorkManager) fica registrado.
- **PLAN.md §2** "Fora da v1": sai "login, nuvem, amigos reais, ligas reais".
- **PLAN.md §7** "Ligas semanais": substituído pelo ranking real da semana.
- **Conquista "Liga Ouro"** é trocada (ver §10, pergunta 4).

## 4. Arquitetura

```
 App Android (Room = verdade local, funciona offline)
   ├─ Login (Supabase Auth: código por e-mail)
   ├─ Fila de envio (Room: tabela outbox)  ──►  RPC submit_session  ──►  study_sessions
   ├─ Retrato do progresso (Backup JSON)   ──►  RPC push_snapshot   ──►  progress_snapshots
   └─ Worker de sincronização (WorkManager, a cada ~1 h + ao abrir + após lição)
         ◄── ranking da semana, amigos, pedidos, ofensivas em dupla (cache no Room para ver offline)

 Supabase (região São Paulo)
   ├─ Auth: e-mail com código de 6 dígitos (OTP)
   ├─ Postgres com RLS: cada um só lê o próprio dado e o dado público dos outros
   └─ Funções SQL (RPC, security definer) para toda escrita, com limites anti-trapaça
```

**Dois canais de sincronização:**
1. **Sessões de estudo** (lição ou treino concluído): evento pequeno, com id único gerado no app (idempotente).
   Alimenta o ranking da semana e as ofensivas. O servidor valida os limites.
2. **Retrato do progresso**: o mesmo JSON do "Exportar progresso" que já existe (`BackupCodec`), com número de
   versão. Serve para restaurar em outro aparelho. Se dois aparelhos divergirem, o app pergunta qual manter.

**No app:** cliente `supabase-kt` (Auth + Postgrest) com Ktor/OkHttp. Interface `SocialApi` com implementação
Supabase e uma falsa para testes, sem rede nos testes. Banco local Room **v2 → v3 com migração explícita**:
agora há dados reais e nada pode ser apagado na atualização.

**Chaves:** no app vai só a URL do projeto e a chave **pública** (anon/publishable), protegidas pelas regras RLS.
A chave `service_role` nunca entra no app, no repositório nem no chat.

## 5. Banco de dados (rascunho)

```sql
profiles          (id uuid PK = auth.uid, display_name text 2..24, character text,
                   friend_code text unique,          -- ex.: GUAI-4821
                   show_in_global bool default true,  -- aparecer no ranking geral
                   streak_current int, streak_longest int, last_study_day date,
                   created_at, updated_at)
study_sessions    (id uuid PK (gerado no app), user_id, day date, kind 'lesson'|'practice',
                   xp int, answers int, duration_ms int, created_at)
friend_requests   (id, from_id, to_id, status 'pending'|'accepted'|'declined', created_at, unique(from_id,to_id))
friendships       (user_a, user_b, created_at)  -- par ordenado a < b
progress_snapshots(user_id PK, version bigint, payload jsonb, device_id text, updated_at)
```

**Funções (RPC):**
- `submit_session(id, day, kind, xp, answers, duration_ms, streak, longest)`: limites (lição ≤ 210 XP, treino ≤ 120,
  ambos até 30 respostas; prova anterior `exam` até 100 respostas e 15 XP por resposta + 30; pelo menos 2 s por resposta; dia entre hoje − 7 e hoje em Brasília; no máximo 40 sessões por dia). Idempotente pelo `id`.
- `weekly_ranking(scope 'global'|'friends')`: XP da semana atual. Na lista geral entra quem tem `show_in_global`;
  amigos aparecem para os amigos mesmo ocultos.
- `request_friend(user_id)` e `respond_request(id, accept)`: pedido pelo ranking precisa ser aceito.
- `add_friend_by_code(code)`: quem compartilhou o código já consentiu, então a amizade vale na hora.
- `remove_friend(user_id)`, `pair_streaks()` e `my_friends()`.
- `push_snapshot(expected_version, payload)` e `pull_snapshot()`.
- `delete_my_account()` (LGPD): apaga perfil, sessões, amizades e retrato.

**Ofensiva em dupla** (calculada no servidor): dias seguidos, terminando hoje ou ontem, em que os dois têm ao
menos uma sessão `lesson`. Sem congelamento na v1 (o congelamento continua valendo só para a ofensiva individual).

**Semana:** segunda 00:00 a domingo 23:59 em `America/Sao_Paulo`. Não precisa de tarefa agendada: o ranking é
calculado na consulta. "Semana passada: você ficou em N.º" sai da mesma consulta com a semana anterior.

## 6. Telas

- **Login** (nova primeira tela): e-mail → código de 6 dígitos → (primeira vez) apelido e personagem.
  Na primeira entrada do dono, o progresso local **sobe para a nuvem** em vez de começar do zero.
- **Aba "Ranking"** (no lugar de "Ligas"): missões do dia no topo e seletor **Geral | Amigos** com o ranking da semana
  ("termina em 3d 4h"). Tocar em alguém do ranking geral abre o cartão com o botão **Adicionar amigo**.
- **Amigos** (na visão Amigos): meu código para compartilhar, campo para adicionar por código, pedidos recebidos
  (aceitar/recusar), lista de amigos com a **ofensiva em dupla** de cada um.
- **Trilha:** chama extra no topo quando há ofensiva em dupla em risco ("Você e Ana: 6 dias. Falta você hoje").
- **Configurações:** conta (e-mail, sair, excluir conta e dados), privacidade ("aparecer no ranking geral"),
  estado da sincronização ("sincronizado há 5 min").
- **Avisos locais** (sem Firebase), gerados quando a sincronização traz novidade: pedido de amizade e ofensiva em
  dupla em risco às 21:30.

## 7. Fases (cada uma termina com testes, commit, push na branch e parada para revisão)

| Fase | Entrega | Aceite |
|---|---|---|
| **S0** | Atualização do CLAUDE.md/PLAN.md, texto de privacidade, `docs/SUPABASE_SETUP.md` com o passo a passo, e o dono cria o projeto | Projeto criado, login por código testado no painel |
| **S1** | `supabase/migrations/*.sql`: tabelas, RLS, funções e limites; testes SQL num Postgres local | Testes de RLS e funções passando; dono aplica no projeto |
| **S2** | Login no app, sessão persistente, sair, excluir conta, permissão de internet | Entrar com código, fechar o app e continuar logado |
| **S3** | Sincronização: fila de sessões, retrato do progresso, migração Room v3, primeira entrada com progresso local, conflito entre aparelhos | Fazer lição offline, ligar a internet e ver no servidor; reinstalar e restaurar tudo |
| **S4** | Aba Ranking (geral e amigos), privacidade, remoção dos rivais simulados, troca da conquista "Liga Ouro" | Dois usuários reais aparecem no ranking da semana |
| **S5** | Amizades (pelo ranking e por código), pedidos, ofensiva em dupla e avisos | Dois aparelhos viram amigos e a ofensiva em dupla conta |
| **S6** | Endurecimento: limites testados, tudo offline, QA em dois aparelhos, versão 0.4.0 | Roteiro do `docs/QA.md` passando |

## 8. Testes

- **Domínio** (Kotlin puro): regras de ofensiva em dupla, semana de Brasília, limites, fila de envio e conflito de versão.
- **SQL:** Postgres local com o esquema `auth` simulado, para testar RLS (ninguém lê o que não deve) e as funções.
- **App:** harness com Robolectric e a `SocialApi` falsa (login, sincronização, ranking, amigos) e testes de acessibilidade das telas novas.
- **Real:** dois aparelhos (ou um aparelho e um emulador) no fim de S5 e S6.

## 9. Riscos e cuidados

- **E-mail do código:** o envio padrão do Supabase tem limite baixo e, pelo que sei, só manda para e-mails da
  equipe do projeto. Para os amigos receberem o código, provavelmente será preciso um SMTP próprio (ex.: Resend ou
  Brevo, gratuitos em volume baixo). **Confirmar no S0.**
- **Plano gratuito:** um projeto sem uso por cerca de uma semana é pausado; com uso diário, não acontece.
- **LGPD:** o app guarda e-mail e dados de estudo de terceiros (os amigos). Por isso: texto de privacidade, exclusão
  de conta no app, e só nome/personagem/XP/ofensiva visíveis aos outros.
- **Trapaça:** com "confiança + limites", alguém que modifique o app ainda consegue inflar XP dentro dos limites.
  Aceitável para um grupo de amigos.
- **Distribuição:** os amigos instalam o APK manualmente; atualizações também. Nada muda nisso.
- **Ambiente de desenvolvimento:** para testar contra o Supabase real a partir desta nuvem, o host do projeto
  (`<ref>.supabase.co`) precisa ser liberado na rede.

## 10. Perguntas em aberto (com a sugestão padrão)

1. **"Ranking diário" ou semanal?** Na resposta sobre amizade apareceu "ranking diário", mas a escolha foi
   "ranking da semana". *Padrão: só semanal.*
2. **Pedido pelo ranking precisa ser aceito?** *Padrão: sim (o outro aceita); pelo código vale na hora.*
3. **Serviço de e-mail (SMTP):** *Padrão: Resend (gratuito para volume baixo), configurado no S0.*
4. **Conquista "Liga Ouro":** *Padrão: vira "Pódio da semana" (terminar a semana entre os 3 primeiros do ranking geral).*
5. **Nome da aba:** *Padrão: "Ligas" vira "Ranking".*
