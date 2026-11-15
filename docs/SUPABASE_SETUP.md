# Supabase — configuração passo a passo (versão social 0.4)

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

## 2. Ligar o login por código de e-mail

1. **Authentication → Sign In / Providers → Email**: deixe **Enable Email provider** ligado. "Confirm email" pode ficar
   ligado (o código já confirma o e-mail).
2. No mesmo lugar, em "Email OTP Length", use **6** (o app pede 6 dígitos). Na validade ("Email OTP Expiration"),
   o padrão serve.
3. **Authentication → Emails → Templates**: edite **Magic Link** *e* **Confirm signup**. O primeiro acesso de um e-mail
   novo pode usar o "Confirm signup". Troque o conteúdo para mostrar o **código** (`{{ .Token }}`) em vez do link:

   **Assunto:**
   ```
   Seu código do Trilha TI: {{ .Token }}
   ```
   **Corpo (HTML):**
   ```html
   <h2>Seu código do Trilha TI</h2>
   <p>Digite este código no app para entrar:</p>
   <p style="font-size:32px;font-weight:bold;letter-spacing:6px">{{ .Token }}</p>
   <p>Ele vale por pouco tempo. Se você não pediu este código, ignore este e-mail.</p>
   ```

Se o e-mail chegar com um **link** em vez do número, o template ainda está sem `{{ .Token }}`.

## 3. Enviar e-mail para os amigos (SMTP próprio)

O envio padrão do Supabase serve só para testes: manda poucos e-mails por hora e **só para os e-mails da equipe do
projeto**. Para um amigo, o app mostra: *"O servidor ainda não pode mandar e-mail para esse endereço…"*. A solução é
configurar um SMTP próprio em **Authentication → Emails → SMTP Settings** → **Enable Custom SMTP**.

### Opção A — Gmail (mais simples, não precisa de domínio)
1. Na conta Google que vai enviar: ative a **verificação em duas etapas**.
2. Em myaccount.google.com → Segurança → **Senhas de app**: crie uma senha para "Trilha TI" (16 letras).
3. No Supabase (SMTP Settings):

   | Campo | Valor |
   |---|---|
   | Sender email | o seu Gmail |
   | Sender name | `Trilha TI` |
   | Host | `smtp.gmail.com` |
   | Port | `465` |
   | Username | o seu Gmail |
   | Password | a senha de app (sem espaços) |

O Gmail aceita algumas centenas de e-mails por dia, bem mais do que um grupo de amigos usa.

### Opção B — Resend (se você tiver um domínio próprio)
1. Crie a conta em resend.com, adicione e **verifique o seu domínio** (registros DNS) e gere uma **API key**.
2. No Supabase: Host `smtp.resend.com`, Port `465`, Username `resend`, Password = a API key, Sender email
   `nao-responda@seudominio.com.br`, Sender name `Trilha TI`.

Sem domínio verificado, o Resend só entrega para o seu próprio e-mail. Por isso, sem domínio, use a opção A.

### Limite de envios
Em **Authentication → Rate Limits**, o limite de e-mails por hora pode ser aumentado depois do SMTP próprio
(ex.: 30/h). O app já espera 60 s antes de deixar pedir outro código.

> A senha de app (ou a API key) fica **só** no painel do Supabase. Ela nunca vai para o app nem para o repositório.

## 4. Testar

1. Instale `dist/trilha-ti-0.4.0-debug.apk` e abra: aparece a tela **"Entre com seu e-mail"**.
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
| "O servidor ainda não pode mandar e-mail para esse endereço" | Passo 3 (SMTP próprio). |
| "Muitos pedidos de código seguidos" | Espere alguns minutos ou aumente o limite (passo 3). |
| "Código inválido ou vencido" | Use o código do **último** e-mail, ou peça outro. |
| O e-mail chega com link, sem número | Passo 2.3 (templates com `{{ .Token }}`). |
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
