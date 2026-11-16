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

---

# Piloto por conceitos — lição u1l1 (16/11/2026)

- **O que mudou:** a lição "Modelo ER e normalização" passou para o formato por conceitos (DECISIONS.md, "Lições por
  conceitos"). Tem 7 conceitos e 46 exercícios no pacote: as 10 questões antigas, agora com conceito e nível, e 36
  exercícios novos de aprender e praticar. As 6 questões oficiais da lição (content/provas) ganharam o conceito.
- **Novos por nível:** 19 de aprender (certo/errado e escolha rápida) e 17 de praticar (associar, completar, ordenar,
  escolha rápida com cenário). Todos com `source: "ia"` e `reviewed: false`.
- **Adaptados das oficiais (13):** TER16 Q54 (2), Q55 (2), Q56 (3), Q58 (2); FGV21 Q41 (2); FGV18 Q57 (2). Eles
  ensinam o mesmo assunto por outro ângulo (a alternativa errada vira certo/errado, o cenário vira escolha rápida ou
  lacuna) e nunca caem na mesma sessão que a questão original.
- **Validador:** sem erros nem avisos. Escolha rápida com a resposta em A/B/C = 5/6/4; certo/errado com 8 Certo e 7
  Errado (contando a u1l1q08).
- **Cada conceito tem** ao menos 2 de aprender, 2 de praticar e 1 de prova, e ao menos um item de aprender e um de
  praticar que não é adaptado (para a sessão nunca ficar sem item quando a oficial está nela).

## Revisão (passada separada, objetivo "achar erros factuais e alternativas ambíguas")
| Item | Problema | Correção |
|---|---|---|
| u1l1q23 | "Na entidade fraca, a chave primária inclui…" mistura E-R e relacional: no E-R a entidade fraca não tem chave própria. | Enunciado passou a dizer "ao passar para o modelo relacional, a chave primária da tabela…". |
| u1l1q31 | Distratora "CPF e nome são sempre iguais" era absurda demais e não ensinava nada. | Trocada por "o CPF é calculado a partir do nome". |
| u1l1q41 (rascunho) | A primeira versão repetia o cenário PEDIDO_ITEM da u1l1q04 e entregava a resposta dela na mesma sessão. | Cenário trocado para ALOCACAO/PROJETO. |
| u1l1q46 (rascunho) | Mesmo problema com FUNCIONARIO/DEPARTAMENTO da u1l1q06. | Cenário trocado para PEDIDO/CLIENTE. |

## Pontos para o dono conferir
1. **u1l1q26 — entidade associativa.** Segue a definição dos livros brasileiros de modelagem (relacionamento redefinido
   como entidade para poder se relacionar com outras). Em E-R estendido há também a "agregação", que a questão não
   cita.
2. **u1l1q36 — RG → Nome.** Vale pela premissa do enunciado ("cada pessoa tem RG próprio"), como na questão da FGV. Na
   vida real o RG é estadual e pode se repetir entre estados.
3. **Repetição de propósito:** u1l1q15 (n:m) explica que o relacionamento vira tabela associativa, e a u1l1q02 (prova)
   cobra isso. É a ideia do formato (ensinar e depois cobrar). Se ficar fácil demais no celular, dá para tirar a
   explicação da q15.

---

# Piloto por conceitos, versão 2 — "aprender fazendo" (0.7.1, 16/11/2026)

**Por que mudou:** no teste do dono, os exercícios novos pareciam soltos e "chutáveis". Eles cobravam um conceito antes
de ensiná-lo, cada um tinha um cenário diferente e o sorteio pulava de assunto a cada exercício. Como erro custa vida,
chutar para aprender sai caro.

**O que mudou no conteúdo da u1l1** (50 exercícios no pacote, mais as 6 oficiais):
- Os 36 exercícios de ensino da versão 1 foram substituídos por **39 exercícios de ensino reescritos**. Cada um traz no
  enunciado a regra ou um exemplo resolvido, e a resposta sai do raciocínio. Exemplo: "o quarto 101 existe em vários
  hotéis, por isso Quarto é entidade fraca de Hotel. Agora: o departamento 01 − RH existe na Secretaria A e na B. Qual é a
  entidade fraca?".
- **Um cenário por conceito** (ALUNO; DEPARTAMENTO e FUNCIONARIO; ÓRGÃO e DEPARTAMENTO; banco da biblioteca; PESSOA;
  NOTA; PEDIDO e CLIENTE) e **passos** em todo exercício de ensino: 1 = exemplo resolvido, 2 = aplicar, 3 = variar
  (praticar). Os passos 2 e 3 têm variantes, para a revisão não repetir sempre o mesmo.
- u1l1q08 e u1l1q09 foram reescritas no mesmo formato (mesmo id).
- **3 questões de prova novas, estilo FCC** (u1l1q50 a q52), para os conceitos que só tinham as oficiais (entidade fraca,
  projeto conceitual/lógico/físico, dependência funcional). Sem elas, quando uma adaptada da oficial está no bloco, a
  sessão ficava sem prova daquele conceito.
- **Adaptadas das oficiais:** 8 (Q54, Q55, Q56 ×2, Q58, FGV21 Q41 ×2, FGV18 Q57). Continuam nunca caindo junto com a
  original.

**Validador:** sem erros nem avisos. Cada conceito tem os passos 1, 2 e 3, um exercício de aprender e um de praticar
que não são adaptados, e ao menos uma questão de prova. Escolha rápida com a resposta em A/B/C = 6/8/6; certo/errado com
3 Certo e 5 Errado (inclui a q08).

## Revisão (passada separada)
| Item | Problema | Correção |
|---|---|---|
| c2, certo/errado do n:m (rascunho) | Usava ALUNO e DISCIPLINA, o mesmo cenário da u1l1q02 (prova), e entregava a resposta dela. | Cenário trocado para AUTOR e LIVRO (tabela AUTORIA). |
| c2, associar cardinalidades (rascunho) | Mesmo problema (aluno e disciplinas). | Trocado para autores e livros. |
| c5, transitividade (rascunho) | Usava FUNCIONARIO/DEPARTAMENTO, o cenário da u1l1q06 (prova de dependência transitiva). | Trocado para PRODUTO/FORNECEDOR. |
| c3, exemplo de entidade fraca | Quarto/Hotel é o exemplo resolvido; a questão de prova nova não podia repetir. | A prova nova usa CONTRATO/PARCELA. |

## Pontos para o dono conferir
1. **Exemplos resolvidos deixam o passo 1 fácil, de propósito.** O objetivo é você chegar à resposta lendo, não
   chutando. Se ficar fácil demais, dá para tirar o "Exemplo:" do passo 2.
2. **u1l1q52 (dependência que NÃO se verifica).** Considera que a placa e o Renavam identificam o veículo, como diz o
   enunciado.
