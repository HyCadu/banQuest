# Guia para montar trilhas do BanQuest com IA

Este documento é a **base que qualquer IA deve ler** antes de transformar uma prova (simulado, prova anterior, PDF com
gabarito) ou um tópico do edital em conteúdo do BanQuest. Ele traz, num arquivo só, a lógica de aprendizado da Unidade 1
(Banco de Dados): o formato por conceitos, as regras de cada exercício, os formatos de arquivo e uma checagem final.

Serve para ChatGPT, Claude, Gemini, Copilot ou qualquer outra IA. Não depende de ferramenta nenhuma: basta anexar este
arquivo à conversa.

---

## Parte 1 — Para a pessoa que vai colaborar

1. **Escolha o que vai fazer.** Uma prova anterior (PDF da prova + PDF do gabarito) ou uma lição do mapa da Parte 6.
   Antes, confira nas issues e PRs abertos se alguém já está nela.
2. **Abra a IA e anexe:**
   - este arquivo (`docs/GUIA_TRILHA_COM_IA.md`);
   - o PDF da prova e o do gabarito (se for uma prova);
   - opcional, mas ajuda muito: o pacote da U1 como exemplo real (`app/src/main/assets/content/u1-banco-de-dados.json`).
3. **Cole o prompt da Parte 9** e peça **uma etapa por vez** (transcrever, classificar, gerar a lição). Lição por lição
   sai melhor do que a unidade inteira de uma vez.
4. **Salve os arquivos** nos caminhos indicados, rode os validadores (Parte 10) e abra um pull request com o relatório
   de revisão. Quem decide o que entra é o dono do projeto (veja `CONTRIBUTING.md`).

> A IA erra. O validador pega erros de formato, não de conteúdo. Leia as questões, resolva cada uma e confira as
> normas citadas antes de abrir o PR.

---

## Parte 2 — Para a IA: o que é o BanQuest

App Android de estudo gamificado (trilha, XP, ofensiva, corações) para o concurso do **Banestes, banca FCC**, com duas
provas à escolha: **Analista de TI – Desenvolvimento de Sistemas** (21/02/2027) e **Técnico Bancário** (24/01/2027).
Idioma: português do Brasil.

A prova do Analista tem 50 questões de Conhecimentos Específicos (peso 2) e 30 de Conhecimentos Gerais (peso 1), em 4
horas: **3:00 por questão**. A do Técnico tem 40 de Específicos (peso 2) e os mesmos 30 de Gerais, em 4 horas: **3:25 por
questão**.

O conteúdo fica em duas trilhas:
- **Específicos do Analista:** unidades U1 a U10 (ids `u1` a `u10`).
- **Específicos do Técnico Bancário:** ids `u16` a `u19`, mais a Matemática financeira (`u15`, lições 1 a 3).
- **Gerais (as duas provas):** G1 a G5 (ids `u11` a `u15`; a G5, Matemática financeira, só nos Gerais do Analista).

O escopo de cada lição vem do edital: Anexo IV, cargo 04, em `docs/EDITAL.md` (Analista) e cargo TB em
`docs/EDITAL_TECNICO_BANCARIO.md` (Técnico). Não inclua assuntos que o edital não cita.

Você pode receber três tipos de pedido. Cada um tem um formato de saída:

| Pedido | O que entregar | Onde fica |
|---|---|---|
| Transcrever uma prova anterior | 1 arquivo JSON por prova (Parte 5.2) | `content/provas/<banca>-<orgao>-<ano>.json` |
| Gerar uma lição ou unidade | O pacote da unidade (Parte 5.1) | `app/src/main/assets/content/uN-<nome>.json` |
| Revisar o que gerou | O relatório de revisão (Parte 8) | `content/review/<pack>.md` |

---

## Parte 3 — Regras que não podem ser quebradas

1. **Todo exercício escrito por IA leva `"source": "ia"` e `"reviewed": false`.** Nunca marque `reviewed: true`: só o
   dono revisa. O app mostra o selo "não revisada".
2. **Nunca invente norma, versão, número de artigo, data ou estatística.** Se não tiver certeza, reformule a questão
   para algo conceitual ou descarte. Cite só as versões da tabela da Parte 7, exatamente como estão lá.
3. **Não diga que uma questão é "da FCC"** (nem escreva "FCC" no enunciado, alternativas ou explicação), a não ser nas
   questões reais transcritas de prova da FCC (`"source": "prova_real"` com `ref.banca` "FCC"). Estilo FCC não é FCC.
