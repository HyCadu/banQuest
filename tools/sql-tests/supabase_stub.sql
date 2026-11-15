-- Simula o mínimo do Supabase num Postgres comum, para testar supabase/migrations sem nuvem.
create role anon nologin;
create role authenticated nologin;
create role service_role nologin;
create schema auth;
create table auth.users (id uuid primary key, email text);
-- Igual ao Supabase: o id do usuário vem do JWT (aqui, de uma configuração da sessão).
create or replace function auth.uid() returns uuid language sql stable as $$
  select nullif(coalesce(current_setting('request.jwt.claim.sub', true),
                         nullif(current_setting('request.jwt.claims', true), '')::jsonb ->> 'sub'), '')::uuid
$$;
grant usage on schema auth to anon, authenticated;
grant execute on function auth.uid() to anon, authenticated;
grant usage on schema public to anon, authenticated;
-- O Supabase dá permissões padrão amplas em "public"; simulamos para provar que o SQL do app as fecha.
alter default privileges in schema public grant all on tables to anon, authenticated;
alter default privileges in schema public grant execute on functions to anon, authenticated;
