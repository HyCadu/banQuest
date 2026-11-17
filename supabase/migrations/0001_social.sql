-- =====================================================================================================================
-- Trilha TI — esquema social no Supabase (versão 1)
--
-- Como aplicar: Supabase > SQL Editor > New query > cole o arquivo inteiro > Run. Pode rodar de novo sem quebrar
-- (usa "if not exists" e "create or replace").
--
-- Princípios:
--   * O app nunca escreve direto nas tabelas: toda escrita passa por funções (RPC) "security definer", que validam
--     quem está chamando (auth.uid()) e aplicam os limites anti-trapaça.
--   * RLS ligado em todas as tabelas. Leitura direta só do próprio perfil e do próprio retrato de progresso.
--   * Os outros usuários veem apenas: nome, mascote, XP da semana e ofensiva (decisão do dono, docs/PLANO_SOCIAL.md).
--   * Dia e semana sempre no fuso America/Sao_Paulo; a semana vai de segunda 00:00 a domingo 23:59.
-- =====================================================================================================================


-- ---------------------------------------------------------------------------------------------------------------------
-- Datas
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.today_brt() returns date
language sql stable set search_path = public, pg_temp as $$
  select (now() at time zone 'America/Sao_Paulo')::date
$$;

create or replace function public.week_start_brt(d date) returns date
language sql immutable set search_path = public, pg_temp as $$
  select d - (extract(isodow from d)::int - 1)
$$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Tabelas
-- ---------------------------------------------------------------------------------------------------------------------

create table if not exists public.profiles (
  id             uuid primary key references auth.users (id) on delete cascade,
  display_name   text not null check (char_length(btrim(display_name)) between 2 and 24),
  mascot         text not null default 'Guaiamum' check (mascot in ('Guaiamum', 'Tartaruga', 'Tamandua', 'Preguica')),
  friend_code    text not null unique,
  show_in_global boolean not null default true,
  streak_current int not null default 0 check (streak_current between 0 and 3650),
  streak_longest int not null default 0 check (streak_longest between 0 and 3650),
  last_study_day date,
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now()
);

create table if not exists public.study_sessions (
  id          uuid primary key,                       -- gerado no app: reenviar não duplica
  user_id     uuid not null references public.profiles (id) on delete cascade,
  day         date not null,
  kind        text not null check (kind in ('lesson', 'practice')),
  xp          int not null check (xp between 0 and 210),
  answers     int not null check (answers between 1 and 30),
  duration_ms bigint not null check (duration_ms >= 0),
  created_at  timestamptz not null default now()
);
create index if not exists study_sessions_user_day_idx on public.study_sessions (user_id, day);
create index if not exists study_sessions_day_idx on public.study_sessions (day);
-- 0.9.1: a lição por conceitos (12 exercícios) chega a 210 XP. O limite era 150 (lição antiga de 8 questões), e as
-- lições boas ficavam fora do ranking. Refaz a restrição para os bancos criados antes (rodar de novo não muda nada).
alter table public.study_sessions drop constraint if exists study_sessions_xp_check;
alter table public.study_sessions add constraint study_sessions_xp_check check (xp between 0 and 210);

create table if not exists public.friend_requests (
  id           uuid primary key default gen_random_uuid(),
  from_id      uuid not null references public.profiles (id) on delete cascade,
  to_id        uuid not null references public.profiles (id) on delete cascade,
  status       text not null default 'pending' check (status in ('pending', 'accepted', 'declined')),
  created_at   timestamptz not null default now(),
  responded_at timestamptz,
  unique (from_id, to_id),
  check (from_id <> to_id)
);

create table if not exists public.friendships (
  user_a     uuid not null references public.profiles (id) on delete cascade,
  user_b     uuid not null references public.profiles (id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_a, user_b),
  check (user_a < user_b)                              -- cada amizade aparece uma vez só
);
create index if not exists friendships_user_b_idx on public.friendships (user_b);

