# Revisão técnica — G2: Raciocínio lógico e estatística (`u12`, pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Os 146 exercícios (44 da l1 e 102 das l2 a l4) estão com
`source: "ia"` e `reviewed: false`. O pacote é gerado por `tools/gen_u16.py u12` a partir de `content/source/u12_*.py`
(em cada exercício a alternativa certa vem primeiro e o gerador espalha a posição; `pos` fixa a posição quando as
alternativas têm ordem natural, como números e dias da semana).

## Escopo

A G2 está completa: lições 1 a 4. A matemática financeira do edital do Analista é outra unidade (G5, `u15`).

| Lição | Conceitos | Exercícios | Oficiais ligadas |
|---|---|---|---|
| u12l1 Lógica de relações | conectivos e tabela-verdade · negação · equivalências do condicional · todo, algum e nenhum · dedução · conjuntos · associações e posições | 44 (14 aprender, 16 praticar, 14 prova) | 26 |
| u12l2 Aritmética | operações e expressões · frações e decimais · múltiplos e divisores · problemas com equações · unidades de medida e tempo · restos, ciclos e sequências | 36 (12, 12, 12) | 17 |
| u12l3 Proporção e porcentagem | razão, proporção e escala · divisão proporcional · regra de três · porcentagem · acréscimos, descontos e variações | 30 (10, 10, 10) | 18 |
| u12l4 Estatística e contagem | média, mediana e moda · medidas de dispersão · gráficos e tabelas · princípio multiplicativo e aditivo · permutações e combinações · casa dos pombos e chance | 36 (12, 12, 12) | 12 |

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

## Lições 2 a 4 (Aritmética, Proporção e porcentagem, Estatística e contagem)

**Material de referência.** Conferidas com a apostila do cargo (Apostilas Opção, OP-121NV-26), seções de Raciocínio
Lógico e de Noções de Estatística. A apostila é licenciada: ficou fora do repositório e nenhum texto, exemplo ou número
foi copiado; serviu para conferir definições, a ordem de cálculo e os métodos (fatores de aumento e desconto, regra de
três composta, MMC e MDC por fatoração, fórmulas de arranjo e combinação, medidas de dispersão).

**Contas conferidas por script.** Cada arquivo de origem (`content/source/u12_l2.py` a `u12_l4.py`) tem um `assert` para
cada conta e para cada distrator que a explicação justifica; se algum resultado estiver errado, a geração para. As 47
oficiais foram resolvidas de novo por script, e todas batem com o gabarito definitivo.

### Divergência com a apostila: variância

A apostila define a variância com denominador **n − 1** (a amostral), embora a descreva como "média dos quadrados dos
desvios". As bancas costumam pedir a **populacional** (÷ n) ou dizem qual querem. Por decisão do dono, o conceito
u12l4c2 segue o padrão das bancas (populacional como regra, amostral só quando pedida); a questão de prova u12l4q12 põe o desvio padrão amostral (1,63) como distrator do
populacional (1,41).

### Cobertura do edital

| Tópico do edital | Conceito |
|---|---|
| Números inteiros e racionais: operações, potenciação, expressões numéricas | l2 c1 |
| Frações e operações com frações | l2 c2 |
| Múltiplos e divisores de números naturais; problemas | l2 c3, c4 |
| Raciocínio matemático e sequencial | l2 c5, c6 |
| Razões e proporções; regra de três simples | l3 c1, c3 |
| Divisão em partes proporcionais | l3 c2 |
| Porcentagem, acréscimos e descontos | l3 c4, c5 |
| Média aritmética simples e ponderada, moda, mediana | l4 c1 |
| Desvio médio, amplitude, variância, desvio padrão | l4 c2 |
| Leitura e interpretação de gráficos (histogramas, setores) e tabelas | l4 c3 |
| Contagem: princípio multiplicativo e aditivo, permutações e combinações simples | l4 c4, c5 |

**Probabilidade fora do edital.** O edital de Raciocínio não cita probabilidade, mas FGV 2018 Q50, FGV 2021 Q35 e Access
2024 Q20 já esperavam pela u12l4. O conceito c6 trata do princípio da casa dos pombos e da probabilidade como razão de
contagens, o mínimo para essas três. A probabilidade mais funda é do Técnico Bancário (`u18`).

### Oficiais ligadas aos conceitos

