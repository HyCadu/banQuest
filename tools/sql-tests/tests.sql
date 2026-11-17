-- Testes do esquema social (rodados por tools/sql-tests/run.sh num Postgres local que simula o Supabase).
\set ON_ERROR_STOP on
\set ana   'a0000000-0000-0000-0000-000000000001'
\set bia   'a0000000-0000-0000-0000-000000000002'
\set caio  'a0000000-0000-0000-0000-000000000003'
\set davi  'a0000000-0000-0000-0000-000000000004'
\set eva   'a0000000-0000-0000-0000-000000000005'
\set fabio 'a0000000-0000-0000-0000-000000000006'
\set lia   'a0000000-0000-0000-0000-000000000007'

-- ---------- utilitários de teste (fora do esquema do app)
create schema tst;
grant usage on schema tst to anon, authenticated;
create function tst.ok(cond boolean, label text) returns void language plpgsql as $$
begin
  if cond is distinct from true then raise exception 'FALHOU: %', label; end if;
  raise notice 'ok: %', label;
end $$;
create function tst.err(q text, state text, label text) returns void language plpgsql as $$
begin
  begin
    execute q;
  exception when others then
    if sqlstate = state then raise notice 'ok: %', label; return; end if;
    raise exception 'FALHOU: % (esperava erro %, veio % %)', label, state, sqlstate, sqlerrm;
  end;
  raise exception 'FALHOU: % (deveria ter dado erro %)', label, state;
end $$;
grant execute on all functions in schema tst to anon, authenticated;
-- sessão de estudo válida (ajuda a montar cenários)
create function tst.study(d date, kind text default 'lesson', xp int default 100) returns json language sql as $$
  select public.submit_session(gen_random_uuid(), d, kind, xp, 8, 8 * 30000, 1, 1, d)
$$;
grant execute on function tst.study(date, text, int) to authenticated;

insert into auth.users (id, email) values
  (:'ana', 'ana@exemplo.com'), (:'bia', 'bia@exemplo.com'), (:'caio', 'caio@exemplo.com'), (:'davi', 'davi@exemplo.com'),
  (:'eva', 'eva@exemplo.com'), (:'fabio', 'fabio@exemplo.com'), (:'lia', 'lia@exemplo.com');

-- =====================================================================================================================
-- 1. Segurança básica
-- =====================================================================================================================
set role anon;
select set_config('request.jwt.claim.sub', '', false);
select tst.err('select public.my_profile()', '42501', 'anônimo não chama funções');
select tst.err('select * from public.profiles', '42501', 'anônimo não lê tabelas');

set role authenticated;
select set_config('request.jwt.claim.sub', :'lia', false);
select tst.ok(public.my_profile() is null, 'sem perfil, my_profile é nulo');
select tst.err($$select tst.study(public.today_brt())$$, 'P0002', 'sem perfil não envia sessão');
select tst.err('select public.social_overview()', 'P0002', 'sem perfil não vê ranking');

-- perfis
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.err($$select public.upsert_my_profile('A', 'Guaiamum')$$, '23514', 'nome curto demais é recusado');
select tst.err($$select public.upsert_my_profile('Ana', 'Dragao')$$, '23514', 'mascote inválido é recusado');
select tst.ok((public.upsert_my_profile('  Ana  ', 'Tartaruga')->>'display_name') = 'Ana', 'cria perfil e apara espaços');
select tst.ok((public.my_profile()->>'friend_code') ~ '^TRI-[A-HJ-NP-Z2-9]{5}$', 'código de amigo no formato TRI-XXXXX');
select tst.ok((public.upsert_my_profile('Ana Clara', 'Preguica')->>'mascot') = 'Preguica', 'atualiza nome e mascote');
select set_config('request.jwt.claim.sub', :'bia', false);   select public.upsert_my_profile('Bia', 'Guaiamum');
select set_config('request.jwt.claim.sub', :'caio', false);  select public.upsert_my_profile('Caio', 'Tamandua');
select public.set_show_in_global(false);
select set_config('request.jwt.claim.sub', :'davi', false);  select public.upsert_my_profile('Davi', 'Guaiamum');
select set_config('request.jwt.claim.sub', :'eva', false);   select public.upsert_my_profile('Eva', 'Tartaruga');
select set_config('request.jwt.claim.sub', :'fabio', false); select public.upsert_my_profile('Fábio', 'Preguica');

