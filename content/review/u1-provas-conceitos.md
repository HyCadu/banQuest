# U1: conceito de cada questão oficial das lições 2 a 5 (0.8.2)

Revisão técnica do vínculo entre as questões de provas anteriores e os conceitos do pacote v4. O texto, o gabarito e a
explicação das questões não mudaram; só entrou o campo `concept` em `content/provas/*.json`. Critério: o conceito
que o aluno precisa ter visto para resolver a questão.

| Questão | Lição | Conceito | Assunto da questão |
|---|---|---|---|
| fcc-dperr-2015-q45 | u1l2 | u1l2c1 Comandos e estrutura | ALTER TABLE para aumentar uma coluna |
| fcc-teresina-2016-q60 | u1l2 | u1l2c1 Comandos e estrutura | classificar COMMIT, CREATE, ROLLBACK, DELETE, REVOKE, UPDATE |
| fgv-banestes-2021-q49 | u1l2 | u1l2c1 Comandos e estrutura | chaves estrangeiras na definição da tabela |
| fgv-banestes-2018-q58 | u1l2 | u1l2c2 Filtros, grupos e junções | LEFT/RIGHT JOIN com UNION e células nulas |
| fgv-banestes-2021-q51 | u1l2 | u1l2c2 Filtros, grupos e junções | `<> NULL`, `NOT (E IS NULL)`, `NOT (E = NULL)` |
| fgv-banestes-2018-q56 | u1l2 | u1l2c5 Isolamento e concorrência | compatibilidade de locks no Oracle |
| fgv-banestes-2021-q37 | u1l2 | u1l2c5 Isolamento e concorrência | isolamento em operações concorrentes: lock |
| fgv-banestes-2018-q65 | u1l3 | u1l3c1 Subconsultas e correlação | DELETE com EXISTS correlacionado |
| fgv-banestes-2018-q66 | u1l3 | u1l3c1 Subconsultas e correlação | UPDATE com subconsulta escalar (MAX) |
| fgv-banestes-2021-q50 | u1l3 | u1l3c1 Subconsultas e correlação | DELETE com MIN e contagem correlacionada |
| fgv-banestes-2018-q64 | u1l3 | u1l3c2 Ausência e valores nulos | INSERT … WHERE NOT EXISTS |
| fgv-banestes-2021-q48 | u1l3 | u1l3c2 Ausência e valores nulos | NOT EXISTS com `E IS NULL` |
| fgv-banestes-2021-q47 | u1l3 | u1l3c2 Ausência e valores nulos | NULL tratado como desconhecido no filtro `E > 19` |
| fcc-dperr-2015-q46 | u1l4 | u1l4c3 MVCC e manutenção | pg_dump para cópia consistente no PostgreSQL |
| fcc-teresina-2016-q59 | u1l4 | u1l4c3 MVCC e manutenção | tuning do PostgreSQL com índices |
| fgv-banestes-2018-q55 | u1l4 | u1l4c5 Oracle multitenant e H2 | Oracle: index organized table |
| fgv-banestes-2018-q61 | u1l4 | u1l4c5 Oracle multitenant e H2 | tabelas temporárias no Oracle e no DB2 |
| fgv-banestes-2018-q62 | u1l4 | u1l4c5 Oracle multitenant e H2 | Oracle: ROWNUM com ORDER BY |
| fgv-banestes-2018-q63 | u1l4 | u1l4c5 Oracle multitenant e H2 | Oracle: produto cartesiano com DUAL |
| fcc-teresina-2016-q57 | u1l5 | u1l5c1 Fatos, dimensões e granularidade | star schema: dimensão descritiva, fato quantitativa |

**Pontos para o dono:** o conceito u1l4c5 ficou com 4 questões de recursos do Oracle e do DB2 que vão além de
multitenant. Se a lição 4 ganhar um conceito "Recursos do Oracle", essas quatro devem migrar para ele. u1l3c2 recebeu
q47, que é sobre produto cartesiano e agrupamento; o detalhe que decide a resposta é o NULL tratado como desconhecido.

**Validação:** `tools/validate_provas.py` (218 questões, OK), `tools/build_provas.py` (123 questões no pacote do app) e
`tools/validate_content.py` (170 questões, 0 erros, 1 aviso: `u1l2q09` é certo/errado no nível prova).
