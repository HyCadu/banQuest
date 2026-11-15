# Supabase — configuração passo a passo (BanQuest)

O app usa o **Supabase Auth** (login por código de 6 dígitos no e-mail) e o **Postgres** do projeto (ranking, amigos,
ofensiva em dupla e progresso salvo). Tudo o que o app precisa fica em um único arquivo SQL:
`supabase/migrations/0001_social.sql`.

O projeto já existe e já está ligado ao app:

| Item | Valor | Onde está no app |
|---|---|---|
| URL | `https://fheldagijfrqqgjjfcdt.supabase.co` | `gradle.properties` → `trilha.supabaseUrl` |
| Chave publishable | `sb_publishable_q-W2…` | `gradle.properties` → `trilha.supabaseKey` |

> **Nunca** coloque a chave `service_role` / `secret` no app, no repositório ou em mensagens. A publishable pode ficar
> no app porque cada tabela tem RLS e toda escrita passa por funções que conferem quem está logado.

Os nomes dos menus abaixo são os do painel atual do Supabase. Se algum estiver um pouco diferente, procure pelo termo
entre aspas.

---

## 1. Criar as tabelas e funções (SQL)

1. Painel do projeto → **SQL Editor** → **New query**.
2. Cole o conteúdo **inteiro** de `supabase/migrations/0001_social.sql` e clique em **Run**.
3. Resultado esperado: *"Success. No rows returned"*. Se rodar de novo, não tem problema: o script pode ser reaplicado.
4. Confira:
   - **Table Editor**: aparecem `profiles`, `study_sessions`, `friend_requests`, `friendships` e `progress_snapshots`,
     todas com o selo **RLS enabled**.
   - **Database → Functions**: aparecem `social_overview`, `submit_session`, `push_snapshot`, `add_friend_by_code` etc.

Sem este passo, o app mostra: *"O banco do servidor ainda não foi configurado (falta rodar o SQL do app no Supabase)."*

Depois, rode também `supabase/migrations/0002_releases.sql` (aviso de versão nova; ver `docs/PUBLICAR_VERSAO.md`).

## 2. Configurar o envio de e-mail (SMTP próprio) — antes dos templates

O painel **não deixa editar os templates** sem um SMTP próprio. Aparece o aviso *"Set up custom SMTP to edit templates"*.
E o template padrão manda só um **link**, sem o código, então o login do app não funciona com ele. Além disso, o envio
padrão manda poucos e-mails por hora e **só para os e-mails da equipe do projeto**. Para um amigo, o app mostraria
*"O servidor ainda não pode mandar e-mail para esse endereço…"*.

Onde configurar: **Authentication → Emails → aba "SMTP Settings"** (ou o botão **"Set up SMTP"** no aviso).

### Opção A — Gmail (mais simples, não precisa de domínio)
1. Na conta Google que vai enviar: ative a **verificação em duas etapas**.
2. Em myaccount.google.com → Segurança → **Senhas de app**: crie uma senha para "BanQuest" (16 letras).
3. No Supabase, ligue o SMTP próprio e preencha:

   | Campo | Valor |
   |---|---|
   | Sender email address | o seu Gmail |
   | Sender name | `BanQuest` |
   | Host | `smtp.gmail.com` |
   | Port number | `465` |
   | Minimum interval between emails | deixe o padrão (60 s) |
   | Username | o seu Gmail |
   | Password | a senha de app (sem espaços) |

4. **Save changes**.

O Gmail aceita algumas centenas de e-mails por dia, bem mais do que um grupo de amigos usa.

### Opção B — Resend (se você tiver um domínio próprio)
1. Crie a conta em resend.com, adicione e **verifique o seu domínio** (registros DNS) e gere uma **API key**.
2. No Supabase: Host `smtp.resend.com`, Port `465`, Username `resend`, Password = a API key, Sender email
   `nao-responda@seudominio.com.br`, Sender name `BanQuest`.

Sem domínio verificado, o Resend só entrega para o seu próprio e-mail. Por isso, sem domínio, use a opção A.

### Limite de envios
Em **Authentication → Rate Limits**, o limite de e-mails por hora pode ser aumentado depois do SMTP próprio
(ex.: 30/h). O app já espera 60 s antes de deixar pedir outro código.

> A senha de app (ou a API key) fica **só** no painel do Supabase. Ela nunca vai para o app nem para o repositório.

## 3. Ligar o login por código (templates com `{{ .Token }}`)