-- escrita direta nas tabelas é proibida; leitura direta só do próprio registro
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.err($$insert into public.profiles (id, display_name, friend_code) values ('a0000000-0000-0000-0000-000000000009', 'X', 'TRI-XXXXX')$$, '42501', 'não insere perfil direto');
select tst.err($$update public.profiles set display_name = 'Hacker'$$, '42501', 'não altera perfil direto');
select tst.err($$insert into public.study_sessions values (gen_random_uuid(), 'a0000000-0000-0000-0000-000000000001', current_date, 'lesson', 150, 8, 99999)$$, '42501', 'não insere sessão direto');
select tst.err('select * from public.study_sessions', '42501', 'não lê sessões dos outros direto');
select tst.err('select * from public.friendships', '42501', 'não lê amizades direto');
select tst.ok((select count(*) from public.profiles) = 1, 'lê só o próprio perfil');
select tst.err($$select public.pair_streak('a0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003')$$, '42501', 'função interna pair_streak fechada');
select tst.err($$select public.are_friends('a0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003')$$, '42501', 'função interna are_friends fechada');
select tst.err($$select public.make_friends('a0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003')$$, '42501', 'função interna make_friends fechada');

-- =====================================================================================================================
-- 2. Sessões e limites anti-trapaça
-- =====================================================================================================================
select tst.ok((public.submit_session('b0000000-0000-0000-0000-000000000001', public.today_brt(), 'lesson', 210, 12, 24000, 1, 1, public.today_brt())->>'ok')::boolean, 'lição com XP máximo (210: 12 exercícios por conceitos) aceita');
select tst.ok((public.submit_session('b0000000-0000-0000-0000-000000000001', public.today_brt(), 'lesson', 210, 12, 24000, 1, 1, public.today_brt())->>'duplicate')::boolean, 'reenviar a mesma sessão não duplica');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt(), 'lesson', 211, 12, 24000, 1, 1, public.today_brt())->>'reason' = 'xp_limit', 'lição acima de 210 XP recusada');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt(), 'practice', 121, 8, 16000, 1, 1, public.today_brt())->>'reason' = 'xp_limit', 'treino acima de 120 XP recusado');
select tst.ok((public.submit_session(gen_random_uuid(), public.today_brt(), 'practice', 120, 8, 16000, 1, 1, public.today_brt())->>'ok')::boolean, 'treino com 120 XP aceito');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt() + 1, 'lesson', 50, 8, 16000, 1, 1, public.today_brt())->>'reason' = 'invalid_day', 'dia no futuro recusado');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt() - 8, 'lesson', 50, 8, 16000, 1, 1, public.today_brt())->>'reason' = 'invalid_day', 'sessão de mais de 7 dias recusada');
select tst.ok((public.submit_session(gen_random_uuid(), public.today_brt() - 7, 'lesson', 40, 8, 16000, 1, 1, public.today_brt())->>'ok')::boolean, 'sessão de 7 dias atrás aceita (semana passada)');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt(), 'lesson', 50, 8, 15999, 1, 1, public.today_brt())->>'reason' = 'too_fast', 'menos de 2 s por resposta recusado');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt(), 'boss', 50, 8, 16000, 1, 1, public.today_brt())->>'reason' = 'invalid_kind', 'tipo inválido recusado');
select tst.ok(public.submit_session(gen_random_uuid(), public.today_brt(), 'lesson', 50, 0, 16000, 1, 1, public.today_brt())->>'reason' = 'invalid_answers', 'zero respostas recusado');

select set_config('request.jwt.claim.sub', :'davi', false);
select count(*) from (select tst.study(public.today_brt(), 'practice', 1) from generate_series(1, 40)) x;
select tst.ok(tst.study(public.today_brt(), 'practice', 1)->>'reason' = 'daily_limit', 'mais de 40 sessões no dia recusado');