4. **Questão de prova real é transcrita fielmente.**
   - O enunciado e as alternativas ficam como no PDF. Corrija só lixo de extração (cabeçalho, número de página,
     hifenização quebrada).
   - A resposta é a do gabarito oficial. Confira resolvendo a questão. Se discordar, registre no relatório e não mude a
     resposta.
   - Só a explicação é sua: ela leva `"explanation_source": "ia"`.
5. **Nada copiado de outros apps ou cursinhos:** textos, questões comentadas, mnemônicos de marca. Escreva com as suas
   palavras.
6. **Português do Brasil**, frases curtas, sem jargão desnecessário. Código e nomes técnicos podem ficar em inglês.

---

## Parte 4 — A lógica de aprendizado (formato por conceitos)

É o coração do app, aplicado em toda a Unidade 1. Siga à risca.

### 4.1 A ideia

Ensinar com tarefas pequenas e repetidas, como nos apps de idiomas, sem abrir mão das questões no nível da prova. O
aluno **aprende fazendo**: cada exercício de ensino mostra a regra (ou um exemplo resolvido) e pede que ele aplique. A
resposta sai do raciocínio, nunca do chute.

### 4.2 Lição = conceitos pequenos, em ordem didática

- Cada lição ensina **de 4 a 7 conceitos** pequenos (U1 tem lições com 5 a 7). Um conceito é uma ideia que cabe numa
  frase de resumo: "Chaves e atributos", "Dependência funcional", "LEFT JOIN".
- Os conceitos ficam **na ordem em que devem ser aprendidos**: o app ensina nessa ordem e só cobra prova de conceito
  já ensinado.
- Cada conceito tem:
  - `id` no formato `uNlMcK` (ex.: `u1l1c1`);
  - `title` curto;
  - `summary` de 2 ou 3 linhas (pelo menos 20 caracteres), com a regra e um exemplo. Esse resumo aparece como "Novo
    conceito" na primeira vez e depois como "Dica".
- Uma lição densa no edital ganha **mais conceitos**, não mais questões soltas.

### 4.3 Três níveis de exercício

| Nível (`level`) | Para quê | Tipos (`type`) | Alvo de tempo |
|---|---|---|---|
| `aprender` | Ver a regra e acertar com raciocínio | `truefalse`, `quick` | 0:40 |
| `praticar` | Aplicar em cenário curto, associar, completar, ordenar | `match`, `fill_blank`, `order_code`, `quick`, `truefalse` | 1:30 |
| `prova` | Questão no nível da banca | `mcq5`, `case`, `code_output` (5 alternativas) | 3:00 |

As questões oficiais (provas reais) entram sempre como `prova`.

### 4.4 Passos dentro do conceito (`step`)

Todo exercício de `aprender` e `praticar` tem `step`:

| `step` | Papel | Como escrever |
|---|---|---|
| 1 | **Exemplo resolvido** | O enunciado mostra a regra e um exemplo já resolvido, e pede o caso análogo. |
| 2 | **Aplicar** | Mesmo cenário, agora o aluno aplica a regra sozinho (a regra ainda pode vir resumida no enunciado). |
| 3 | **Variar** | Outro ângulo do mesmo conceito: a pegadinha comum, o caso-limite, a associação de termos. |

- Pode haver variantes com o mesmo passo (dois exercícios de passo 2, por exemplo): o app sorteia entre eles.
- **Um cenário por conceito**, mantido nos passos (ex.: no conceito "Chaves e atributos", sempre tabelas ALUNO,
  PRODUTO). Cenário novo a cada exercício confunde.
- Questões de `prova` **não** têm `step`.

### 4.5 Como o app monta a sessão (por que as quantidades importam)

A sessão de uma lição tem **12 exercícios**:
1. Escolhe **3 conceitos**, na ordem da lição: primeiro os que o aluno ainda não viu, depois os que ele errou, depois
   os outros (revisão).
2. Cada conceito vira um **bloco de 3 exercícios em sequência**: passo 1, 2 e 3.
3. Fecha com **3 questões de prova** dos conceitos da sessão.

Erros voltam depois em "Praticar erros" (revisão espaçada). Um exercício adaptado de uma questão oficial nunca cai na
mesma sessão que a original.

Por isso, **cada conceito precisa de, no mínimo:**
- 2 exercícios de `aprender` e 2 de `praticar`;
- os passos 1, 2 e 3 presentes;
- 1 questão de `prova` (do pacote ou uma oficial ligada a ele);
- pelo menos 1 de `aprender` e 1 de `praticar` que **não** sejam adaptados de prova.

