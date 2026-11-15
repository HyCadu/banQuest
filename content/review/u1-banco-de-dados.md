# Revisão técnica — Unidade 1: Banco de dados (`u1-banco-de-dados`)

- **Pacote:** 5 lições × 10 questões = 50 questões. Todas com `source: "ia"` e `reviewed: false`.
- **Quem revisou:** passada separada da geração, feita pela própria IA com o objetivo "achar erros factuais e alternativas ambíguas". **Não substitui a revisão do dono.** O app mostra o selo "não revisada" em todas até que o dono as marque como revisadas.
- **Validador:** `python3 tools/validate_content.py app/src/main/assets/content/` — sem erros.

## Resultado do validador (após os ajustes)
- Estrutura, ids, 5 alternativas, índice da resposta, explicações: ok.
- Posição da resposta correta nas 44 questões de 5 alternativas: A=9, B=9, C=9, D=9, E=8.
- Mistura: 40% de casos práticos, 5 questões de exceção (NÃO é / EXCETO), demais literais e conceituais. Tipos: 22 `mcq5`, 19 `case`, 2 `code_output`, 1 `fill_blank`, 2 `truefalse`, 3 `match`, 1 `order_code`. Estilos: 20 `caso_pratico` (40%), 15 `literal`, 10 `conceitual`, 5 `excecao`.
- Viés de tamanho: a primeira versão tinha a alternativa correta como a mais longa em 52% das questões; foi corrigido, deixando abaixo de 35%.

## Problemas achados na revisão e corrigidos
| Questão | Problema | Correção |
|---|---|---|
| u1l1q05 (3FN) | Duas distratoras ("atributos atômicos" e "sem dependências parciais") também são verdadeiras para uma tabela em 3FN; apenas não a definem. Ambiguidade real. | Enunciado reescrito: "o que caracteriza a 3FN, em relação à 2FN". Cada alternativa agora descreve a exigência de uma forma normal diferente. |
| u1l2q05 | `BEGIN;` e `SAVEPOINT` são sintaxe de PostgreSQL e o enunciado não dizia o SGBD. | Enunciado passou a dizer "Em um banco PostgreSQL". |
| u1l3q05 | `WITH RECURSIVE` é dialeto do PostgreSQL (no Oracle é `WITH` sem `RECURSIVE`). | Alternativa correta reescrita sem a palavra-chave. |

## Pontos que o dono deve conferir com mais atenção
1. **u1l4q06 — MERGE no PostgreSQL 15.** A resposta (versão 15) é afirmação sobre versão. Confirmo com razoável segurança (o MERGE foi uma das novidades do PostgreSQL 15), mas o edital cita PostgreSQL 14/15, então vale conferir nas notas de versão.
2. **u1l4q08 — H2 "em um único arquivo JAR".** É a forma de distribuição usual do H2, mas pode variar entre versões; a alternativa correta (a EXCETO) não depende disso.
3. **u1l2q06 — níveis de isolamento.** A resposta segue o **padrão SQL**. Alguns SGBDs se comportam diferente (por exemplo, o REPEATABLE READ do PostgreSQL também impede leituras fantasmas, e o READ UNCOMMITTED dele se comporta como READ COMMITTED). O enunciado diz "segundo o padrão SQL" por isso.
4. **u1l1q05 e u1l1q10 — formas normais.** Definições clássicas de Date/Elmasri; bancas às vezes usam definições ligeiramente diferentes de 3FN (com "atributos primos"). Se a FCC usar outra formulação, ajustar o estilo.
5. **Estilo FCC:** estas questões não foram calibradas com questões reais da banca (a pasta `content/inbox/` está vazia). Cole algumas no `content/inbox/` para calibrar o enunciado e a dificuldade.

## Cobertura frente ao Anexo IV (cargo 04)
Modelo ER e normalização · SQL (DML/DDL/DCL/TCL) e transações (ACID, isolamento, SAVEPOINT) · subconsultas, CTEs (inclusive recursiva) e funções de janela · PL/SQL, PL/pgSQL, PostgreSQL (MVCC, VACUUM, MERGE), Oracle 19c (multitenant), H2 · modelagem dimensional, DW, data mart, OLAP, data lake, NoSQL, ELT e governança de dados.

Fora do escopo desta unidade, de propósito: tuning avançado, particionamento, replicação e backup (podem entrar num lote extra de questões se o dono pedir).
