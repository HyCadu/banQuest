#!/usr/bin/env bash
# Testa supabase/migrations num Postgres local descartável (sem Supabase, sem rede).
# Uso: tools/sql-tests/run.sh      (precisa do servidor Postgres instalado: initdb, pg_ctl, psql)
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
PGBIN=${PGBIN:-$(ls -d /usr/lib/postgresql/*/bin 2>/dev/null | sort -V | tail -1)}
WORK=$(mktemp -d)
as_pg() { if [ "$(id -u)" = 0 ]; then runuser -u postgres -- "$@"; else "$@"; fi; }
[ "$(id -u)" = 0 ] && chown postgres "$WORK"
as_pg "$PGBIN/initdb" -D "$WORK/data" -A trust -U postgres >/dev/null
as_pg "$PGBIN/pg_ctl" -D "$WORK/data" -o "-k $WORK -c listen_addresses='' -c timezone=UTC" -l "$WORK/log" -w start >/dev/null
trap 'as_pg "$PGBIN/pg_ctl" -D "$WORK/data" stop -m fast >/dev/null 2>&1; rm -rf "$WORK"' EXIT
PSQL=("$PGBIN/psql" -h "$WORK" -U postgres -v ON_ERROR_STOP=1 -q -X)
"${PSQL[@]}" -d postgres -c "create database trilha"
"${PSQL[@]}" -d trilha -f "$HERE/supabase_stub.sql"
export PGOPTIONS="-c client_min_messages=warning"
for f in "$ROOT"/supabase/migrations/*.sql; do
  "${PSQL[@]}" -d trilha -f "$f"
  "${PSQL[@]}" -d trilha -f "$f"   # de novo: o SQL precisa poder ser reaplicado
done
# banco criado antes da 0.9.1/0.10.0 (lição até 150 XP, sem 'exam', até 30 respostas): rodar o 0001 de novo tem de
# refazer as restrições da tabela
"${PSQL[@]}" -d trilha -c "alter table public.study_sessions drop constraint study_sessions_xp_check, add constraint study_sessions_xp_check check (xp between 0 and 150), drop constraint study_sessions_kind_check, add constraint study_sessions_kind_check check (kind in ('lesson', 'practice')), drop constraint study_sessions_answers_check, add constraint study_sessions_answers_check check (answers between 1 and 30)"
# o dono pode rodar o 0001 de novo depois do 0002: isso não pode tirar a permissão do latest_release()
"${PSQL[@]}" -d trilha -f "$ROOT/supabase/migrations/0001_social.sql"
export PGOPTIONS="-c client_min_messages=notice"
"${PSQL[@]}" -d trilha -f "$HERE/tests.sql" -o /dev/null 2>&1 | sed -E 's/^psql:[^ ]* (NOTICE|ERROR):  //'
[ "${PIPESTATUS[0]}" = 0 ] || { echo "SQL: FALHOU"; exit 1; }
echo "SQL: todos os testes passaram"
