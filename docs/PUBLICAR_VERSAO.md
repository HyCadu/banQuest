# Publicar uma versão nova do BanQuest

O app confere sozinho se há versão nova: ao abrir, ao voltar para ele e de hora em hora em segundo plano. Quando
encontra uma:
- mostra um aviso na Trilha (Baixar / Agora não);
- manda **uma** notificação por versão;
- mostra a versão em **Configurações → Sobre o app**.

Ele lê a versão mais nova da tabela `app_releases` do Supabase (`supabase/migrations/0002_releases.sql`).

## Uma vez só: preparar o Supabase

1. **SQL Editor:** rode `supabase/migrations/0002_releases.sql` (depois do `0001`). Pode rodar de novo sem problema.
2. **Storage → New bucket:** nome `apk`, marque **Public bucket** e salve. É onde ficam os APKs para download.

## A cada versão

1. **Gerar o APK assinado** (ver `docs/ASSINATURA.md`): `tools/build_apk.sh` → `dist/banquest-<versão>.apk`.
   O `versionCode` em `app/build.gradle.kts` precisa ser maior que o da versão anterior.
2. **Subir o APK:** **Storage → apk → Upload file** e escolha o arquivo. Depois, nos três pontinhos do arquivo,
   **Get URL / Copy URL**. O link tem este formato:
   `https://fheldagijfrqqgjjfcdt.supabase.co/storage/v1/object/public/apk/banquest-0.5.1.apk`
3. **Anunciar no SQL Editor** (troque os valores):
   ```sql
   insert into public.app_releases (version_code, version_name, apk_url, notes)
   values (9, '0.5.1',
           'https://fheldagijfrqqgjjfcdt.supabase.co/storage/v1/object/public/apk/banquest-0.5.1.apk',
           'Correções no ranking e personagens mais rápidos.');
   ```
   - `version_code`: o `versionCode` do `app/build.gradle.kts`.
   - `notes`: até 500 caracteres. Aparecem no aviso e na notificação.
4. Em até ~30 min (ou na hora, em Configurações → Sobre o app → Verificar atualização), quem tiver uma versão
   anterior vê o aviso. Tocando em **Baixar**, o navegador baixa o APK. Na primeira vez, o Android pede permissão para
   o navegador instalar apps, e a versão nova entra por cima, sem perder nada.

### Versão obrigatória

Quando o servidor mudar de um jeito que versões antigas não funcionem mais, preencha `min_version_code` com o menor
`versionCode` que ainda funciona:
```sql
insert into public.app_releases (version_code, version_name, apk_url, notes, min_version_code)
values (12, '0.7.0', 'https://…/banquest-0.7.0.apk', 'Mudança no servidor: atualize para continuar.', 12);
```
Quem estiver abaixo vê um aviso que **não fecha**, só com o botão Baixar.

### Corrigir um anúncio errado

```sql
update public.app_releases set apk_url = 'https://…/banquest-0.5.1.apk' where version_code = 9;
delete from public.app_releases where version_code = 9;   -- some o aviso (se ainda ninguém atualizou)
```

O link precisa ser `https`. Link `http` ou com espaço é recusado pelo banco e ignorado pelo app.

## E o site de divulgação?

O site (`site-banquest/dist/`) lê a mesma função `latest_release()` ao abrir e troca sozinho todos os botões "Baixar"
pelo `apk_url` da versão mais nova. Publicando como acima, o site já baixa a versão nova, sem editar nada.
- O link só é aceito se estiver neste Supabase, no bucket público `apk`, com `https` e terminando em `.apk`. Link de
  outro lugar é ignorado pelo site, que continua no link anterior.
- Os links escritos no `index.html` (3 botões com `data-apk`) são a reserva para quando a consulta falha. Vale trocar o
  nome do arquivo neles a cada versão.
- Para fixar um link manualmente, de qualquer endereço `https`, preencha `window.BANQUEST_APK_URL` no
  `site-banquest/dist/config.js`. Ele passa na frente de tudo; deixe `""` para voltar ao automático.
- As telas do celular do site saem do próprio app. Para regerar depois de mudar o visual:
  `./gradlew -p tools/preview-harness :shot:testDebugUnitTest --tests 'shot.Site*'` e
  `python3 tools/build_site_assets.py`.