Na prática, a U1 usa de 5 a 9 exercícios por conceito, mais as questões oficiais. O banco da lição fica entre
**24 e 80 exercícios** (a u1l1 tem 50; as outras lições da U1, 30 cada).

### 4.6 Aprender fazendo: o que é proibido

- **Cobrar o que não foi mostrado.** Se o passo 1 não mostrou a regra, o enunciado precisa mostrar.
- **Exercício chutável:** alternativas absurdas, resposta óbvia pelo tamanho ou pela gramática.
- **Entregar de bandeja a resposta de uma questão de prova da mesma lição.** O exercício simples usa outro cenário, e
  a questão de prova continua exigindo o raciocínio.
- **Explicação que só repete a resposta.** A explicação diz por que a certa é certa e por que as outras não são.

### 4.7 Estilo das questões de prova (estilo FCC)

- **5 alternativas** de tamanho parecido, uma correta. Nada de "todas as anteriores" ou "nenhuma das anteriores".
- **Mistura de estilos** (`style`): `literal` (definição), `conceitual`, `excecao` ("NÃO é…", "EXCETO") e cerca de
  **40% de casos práticos** (`caso_pratico`).
- **Resposta correta distribuída por igual entre A e E.** A correta não pode ser sempre a mais longa.
- **Escolha rápida (`quick`):** 3 alternativas, com a correta bem distribuída entre A, B e C (nenhuma posição abaixo
  de 15% nem acima de 55%).

### 4.8 Questões oficiais e exercícios adaptados

- **Questão oficial:**
  - é transcrita em `content/provas`;
  - aponta para a lição (`lesson`) e para o **conceito** (`concept`) que cobra;
  - só aparece ao aluno depois que esse conceito foi ensinado.
- **Exercício adaptado:**
  - é escrito por IA a partir de uma questão oficial, para ensinar o mesmo assunto de outro jeito;
  - leva `adaptedFrom` com `id`, `banca`, `orgao`, `ano` e `numero` exatamente iguais aos da questão em `content/provas`;
  - continua `source: "ia"`.

---

## Parte 5 — Formatos de arquivo

### 5.1 Pacote da unidade (`app/src/main/assets/content/uN-<nome>.json`)

```json
{
  "schema": 1,
  "pack": "u1-banco-de-dados",
  "version": 1,
  "unit": { "id": "u1", "order": 1, "title": "Banco de dados", "editalTopics": ["Modelo ER e normalização", "..."] },
  "lessons": [
    {
      "id": "u1l1", "order": 1, "title": "Modelo ER e normalização",
      "concepts": [
        { "id": "u1l1c1", "title": "Chaves e atributos",
          "summary": "Chave primária: a coluna que não se repete e identifica cada linha. Chave candidata: outra coluna que também poderia ser a primária." }
      ],
      "questions": [ { "...": "exercícios, formato abaixo" } ]
    }
  ]
}
```

**Formato de cada exercício:**

| Campo | Obrigatório | Regra |
|---|---|---|
| `id` | sim | `uNlMqNN`, dois dígitos, sem repetir no pacote (ex.: `u2l1q07`). |
| `type` | sim | `mcq5`, `case`, `code_output`, `truefalse`, `quick`, `match`, `fill_blank`, `order_code`. |
| `topic`, `subtopic` | sim / não | Assunto curto (ex.: "BD", "er"). |
| `difficulty` | sim | 1, 2 ou 3. |
| `style` | sim | `conceitual`, `literal`, `caso_pratico` ou `excecao`. |
| `stem` | sim | Enunciado, de 15 a 700 caracteres. |
| `code` | depende | Trecho de código. Obrigatório em `code_output` e em `fill_blank`, que leva a lacuna `___`. |
| `options` + `answer` | depende | `answer` é o índice (0 = A). A quantidade de alternativas varia por tipo (ver abaixo). |
| `pairs` | `match` | De 3 a 5 pares `{ "left": "...", "right": "..." }`, sem repetir. |
| `lines` | `order_code` | De 3 a 8 linhas, **na ordem correta**: o app embaralha. |
| `explanation` | sim | Pelo menos 20 caracteres: por que a certa é certa e as outras não. |
| `source`, `reviewed` | sim | `"ia"` e `false`. |
| `concept` | sim | Id de um conceito da lição. |
| `level` | sim | `aprender`, `praticar` ou `prova`. |
| `step` | só em aprender/praticar | 1, 2, 3… |
| `adaptedFrom` | se for adaptado | `{ "id", "banca", "orgao", "ano", "numero" }` da questão oficial. |

