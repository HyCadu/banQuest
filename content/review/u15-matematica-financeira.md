# Revisão técnica — G5: Matemática financeira (`u15`, pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Os 114 exercícios estão com
`source: "ia"` e `reviewed: false`. O pacote é gerado por `tools/gen_u16.py u15` a partir de `content/source/u15_*.py`
(em cada exercício a alternativa certa vem primeiro; nas numéricas, as alternativas ficam em ordem crescente e a posição
da certa é fixada).

## Escopo

| Lição | Conceitos | Exercícios | Oficiais ligadas | Trilha |
|---|---|---|---|---|
| u15l1 Juros e taxas | juros simples · juros compostos · taxas equivalentes · taxa nominal e efetiva · taxa real e rentabilidade | 30 (10 aprender, 10 praticar, 10 prova) | 8 | Analista e Técnico |
| u15l2 Descontos e séries | desconto comercial simples · desconto racional simples · descontos compostos · valor presente, valor futuro e equivalência · séries uniformes · séries antecipadas, com carência e não uniformes | 36 (12, 12, 12) | 6 | Analista e Técnico |
| u15l3 Amortização | prestação, juros e amortização · SAC · Price · sistema misto e comparação | 24 (8, 8, 8) | 3 | Analista e Técnico |
| u15l4 Avaliação de projetos | fluxo de caixa e TMA · VPL · TIR · capitalização contínua | 24 (8, 8, 8) | 0 | só Analista |

**Capitalização contínua na l4.** O plano (`docs/CONTENT_PLAN.md`) punha a capitalização contínua na lição de juros, mas
o edital do Técnico não a cita, e ele vê as lições 1 a 3. Ela foi para a l4, junto com VPL e TIR, que também são só do
Analista. Decisão registrada em `docs/DECISIONS.md`.

## Material de referência

Conferido com a apostila do cargo (Apostilas Opção, OP-121NV-26), seção de Matemática Financeira. A apostila é
licenciada: ficou fora do repositório e nenhum texto, exemplo ou número foi copiado. Ela serviu para conferir as
fórmulas e as convenções (mês e ano comerciais, desconto “por fora” e “por dentro”, taxa aparente × real, data focal,
Price, SAC e sistema misto).

**Divergências e lacunas da apostila**

1. **SACRE × SAM.** A apostila chama o sistema misto de “Sistema de Amortização Crescente (SACRE) ou Sistema de
   Amortização Misto (SAM)”, como se fossem o mesmo. Vários autores tratam o SACRE como outro sistema. A lição usa só a
   definição que a própria apostila dá ao SAM (prestação = média das prestações do SAC e da Price) e não cita o SACRE.
2. **VPL, TIR e capitalização contínua** não aparecem no texto da apostila, embora estejam no índice copiado do edital.
   As definições da l4 seguem o uso corrente (VPL = valor presente das entradas − das saídas pela TMA; TIR = taxa que
   zera o VPL; M = C × eʳᵗ e taxa efetiva eʳ − 1), sem norma nem autor citado.

## Contas conferidas por script

Cada arquivo de origem tem um `assert` para cada conta e para cada distrator que a explicação justifica; se algum
resultado não bater, a geração para. Na escrita, os `assert` pegaram dois erros antes de chegar ao pacote: um distrator
de juros compostos (8.878,74 virou 8.878,76) e uma comparação de ponto flutuante. Price e SAC são conferidos pela
fórmula, não só pelo fator dado no enunciado (ex.: a prestação de R$ 2.246,27 sai de 10.000 a 4% em 5 meses). As 17
oficiais foram resolvidas de novo por script, e todas batem com o gabarito definitivo.

**Fatores dados.** Como nas provas, as potências e os fatores (1,1³ = 1,331; a = 8,66; e^0,3 ≈ 1,35) vêm no enunciado. Em
u15l3q17 o fator a = 2,4869 é arredondado: a prestação pela fórmula exata é 2.412,69, e a questão, que diz “valor
aproximado”, usa 2.412,64, o resultado com o fator dado.

## Oficiais ligadas aos conceitos

Todas são da FGV · Banestes 2018, Técnico Bancário.