-- ofensiva individual
select set_config('request.jwt.claim.sub', :'bia', false);
select tst.ok((public.update_my_streak(5, 5, public.today_brt() - 1)->>'ok')::boolean, 'atualiza ofensiva');
select tst.ok((public.my_profile()->>'streak')::int = 5, 'ofensiva até ontem ainda aparece');
select public.update_my_streak(5, 5, public.today_brt() - 2);
select tst.ok((public.my_profile()->>'streak')::int = 0, 'ofensiva parada há 2 dias aparece zerada');
select tst.ok(public.update_my_streak(6, 5, public.today_brt())->>'reason' = 'invalid_streak', 'atual maior que o recorde recusado');
select tst.ok(public.update_my_streak(1, 1, public.today_brt() + 1)->>'reason' = 'invalid_day', 'último dia no futuro recusado');
select public.update_my_streak(9, 9, public.today_brt());
select tst.study(public.today_brt(), 'lesson', 80);

-- =====================================================================================================================
-- 3. Ranking da semana
-- =====================================================================================================================
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok((select xp from public.weekly_ranking('global') where friend_state = 'me') = 330,
              'XP da semana soma as sessões aceitas desta semana (210 + 120; a de 7 dias atrás é da semana passada)');
select tst.ok((select pos from public.weekly_ranking('global') where friend_state = 'me') = 1, 'Ana em 1º no geral');
select tst.ok(not exists (select 1 from public.weekly_ranking('global') where display_name = 'Caio'), 'quem está oculto não aparece no geral');
select tst.ok((select count(*) from public.weekly_ranking('friends')) = 1, 'sem amigos, o ranking de amigos só tem você');
select tst.ok((select friend_state from public.weekly_ranking('global') where display_name = 'Bia') = 'none', 'Bia ainda não é amiga');
select tst.ok(public.last_week_result() ->> 'pos' is not null, 'resultado da semana passada disponível');
select set_config('request.jwt.claim.sub', :'caio', false);
select tst.ok(exists (select 1 from public.weekly_ranking('global') where friend_state = 'me'), 'quem está oculto se vê no próprio ranking');

-- =====================================================================================================================
-- 4. Amizades
-- =====================================================================================================================
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok(public.request_friend(:'bia') = 'requested', 'pedido pelo ranking enviado');
select tst.ok((select friend_state from public.weekly_ranking('global') where display_name = 'Bia') = 'outgoing', 'aparece como pedido enviado');
select tst.ok(public.request_friend(:'ana') = 'self', 'não pede amizade a si mesmo');
select tst.ok(public.request_friend(:'caio') = 'not_found', 'não pede amizade a quem está oculto (só por código)');

select set_config('request.jwt.claim.sub', :'bia', false);
select tst.ok((select count(*) from public.my_friend_requests()) = 1, 'Bia vê o pedido recebido');
select tst.ok((select friend_state from public.weekly_ranking('global') where display_name = 'Ana Clara') = 'incoming', 'aparece como pedido recebido');
select tst.ok(public.respond_friend_request((select request_id from public.my_friend_requests() limit 1), true) = 'accepted', 'Bia aceita');
select tst.ok((select count(*) from public.my_friend_requests()) = 0, 'pedido some depois de aceito');

select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok((select friend_state from public.weekly_ranking('global') where display_name = 'Bia') = 'friend', 'agora são amigas');
select tst.ok((select count(*) from public.weekly_ranking('friends')) = 2, 'ranking de amigos tem as duas');
select tst.ok(public.request_friend(:'bia') = 'already_friends', 'pedir de novo avisa que já são amigas');