create table if not exists public.progress_snapshots (
  user_id    uuid primary key references public.profiles (id) on delete cascade,
  version    bigint not null check (version > 0),
  device_id  text not null check (char_length(device_id) between 1 and 64),
  payload    jsonb not null check (pg_column_size(payload) < 5000000),
  updated_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------------------------------------------------
-- Segurança: RLS ligado, leitura direta só do que é seu, escrita direta proibida
-- ---------------------------------------------------------------------------------------------------------------------

alter table public.profiles enable row level security;
alter table public.study_sessions enable row level security;
alter table public.friend_requests enable row level security;
alter table public.friendships enable row level security;
alter table public.progress_snapshots enable row level security;

drop policy if exists profiles_select_own on public.profiles;
create policy profiles_select_own on public.profiles for select to authenticated using (id = auth.uid());

drop policy if exists snapshots_select_own on public.progress_snapshots;
create policy snapshots_select_own on public.progress_snapshots for select to authenticated using (user_id = auth.uid());

revoke insert, update, delete, truncate on public.profiles, public.study_sessions, public.friend_requests,
  public.friendships, public.progress_snapshots from anon, authenticated;
revoke all on public.profiles, public.study_sessions, public.friend_requests, public.friendships,
  public.progress_snapshots from anon;

-- ---------------------------------------------------------------------------------------------------------------------
-- Funções internas
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.require_uid() returns uuid
language plpgsql stable set search_path = public, pg_temp as $$
declare uid uuid := auth.uid();
begin
  if uid is null then
    raise exception 'login necessário' using errcode = '28000';
  end if;
  return uid;
end $$;

create or replace function public.require_profile() returns uuid
language plpgsql stable security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_uid();
begin
  if not exists (select 1 from profiles where id = uid) then
    raise exception 'perfil não criado' using errcode = 'P0002';
  end if;
  return uid;
end $$;

create or replace function public.are_friends(a uuid, b uuid) returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (select 1 from friendships where user_a = least(a, b) and user_b = greatest(a, b))
$$;

-- Ofensiva mostrada aos outros: vale até ontem (o aluno ainda pode estudar hoje); antes disso, zerou.
create or replace function public.visible_streak(streak int, last_day date) returns int
language sql stable set search_path = public, pg_temp as $$
  select case when last_day is not null and last_day >= public.today_brt() - 1 then streak else 0 end
$$;

create or replace function public.new_friend_code() returns text
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare
  alphabet text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';  -- sem 0/O e 1/I, para ditar sem erro
  code text;
begin
  loop
    code := 'TRI-';
    for i in 1..5 loop
      code := code || substr(alphabet, 1 + floor(random() * length(alphabet))::int, 1);
    end loop;
    exit when not exists (select 1 from profiles where friend_code = code);
  end loop;
  return code;
end $$;

-- Ofensiva em dupla: dias seguidos, terminando hoje (ou ontem, se hoje ainda falta alguém), em que os dois
-- concluíram ao menos uma lição.
create or replace function public.pair_streak(a uuid, b uuid) returns int
language plpgsql stable security definer set search_path = public, pg_temp as $$
declare
  d date := public.today_brt();
  n int := 0;
begin
  if not (exists (select 1 from study_sessions where user_id = a and day = d and kind = 'lesson')
          and exists (select 1 from study_sessions where user_id = b and day = d and kind = 'lesson')) then
    d := d - 1;
  end if;
  while exists (select 1 from study_sessions where user_id = a and day = d and kind = 'lesson')
    and exists (select 1 from study_sessions where user_id = b and day = d and kind = 'lesson') loop
    n := n + 1;
    d := d - 1;
    exit when n >= 3650;
  end loop;
  return n;
end $$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Perfil
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.my_profile() returns json
language sql stable security definer set search_path = public, pg_temp as $$
  select json_build_object(
    'id', p.id, 'display_name', p.display_name, 'mascot', p.mascot, 'friend_code', p.friend_code,
    'show_in_global', p.show_in_global, 'streak', public.visible_streak(p.streak_current, p.last_study_day),
    'streak_longest', p.streak_longest, 'last_study_day', p.last_study_day)
  from profiles p where p.id = public.require_uid()
$$;

create or replace function public.upsert_my_profile(p_display_name text, p_mascot text) returns json
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_uid();
begin
  insert into profiles (id, display_name, mascot, friend_code)
  values (uid, btrim(p_display_name), p_mascot, public.new_friend_code())
  on conflict (id) do update set display_name = btrim(excluded.display_name), mascot = excluded.mascot, updated_at = now();
  return public.my_profile();
end $$;

create or replace function public.set_show_in_global(p_show boolean) returns json
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_profile();
begin
  update profiles set show_in_global = p_show, updated_at = now() where id = uid;
  return public.my_profile();
end $$;

-- Atualiza a ofensiva individual (o app manda o valor já reconciliado: congelamento, perda etc.).
create or replace function public.update_my_streak(p_streak int, p_longest int, p_last_study_day date) returns json
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_profile();
begin
  if p_streak not between 0 and 3650 or p_longest not between 0 and 3650 or p_streak > p_longest then
    return json_build_object('ok', false, 'reason', 'invalid_streak');
  end if;
  if p_last_study_day is not null and (p_last_study_day > public.today_brt() or p_last_study_day < public.today_brt() - 3650) then
    return json_build_object('ok', false, 'reason', 'invalid_day');
  end if;
  update profiles set streak_current = p_streak, streak_longest = greatest(streak_longest, p_longest),
    last_study_day = p_last_study_day, updated_at = now()
  where id = uid;
  return json_build_object('ok', true);
end $$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Sessões de estudo (alimentam ranking e ofensiva em dupla) — com limites anti-trapaça
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.submit_session(
  p_id uuid, p_day date, p_kind text, p_xp int, p_answers int, p_duration_ms bigint,
  p_streak int, p_streak_longest int, p_last_study_day date
) returns json
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare
  uid uuid := public.require_profile();
  today date := public.today_brt();
  -- lição: 12 exercícios × 15 XP + 20 de conclusão + 10 de perfeita = 210 (igual a SessionLimits no app)
  max_xp int := case p_kind when 'lesson' then 210 when 'practice' then 120 else -1 end;
begin
  if exists (select 1 from study_sessions where id = p_id) then
    return json_build_object('ok', true, 'duplicate', true);
  end if;
  if max_xp < 0 then return json_build_object('ok', false, 'reason', 'invalid_kind'); end if;
  if p_day is null or p_day > today or p_day < today - 7 then return json_build_object('ok', false, 'reason', 'invalid_day'); end if;
  if p_xp is null or p_xp < 0 or p_xp > max_xp then return json_build_object('ok', false, 'reason', 'xp_limit'); end if;
  if p_answers is null or p_answers not between 1 and 30 then return json_build_object('ok', false, 'reason', 'invalid_answers'); end if;
  if p_duration_ms is null or p_duration_ms < p_answers * 2000 then return json_build_object('ok', false, 'reason', 'too_fast'); end if;
  if (select count(*) from study_sessions where user_id = uid and day = p_day) >= 40 then
    return json_build_object('ok', false, 'reason', 'daily_limit');
  end if;

  insert into study_sessions (id, user_id, day, kind, xp, answers, duration_ms)
  values (p_id, uid, p_day, p_kind, p_xp, p_answers, p_duration_ms)
  on conflict (id) do nothing;

  perform public.update_my_streak(p_streak, p_streak_longest, p_last_study_day);
  return json_build_object('ok', true);
end $$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Ranking da semana
--   p_scope: 'global' (todos que aparecem no geral + você + seus amigos) ou 'friends' (você + amigos)
--   p_week_offset: 0 = semana atual, 1 = semana passada
--   friend_state: me | friend | outgoing (pedido enviado) | incoming (pedido recebido) | none
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.weekly_ranking(p_scope text default 'global', p_week_offset int default 0)
returns table (user_id uuid, display_name text, mascot text, xp int, streak int, pos int, friend_state text)
language sql stable security definer set search_path = public, pg_temp as $$
  with me as (select public.require_profile() as id),
  wk as (select public.week_start_brt(public.today_brt()) - 7 * greatest(p_week_offset, 0) as ws),
  people as (
    select p.* from profiles p, me
    where p.id = me.id or public.are_friends(me.id, p.id) or (p_scope = 'global' and p.show_in_global)
  ),
  totals as (
    select pe.id, coalesce(sum(s.xp), 0)::int as xp
    from people pe cross join wk
    left join study_sessions s on s.user_id = pe.id and s.day between wk.ws and wk.ws + 6
    group by pe.id
  )
  select pe.id, pe.display_name, pe.mascot, t.xp,
         public.visible_streak(pe.streak_current, pe.last_study_day),
         (rank() over (order by t.xp desc))::int,
         case
           when pe.id = me.id then 'me'
           when public.are_friends(me.id, pe.id) then 'friend'
           when exists (select 1 from friend_requests r where r.from_id = me.id and r.to_id = pe.id and r.status = 'pending') then 'outgoing'
           when exists (select 1 from friend_requests r where r.from_id = pe.id and r.to_id = me.id and r.status = 'pending') then 'incoming'
           else 'none'
         end
  from people pe join totals t on t.id = pe.id cross join me
  order by t.xp desc, pe.display_name
$$;

-- Resultado da semana passada no ranking geral (para a conquista "Pódio da semana").
create or replace function public.last_week_result() returns json
language sql stable security definer set search_path = public, pg_temp as $$
  select json_build_object('pos', r.pos, 'xp', r.xp,
                           'participants', (select count(*) from public.weekly_ranking('global', 1) x where x.xp > 0))
  from public.weekly_ranking('global', 1) r where r.friend_state = 'me'
$$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Amizades
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.make_friends(a uuid, b uuid) returns void
language sql volatile security definer set search_path = public, pg_temp as $$
  insert into friendships (user_a, user_b) values (least(a, b), greatest(a, b)) on conflict do nothing;
  update friend_requests set status = 'accepted', responded_at = now()
  where status = 'pending' and ((from_id = a and to_id = b) or (from_id = b and to_id = a));
$$;

-- Pedido feito a partir do ranking geral: o outro precisa aceitar. Se ele já tinha pedido você, vira amizade na hora.
create or replace function public.request_friend(p_user uuid) returns text
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_profile();
begin
  if p_user = uid then return 'self'; end if;
  if not exists (select 1 from profiles where id = p_user and show_in_global) then return 'not_found'; end if;
  if public.are_friends(uid, p_user) then return 'already_friends'; end if;
  if exists (select 1 from friend_requests where from_id = p_user and to_id = uid and status = 'pending') then
    perform public.make_friends(uid, p_user);
    return 'accepted';
  end if;
  insert into friend_requests (from_id, to_id) values (uid, p_user)
  on conflict (from_id, to_id) do update set status = 'pending', created_at = now(), responded_at = null;
  return 'requested';
end $$;

create or replace function public.respond_friend_request(p_request uuid, p_accept boolean) returns text
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare
  uid uuid := public.require_profile();
  req friend_requests%rowtype;
begin
  select * into req from friend_requests where id = p_request and to_id = uid and status = 'pending';
  if not found then return 'not_found'; end if;
  if p_accept then
    perform public.make_friends(req.from_id, uid);
    return 'accepted';
  end if;
  update friend_requests set status = 'declined', responded_at = now() where id = req.id;
  return 'declined';
end $$;

-- Convite por código: quem compartilhou o código já consentiu, então vira amizade na hora (funciona mesmo para
-- quem está oculto do ranking geral).
create or replace function public.add_friend_by_code(p_code text) returns json
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare
  uid uuid := public.require_profile();
  other profiles%rowtype;
begin
  select * into other from profiles where friend_code = upper(btrim(p_code));
  if not found then return json_build_object('result', 'not_found'); end if;
  if other.id = uid then return json_build_object('result', 'self'); end if;
  if public.are_friends(uid, other.id) then
    return json_build_object('result', 'already_friends', 'display_name', other.display_name);
  end if;
  perform public.make_friends(uid, other.id);
  return json_build_object('result', 'added', 'display_name', other.display_name);
end $$;

create or replace function public.remove_friend(p_user uuid) returns text
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_profile();
begin
  delete from friendships where user_a = least(uid, p_user) and user_b = greatest(uid, p_user);
  delete from friend_requests where (from_id = uid and to_id = p_user) or (from_id = p_user and to_id = uid);
  return 'removed';
end $$;

create or replace function public.my_friend_requests()
returns table (request_id uuid, from_id uuid, display_name text, mascot text, created_at timestamptz)
language sql stable security definer set search_path = public, pg_temp as $$
  select r.id, p.id, p.display_name, p.mascot, r.created_at
  from friend_requests r join profiles p on p.id = r.from_id
  where r.to_id = public.require_profile() and r.status = 'pending'
  order by r.created_at desc
$$;

create or replace function public.my_friends()
returns table (user_id uuid, display_name text, mascot text, streak int, week_xp int, pair_streak int,
               me_today boolean, friend_today boolean)
language sql stable security definer set search_path = public, pg_temp as $$
  with me as (select public.require_profile() as id),
  t as (select public.today_brt() as d, public.week_start_brt(public.today_brt()) as ws)
  select p.id, p.display_name, p.mascot,
         public.visible_streak(p.streak_current, p.last_study_day),
         coalesce((select sum(s.xp) from study_sessions s where s.user_id = p.id and s.day between t.ws and t.ws + 6), 0)::int,
         public.pair_streak(me.id, p.id),
         exists (select 1 from study_sessions s where s.user_id = me.id and s.day = t.d and s.kind = 'lesson'),
         exists (select 1 from study_sessions s where s.user_id = p.id and s.day = t.d and s.kind = 'lesson')
  from me cross join t
  join friendships f on f.user_a = me.id or f.user_b = me.id
  join profiles p on p.id = case when f.user_a = me.id then f.user_b else f.user_a end
  order by 6 desc, p.display_name
$$;

-- Tudo que a tela social precisa, numa chamada só (uma ida ao servidor por sincronização).
create or replace function public.social_overview() returns json
language sql stable security definer set search_path = public, pg_temp as $$
  select json_build_object(
    'profile', public.my_profile(),
    'week_start', public.week_start_brt(public.today_brt()),
    'today', public.today_brt(),
    'global', coalesce((select json_agg(r) from public.weekly_ranking('global', 0) r), '[]'::json),
    'friends_ranking', coalesce((select json_agg(r) from public.weekly_ranking('friends', 0) r), '[]'::json),
    'friends', coalesce((select json_agg(f) from public.my_friends() f), '[]'::json),
    'requests', coalesce((select json_agg(q) from public.my_friend_requests() q), '[]'::json),
    'last_week', public.last_week_result()
  )
$$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Retrato do progresso (restaurar em outro aparelho). Controle de versão otimista:
--   p_expected_version = versão que o aparelho conhece (0 = nenhuma); -1 = forçar (o aluno escolheu "este aparelho").
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.push_snapshot(p_expected_version bigint, p_device_id text, p_payload jsonb) returns json
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare
  uid uuid := public.require_profile();
  cur progress_snapshots%rowtype;
begin
  select * into cur from progress_snapshots where user_id = uid for update;
  if not found then
    insert into progress_snapshots (user_id, version, device_id, payload) values (uid, 1, p_device_id, p_payload);
    return json_build_object('ok', true, 'version', 1);
  end if;
  if p_expected_version <> -1 and p_expected_version <> cur.version then
    return json_build_object('ok', false, 'reason', 'conflict', 'server_version', cur.version,
                             'server_device', cur.device_id, 'server_updated_at', cur.updated_at);
  end if;
  update progress_snapshots set version = cur.version + 1, device_id = p_device_id, payload = p_payload, updated_at = now()
  where user_id = uid;
  return json_build_object('ok', true, 'version', cur.version + 1);
end $$;

create or replace function public.pull_snapshot() returns json
language sql stable security definer set search_path = public, pg_temp as $$
  select json_build_object('version', s.version, 'device_id', s.device_id, 'updated_at', s.updated_at, 'payload', s.payload)
  from progress_snapshots s where s.user_id = public.require_profile()
$$;

-- ---------------------------------------------------------------------------------------------------------------------
-- LGPD: excluir a própria conta e todos os dados (perfil, sessões, amizades, pedidos e retrato saem em cascata)
-- ---------------------------------------------------------------------------------------------------------------------

create or replace function public.delete_my_account() returns text
language plpgsql volatile security definer set search_path = public, pg_temp as $$
declare uid uuid := public.require_uid();
begin
  delete from auth.users where id = uid;
  return 'deleted';
end $$;

-- ---------------------------------------------------------------------------------------------------------------------
-- Permissões das funções: só usuário logado chama as funções públicas; as internas ficam fechadas
-- ---------------------------------------------------------------------------------------------------------------------

revoke execute on all functions in schema public from public, anon;

grant execute on function
  public.my_profile(), public.upsert_my_profile(text, text), public.set_show_in_global(boolean),
  public.update_my_streak(int, int, date),
  public.submit_session(uuid, date, text, int, int, bigint, int, int, date),
  public.weekly_ranking(text, int), public.last_week_result(),
  public.request_friend(uuid), public.respond_friend_request(uuid, boolean), public.add_friend_by_code(text),
  public.remove_friend(uuid), public.my_friend_requests(), public.my_friends(), public.social_overview(),
  public.push_snapshot(bigint, text, jsonb), public.pull_snapshot(), public.delete_my_account()
to authenticated;

-- Funções internas: as públicas acima rodam como dono do banco (security definer) e as chamam por dentro, então elas
-- não precisam ficar expostas. Fechadas de propósito: pair_streak(a, b) e are_friends(a, b) revelariam a atividade e as
-- amizades de outras pessoas se pudessem ser chamadas pela API.
revoke execute on function
  public.require_uid(), public.require_profile(),
  public.are_friends(uuid, uuid), public.visible_streak(int, date), public.pair_streak(uuid, uuid),
  public.make_friends(uuid, uuid), public.new_friend_code()
from authenticated;

-- Datas de Brasília: inofensivas e úteis para o app conferir o "hoje" do servidor.
grant execute on function public.today_brt(), public.week_start_brt(date) to authenticated;

-- Leitura direta (RLS limita ao próprio registro). As demais tabelas só são lidas pelas funções.
grant select on public.profiles, public.progress_snapshots to authenticated;
revoke select on public.study_sessions, public.friend_requests, public.friendships from authenticated;

-- O 0002 (versões do app) libera latest_release() para quem não está logado. Se este arquivo for rodado de novo
-- depois do 0002, o "revoke ... from anon" acima tiraria essa permissão: devolve aqui.
do $$
begin
  if to_regprocedure('public.latest_release()') is not null then
    grant execute on function public.latest_release() to anon, authenticated;
  end if;
end $$;
