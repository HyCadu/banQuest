# Revisão técnica — G2: Raciocínio lógico e estatística (`u12`, pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Os 44 exercícios estão com
`source: "ia"` e `reviewed: false`. O pacote é gerado por `tools/gen_u16.py u12` a partir de `content/source/u12_*.py`
(em cada exercício a alternativa certa vem primeiro e o gerador espalha a posição; `pos` fixa a posição quando as
alternativas têm ordem natural, como números e dias da semana).

## Escopo

Por enquanto só a lição 1. As lições 2 a 4 (Aritmética, Proporção e porcentagem, Estatística e contagem) entram em PRs
seguintes.

| Lição | Conceitos | Exercícios | Oficiais ligadas |
|---|---|---|---|
| u12l1 Lógica de relações | conectivos e tabela-verdade · negação · equivalências do condicional · todo, algum e nenhum · dedução · conjuntos · associações e posições | 44 (14 aprender, 16 praticar, 14 prova) | 26 |

**Material de referência.** A ordem dos conceitos e os métodos de resolução (tabela de eliminação nas associações, leis
de De Morgan, negação e equivalências do condicional, classificação em tautologia, contradição e contingência) seguem as
aulas de lógica da playlist "Bônus: Editora Opção", do canal Apostilas Opção no YouTube (vídeos 42 a 53). Os vídeos
serviram só de roteiro: nenhum texto, exemplo ou número foi copiado. Os cenários são de agência e de banco, escritos
para o app.

## Cobertura do edital

Tópicos do edital (Raciocínio Lógico e Estatística) tratados na l1:

| Tópico do edital | Conceito |
|---|---|
| Estrutura lógica de relações arbitrárias entre pessoas, lugares, objetos ou eventos; deduzir informações | c7 associações e posições; c5 dedução |
| Raciocínio verbal, sequencial, orientação espacial e temporal | c7 (fila, mesa, dias da semana) |
| Processo lógico que, a partir de hipóteses, conduz de forma válida a conclusões | c1 a c5 |
| Formação de conceitos, discriminação de elementos | c4 quantificadores; c6 conjuntos |

Lógica proposicional (conectivos, negação, equivalência) não aparece com esse nome no edital, mas é o que as provas
oficiais do Banestes mais cobram nesta parte. Das 26 oficiais da lição, 11 são de negação, equivalência ou dedução com
condicionais.

## Oficiais ligadas aos conceitos

| Conceito | Oficiais |
|---|---|
| c1 Conectivos e tabela-verdade | FGV 2018 Q31 |
| c2 Negação | FGV 2018 Q32, Q48; FGV 2018 TB Q43 |
| c3 Equivalências do condicional | FGV 2018 Q33; FGV 2021 Q27 |
| c4 Todo, algum e nenhum | FGV 2018 Q34; FGV 2018 TB Q42, Q46; Access 2024 Q27, Q29 |
| c5 Dedução | FGV 2018 Q47; FGV 2021 Q28, Q29, Q31; FGV 2023 TB Q21; Access 2024 Q18, Q25 |
| c6 Conjuntos e diagramas | FGV 2023 TB Q17, Q24; Access 2024 Q28 |
| c7 Associações e posições | FGV 2018 Q40, Q41; FGV 2018 TB Q44, Q47, Q49 |

Os enunciados de ensino usam cenários diferentes dos das oficiais (ex.: a oficial de negação do condicional usa
Emília e moqueca; o exercício usa senha e bloqueio de acesso). Nenhum exercício é `adaptedFrom`.

## Questões de prova resolvidas

Todas as contas e tabelas-verdade foram conferidas por script, além da resolução à mão:

- q06 tautologia (definição). q07 com p = V, q = F, r = V: só (p ou q) → r é V.
- q12 a única negação errada é a do condicional que nega as duas partes. q13 condicional falso: meta batida e sem bônus.
- q18 contrapositiva da norma de R$ 10 mil. q19 as quatro outras são equivalentes a "não pagou → aviso" (tabela
  conferida); a inversa não é.
- q24 "algum" é negado por "nenhum". q25 caixas dentro de concursados, estagiários fora: nenhum caixa é estagiário.
- q31 cadeia de modus tollens a partir de "Rui não é contador". q32 53 clientes em 5 caixas: 5 × 10 = 50 < 53, ao menos
  um atendeu 11 ou mais. Contraexemplos das erradas: 11, 11, 11, 10, 10 derruba "12 ou mais"; 13, 10, 10, 10, 10
  derruba "exatamente 11 em algum"; 33, 5, 5, 5, 5 derruba "cada um ao menos 10" e "nenhum passou de 11".
- q37 3x + 8 = 50, x = 14. q38 diferença simétrica {1, 2, 5}.
- q43 Rui em Vila Velha, Sol em Vitória, Teo na Serra (única permutação). q44 água na segunda, gás na terça, luz na
  quinta (única solução).

Distribuição da resposta nas 14 de prova: A 3, B 3, C 3, D 3, E 2. Escolha rápida: A 7, B 7, C 6. A correta é a mais
longa em 5 de 14.

## Pontos de risco

- Nenhuma norma, lei ou número de artigo citado.
- **Direita e esquerda em mesa** (q42): o resumo do conceito define que são de quem está sentado, a convenção das bancas.
- **q19** é a mais difícil: exige reconhecer "Não é verdade que Pedro não pagou e não recebeu aviso" como dupla negação.
  Dificuldade 3.

## O que o dono precisa conferir

1. Se a lógica proposicional deve mesmo ficar na l1 (o título da lição no app é "Lógica de relações").
2. A redação das explicações de q19 e q32, as mais densas.
3. Se o uso da playlist como roteiro está de acordo (créditos só neste relatório e no comentário do arquivo de origem).