-- código de convite (vale na hora, inclusive para quem está oculto)
select set_config('request.jwt.claim.sub', :'caio', false);
select public.my_profile()->>'friend_code' as caio_code \gset
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok(public.add_friend_by_code(lower(:'caio_code'))->>'result' = 'added', 'adiciona por código (aceita minúsculas)');
select tst.ok(public.add_friend_by_code(:'caio_code')->>'result' = 'already_friends', 'código repetido avisa que já são amigos');
select tst.ok(public.add_friend_by_code('TRI-ZZZZZ')->>'result' = 'not_found', 'código inexistente');
select tst.ok(public.add_friend_by_code(public.my_profile()->>'friend_code')->>'result' = 'self', 'o próprio código');
select tst.ok(exists (select 1 from public.weekly_ranking('global') where display_name = 'Caio' and friend_state = 'friend'), 'amigo oculto aparece para os amigos');

-- pedidos cruzados viram amizade; recusar não cria amizade
select set_config('request.jwt.claim.sub', :'davi', false);
select tst.ok(public.request_friend(:'ana') = 'requested', 'Davi pede Ana');
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok(public.request_friend(:'davi') = 'accepted', 'pedido cruzado vira amizade na hora');
select set_config('request.jwt.claim.sub', :'bia', false);
select public.request_friend(:'davi');
select set_config('request.jwt.claim.sub', :'davi', false);
select tst.ok(public.respond_friend_request((select request_id from public.my_friend_requests() limit 1), false) = 'declined', 'Davi recusa Bia');
select tst.ok(not exists (select 1 from public.my_friends() where display_name = 'Bia'), 'recusar não cria amizade');
select tst.ok(public.respond_friend_request(gen_random_uuid(), true) = 'not_found', 'responder pedido inexistente');

select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok(public.remove_friend(:'caio') = 'removed', 'desfaz amizade');
select tst.ok(not exists (select 1 from public.my_friends() where display_name = 'Caio'), 'Caio saiu da lista de amigos');
select tst.ok(not exists (select 1 from public.weekly_ranking('global') where display_name = 'Caio'), 'Caio oculto some do geral de novo');

-- =====================================================================================================================
-- 5. Ofensiva em dupla
-- =====================================================================================================================
select set_config('request.jwt.claim.sub', :'eva', false);
select tst.study(public.today_brt() - 1); select tst.study(public.today_brt() - 2);
select set_config('request.jwt.claim.sub', :'fabio', false);
select tst.study(public.today_brt()); select tst.study(public.today_brt() - 1); select tst.study(public.today_brt() - 2);
select tst.study(public.today_brt() - 3);
select public.my_profile()->>'friend_code' as fabio_code \gset
select set_config('request.jwt.claim.sub', :'eva', false);
select public.add_friend_by_code(:'fabio_code');
select tst.ok((select pair_streak from public.my_friends() where display_name = 'Fábio') = 2, 'dupla: 2 dias (hoje ainda falta a Eva)');
select tst.ok((select me_today = false and friend_today = true from public.my_friends() where display_name = 'Fábio'), 'mostra quem já estudou hoje');
select tst.study(public.today_brt() - 3, 'practice', 50);
select tst.ok((select pair_streak from public.my_friends() where display_name = 'Fábio') = 2, 'treino não conta para a ofensiva em dupla');
select tst.study(public.today_brt());
select tst.ok((select pair_streak from public.my_friends() where display_name = 'Fábio') = 3, 'Eva estudou hoje: dupla vai a 3');
select set_config('request.jwt.claim.sub', :'fabio', false);
select tst.ok((select pair_streak from public.my_friends() where display_name = 'Eva') = 3, 'a ofensiva em dupla é a mesma dos dois lados');

-- visão geral numa chamada
select tst.ok((select json_typeof(public.social_overview()->'global') = 'array' and json_typeof(public.social_overview()->'friends') = 'array'
               and public.social_overview()->'profile'->>'display_name' = 'Fábio'), 'social_overview traz perfil, rankings e amigos');
select tst.ok(public.social_overview()::text not like '%@exemplo.com%', 'e-mails nunca aparecem para os outros');