**Alternativas por tipo:**

| Tipo | `options` |
|---|---|
| `mcq5`, `case`, `code_output` | 5 alternativas |
| `fill_blank` | 4 ou 5 alternativas |
| `quick` | 3 alternativas |
| `truefalse` | exatamente `["Certo", "Errado"]` (`answer` 0 = Certo) |

### 5.2 Prova anterior (`content/provas/<banca>-<orgao>-<ano>.json`)

Um arquivo por prova. Exemplo de uma questão:

```json
{
  "schema": 1, "banca": "FCC", "orgao": "Prefeitura de Teresina", "ano": 2016,
  "cargo": "Técnico de Nível Superior – Analista de Sistemas", "gabarito": "preliminar",
  "questions": [
    {
      "id": "fcc-teresina-2016-q21",
      "ref": { "banca": "FCC", "orgao": "Prefeitura de Teresina", "ano": 2016,
               "cargo": "Técnico de Nível Superior – Analista de Sistemas", "prova": "Caderno P27, Tipo 001", "numero": 21 },
      "gabarito": "preliminar",
      "unit": "u4", "lesson": "u4l5",
      "type": "mcq5", "style": "conceitual", "difficulty": 1,
      "topic": "Arquitetura", "subtopic": "camadas",
      "stem": "Considere o serviço de páginas web implementado em uma arquitetura de três ou quatro camadas. Nessa arquitetura, o Browser se localiza na camada de",
      "code": null,
      "options": ["Apresentação.", "Aplicação.", "Web.", "Serviços.", "Negócios."],
      "answer_letter": "A", "answer": 0,
      "explanation": "O navegador é a interface com o usuário, então está na camada de apresentação. A lógica de negócio fica nos servidores de aplicação, e os dados na camada de dados.",
      "explanation_source": "ia", "source": "prova_real", "reviewed": false,
      "flags": []
    }
  ]
}
```

**Regras da prova anterior:**
- **`id`:** `<arquivo>-q<número da questão na prova>`.
- **`gabarito`:** `definitivo`, `preliminar` ou `nao_confirmado`.
- **Tipos:** só `mcq5`, `case` ou `code_output`, sempre com 5 alternativas.
- **`answer_letter` e `answer`:** precisam bater (A = 0).
- **Destino:** `unit` e `lesson` vêm do mapa da Parte 6. `concept` é o conceito da lição que a questão cobra; use-o só
  se a lição já tiver conceitos definidos, senão proponha um no relatório.
- **`flags`, quando valer:**
  - `versao_antiga`: só vale para uma versão de norma fora do edital. Fica fora do app.
  - `versao_citada_anterior`: cita uma versão anterior, mas a resposta não muda.
  - `fora_do_edital`: assunto que o edital não cita.
  - `depende_de_figura`: a figura virou texto descrito no enunciado.
  - `repetida`: a mesma questão já está no app por outra prova (fica guardada, sem ir ao app).
  - `produto_especifico`: cobra um produto específico.
- **Bancas aceitas:** FCC, FGV, IDECAN, CESPE, CEBRASPE, VUNESP e CESGRANRIO.
- **Anuladas:** questões anuladas não entram.
- **Texto-base:** texto que serve a várias questões entra no enunciado de cada uma.

---

## Parte 6 — Mapa das unidades e lições

Use estes ids. As lições estão na ordem da trilha.