1. **Authentication → Sign In / Providers → Email**: deixe **Enable Email provider** ligado e **desligue "Confirm email"**.
   Com ele ligado, um e-mail que já recebeu um link fica como conta "não confirmada", e o Supabase passa a mandar o
   template de confirmação (com link) em vez do código. Desligar não abre brecha: para entrar, a pessoa precisa
   digitar o código que chegou no e-mail dela. Se houver a opção "Email OTP Length", use **6**.
2. **Authentication → Emails → aba "Templates"**: com o SMTP salvo, os templates passam a ser editáveis. Edite dois:
   - **"Magic link or OTP"**: usado quando a conta já existe;
   - **"Confirm sign up"**: o primeiro acesso de um e-mail novo pode usar este.

   Em cada um, troque o **Subject** e, no **Body**, a aba **Source** (não a Preview), e clique em **Save changes**:

   **Subject:**
   ```
   Seu código do BanQuest: {{ .Token }}
   ```
   **Body (Source):**
   ```html
   <h2>Seu código do BanQuest</h2>
   <p>Digite este código no app para entrar:</p>
   <p style="font-size:32px;font-weight:bold;letter-spacing:6px">{{ .Token }}</p>
   <p>Ele vale por pouco tempo. Se você não pediu este código, ignore este e-mail.</p>
   ```

Se o e-mail chegar com um **link** em vez do número, o template ainda está sem `{{ .Token }}`.

## 4. Testar

1. Instale o APK mais recente (`dist/banquest-<versão>.apk`) e abra: aparece a tela **"Entre com seu e-mail"**.
2. Digite o e-mail → **Receber código** → o e-mail chega com o número → digite → **Entrar**.
3. Escolha apelido e personagem → **Começar**. Se já havia progresso no celular, ele sobe para a nuvem.
4. No painel: **Authentication → Users** mostra o e-mail. **Table Editor → profiles** mostra o apelido e o
   `friend_code` (`TRI-XXXXX`).
5. Faça uma lição e abra a aba **Ranking**: seu XP aparece. **Table Editor → study_sessions** mostra a sessão.
6. Num segundo celular (ou de um amigo), crie outra conta e adicione pelo código. A ofensiva em dupla aparece nos dois.

## 5. Problemas comuns

| O app diz… | O que fazer |
|---|---|
| "falta rodar o SQL do app no Supabase" | Passo 1. |
| "O servidor ainda não pode mandar e-mail para esse endereço" | Passo 2 (SMTP próprio). |
| "Muitos pedidos de código seguidos" | Espere alguns minutos ou aumente o limite (passo 2). |
| "Código inválido ou vencido" | Use o código do **último** e-mail, ou peça outro. |
| O e-mail chega com link, sem número | Templates sem `{{ .Token }}` ou não salvos (passo 3.2), ou "Confirm email" ainda ligado (passo 3.1). Neste caso, apague em **Authentication → Users** o usuário que ficou "não confirmado" e peça o código de novo. |
| "O Supabase não conseguiu enviar o e-mail com o código" | O SMTP recusou o envio. Veja o motivo em **Logs → Auth** (ex.: `535 Username and Password not accepted` = senha de app errada) e corrija no passo 2. |
| "O servidor deu erro (500: …)" | Veja o texto entre parênteses e o **Logs → Auth** no painel. |
| Nada carrega e o painel diz "Paused" | O plano gratuito pausa projetos parados por ~1 semana: clique em **Restore**. |

## 6. Privacidade (para mandar aos amigos)

- Para entrar, o app guarda o seu **e-mail** (só para o login) e o seu **progresso de estudo** (XP, lições, ofensiva).
- Os outros usuários veem **só**: apelido, personagem, XP da semana e ofensiva. Nunca o e-mail.
- Em **Configurações → Conta**, dá para sair do ranking geral (só amigos te veem) e **excluir a conta**. A exclusão
  apaga perfil, progresso, sessões e amizades na hora.
- Sem anúncios, sem rastreamento, sem venda de dados. Os dados ficam no projeto Supabase do dono do app.

## 7. Para o desenvolvedor

- O SQL é testado num Postgres local: `tools/sql-tests/run.sh` (aplica a migração duas vezes e roda os testes de RLS e
  das regras).
- O cliente do app é testado contra um Supabase falso em memória: `SocialSyncTest`, em `tools/preview-harness`.
- Para trocar de projeto: mude `trilha.supabaseUrl` e `trilha.supabaseKey` em `gradle.properties`, rode o SQL no
  projeto novo e gere outro APK.