-- =====================================================================================================================
-- 6. Retrato do progresso
-- =====================================================================================================================
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok(public.pull_snapshot() is null, 'sem retrato ainda');
select tst.ok((public.push_snapshot(0, 'celular-a', '{"xp": 1}')->>'version')::int = 1, 'primeiro envio vira versão 1');
select tst.ok((public.push_snapshot(1, 'celular-a', '{"xp": 2}')->>'version')::int = 2, 'envio com a versão certa avança');
select tst.ok(public.push_snapshot(1, 'celular-b', '{"xp": 9}')->>'reason' = 'conflict', 'versão desatualizada vira conflito');
select tst.ok((public.push_snapshot(1, 'celular-b', '{"xp": 9}')->>'server_device') = 'celular-a', 'conflito informa o outro aparelho');
select tst.ok((public.push_snapshot(-1, 'celular-b', '{"xp": 9}')->>'version')::int = 3, 'forçar (este aparelho) sobrescreve');
select tst.ok((public.pull_snapshot()->'payload'->>'xp')::int = 9 and (public.pull_snapshot()->>'version')::int = 3, 'baixa o retrato atual');
select set_config('request.jwt.claim.sub', :'bia', false);
select tst.ok((select count(*) from public.progress_snapshots) = 0, 'ninguém lê o retrato dos outros');

-- =====================================================================================================================
-- 7. Excluir conta (LGPD)
-- =====================================================================================================================
select set_config('request.jwt.claim.sub', :'davi', false);
select tst.ok(public.delete_my_account() = 'deleted', 'exclui a própria conta');
reset role;
select tst.ok(not exists (select 1 from auth.users where id = :'davi'), 'usuário apagado do auth');
select tst.ok(not exists (select 1 from public.profiles where id = :'davi'), 'perfil apagado');
select tst.ok(not exists (select 1 from public.study_sessions where user_id = :'davi'), 'sessões apagadas');
select tst.ok(not exists (select 1 from public.friendships where user_a = :'davi' or user_b = :'davi'), 'amizades apagadas');
select tst.ok(not exists (select 1 from public.friend_requests where from_id = :'davi' or to_id = :'davi'), 'pedidos apagados');

-- =====================================================================================================================
-- 8. Versões do app (0002_releases.sql)
-- =====================================================================================================================
set role anon;
select set_config('request.jwt.claim.sub', '', false);
select tst.ok(public.latest_release() is null, 'sem versão publicada: latest_release é nulo, e anônimo pode chamar');
reset role;
insert into public.app_releases (version_code, version_name, apk_url, notes)
values (8, '0.5.0', 'https://exemplo.supabase.co/storage/v1/object/public/apk/banquest-0.5.0.apk', 'Primeira versão BanQuest');
insert into public.app_releases (version_code, version_name, apk_url, notes, min_version_code)
values (9, '0.5.1', 'https://exemplo.supabase.co/storage/v1/object/public/apk/banquest-0.5.1.apk', 'Correções', 8);
select tst.err($$insert into public.app_releases (version_code, version_name, apk_url) values (10, '0.6', 'http://inseguro/x.apk')$$, '23514', 'link do APK precisa ser https');
select tst.err($$insert into public.app_releases (version_code, version_name, apk_url, min_version_code) values (10, '0.6', 'https://x/a.apk', 11)$$, '23514', 'versão mínima não passa da própria versão');
set role anon;
select tst.ok((public.latest_release()->>'version_code')::int = 9, 'anônimo vê a versão mais nova');
select tst.ok((public.latest_release()->>'min_version_code')::int = 8 and public.latest_release()->>'notes' = 'Correções', 'versão mínima e notas vêm junto');
select tst.err('select * from public.app_releases', '42501', 'anônimo não lê a tabela de versões');
select tst.err($$insert into public.app_releases (version_code, version_name, apk_url) values (99, 'x', 'https://x/a.apk')$$, '42501', 'anônimo não publica versão');
set role authenticated;
select set_config('request.jwt.claim.sub', :'ana', false);
select tst.ok(public.latest_release()->>'version_name' = '0.5.1', 'logado também vê a versão mais nova');
select tst.err($$insert into public.app_releases (version_code, version_name, apk_url) values (99, 'x', 'https://x/a.apk')$$, '42501', 'logado não publica versão');
reset role;

