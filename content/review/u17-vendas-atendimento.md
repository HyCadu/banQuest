# Revisão técnica — U2 do Técnico Bancário: Vendas, negociação e atendimento (`u17`, pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Os 123 exercícios estão com
`source: "ia"` e `reviewed: false`. O pacote é gerado por `tools/gen_u16.py u17` a partir de `content/source/u17_*.py`
(em cada exercício a alternativa certa vem primeiro e o gerador espalha a posição).

## Escopo e plano

Cinco lições no formato por conceitos, dentro do Anexo IV do edital de nível médio (cargo TB, "Vendas, Negociação e
Atendimento"; `docs/EDITAL_TECNICO_BANCARIO.md`). O plano foi definido antes do JSON: 5 conceitos por lição, um cenário
por conceito, 2 de aprender (passo 1 com a regra e um exemplo resolvido no enunciado, passo 2 de certo/errado) e 2 ou 3
de praticar (passos 2 e 3), mais 1 questão de prova quando o conceito não tem oficial.

| Lição | Conceitos (cenário) | Exercícios | Oficiais ligadas |
|---|---|---|---|
| u17l1 Atendimento ao cliente | características dos serviços (agenda da gerente Ana) · momentos da verdade (Marcos) · qualidade e satisfação (Lúcia e o cartão) · comunicação e técnicas (Paulo e Dona Rosa) · ética e conduta (Bia) | 25 | 8 |
| u17l2 Comportamento do consumidor | valor percebido (Carla) · vantagem competitiva (Banco Litoral) · decisão de compra (Pedro e o financiamento) · fatores de influência (Rafael) · adequação e orientação (Sérgio, padaria) | 24 | 2 |
| u17l3 Vendas, prospecção e fidelização | venda consultiva e objeções (Diego e Helena) · etapas da venda (Camila e Jorge) · mix de marketing (conta universitária) · análise ambiental e SWOT (agência Jardim) · gestão da equipe (Fábio) | 25 | 10 |
| u17l4 Negociação | etapas (Lívia renegocia a dívida de Otávio) · posições e interesses (Transportadora Rota) · distributiva e integrativa (produtor Ivo) · MAPAN e zona de acordo (Teresa e o consignado) · concessões e técnicas (Márcia e a clínica Vida) | 25 | 0 |
| u17l5 CRM, reclamações e canais digitais | CRM e relacionamento (Renata e Gustavo) · segmentação (carteira de Sandra) · reclamações (Neide) · conflitos e mediação (Wagner) · canais e omnichannel (Yara) | 24 | 4 |

Por nível: 50 de aprender, 63 de praticar e 10 de prova do pacote, mais as 24 oficiais (FGV, Banestes 2018 e 2023, Técnico
Bancário), cada uma ligada ao conceito que cobra. A lição 4 não tem oficial; as 5 de prova dela são do pacote.

## Cobertura do edital

| Tópico do edital | Onde |
|---|---|
| Atendimento: conceitos, princípios, qualidade, excelência e satisfação | l1c1, l1c2, l1c3 |
| Comunicação interpessoal; técnicas de atendimento presencial e digital | l1c4 |
| Perfil, necessidades e comportamento do consumidor | l2c1 a l2c4 |
| Técnicas de vendas e pós-venda; prospecção, fidelização e retenção | l3c1, l3c2, l5c2 (reativação) |
| Negociação: conceitos, etapas, estratégias e técnicas | l4c1 a l4c5 |
| CRM; marketing de relacionamento e experiência do cliente | l5c1, l5c5 |
| Produtos e serviços: características, benefícios e adequação ao perfil | l2c5 (sem repetir a suitability da u16l7) |
| Ética, transparência e responsabilidade na oferta | l1c5 |
| Resolução de conflitos, reclamações e mediação | l5c3, l5c4 |
| Canais de atendimento e transformação digital | l5c5 |
| Educação financeira e orientação ao cliente | l2c5 |

Mix de marketing, análise ambiental e gestão da equipe comercial (l3c3 a l3c5) não estão escritos no edital, mas são o que
as oficiais da FGV cobram dentro de "técnicas de vendas"; entraram para que essas oficiais tenham conceito ensinado antes.

## Normas e referências citadas

- **Código de Defesa do Consumidor**: só a proibição de venda casada, sem número de artigo.
- **Sigilo bancário** e **ouvidoria** como instância depois do atendimento habitual: no nível do princípio, sem prazo
  nem número de resolução.
- **Negociação baseada em princípios** (os quatro pontos: pessoas, interesses, opções, critérios), **estratégias
  genéricas** (custo, diferenciação, foco), os **quatro grupos de fatores** do consumidor e as **cinco etapas** da decisão
  de compra: modelos clássicos da literatura de marketing e negociação, sem citar autor ou edição.
- **MAPAN** (melhor alternativa para um acordo negociado; em inglês BATNA): sigla usada em português nos livros de
  negociação.

Nenhuma taxa de mercado, número de artigo ou estatística foi citado. As taxas da l4c4 (1,6% a 2,0% ao mês) são de um
exemplo fictício.

## Pontos de risco e dúvidas

- **Etapas da venda e da negociação** variam de autor para autor (há modelos de 5 a 8 etapas, com nomes diferentes). O
  pacote usa a sequência mais comum e as questões de prova (l4q05) dependem só da diferença entre preparar e conduzir.
- **fgv-banestes-2023-tb-q67 (metas)**: o gabarito definitivo é C ("planos de ação comercial"), mas a definição usual de
  meta é a D ("valores a atingir em um espaço de tempo definido"). A resposta não foi mudada, e a explicação já avisava.
  Para não ensinar uma definição que contradiz o gabarito, a l3c5 fala de metas "realistas e desafiadoras" sem definir o
  termo. Ligada à l3c4 (planejamento comercial).
- **fgv-banestes-2023-tb-q68**: pelo gabarito, só a II. A l3c5 ensina reconhecimento e clima de equipe sem afirmar que
  premiar "só quem bate a meta" é correto, para não brigar com a leitura da banca.
- **fgv-banestes-2023-tb-q59 (etiqueta empresarial)**: o termo aparece no resumo da l1c5; nenhum exercício simples o cobra.

## Ajustes desta passada

- A alternativa certa era a mais longa em 71% das escolhas rápidas (u1 tem 22%, u2 31%) e em 6 de 10 questões de prova:
  59 alternativas foram reescritas (corretas enxutas, distratores alongados). Ficou 17% na escolha rápida e nenhuma
  questão de prova com a certa como a mais longa.
- Exercícios que repetiam quase literalmente a resposta de uma oficial da mesma lição foram trocados: o pós-venda da
  l3c2 não diz mais "reter e fidelizar" (q64), a terceira variação da l5c3 não fala de reclamação como melhoria de
  processo (q66), e a l5c1 não pede "manter o cadastro atualizado" como resposta de reativação (q56).
- Na l2c4, o caso do pai que perdeu dinheiro numa aplicação podia ser lido como fator social (família) ou psicológico;
  virou uma experiência do próprio cliente, que é inequivocamente psicológica.
- Na l4c3, a troca de taxa menor por seguro agrícola foi substituída por taxa menor em troca de garantia, para não sugerir
  venda casada.

## O que o dono deve olhar primeiro

1. l4 inteira (Negociação): a única lição sem oficial para calibrar; conferir se o nível das 5 questões de prova está
   perto do que a FGV cobra no resto da unidade.
2. l3c4 e l3c5, por causa das oficiais q67 e q68, em que o gabarito diverge da literatura.
3. l1c5 (venda casada, sigilo) e l5c3 (ouvidoria): as poucas afirmações normativas do pacote.
