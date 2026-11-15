-- =====================================================================================================================
-- BanQuest — versões publicadas do app (aviso de "nova versão disponível").
--
-- Como aplicar: Supabase > SQL Editor > cole o arquivo inteiro > Run (depois do 0001). Pode rodar de novo.
-- Como publicar uma versão: veja docs/PUBLICAR_VERSAO.md (sobe o APK e insere uma linha em app_releases).
--
-- Só o dono escreve aqui (pelo painel/SQL Editor). O app só lê a versão mais nova pela função latest_release(), que
-- pode ser chamada até sem login (a tela de entrada também avisa) e não expõe nada pessoal.
-- =====================================================================================================================

create table if not exists public.app_releases (
  version_code     int primary key check (version_code > 0),
  version_name     text not null check (char_length(version_name) between 1 and 20),
  apk_url          text not null check (apk_url ~ '^https://\S+$'),
  notes            text not null default '' check (char_length(notes) <= 500),
  -- Versões abaixo desta precisam atualizar para continuar (ex.: quando o servidor muda).
  min_version_code int not null default 1 check (min_version_code > 0 and min_version_code <= version_code),
  published_at     timestamptz not null default now()
);

alter table public.app_releases enable row level security;
revoke all on public.app_releases from anon, authenticated;

create or replace function public.latest_release() returns json
language sql stable security definer set search_path = public, pg_temp as $$
  select json_build_object(
    'version_code', r.version_code, 'version_name', r.version_name, 'apk_url', r.apk_url, 'notes', r.notes,
    'min_version_code', r.min_version_code, 'published_at', r.published_at)
  from app_releases r
  order by r.version_code desc
  limit 1
$$;

revoke execute on function public.latest_release() from public;
grant execute on function public.latest_release() to anon, authenticated;