| Conceito | Oficiais |
|---|---|
| l2 c1 Operações e expressões | FGV 2018 Q36; FGV 2018 TB Q45 |
| l2 c2 Frações e decimais | Access 2024 Q17, Q24; IDECAN 2012 Q12 |
| l2 c3 Múltiplos e divisores | FGV 2021 Q32 (paridade); Access 2024 Q21 |
| l2 c4 Problemas com equações | FGV 2018 Q39, Q45; IDECAN 2012 Q9 |
| l2 c5 Unidades de medida e tempo | FGV 2018 Q35; FGV 2018 TB Q41, Q48; FGV 2021 Q26; FGV 2023 TB Q15 |
| l2 c6 Restos, ciclos e sequências | FGV 2023 TB Q19; IDECAN 2012 Q10 |
| l3 c1 Razão, proporção e escala | Access 2024 Q22; FGV 2018 Q37; FGV 2023 TB Q13 |
| l3 c2 Divisão proporcional | Access 2024 Q30; FGV 2018 TB Q21 |
| l3 c3 Regra de três | FGV 2018 Q43, Q44; Access 2024 Q23; FGV 2023 TB Q20 |
| l3 c4 Porcentagem | Access 2024 Q26; FGV 2018 TB Q27, Q50; FGV 2021 Q30; FGV 2023 TB Q14, Q23 |
| l3 c5 Acréscimos, descontos e variações | FGV 2018 Q42, Q46; FGV 2018 TB Q35 |
| l4 c1 Média, mediana e moda | FGV 2021 Q34 |
| l4 c2 Medidas de dispersão | nenhuma (só as duas questões de prova do pacote) |
| l4 c3 Gráficos e tabelas | Access 2024 Q16, Q19; FGV 2023 TB Q25 |
| l4 c4 Princípio multiplicativo e aditivo | FGV 2018 Q38; FGV 2023 TB Q22 |
| l4 c5 Permutações e combinações | FGV 2021 Q33; IDECAN 2012 Q11 |
| l4 c6 Casa dos pombos e chance | FGV 2018 Q49, Q50; FGV 2021 Q35; Access 2024 Q20 |

Os exercícios de ensino usam cenários diferentes dos das oficiais (ex.: a oficial de escala usa praças a 35 cm num mapa
1 : 12.000; o exercício usa agências a 8 cm num mapa 1 : 25.000). Nenhum exercício é `adaptedFrom`.

### Questões de prova resolvidas

- **l2:** q05 18 − 2 × 9 ÷ 6 + 5 = 20. q06 (150 − 72 + 50) ÷ 4 = 32. q11 2/3 × 3/4 = 1/2 do saldo, saldo 1.800. q12
  5/6 × 4/5 = 2/3. q17 MMC(8, 12, 15) = 120. q18 (exceção) o MMC de 36 e 60 é 180, não 2.160. q23 46 + 8 = 54 caixas.
  q24 26 cédulas de R$ 50 e 14 de R$ 20. q29 252 min a partir de 22h40 = 2h52. q30 45.000 m² = 4,5 ha. q35 500 ÷ 6
  deixa resto 2: B. q36 (10 + 105) × 20 ÷ 2 = 1.150.
- **l3:** q05 315 ÷ 9 × 2 = 70. q06 8t/5 = 160 min, t = 1h40. q11 4.400 ÷ 16 × 5 = 1.375. q12 partes 6, 4 e 3: 240.
  q17 6 × 1,5 × 4/3 = 12. q18 4.320 ÷ 1.080 = 4 h. q23 180 + x = 0,6 (400 + x), x = 150. q24 10 de 40 mulheres: 25%.
  q29 1,25 × 0,8 = 1. q30 lucro de 20% sobre a venda: custo 480.
- **l4:** q05 (exceção) média 200 > mediana 90. q06 37 − 28 = 9. q11 (exceção) ×3 nos dados multiplica a variância por
  9. q12 √2 ≈ 1,41 (populacional). q17 480 ÷ 1.200 = 40%. q18 54° = 15% de 1.200 = 180. q23 20 × 100 = 2.000. q24
  2 × 4 × 3 = 24. q29 7 × 6 = 42. q30 C(4, 2) × C(6, 3) = 120. q35 6/10 × 4/9 = 4/15. q36 5/36.

Distribuição da resposta nas 48 de prova do pacote (l1 a l4): A 8, B 11, C 12, D 10, E 7. Escolha rápida das l2 a l4:
A 17, B 17, C 17. Nas numéricas, as alternativas vêm em ordem crescente (como nas provas), e os distratores foram
escolhidos para a certa não cair sempre no meio. Exceção: 5 de 48 no pacote (o validador pede ~10%).

### Pontos de risco

- Nenhuma norma, lei ou número de artigo citado; nenhum valor de taxa real (Selic, poupança).
- **Variância** amostral × populacional (acima).
- **FGV 2018 TB Q35** usa "lucro sobre o preço de venda", que engana quem calcula sobre o custo. O conceito l3 c5 avisa
  da base, e a questão de prova u12l3q30 treina o mesmo ponto com outros números.
- **Access 2024 Q16 e Q19** dependem de figura; a transcrição já traz o gráfico e a tabela em texto.
- **Estilo:** nas l2 a l4, 27 das 34 questões de prova do pacote são casos práticos com conta, acima dos ~40% do guia;
  é a natureza da matéria, e as oficiais seguem o mesmo padrão.

### Decisões do dono (24/11/2026)

1. **Variância:** segue o padrão das bancas. O resumo do conceito u12l4c2 passa a dar a populacional (÷ n) como regra
   e a amostral (÷ n − 1) só como exceção, quando a questão pedir.
2. **Probabilidade na l4:** fica, no básico do conceito c6.
3. **Estilo:** a proporção maior de casos práticos nas lições de cálculo está aceita; o conteúdo segue o edital.

### O que o dono ainda pode conferir

1. Se as alternativas em ordem crescente (como nas provas) atendem à regra de espalhar a resposta.
