# G2 Raciocínio lógico (u12), lição 1: Lógica de relações. Gerado para o app por tools/gen_u16.py u12.
# Em cada exercício a alternativa certa vem PRIMEIRO; o gerador espalha a posição ("pos" fixa quando há ordem natural).
# Conteúdo de IA (não revisado). Roteiro dos conceitos: aulas de lógica da playlist "Bônus: Editora Opção" (Apostilas
# Opção, vídeos 42 a 53), usadas só como referência de ordem e de método; textos, cenários e números são próprios.

def A1(stem, c, w, exp, diff=1, style="caso_pratico"):
    return dict(type="quick", level="aprender", step=1, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def TF(stem, true, exp, diff=1, style="conceitual"):
    return dict(type="truefalse", level="aprender", step=2, stem=stem, true=true, exp=exp, diff=diff, style=style)

def QK(stem, c, w, exp, step=2, diff=2, style="caso_pratico"):
    return dict(type="quick", level="praticar", step=step, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def MT(stem, pairs, exp, step=3, diff=2):
    return dict(type="match", level="praticar", step=step, stem=stem, pairs=pairs, exp=exp, diff=diff, style="conceitual")

def PV(stem, c, w, exp, diff=2, style="conceitual", t="mcq5", pos=None):
    d = dict(type=t, level="prova", stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)
    if pos is not None:
        d["pos"] = pos
    return d


LESSONS = [
 dict(order=1, title="Lógica de relações", concepts=[
  dict(title="Conectivos e tabela-verdade", subtopic="conectivos",
   summary="Proposição é uma frase que é verdadeira (V) ou falsa (F). “p e q” só é V com as duas V; “p ou q” só é F "
           "com as duas F; “se p, então q” (p → q) só é F quando p é V e q é F; “p se e somente se q” (p ↔ q) é V "
           "quando as duas têm o mesmo valor. Com n proposições, a tabela-verdade tem 2ⁿ linhas. Sempre V: tautologia; sempre F: contradição; "
           "misto: contingência.",
   items=[
    A1("Exemplo: “O cliente tem conta e tem cartão” é falsa se ele não tiver cartão, porque o “e” exige as duas partes "
       "verdadeiras. Já o “ou” só é falso quando as duas partes são falsas. Agora: “O boleto foi pago ou foi cancelado” "
       "é falsa em qual situação?",
       "Não foi pago nem cancelado", ["Foi pago e não foi cancelado", "Foi cancelado e não foi pago"],
       "O “ou” é verdadeiro se pelo menos uma parte for verdadeira. Ele só falha quando nenhuma das duas acontece."),
    TF("No “se p, então q”, a única linha falsa é p verdadeira com q falsa. Assim, “Se chover, levo guarda-chuva” é "
       "verdadeira num dia sem chuva em que não levei guarda-chuva.", True,
       "Com a primeira parte falsa (não choveu), o condicional é verdadeiro, qualquer que seja a segunda. A promessa só "
       "é quebrada se chover e eu não levar."),
    QK("Regra: “se p, então q” só é falsa quando p é verdadeira e q é falsa. A sentença “Se a senha está errada, então "
       "o acesso é bloqueado” é FALSA quando:",
       "a senha está errada e o acesso não é bloqueado",
       ["a senha está certa e o acesso é bloqueado", "a senha está certa e o acesso não é bloqueado"],
       "Falsa só na combinação V e F: a senha errada (p verdadeira) e o acesso liberado (q falsa). Com a senha certa, a "
       "sentença é verdadeira em qualquer caso."),
    QK("A tabela-verdade tem 2ⁿ linhas, em que n é o número de proposições simples: com p e q, são 2² = 4 linhas. "
       "Quantas linhas tem a tabela de uma proposição formada por p, q e r?",
       "8", ["6", "9"],
       "Três proposições: 2³ = 2 × 2 × 2 = 8 linhas. 6 seria 2 × 3, e 9 seria 3², contas que não seguem a regra.",
       step=3, style="literal"),
    MT("Associe cada proposição composta à regra do seu valor lógico:",
       [("p e q", "Verdadeira só com p e q verdadeiras"), ("p ou q", "Falsa só com p e q falsas"),
        ("se p, então q", "Falsa só com p verdadeira e q falsa"),
        ("p se e somente se q", "Verdadeira quando p e q têm o mesmo valor")],
       "Conjunção exige as duas; disjunção basta uma; o condicional só falha em V → F; o bicondicional pede valores "
       "iguais."),
    PV("Uma proposição composta que resulta verdadeira em todas as linhas da sua tabela-verdade, quaisquer que sejam os "
       "valores das proposições simples, é chamada de",
       "tautologia.", ["contradição.", "contingência.", "equivalência.", "negação."],
       "Sempre verdadeira é tautologia. Contradição é sempre falsa; contingência tem linhas V e F. Equivalência é uma "
       "relação entre duas proposições, e negação é uma operação.",
       diff=1, style="literal"),
    PV("Sabe-se que a proposição p é verdadeira, q é falsa e r é verdadeira. Entre as proposições compostas a seguir, a "
       "única verdadeira é",
       "(p ou q) → r", ["p e q", "p → q", "(p e r) → q", "q ↔ r"],
       "(p ou q) é V, e V → r (V) dá V. As outras são falsas: p e q tem q falsa; p → q é V → F; (p e r) → q é V → F; "
       "q ↔ r compara F com V.",
       style="caso_pratico"),
   ]),
  dict(title="Negação de proposições compostas", subtopic="negação",
   summary="Negar “p e q” dá “não p ou não q”; negar “p ou q” dá “não p e não q”: nega as duas partes e troca o "
           "conectivo (leis de De Morgan). Negar “se p, então q” dá “p e não q”: mantém a primeira, nega a segunda e usa "
           "“e”. A negação de um condicional nunca é outro “se… então”.",
   items=[
    A1("Exemplo: a negação de “O caixa está aberto e tem fila” é “O caixa não está aberto ou não tem fila” (nega as "
       "duas partes e troca “e” por “ou”). Agora: a negação de “O sistema está lento ou fora do ar” é:",
       "O sistema não está lento e não está fora do ar",
       ["O sistema não está lento ou não está fora do ar", "O sistema está lento e não está fora do ar"],
       "Para negar o “ou”, as duas partes são negadas e o conectivo vira “e”. Manter o “ou” ou negar só uma parte não "
       "produz a negação."),
    TF("Para negar “se p, então q”, mantém-se p, nega-se q e usa-se “e”. Logo, a negação de “Se o pagamento atrasar, "
       "então haverá multa” é “O pagamento atrasa e não há multa”.", True,
       "A promessa é desmentida quando o atraso acontece e a multa não vem: p e não q."),
    QK("Regra: a negação de “se p, então q” é “p e não q”. Qual é a negação de “Se Rita é gerente, então ela assina o "
       "contrato”?",
       "Rita é gerente e não assina o contrato",
       ["Se Rita não é gerente, então não assina o contrato", "Rita não é gerente e assina o contrato"],
       "Mantém “Rita é gerente”, nega “assina o contrato” e liga com “e”. Trocar por outro “se… então” não nega nada."),
    QK("Um colega disse que a negação de “Lucas estudou e passou” é “Lucas não estudou e não passou”. Ele errou porque:",
       "o “e” deveria virar “ou”",
       ["só a segunda parte deveria ser negada", "a negação deveria ser um “se… então”"],
       "Negação correta: “Lucas não estudou ou não passou”. Basta uma das partes falhar para a frase original ser falsa; "
       "por isso o conectivo vira “ou”.",
       step=3),
    PV("Assinale a alternativa que NÃO apresenta a negação correta da sentença indicada.",
       "“Se Bia fica, Ana viaja”: “Se Bia não fica, Ana não viaja”.",
       ["“Ana viaja e Bia fica”: “Ana não viaja ou Bia não fica”.",
        "“Ana viaja ou Bia fica”: “Ana não viaja e Bia não fica”.",
        "“Se Ana viaja, Bia fica”: “Ana viaja e Bia não fica”.",
        "“Ana não viaja ou Bia fica”: “Ana viaja e Bia não fica”."],
       "A negação de “se p, então q” é “p e não q”: “Bia fica e Ana não viaja”. Negar as duas partes e manter o "
       "condicional não nega. As outras aplicam De Morgan ou a regra do condicional corretamente.",
       style="excecao"),
    PV("Um gerente afirmou: “Se a meta foi batida, então a equipe recebe bônus”. Mais tarde, verificou-se que a "
       "afirmação era falsa. Conclui-se que",
       "a meta foi batida e a equipe não recebeu bônus.",
       ["a meta não foi batida e a equipe recebeu bônus.", "a meta não foi batida e a equipe não recebeu bônus.",
        "a meta foi batida e a equipe recebeu bônus.", "a equipe recebeu bônus, mas a meta pode não ter sido batida."],
       "Condicional falso só na linha V → F: a meta batida (V) e o bônus não pago (F). Nas demais combinações a "
       "afirmação seria verdadeira.",
       style="caso_pratico"),
   ]),
  dict(title="Equivalências do condicional", subtopic="equivalência",
   summary="“Se p, então q” equivale a “se não q, então não p” (contrapositiva: inverte a ordem e nega as duas) e a "
           "“não p ou q”. Não equivale a “se q, então p” (recíproca) nem a “se não p, então não q” (inversa). Duas "
           "proposições são equivalentes quando têm a mesma tabela-verdade.",
   items=[
    A1("Exemplo: “Se é feriado, a agência fecha” equivale a “Se a agência não fecha, não é feriado” (inverte a ordem e "
       "nega as duas partes). Agora: “Se o cartão foi bloqueado, a compra é recusada” equivale a:",
       "Se a compra não é recusada, o cartão não foi bloqueado",
       ["Se a compra é recusada, o cartão foi bloqueado", "Se o cartão não foi bloqueado, a compra não é recusada"],
       "Contrapositiva: inverte e nega. Só inverter (recíproca) ou só negar (inversa) muda o sentido: a compra pode ser "
       "recusada por outro motivo."),
    TF("“Se p, então q” equivale a “não p ou q”. Assim, “Se estudo, passo” equivale a “Não estudo ou passo”.", True,
       "Nega a primeira parte, troca por “ou” e mantém a segunda. As duas formas só são falsas quando estudo e não passo."),
    QK("Regra: “se p, então q” equivale a “não p ou q”. A frase “Se o cliente é novo, ganha desconto” equivale a:",
       "O cliente não é novo ou ganha desconto",
       ["O cliente é novo ou não ganha desconto", "O cliente é novo e ganha desconto"],
       "Nega “é novo”, usa “ou” e mantém “ganha desconto”. A forma com “e” afirma as duas coisas, o que o condicional "
       "não garante."),
    MT("Associe cada frase obtida de “Se p, então q” ao que ela é:",
       [("Se não q, então não p", "Contrapositiva, equivalente"), ("Se q, então p", "Recíproca, não equivalente"),
        ("Se não p, então não q", "Inversa, não equivalente"), ("Não p ou q", "Disjunção equivalente")],
       "Só a contrapositiva e a forma “não p ou q” mantêm a tabela-verdade. Recíproca e inversa erram na linha em que "
       "p e q têm valores diferentes."),
    PV("Uma norma interna diz: “Se o valor passa de R$ 10 mil, então o gerente aprova a transferência”. Uma frase "
       "logicamente equivalente a essa é:",
       "Se o gerente não aprova a transferência, então o valor não passa de R$ 10 mil.",
       ["Se o gerente aprova a transferência, então o valor passa de R$ 10 mil.",
        "Se o valor não passa de R$ 10 mil, então o gerente não aprova a transferência.",
        "O valor passa de R$ 10 mil e o gerente aprova a transferência.",
        "O valor passa de R$ 10 mil ou o gerente não aprova a transferência."],
       "Contrapositiva: inverte e nega. A recíproca e a inversa não são equivalentes; a forma com “e” afirma os fatos; "
       "a disjunção correta seria “o valor não passa de R$ 10 mil ou o gerente aprova”.",
       style="caso_pratico"),
    PV("Todas as frases a seguir são logicamente equivalentes a “Se Pedro não pagou, então recebeu aviso”, EXCETO:",
       "Se Pedro pagou, então não recebeu aviso.",
       ["Se Pedro não recebeu aviso, então pagou.", "Pedro pagou ou recebeu aviso.",
        "Não é verdade que Pedro não pagou e não recebeu aviso.", "Pedro recebeu aviso ou pagou."],
       "Negar as duas partes sem inverter (inversa) não é equivalente. A contrapositiva, a forma “não p ou q” (com "
       "“não p” = “pagou”), a dupla negação do “p e não q” e a troca de ordem do “ou” mantêm o sentido.",
       diff=3, style="excecao"),
   ]),
  dict(title="Todo, algum e nenhum", subtopic="quantificadores",
   summary="Negação: “todo A é B” vira “algum A não é B”; “algum A é B” vira “nenhum A é B”; “nenhum A é B” vira "
           "“algum A é B”. “Todo A é B” põe o grupo A dentro de B: quem é A é B, mas quem é B pode não ser A. Para "
           "concluir, desenhe os conjuntos e veja o que é obrigatório.",
   items=[
    A1("Exemplo: para negar “Todo boleto venceu”, basta um contraexemplo: “Algum boleto não venceu”. Agora: a negação de "
       "“Todo cliente tem senha” é:",
       "Algum cliente não tem senha", ["Nenhum cliente tem senha", "Algum cliente tem senha"],
       "Um único cliente sem senha já derruba o “todo”. “Nenhum” exagera, e “algum tem” é compatível com a frase "
       "original."),
    TF("A negação de “Nenhum caixa está fechado” é “Algum caixa está fechado”.", True,
       "Para desmentir “nenhum”, basta um caso: um caixa fechado."),
    QK("Se é verdade que “todo gerente é funcionário” e que Carla é gerente, então:",
       "Carla é funcionária", ["Nada se pode concluir sobre Carla", "Todo funcionário é gerente"],
       "Carla está dentro do grupo dos gerentes, que está inteiro dentro do grupo dos funcionários. A frase não diz "
       "que todo funcionário é gerente."),
    QK("Sabe-se que todo auditor é contador e que Lia é contadora. O que se conclui?",
       "Lia pode ser ou não auditora", ["Lia é auditora", "Lia não é auditora"],
       "O grupo dos contadores é maior que o dos auditores. Lia está entre os contadores, mas pode estar fora do grupo "
       "dos auditores: nada é obrigatório.",
       step=3),
    PV("A negação lógica da sentença “Algum relatório foi entregue com atraso” é:",
       "Nenhum relatório foi entregue com atraso.",
       ["Algum relatório não foi entregue com atraso.", "Todo relatório foi entregue com atraso.",
        "Algum relatório foi entregue no prazo.", "Nem todo relatório foi entregue com atraso."],
       "A negação de “algum A é B” é “nenhum A é B”. As outras são compatíveis com a frase original: podem ser "
       "verdadeiras junto com ela.",
       diff=1, style="literal"),
    PV("Em uma agência, são verdadeiras as afirmações: “Todo caixa é concursado” e “Nenhum estagiário é concursado”. "
       "É correto concluir que",
       "nenhum caixa é estagiário.",
       ["algum caixa é estagiário.", "todo concursado é caixa.", "algum estagiário é caixa.",
        "todo concursado é estagiário."],
       "O grupo dos caixas está dentro do dos concursados, e os estagiários estão todos fora dele: os caixas e os "
       "estagiários não se tocam. As demais afirmações não decorrem das premissas.",
       style="caso_pratico"),
   ]),
  dict(title="Dedução a partir de premissas", subtopic="dedução",
   summary="Comece pela premissa simples (sem “se” nem “ou”) e trate-a como verdade. Em “se p, então q”: p verdadeira "
           "obriga q; q falsa obriga p falsa. Em “p ou q”: p falsa obriga q. Saber que q é verdadeira não diz nada sobre "
           "p. Em quantidades, teste o caso-limite: 23 tarefas entre 4 pessoas obrigam alguém a fazer 6 ou mais.",
   items=[
    A1("Exemplo: de “Se o sistema cai, o atendimento para” e “O sistema caiu”, conclui-se que o atendimento parou. "
       "Agora: de “Se a taxa sobe, o crédito encarece” e “A taxa subiu”, conclui-se que:",
       "o crédito encareceu", ["a taxa vai voltar a cair", "nada se pode concluir"],
       "A primeira parte do condicional aconteceu, então a segunda é obrigatória."),
    TF("De “Se Bruno viajou, então perdeu a reunião” e “Bruno não perdeu a reunião”, conclui-se que Bruno não viajou.",
       True,
       "Se ele tivesse viajado, teria perdido a reunião. Como não perdeu, não viajou (a segunda parte falsa obriga a "
       "primeira a ser falsa)."),
    QK("Regra: em “p ou q”, se p é falsa, q é verdadeira. São verdadeiras: “Davi está na agência ou em casa” e “Davi "
       "não está em casa”. Logo:",
       "Davi está na agência", ["Davi está em casa", "Davi não está na agência"],
       "Uma das duas partes do “ou” precisa valer. Como “em casa” é falsa, “na agência” é verdadeira."),
    QK("São verdadeiras: “Se Elisa é gerente, então tem a chave do cofre” e “Elisa tem a chave do cofre”. O que se "
       "conclui?",
       "Nada sobre Elisa ser gerente", ["Elisa é gerente", "Elisa não é gerente"],
       "A segunda parte verdadeira não obriga a primeira: outras pessoas também podem ter a chave. Concluir que ela é "
       "gerente é o erro clássico de inverter o condicional.",
       step=3),
    QK("Caso-limite: se 23 tarefas vão para 4 pessoas, alguém faz ao menos 6, porque 4 × 5 = 20 não chega a 23. Se 31 "
       "boletos são conferidos por 6 analistas, pelo menos um deles confere no mínimo:",
       "6 boletos", ["5 boletos", "7 boletos"],
       "Se cada um conferisse no máximo 5, seriam 6 × 5 = 30 boletos, menos que 31. Logo alguém confere 6 ou mais. "
       "Nada obriga alguém a chegar a 7.",
       step=3),
    PV("Considere verdadeiras as sentenças: 1. Se Caio é analista, então Lia é auditora. 2. Se Lia é auditora, então "
       "Rui é contador. 3. Rui não é contador. Deduz-se que",
       "Caio não é analista e Lia não é auditora.",
       ["Caio é analista e Lia não é auditora.", "Caio não é analista e Lia é auditora.",
        "Caio é analista e Lia é auditora.", "Lia não é auditora, mas Caio pode ser analista."],
       "Comece pela 3: Rui não é contador. Pela 2, Lia não pode ser auditora. Pela 1, Caio não pode ser analista. Cada "
       "segunda parte falsa derruba a primeira.",
       style="caso_pratico"),
    PV("Uma agência tem 5 caixas, que atenderam juntos 53 clientes numa manhã. É correto concluir que",
       "pelo menos um caixa atendeu 11 clientes ou mais.",
       ["cada caixa atendeu pelo menos 10 clientes.", "algum caixa atendeu exatamente 11 clientes.",
        "nenhum caixa atendeu mais de 11 clientes.", "pelo menos um caixa atendeu 12 clientes ou mais."],
       "Com no máximo 10 cada, seriam 50 clientes, menos que 53: alguém atendeu 11 ou mais. Nada impede que um caixa "
       "atenda poucos e outro muitos, então as outras afirmações não são garantidas.",
       diff=3, style="caso_pratico"),
   ]),
  dict(title="Conjuntos e diagramas", subtopic="conjuntos",
   summary="Quando todos estão em pelo menos um de dois grupos: total = só A + só B + os dois. Comece pela interseção e "
           "desconte-a de cada grupo. União (A ∪ B): está em A ou em B. Interseção (A ∩ B): está nos dois. Diferença "
           "(A − B): está em A e não está em B.",
   items=[
    A1("Exemplo: 10 clientes têm cartão, 7 têm seguro e 4 têm os dois. Só cartão: 10 − 4 = 6. Agora: 12 funcionários "
       "falam inglês, 8 falam espanhol e 5 falam os dois idiomas. Quantos falam só inglês?",
       "7", ["12", "3"],
       "Desconta a interseção: 12 − 5 = 7. O número 12 inclui quem fala os dois, e 3 é quem fala só espanhol (8 − 5)."),
    TF("Num grupo em que todos usam Pix ou boleto, 9 pessoas usam Pix, 6 usam boleto e 2 usam os dois. O grupo tem 13 "
       "pessoas.", True,
       "Somar 9 + 6 conta duas vezes quem usa os dois. Total: 9 + 6 − 2 = 13 (só Pix 7, só boleto 4, os dois 2)."),
    QK("Regra: total = só A + só B + os dois = A + B − os dois. Numa equipe de 20 pessoas, todas usam planilha ou "
       "painel: 14 usam planilha e 11 usam painel. Quantas usam os dois?",
       "5", ["3", "6"],
       "14 + 11 = 25 passa do total em 5: são as pessoas contadas duas vezes, que usam os dois."),
    MT("Associe cada operação entre conjuntos ao que ela reúne:",
       [("União (A ∪ B)", "Está em A ou em B"), ("Interseção (A ∩ B)", "Está em A e em B"),
        ("Diferença (A − B)", "Está em A e não está em B"), ("Complementar de A", "Não está em A")],
       "União junta tudo; interseção pega o comum; diferença tira de A o que também está em B; complementar é o resto "
       "do universo."),
    PV("Uma pesquisa com 50 clientes perguntou se usam o aplicativo ou o internet banking; todos usam pelo menos um "
       "deles. O número dos que usam só o aplicativo é o dobro do número dos que usam só o internet banking, e 8 usam "
       "os dois. Quantos clientes usam só o internet banking?",
       "14.", ["16.", "21.", "28.", "42."],
       "Só internet banking = x, só aplicativo = 2x: x + 2x + 8 = 50, então 3x = 42 e x = 14. 28 é o número de quem usa "
       "só o aplicativo, e 42 é quem usa só um dos dois.",
       style="caso_pratico", pos=0),
    PV("Sejam A = {1, 2, 3, 4} e B = {3, 4, 5}. O conjunto dos elementos que estão em A ou em B, mas não nos dois, é",
       "{1, 2, 5}", ["{3, 4}", "{1, 2}", "{5}", "{1, 2, 3, 4, 5}"],
       "União {1, 2, 3, 4, 5} menos a interseção {3, 4} dá {1, 2, 5}. {1, 2} é só A − B, {5} é só B − A, e {3, 4} é a "
       "interseção.",
       style="literal"),
   ]),
  dict(title="Associações e posições", subtopic="associação lógica",
   summary="Monte uma tabela pessoa × característica e marque X no que cada dica proíbe. Quando sobra uma opção numa "
           "linha, ela é a resposta e sai das outras linhas. Em filas, posição = pessoas à frente + 1. Em mesas, fixe "
           "uma pessoa e posicione as outras em relação a ela; direita e esquerda são de quem está sentado.",
   items=[
    A1("Exemplo: Ana, Beto e Caio são caixa, gerente e analista, um cargo para cada. Ana não é caixa nem gerente: logo, "
       "é analista, e esse cargo sai das outras linhas. Agora, sabendo também que Beto não é caixa, qual é o cargo de "
       "Beto?",
       "Gerente", ["Caixa", "Analista"],
       "Analista já é da Ana, e caixa foi eliminado pela dica: sobra gerente. Caio fica com caixa."),
    TF("Numa fila, se há 6 pessoas na frente de Rui, ele ocupa a 7ª posição.", True,
       "Posição = pessoas à frente + 1: 6 + 1 = 7."),
    QK("Posição = pessoas à frente + 1. Numa fila de 20 pessoas, Sara tem 4 pessoas à sua frente. Quantas pessoas estão "
       "atrás dela?",
       "15", ["16", "14"],
       "Sara é a 5ª (4 + 1). Atrás dela: 20 − 5 = 15. O 16 esquece de descontar a própria Sara."),
    QK("Quatro amigos sentam numa mesa quadrada, um de cada lado. Tânia está em frente a Ulisses, e Vera está à direita "
       "de Tânia. Onde está Xavier?",
       "Em frente a Vera", ["À direita de Tânia", "Em frente a Tânia"],
       "Fixando Tânia, Ulisses fica em frente e Vera à direita dela. Sobra o lugar à esquerda de Tânia, que fica em "
       "frente ao de Vera.",
       step=3),
    PV("Rui, Sol e Teo trabalham nas agências de Vitória, Serra e Vila Velha, cada um numa agência diferente. Sabe-se "
       "que Rui não trabalha em Vitória nem na Serra e que Sol não trabalha na Serra. Conclui-se que",
       "Teo trabalha na Serra e Sol, em Vitória.",
       ["Teo trabalha em Vitória e Sol, na Serra.", "Rui trabalha na Serra e Teo, em Vitória.",
        "Sol trabalha em Vila Velha e Rui, em Vitória.", "Teo trabalha em Vila Velha e Rui, na Serra."],
       "Rui só pode estar em Vila Velha. Sol não está na Serra nem em Vila Velha (já ocupada): Vitória. Sobra a Serra "
       "para Teo.",
       style="caso_pratico"),
    PV("Cinco contas de uma família venceram em dias diferentes de uma mesma semana, de segunda a sexta-feira. A de "
       "internet venceu na quarta-feira e a de aluguel, na sexta. A de gás venceu um dia depois da de água, e a de luz "
       "venceu depois da de água. Em que dia venceu a conta de luz?",
       "Quinta-feira.", ["Segunda-feira.", "Terça-feira.", "Quarta-feira.", "Sexta-feira."],
       "Sobram segunda, terça e quinta. Gás no dia seguinte à água só cabe em água na segunda e gás na terça. Luz, "
       "depois da água, fica com a quinta.",
       diff=3, style="caso_pratico", pos=3),
   ]),
 ]),
]
