# Revisão cruzada com a apostila do cargo (OP-121NV-26)

Revisão técnica dos pacotes publicados (`u1`, `u2`, `u11`, `u12`, `u16`), feita pela IA, conferindo cada conceito e cada
exercício com a apostila comercial do Analista de TI (Apostilas Opção, código OP-121NV-26, edição para o Banestes).

**Sobre a fonte.** A apostila é material licenciado, com marca d'água pessoal. Por isso ela **não** entra no repositório e
nenhum trecho dela foi copiado para o app: serviu só de referência para conferir fatos. As páginas citadas abaixo são as
da numeração impressa. Esta revisão não substitui a do dono, e todos os exercícios continuam `source: "ia"` e
`reviewed: false`.

## Resultado

| Pacote | Exercícios | Erros de conteúdo | Corrigidos neste PR | Observações |
|---|---|---|---|---|
| u1 Banco de dados | 170 | 0 | 3 ajustes | 60 explicações só repetem a resposta (abaixo) |
| u2 Programação | 154 | 1 (dinheiro em `double`) | 4 | — |
| u11 Língua Portuguesa | 168 | 1 (duas respostas defensáveis) | 2 | — |
| u12 Raciocínio lógico | 44 | 0 | 0 | — |
| u16 Conhecimentos bancários | 170 | 0 | 0 | lacunas de cobertura (abaixo) |

`python tools/validate_content.py`: 0 erros, 1 aviso antigo (`u1l2q09`, certo/errado no nível prova).

## Correções feitas

| Exercício | Problema | Correção |
|---|---|---|
| `u11l1q17` | A alternativa “O cliente não informou ao gerente porque desistiu do empréstimo” pode ser lida como causal (“não avisou, porque desistiu”), e aí está correta. A questão ficava com duas respostas. | Trocada por “Ninguém sabe porque o cliente desistiu do empréstimo.”, pergunta indireta sem leitura causal. |
| `u2l1q01`, `u2l1q04`, conceito `u2l1c1` | Ensinavam `double` para saldo e “valor com centavos”. A apostila (p. 280–281) recomenda tipo decimal para dinheiro, porque `float`/`double` acumulam erro de arredondamento; é pegadinha comum de prova. | Exemplos trocados por média (12.5). A explicação e o resumo do conceito passam a avisar: dinheiro em `BigDecimal` (Java) ou `decimal` (C#). |
| `u1l5q09` | O enunciado dava ao *data steward* a tarefa de “definir regras e padrões de uso”. A apostila (p. 292) separa os papéis: o *data owner* decide regras, acesso e qualidade; o *steward* cuida da aplicação no dia a dia, resolve inconsistências e mantém definições. | Enunciado reescrito para o papel do *steward*. |
| Conceito `u1l5c4` (resumo e explicações de `u1l5q23` e `u1l5q26`) | “Famílias de colunas: outra categoria clássica de NoSQL” não ensinava nada. | Agora: “Colunas largas: famílias de colunas distribuídas entre muitos nós” (uso típico na apostila, p. 291: grande volume de escrita). |
| `u1l5q08` (distrator) | Descrevia o NoSQL de colunas como “otimizado para varreduras analíticas”, misturando banco colunar analítico com colunas largas. | Distrator descreve as colunas largas corretamente; continua errado para o cenário (perfis hierárquicos pedem documentos). |
| `u11l5q20` | “Os clientes aumentaram” é estranho (clientes não aumentam; o número deles, sim). | “O número de clientes aumentou”. |

## Conferido sem divergência

- **u1:** formas normais e FNBC; tabela de níveis de isolamento do padrão SQL (o PostgreSQL também barra fantasma no
  REPEATABLE READ, e o Oracle não tem esse nível: nossas questões dizem “segundo o padrão SQL”, então estão certas);
  MERGE no PostgreSQL 15; CTE recursiva; ROW_NUMBER, RANK e DENSE_RANK; LAG e LEAD; MVCC e VACUUM; CDB e PDB no Oracle
  19c; H2 embutido e em memória; estrela e floco de neve; ETL e ELT; data lake com *schema-on-read*.
- **u2:** divisão inteira, curto-circuito, precedência (a atribuição é a última), laços, `finally`, ordem dos `catch`,
  IL, CLR e JIT, `virtual`/`override`, sobrecarga em compilação e sobrescrita em execução, os cinco princípios SOLID e
  seus sintomas, AAA, TDD e BDD (Gherkin), GitFlow (hotfix sai da main e volta à develop), uso responsável de IA.
- **u11:** acentuação e Acordo, hífen, crase, porquês, concordância, regência, vozes, correlação de tempos, pontuação.
- **u12:** conectivos, De Morgan, negação e equivalências do condicional, quantificadores. Contas e tabelas-verdade
  refeitas.
- **u16:** composição do CMN (Fazenda, Planejamento e Orçamento, BC), papéis de normativos e supervisores, Copom e Selic,
  Pix (chave opcional, gratuito para pessoa física, tarifável para empresa), Open Finance (consentimento revogável a
  qualquer momento, iniciação de pagamento). Números que a apostila não traz (FGC, IR regressivo, poupança, PEP, Coaf,
  Res. 4.949 e 4.966) seguem conferidos contra a Parte 7 do guia, sem mudança.

## Pendências (não mexidas neste PR)

1. **60 explicações da u1 (lições 2 a 5) só repetem a resposta** e colam o resumo do conceito (“X é a resposta que
   aplica essa regra”), o que a Parte 4.6 do guia proíbe. As de `match` são aceitáveis (listam os pares); as de
   `quick` e `truefalse` precisam dizer por que as outras opções erram. Lista: `u1l2q11` a `q30`, `u1l3q11` a `q30`,
   `u1l4q11` a `q30`, `u1l5q11` a `q30` (passos de aprender e praticar). Proposta: um PR só para reescrevê-las.
2. **Lacunas de cobertura** que a apostila trata e o app ainda não:
   - u1: dimensões que mudam no tempo (tipo 1 e tipo 2), teorema CAP e consistência eventual, OLAP *pivot*, paginação
     por dialeto (LIMIT no PostgreSQL e H2, FETCH FIRST no Oracle, que não tem LIMIT), DDL transacional no PostgreSQL e
     commit implícito no Oracle, novidades do PostgreSQL 14 e 15 além do MERGE.
   - u2: modificador `internal` do C#, `throw;` que preserva a pilha, tipos de dublê (stub, spy, mock, fake),
     cobertura de código e seus limites, desenvolvimento baseado em tronco.
   - u16: Pix Automático, Pix Cobrança, Saque e Troco, portabilidade de chave Pix, participantes obrigatórios e
     facultativos do Pix.
3. **Aviso antigo** `u1l2q09`: certo/errado no nível prova.