| Conceito | Oficiais |
|---|---|
| l1 c1 Juros simples | Q22 (8 dias de atraso, mês comercial) |
| l1 c2 Juros compostos | Q25 (interpolação de potência), Q29, Q33 |
| l1 c4 Taxa nominal e efetiva | Q26, Q30 |
| l1 c5 Taxa real e rentabilidade | Q24 (aumento real), Q39 (rentabilidade de carteira) |
| l2 c1 Desconto comercial simples | Q32, Q40 (com IOF e tarifa) |
| l2 c3 Descontos compostos | Q36, Q38 |
| l2 c6 Séries antecipadas, com carência e não uniformes | Q23 (carência e pagamento final), Q34 (antecipada, taxa de 100%) |
| l3 c2 SAC | Q31, Q37 |
| l3 c3 Price | Q28 |

Os conceitos l1 c3, l2 c2, l2 c4, l2 c5, l3 c1, l3 c4 e toda a l4 têm só as questões de prova do pacote. Os exercícios
de ensino usam números diferentes dos das oficiais (ex.: a oficial de desconto comercial composto usa R$ 4.000 a 10%; o
exercício usa R$ 5.000 a 20%). Nenhum exercício é `adaptedFrom`.

## Questões de prova resolvidas

- **l1:** q05 8.000 × (1 + 0,015 × 7) = 8.840. q06 1 ÷ 0,04 = 25 meses. q11 10.000 × 0,061208 = 612,08. q12 6.050 −
  1.050 = 5.000; × 1,1 = 5.500. q17 1,05² = 1,1025. q18 (exceção) 2% ao mês compostos ≈ 26,8% ao ano, não 24%. q23
  1,04³ − 1 ≈ 12,49%. q24 2.000 × 1,12 = 2.240. q29 1,08 ÷ 1,10 − 1 ≈ −1,82%. q30 0,7 × 12 + 0,3 × 2 = 9%.
- **l2:** q05 15.000 − 600 = 14.400. q06 7.200 ÷ 0,9 = 8.000. q11 636 − 600 = 36. q12 (exceção) o racional incide
  sobre o valor atual. q17 20.000 × 0,729 = 14.580. q18 N = 10.000; 10.000 × 0,75² = 5.625. q23 2.200 + 2.000 =
  4.200. q24 3.300 + 3.300 = 6.600. q29 500 × 3,31 = 1.655. q30 43.300 ÷ 8,66 = 5.000. q35 1.210 + 1.100 + 1.000 =
  3.310. q36 P × (1/1,21 + 1/1,331) = 10.000, P ≈ 6.338,10.
- **l3:** q05 juros 1.000, prestação 5.000, saldo 46.000. q06 juros sobre o saldo do início do período. q11 4ª
  prestação SAC: 5.000 + 0,03 × 15.000 = 5.450. q12 1.440 ÷ 48.000 = 3%. q17 6.000 ÷ 2,4869 ≈ 2.412,64; juros 600.
  q18 10.400 − 2.246,27 = 8.153,73. q23 (12.000 + 11.132,65) ÷ 2 ≈ 11.566,33. q24 (exceção) a 1ª do SAC é maior.
- **l4:** q05 3.000 + 3.000 = 6.000. q06 TMA = custo de oportunidade. q11 12.000 − 10.000 = 2.000. q12 maior VPL entre
  excludentes: Z. q17 √1,44 = 1,2: 20%. q18 a 15% o VPL é zero. q23 20.000 × 1,35 = 27.000. q24 (exceção) a efetiva é
  eʳ − 1, não r.

Distribuição da resposta nas 38 de prova: A 6, B 10, C 10, D 6, E 6. Escolha rápida: A 19, B 18, C 18. Estilos das de
prova: 30 casos práticos, 4 literais, 4 de exceção (decisão do dono na G2: mais casos práticos nas lições de cálculo
estão aceitos).

## Pontos de risco

- Nenhuma norma, lei, alíquota real ou valor de taxa do mercado (Selic, IOF vigente) citado. Os exercícios não usam
  IOF; o da oficial Q40 é o que a banca deu no enunciado.
- **Sistema misto** (acima): só a definição de média de SAC e Price.
- **Arredondamento** em u15l3q17 (fator dado) e u15l3q23 (média que termina em 0,5 centavo): as duas dizem
  “aproximadamente”.
- **l4 sem oficial:** as questões de VPL, TIR e capitalização contínua ainda não foram calibradas com prova real.

## O que o dono precisa conferir

1. A mudança da capitalização contínua para a l4 (fica fora da trilha do Técnico).
2. O tratamento do SACRE: não citar e usar só o SAM como média de SAC e Price.