| Unidade | Lições (`uNl1`, `uNl2`, …) |
|---|---|
| `u1` Banco de dados | Modelo ER e normalização · SQL: DML, DDL, DCL e transações · Subconsultas, CTEs e funções analíticas · PL/SQL, PL/pgSQL e SGBDs · Modelagem dimensional, DW e NoSQL |
| `u2` Programação e OO | Fundamentos de programação · Orientação a objetos · Princípios SOLID · Linguagens e plataformas · Testes, TDD e Git |
| `u3` Engenharia de software e ágil | Processos de software · Requisitos, UML e BPMN · Scrum, Kanban e XP · Testes e gestão de configuração · Qualidade e rastreabilidade |
| `u4` Web, APIs e arquitetura | HTML, CSS e JavaScript · Frameworks front-end e Node.js · Web services REST e SOAP (no edital: REST e XML, sem SOAP) · Usabilidade e acessibilidade · Arquitetura de software |
| `u5` DevOps e containers | CI/CD e infraestrutura como código · Docker e Kubernetes · Estratégias de implantação · Observabilidade e DevSecOps |
| `u6` Segurança e LGPD | Conceitos e gestão de riscos · Criptografia e certificados · Controle de acesso e identidade · Proteção de redes e ataques · Monitoramento e resposta a incidentes · LGPD |
| `u7` Governança de TI | Projetos e portfólio · ITIL e melhoria contínua · COBIT e planejamento de TI · Gestão financeira e de riscos · Contratações, arquitetura corporativa e TOGAF |
| `u8` Sistemas operacionais | Processos, threads e sincronização · Memória, E/S e armazenamento · Linux · Windows, AD e virtualização |
| `u9` Redes | Fundamentos e meios de transmissão · TCP/IP e protocolos · Roteamento, VLAN e SDN · Cabeamento e Wi-Fi · QoS, VoIP e multicast |
| `u10` Serviços, monitoramento e nuvem | Servidores e serviços · Gerência de redes e monitoramento · Computação em nuvem · Inglês técnico |
| `u11` (G1) Língua Portuguesa | Ortografia e crase · Interpretação de texto · Morfologia · Sintaxe · Pontuação e reescrita |
| `u12` (G2) Raciocínio lógico e estatística | Lógica de relações · Aritmética · Proporção e porcentagem · Estatística e contagem |
| `u13` (G3) Atualidades do mercado financeiro | Sistema Financeiro Nacional · Política monetária · Mercado e inovação · Cidadania financeira e cenários |
| `u14` (G4) Ética, compliance e ESG | Ética e integridade · Governança e compliance · Corrupção, fraude e dados · ESG e sustentabilidade |
| `u15` (G5) Matemática financeira | Juros e taxas · Descontos e séries · Amortização · Avaliação de projetos (o Técnico vê só as 3 primeiras) |
| `u16` Conhecimentos bancários e securitários (Técnico) | Sistema Financeiro Nacional · Produtos e serviços bancários · Mercado financeiro e política monetária · Pix, Open Finance e inovação · Lavagem de dinheiro e COAF · Seguros, previdência, capitalização e consórcio · Regulação e perfil do cliente |
| `u17` Vendas, negociação e atendimento (Técnico) | Atendimento ao cliente · Comportamento do consumidor · Vendas, prospecção e fidelização · Negociação · CRM, reclamações e canais digitais |
| `u18` Probabilidade e estatística (Técnico) | Dados, amostragem e gráficos · Medidas de posição e dispersão · Probabilidade · Condicional e distribuição normal |
| `u19` Informática e comportamentos digitais (Técnico) | LGPD no setor bancário · Hardware, software e sistemas · Internet, redes e nuvem · Google Workspace · Segurança digital e golpes · Inteligência artificial |

Os tópicos do edital de cada lição, com as observações de densidade e risco, estão em `docs/CONTENT_PLAN.md`. A
transcrição dos editais está em `docs/EDITAL.md` (Analista) e `docs/EDITAL_TECNICO_BANCARIO.md` (Técnico).

---

## Parte 7 — Normas e versões que podem ser citadas

Só estas, exatamente assim (são as do edital). Fora delas, a questão fica conceitual.

| Área | Versão no edital |
|---|---|
| Processo | CMMI-DEV v2.0 · ABNT NBR ISO/IEC/IEEE 12207:2021 · MR-MPS-SW versão 2023 · UML 2.5 |
| Qualidade | ISO/IEC 25010:2024 |
| Governança | PMBOK 8ª edição · ITIL (Version 5) · COBIT 2019 · ABNT NBR ISO 31000:2018 · Lei nº 14.133/2021 · TOGAF (fundamentos) |
| Banco de dados | PostgreSQL 14 e 15 · Oracle 19c · H2 |
| Web | Bootstrap 5 · WCAG 2.1/2.2 · ABNT NBR 17225:2025 |
| SO | Red Hat e Oracle Linux · Windows 11 · Windows Server 2019 e 2022 |
| Redes | ABNT NBR 14565:2019 (cat. 5e, 6, 6a, 7) · IEEE 802.1D, 802.1Q/p, 802.1D-2004, 802.1Q-2005, 802.1X, 802.3, 802.3u, 802.3z, 802.3ae, 802.3af · 802.11n/ac/ax · RFC 2544 · RFC 2889 |
| Segurança | ABNT NBR ISO/IEC 27001:2024 · 27002:2022 · 27005:2023 · 27035-1:2023 · 27017:2016 · NIST SP 800-61 Rev. 3 · OWASP Top 10:2021 · LGPD (Lei nº 13.709/2018) |

