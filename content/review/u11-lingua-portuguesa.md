# Revisão técnica — G1 Língua Portuguesa (`u11`, pacote v1)

Segunda passada, separada da geração e feita pela mesma IA. Não substitui a revisão do dono. Todos os 168 exercícios
estão com `source: "ia"` e `reviewed: false`.

## Escopo

Cinco lições no formato por conceitos, dentro do Anexo IV do edital (Língua Portuguesa, comum a todos os cargos):

| Lição | Conceitos | Exercícios | Oficiais ligadas |
|---|---|---|---|
| u11l1 Ortografia e crase | acentuação · Acordo Ortográfico e hífen · grafias que confundem · crase obrigatória · crase proibida e facultativa | 30 | 0 |
| u11l2 Interpretação de texto | tese e finalidade · inferência e pressupostos · coesão, coerência e ambiguidade · sentido das palavras e figuras · discursos · intertextualidade e contexto | 36 | 22 |
| u11l3 Morfologia | formação de palavras · sinônimos e parônimos · pronomes e artigos · flexão · vozes do verbo · tempos e modos | 36 | 4 |
| u11l4 Sintaxe | termos da oração e referência · concordância verbal · concordância nominal · regência · coordenação e subordinação · conectivos | 36 | 5 |
| u11l5 Pontuação e reescrita | vírgula (o que não se separa) · vírgula (intercalações) · outros sinais · reescrita · concisão e redundância | 30 | 4 |

Cada conceito tem 2 exercícios de aprender e 2 de praticar (passos 1, 2 e 3) e 2 questões de prova no estilo FCC. Os
textos de interpretação são autorais e curtos. Nenhum é copiado de jornal ou livro; os versos citados (Gonçalves Dias,
Fernando Pessoa, Drummond, Shakespeare) são trechos de domínio comum, usados como base de paródia.

## Critérios de correção

Só entram regras da norma-padrão que os gramáticos tradicionais adotam sem divergência. Onde há variação aceita, a
questão foi montada para que a variação não decida a resposta:
- **Crase facultativa** (possessivo feminino, nome próprio feminino, depois de "até"): usada só na questão de exceção,
  em que todas as alternativas erradas são facultativas e a certa é obrigatória.
- **"Comparecer a/em"**: na questão de regência (u11l4c4), todos os distratores têm outro erro inequívoco, então a
  resposta não depende dessa preposição.
- **"A maioria de" + verbo** (singular ou plural): citado só no resumo, sem questão.
- **"Visar a"** (ter como objetivo): mantido pela norma tradicional, que é a que as bancas cobram.
- **Acordo Ortográfico**: regras de acentuação (ditongos abertos, hiatos, trema, diferenciais) e de hífen com prefixos
  em vogal, h, r e s. Sem casos controversos, como "co-" e "re-".

## Ajustes desta passada

- A correta era a alternativa mais longa em 41% das questões de prova; com 10 distratores alongados, caiu para 20%.
- Distrator "Embora o banco o tenha lançado em março, o aplicativo já tem…" (u11l5c4) saiu: dava para lê-lo como
  concessão legítima. Entrou um erro inequívoco de concordância ("que já têm").
- Distrator "resultado final" (u11l5c5) saiu: nem todo gramático o considera pleonasmo. Entrou "repetiu de novo".
- Os resumos dos conceitos aparecem como dica nos exercícios de ensino, então não podem trazer a resposta das questões
  de prova. Saíram "embarcar" e "pontapé" (u11l3c1) e "previr" e "detiveram" (u11l3c4), trocados por outros exemplos da
  mesma regra.
- Três enunciados curtos e quase iguais ("… está de acordo com a norma-padrão em:") foram reescritos.

## Questões oficiais ligadas aos conceitos

As 35 questões de Português das provas FGV Banestes 2018 e 2021 ganharam `concept` em `content/provas`:

| Conceito | Questões |
|---|---|
| u11l2c1 tese, finalidade e situação | 2018 q1, q2, q9, q13 · 2021 q2, q3, q13 (fatores da comunicação) |
| u11l2c2 inferência e pressupostos | 2018 q10 (pergunta que condiciona a resposta), q11, q12, q16 (charge) · 2021 q4, q5, q6 |
| u11l2c3 coesão, coerência e ambiguidade | 2018 q3 (valor dos parênteses), q7 · 2021 q1, q14 (exemplificação), q15 (informatividade) |
| u11l2c4 sentido das palavras e figuras | 2018 q20 · 2021 q12 (campo lexical) |
| u11l2c6 intertextualidade | 2018 q18 |
| u11l3c1 formação de palavras | 2018 q17 (locução adjetiva) · 2021 q9 (estrangeirismos) |
| u11l3c2 sinônimos e parônimos | 2018 q15 |
| u11l3c3 pronomes e artigos | 2018 q19 (artigo) |
| u11l4c1 termos da oração e referência | 2018 q5 (referente dos pronomes) |
| u11l4c5 coordenação e subordinação | 2018 q4 (paralelismo) |
| u11l4c6 conectivos | 2018 q8 · 2021 q7, q11 |
| u11l5c4 reescrita | 2021 q10 |
| u11l5c5 concisão e clareza | 2018 q6, q14 · 2021 q8 (títulos jornalísticos) |

A lição u11l1 (ortografia e crase) não tem questão oficial: nenhuma das provas transcritas cobra o assunto diretamente.

## Calibração com provas do cargo

Além das FGV, foram lidas as provas do Banestes de 2012 (IDECAN) e 2024. A de 2012 cobra concordância, reescrita,
pontuação (travessão), coesão referencial, conectivos e finalidade do texto. A de 2024 cobra tipologia textual, tese,
aposto, intertextualidade, pronome relativo, classificação de orações substantivas e acentuação, com 4 alternativas.
Todos esses temas estão nas lições. **As duas não foram transcritas**: vieram sem gabarito.

## Para o dono olhar com atenção

- u11l2c6: "Ser ou não ser cliente, eis a questão" classificado como paródia. A rigor, há quem chame de alusão; a
  explicação justifica pela intenção humorística.
- u11l3c1: "embarcar" como derivação parassintética e "aguardente" como aglutinação. É a classificação usual dos
  livros, mas alguns autores a discutem.
- u11l3c3: a questão sobre "este" e "esse" segue a regra tradicional (este = o que ainda vai ser dito, esse = o que já
  foi dito). No uso corrente, muita gente troca um pelo outro.

## Validação

`python tools/validate_content.py app/src/main/assets/content/`: sem erros. Testes e lint: ver `docs/QA.md`, versão
0.10.0.
