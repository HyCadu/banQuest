# Revisão técnica — U1 do Técnico Bancário: Conhecimentos bancários e securitários (`u16`, pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Os 170 exercícios estão com
`source: "ia"` e `reviewed: false`. O pacote é gerado por `tools/gen_u16.py` a partir de `content/source/u16_*.py`
(em cada exercício a alternativa certa vem primeiro e o gerador espalha a posição).

## Escopo

Sete lições no formato por conceitos, dentro do Anexo IV do edital de nível médio (cargo TB, "Conhecimentos
bancário/securitários"; `docs/EDITAL_TECNICO_BANCARIO.md`):

| Lição | Conceitos | Exercícios | Oficiais ligadas |
|---|---|---|---|
| u16l1 Sistema Financeiro Nacional | estrutura · CMN · Banco Central · bancárias e não bancárias · banco múltiplo | 25 | 4 |
| u16l2 Produtos e serviços bancários | contas e poupança · cartões · crédito e CET · investimentos bancários · FGC · fiança bancária | 25 | 7 (+4 guardadas) |
| u16l3 Mercado financeiro e política monetária | Copom e Selic · mercado aberto · compulsório e redesconto · mercado de capitais e CVM · câmbio | 24 | 9 |
| u16l4 Pix, Open Finance e inovação | Pix · Open Finance · SCD e SEP · instituições de pagamento · criptoativos | 24 | 3 (+1 guardada) |
| u16l5 Lavagem de dinheiro e COAF | etapas · Lei 9.613/1998 · deveres das instituições · Coaf e sanções · sinais de alerta e PEP | 24 | 2 |
| u16l6 Seguros, previdência, capitalização e consórcio | sistema de seguros · pessoas e danos · previdência · capitalização · consórcio e bancassurance | 24 | 2 |
| u16l7 Regulação e perfil do cliente | autorregulação · Res. CMN 4.949/2021 · adequação ao perfil · portabilidade · Res. CMN 4.966/2021 | 24 | 3 (+1 guardada) |

As oficiais são das provas de Técnico Bancário do Banestes (FGV 2018 e 2023), cada uma ligada ao conceito que cobra. As
"guardadas" têm `fora_do_edital` ou `versao_antiga` e não vão ao app.

Cada conceito tem 2 exercícios de aprender (passo 1 com a regra ou um exemplo resolvido no enunciado, passo 2 de
certo/errado) e 2 de praticar (passos 2 e 3, em cenários de agência), mais 1 questão de prova no estilo da banca quando
não há oficial suficiente. Os cenários de ensino evitam repetir o das oficiais (ex.: a oficial de mercado aberto usa Selic
diária acima da meta; o exercício de praticar usa abaixo).

## Normas e números citados (conferir com mais cuidado)

- **Lei Complementar 179/2021** (autonomia do Banco Central, mandatos de 4 anos não coincidentes).
- **Poupança**: TR + 0,5% ao mês com meta da Selic acima de 8,5% ao ano; TR + 70% da meta, se igual ou menor.
- **Imposto de renda da renda fixa**: 22,5% / 20% / 17,5% / 15% (até 180, 360, 720 e acima de 720 dias). LCI e LCA
  isentas para pessoa física.
- **FGC**: R$ 250 mil por CPF/CNPJ por instituição ou conglomerado, teto de R$ 1 milhão a cada 4 anos.
- **Cartão**: rotativo só até a fatura seguinte, depois parcelamento em condições melhores.
- **Lei 9.613/1998** e **Lei 12.683/2012** (fim da lista de crimes antecedentes); Coaf vinculado ao Banco Central.
- **PEP**: cargo ou função pública relevante, mais os cinco anos seguintes à saída.
- **Lei 14.478/2022** (prestadores de serviços de ativos virtuais; o Banco Central é o regulador).
- **PGBL**: dedução até 12% da renda bruta tributável na declaração completa.
- **Resolução CMN 4.949/2021** (relacionamento com clientes, todas as fases da relação) e **4.966/2021** (instrumentos
  financeiros, perda esperada em três estágios, três categorias de classificação, vigência em 2025). A 4.966 é o tema de
  maior risco: o conteúdo ficou conceitual, sem número de artigo.

Nenhum número de artigo foi citado. Taxas de mercado (Selic, câmbio) não aparecem com valores atuais.

## Ajustes desta passada

- A alternativa certa era a mais longa em 78% das questões de prova: foram enxutas corretas e alongados distratores, e
  quatro questões viraram de exceção ("NÃO é correto"), que faltavam no pacote.
- A frase de que a Resolução 4.949 "veda prometer resultado garantido" foi trocada por uma formulação de princípio
  (induzir a erro contraria transparência e tratamento justo): a vedação literal está no Guia de Conduta do Banestes e não
  tenho certeza do texto exato da resolução.
- O enunciado da questão de exceção do Pix ficou parecido demais com o do consórcio e ganhou cenário próprio.

## O que o dono deve olhar primeiro

1. u16l7c5 (Resolução 4.966): conferir se a descrição dos estágios e das categorias está como o edital espera.
2. u16l2c1 (poupança) e u16l2c4 (tabela do IR): números que mudam por lei.
3. u16l4c1 (tarifas do Pix para empresas) e u16l4c2 (quem é obrigado a participar do Open Finance).