Cuidados:
- Não cite número de controle das normas ISO nem número de artigo da LGPD.
- O NIST SP 800-61 Rev. 3 mudou a estrutura da Rev. 2: não use as fases antigas.
- Normas novas (ISO/IEC 25010:2024, NBR 17225:2025, ITIL Version 5, PMBOK 8ª) só no que você tem certeza.

---

## Parte 8 — Revisão antes de entregar

### 8.1 Checagem final (a IA confere antes de responder)

- [ ] Cada conceito tem resumo, ao menos 2 de `aprender` e 2 de `praticar`, os passos 1, 2 e 3 e ao menos 1 de `prova`.
- [ ] Todo exercício de ensino mostra a regra ou um exemplo antes de cobrar. Um cenário por conceito.
- [ ] Nenhum exercício simples entrega a resposta de uma questão de prova da mesma lição.
- [ ] Questões de prova: 5 alternativas parecidas, sem "todas/nenhuma das anteriores", ~40% casos práticos, resposta
      espalhada entre A e E, correta nem sempre a mais longa.
- [ ] `quick` com resposta espalhada entre A, B e C.
- [ ] Nenhuma norma, versão ou artigo fora da Parte 7. Nenhum "FCC" em texto que não seja de prova real da FCC.
- [ ] Todos com `"source": "ia"` e `"reviewed": false`. Ids no padrão e sem repetir.
- [ ] Resolvi cada questão de prova e a resposta marcada está certa.
- [ ] Questões transcritas: texto igual ao PDF, resposta igual ao gabarito, explicação marcada como `ia`.

### 8.2 Relatório de revisão (`content/review/<pack>.md`)

Entregue junto um relatório curto:
1. O que foi feito: lições, conceitos, quantos exercícios por nível.
2. Cobertura do edital: cada tópico da lição e onde foi tratado.
3. Pontos de risco: normas citadas, assuntos que mudam com o tempo, questões em que você teve dúvida.
4. Para provas transcritas: questões com flag e por quê; discordâncias com o gabarito (sem mudar a resposta).
5. O que o dono precisa conferir.

---

## Parte 9 — Prompt pronto para colar na IA

```text
Você vai produzir conteúdo para o app BanQuest. Leia o arquivo GUIA_TRILHA_COM_IA.md anexado por inteiro antes de
começar e siga todas as regras dele (Partes 3 a 8). Se alguma coisa aqui conflitar com o guia, vale o guia.

Tarefa: [escolha uma]
(a) Transcrever a prova anexada (PDF da prova + PDF do gabarito) para o formato da Parte 5.2, um arquivo JSON,
    classificando cada questão na unidade e lição do mapa da Parte 6.
(b) Gerar a lição [uNlM — título] no formato por conceitos (Parte 4), no formato da Parte 5.1, usando como questões de
    prova as oficiais desta lição que estão em [arquivo(s) de content/provas], ligadas aos conceitos.
(c) Revisar o JSON anexado contra a checagem da Parte 8.1 e listar os problemas.

Como trabalhar:
1. Antes de escrever JSON, me mostre o plano: os conceitos da lição em ordem, o cenário de cada um e quantos
   exercícios por nível. Espere meu ok.
2. Depois, entregue um conceito por vez, em JSON válido.
3. No fim, faça a checagem da Parte 8.1, corrija o que falhar e entregue o relatório da Parte 8.2.
Nunca invente norma, versão ou artigo. Na dúvida, deixe a questão conceitual e avise no relatório.
```

---

## Parte 10 — Depois da IA (para quem vai abrir o PR)

Na pasta do projeto:

```bash
python3 tools/validate_content.py app/src/main/assets/content/    # pacote da unidade
python3 tools/validate_provas.py && python3 tools/build_provas.py   # provas: valida e regera o pacote do app
./gradlew test lint                                                 # se tiver o ambiente Android
```

- **Pacote do app:** não edite `app/src/main/assets/provas/` à mão. Ele é gerado pelo `tools/build_provas.py`.
- **Versão do pacote:** ao atualizar uma unidade que já existe, aumente `version` no pacote.
- **PR:** abra um PR por lição ou por prova, com o relatório de revisão. O fluxo completo está em `CONTRIBUTING.md`.

Referência viva: a Unidade 1 (`app/src/main/assets/content/u1-banco-de-dados.json`) é o exemplo completo do formato. Em
caso de dúvida sobre como um exercício deve ficar, siga o que ela faz.
