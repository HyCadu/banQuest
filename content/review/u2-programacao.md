# Revisão técnica — U2 Programação e orientação a objetos (pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Todos os 154 exercícios
estão com `source: "ia"` e `reviewed: false`.

## Escopo

Cinco lições no formato por conceitos, todas dentro do Anexo IV do edital (Programação, cargo 04):

| Lição | Conceitos | Exercícios | Oficiais ligadas |
|---|---|---|---|
| u2l1 Fundamentos de programação | tipos e representação · operadores · controle e repetição · strings e coleções · exceções · compilação e acesso a dados | 36 | 10 |
| u2l2 Orientação a objetos | classes e construtores · encapsulamento e pacotes · herança e abstratas · interfaces · sobrecarga e sobrescrita | 30 | 2 |
| u2l3 Princípios SOLID | SRP · OCP · LSP · ISP · DIP | 28 | 2 |
| u2l4 Linguagens e plataformas | Java · C# e .NET · Python · PHP · C | 30 | 6 |
| u2l5 Testes, TDD e Git | unitário × integração · TDD e BDD · Git · GitFlow, PR e code review · IA assistente de código | 30 | 2 |

Cada conceito tem 2 exercícios de aprender e 2 de praticar (passos 1, 2 e 3, num cenário próprio) e de 1 a 2 questões de
prova no estilo FCC. Nos SRP e OCP da u2l3, a segunda questão de prova é a oficial (FCC DPE-RR 2015 q52, "the blob", e FCC
Teresina 2016 q23, Factory Method).

As cinco linguagens do edital aparecem com peso igual (decisão do CONTENT_PLAN): na u2l4, cada uma tem o seu conceito e
uma questão de saída de código; nas lições 1 e 2, os trechos alternam entre Java, C#, Python e C.

## Conferência das respostas

- **Código executado** (Java 17, .NET 6, Python 3.12), todas com a saída igual à alternativa marcada:
  `total / 2` em double → `3.0`; `a++ * 2 + --b` → `9 4`; switch sem break → `BC`; return dentro de try com finally →
  `F1`; contador static → `1 3`; sobrescrita com variável da superclasse → `C0.0`; `equals` e `length` → `true 8`;
  `List<int>.Remove(1)` remove o valor → `3 2`; fatiamento → `['Bia', 'Caio'] Eva`; valor padrão mutável → `[1, 2]`.
- **Conferido à mão** (não há PHP nem compilador C neste ambiente): array associativo PHP → `3-80`; troca por ponteiros
  em C → `2 1`; `char nome[] = "Ana"` ocupa 4 bytes.
- Nenhuma afirmação de versão de linguagem. Em .NET, só que o .NET atual é multiplataforma (o que torna errada a
  alternativa "só executa em Windows").

## Ajustes desta passada

- A correta era a alternativa mais longa em 79% das questões de prova. Foram alongados distratores (com conteúdo
  plausível) e enxugadas corretas em 22 questões: agora 24%.
- Cinco questões de saída de código ganharam cenário (caso prático), levando os casos práticos de 28% para acima da meta
  de 30%.
- u2l1q21: as alternativas `"ana"`, `"ANA"` e `"Ana"` são iguais para o validador (que compara em minúsculas); passaram a
  ter rótulo ("sem mudança", "em maiúsculas").
- Exercícios de ensino não entregam as oficiais: o acesso a banco é ensinado em Python (DB-API) para não antecipar a
  oficial de `PreparedStatement`/`ResultSet` (FCC DPE-RR q42 e Teresina q38); o PHP por referência (`&$a`) não aparece em
  nenhum exercício, porque é a resposta da FCC DPE-RR q43.

## Questões oficiais ligadas aos conceitos

As 22 questões de provas anteriores marcadas para a U2 ganharam `concept` em `content/provas` (sem isso o planejador
nunca as sorteia). Algumas cobram assuntos que o edital não lista e foram para o conceito mais próximo:

| Questão | Assunto | Conceito |
|---|---|---|
| FGV 2018 q60, FGV 2021 q43 | ponto flutuante, complemento a dois | u2l1c1 tipos e representação |
| FGV 2018 q67 | `++m` em laço | u2l1c2 operadores |
| FGV 2018 q53 e q69, FGV 2021 q36 | primos, MDC por subtrações, bubble sort | u2l1c3 controle e repetição |
| FGV 2021 q38 | tabela hash O(1) | u2l1c4 strings e coleções |
| FGV 2018 q68 | try/catch/finally | u2l1c5 exceções |
| FCC DPE-RR q42, Teresina q38 | JDBC | u2l1c6 compilação e acesso a dados |
| FCC DPE-RR q37 | sobrecarga | u2l2c5 |
| FGV 2021 q55 | propriedade C# | u2l2c2 encapsulamento |
| FCC DPE-RR q52 | anti-pattern "the blob" | u2l3c1 SRP |
| FCC Teresina q23 | Factory Method | u2l3c2 OCP (a subclasse estende a criação sem mudar o criador) |
| FCC DPE-RR q41 | NetBeans | u2l4c1 Java |
| FGV 2018 q54 e q70 | `#if DEBUG`, Hashtable | u2l4c2 C# e .NET |
| FCC Teresina q37, FGV 2021 q53 | Python | u2l4c3 Python |
| FCC DPE-RR q43 | passagem por referência | u2l4c4 PHP |
| FCC Teresina q39, q40 | Mercurial, ciclo do TDD | u2l5c3 Git, u2l5c2 TDD |

## Calibração com provas do cargo

Além das 22 oficiais, foram lidas as provas do Banestes de 2012 (IDECAN, arquivo `banestes2011_prova.pdf`) e 2024
(4 alternativas por questão) para o mesmo cargo. Elas cobram leitura de código Java e C/C++, polimorfismo, herança e
abstração, e acesso a banco com parâmetros em Python, temas que este pacote cobre. Com os gabaritos enviados depois, as
duas foram transcritas na 0.11.0 (`content/provas/idecan-banestes-2012.json` e `access-banestes-2024.json`), e as
questões delas que caem na U2 entraram ligadas aos conceitos desta unidade.

## Para o dono olhar com atenção

- u2l1 (complemento a dois): o distrator "−127 a 127" é explicado como o intervalo de sinal-magnitude e de complemento a
  um.
- u2l3c3 (LSP): o passo 3 pede a solução para um boleto que não admite estorno ("rever a hierarquia"). É a resposta
  aceita na literatura, mas o tema admite discussão.
- u2l5c5 (IA assistente de código): conteúdo só conceitual, sem citar ferramentas.

## Validação

`python tools/validate_content.py app/src/main/assets/content/`: sem erros. Avisos: só o deste relatório antes de ser
gravado e um antigo da U1 (u1l2q09). Testes e lint: ver `docs/QA.md`, versão 0.10.0.
