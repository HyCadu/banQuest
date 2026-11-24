# G5 Matemática financeira (u15), lição 4: Avaliação de projetos. Gerado para o app por tools/gen_u16.py u15.
# Só a trilha do Analista vê esta lição (o edital do Técnico não cita VPL, TIR nem capitalização contínua). Mesmo
# formato da lição 1 (certa primeiro; PVS ordena alternativas numéricas; um assert por conta e por distrator
# justificado; potências dadas no enunciado). Conteúdo de IA (não revisado). A apostila do cargo (OP-121NV-26) não trata
# VPL, TIR nem capitalização contínua; as definições seguem o uso corrente, sem norma nem autor citado.
from fractions import Fraction as F
from math import exp


def A1(stem, c, w, exp_, diff=1, style="caso_pratico"):
    return dict(type="quick", level="aprender", step=1, stem=stem, correct=c, wrong=w, exp=exp_, diff=diff, style=style)

def TF(stem, true, exp_, diff=1, style="conceitual"):
    return dict(type="truefalse", level="aprender", step=2, stem=stem, true=true, exp=exp_, diff=diff, style=style)

def QK(stem, c, w, exp_, step=2, diff=2, style="caso_pratico"):
    return dict(type="quick", level="praticar", step=step, stem=stem, correct=c, wrong=w, exp=exp_, diff=diff,
                style=style)

def PV(stem, c, w, exp_, diff=2, style="conceitual", t="mcq5", pos=None):
    d = dict(type=t, level="prova", stem=stem, correct=c, wrong=w, exp=exp_, diff=diff, style=style)
    if pos is not None:
        d["pos"] = pos
    return d

def PVS(stem, options, c, exp_, diff=2, style="caso_pratico", t="case"):
    """Alternativas numéricas já em ordem crescente; a certa fica na posição natural."""
    assert c in options and len(options) == 5 and len(set(options)) == 5, stem
    return PV(stem, c, [o for o in options if o != c], exp_, diff=diff, style=style, t=t, pos=options.index(c))


def r2(x):
    return round(float(x) + 1e-9, 2)


def vpl(rate, flows):
    return sum(f / (1 + rate) ** t for t, f in enumerate(flows))


# --- contas conferidas ---------------------------------------------------------------------------------------------
assert 2420 / F(121, 100) == 2000 and 2420 / F(11, 10) == 2200 and 2420 * F(8, 10) == 1936
assert 1100 / F(11, 10) + 1210 / F(121, 100) == 2000 and 1100 + 1210 == 2310 and 1100 + 1210 / F(121, 100) == 2100
assert 3300 / F(11, 10) + 3630 / F(121, 100) == 6000 and 3000 + 3630 / F(11, 10) == 6300 and 3300 + 3630 == 6930
assert 4620 / F(11, 10) - 4000 == 200 and 4620 - 4000 == 620 and 4620 / F(11, 10) == 4200
assert vpl(F(1, 10), [-10000, 6600, 7260]) == 2000 and 6600 + 7260 - 10000 == 3860 and 6600 + 7260 == 13860
assert vpl(0.08, [-1000, 600, 600]) > vpl(0.12, [-1000, 600, 600])
assert F(2360, 2000) - 1 == F(18, 100) and r2(F(360, 2360) * 100) == 15.25
assert F(11, 10) ** 2 == F(121, 100)
assert F(12, 10) ** 2 == F(144, 100)
assert vpl(F(15, 100), [-20000, 11500, 13225]) == 0
assert vpl(0.10, [-20000, 11500, 13225]) > 0 > vpl(0.20, [-20000, 11500, 13225])
assert r2((11500 + 13225) / 20000 - 1) == 0.24 and abs((11500 + 13225) / 20000 - 1.23625) < 1e-9
assert r2(5000 * 1.1052) == 5526 and 5000 * F(105, 100) ** 2 == F(551250, 100) and abs(exp(0.1) - 1.1052) < 1e-4
assert abs(exp(0.2) - 1.2214) < 1e-4 and F(11, 10) ** 2 == F(121, 100)
assert abs(exp(0.3) - 1.3499) < 1e-4 and r2(10000 * 1.3499) == 13499 and 10000 * F(1331, 1000) == 13310
assert r2(20000 * 1.35) == 27000 and r2(20000 * (1 + 0.06 * 5)) == 26000 and r2(20000 * 1.06 ** 5) == 26764.51
assert r2(20000 * 1.06) == 21200 and r2(20000 * exp(0.06)) == 21236.73
assert exp(0.12) > 1.01 ** 12                              # contínua rende mais que a mensal, mesma nominal


LESSONS = [
 dict(order=4, title="Avaliação de projetos", concepts=[
  dict(title="Fluxo de caixa e TMA", subtopic="fluxo de caixa",
   summary="Fluxo de caixa: as entradas (+) e as saídas (−) de dinheiro em cada data; o investimento inicial fica na "
           "data 0, com sinal negativo. TMA (taxa mínima de atratividade) é o retorno que a empresa exige, o custo de "
           "oportunidade do dinheiro. Para comparar valores de datas diferentes, traga tudo à data 0 descontando pela "
           "TMA: VP = FC ÷ (1 + TMA)ᵗ.",
   items=[
    A1("Exemplo: com TMA de 10% ao ano, receber R$ 1.210 daqui a 2 anos vale hoje 1.210 ÷ 1,21 = R$ 1.000. Agora: um "
       "projeto paga R$ 2.420 daqui a 2 anos. Com TMA de 10% ao ano, quanto isso vale hoje?",
       "R$ 2.000", ["R$ 2.200", "R$ 1.936"],
       "2.420 ÷ 1,21 = 2.000. O R$ 2.200 desconta só um ano; o R$ 1.936 tira 20% do valor, que não é o mesmo que "
       "dividir por 1,21."),
    TF("No fluxo de caixa de um projeto, o investimento inicial aparece na data 0, com sinal negativo.", True,
       "É uma saída de dinheiro no início do projeto; as entradas futuras aparecem nas datas seguintes, com sinal "
       "positivo.",
       style="literal"),
    QK("A TMA é o custo de oportunidade: o que o dinheiro renderia na melhor alternativa disponível. Uma empresa "
       "consegue 12% ao ano sem risco no mercado. Que TMA faz sentido para avaliar os projetos dela?",
       "pelo menos 12% ao ano", ["0%, porque o dinheiro é próprio", "a inflação do ano"],
       "Um projeto que rende menos que 12% perde para a aplicação sem risco. Dinheiro próprio também tem custo de "
       "oportunidade, e a inflação, sozinha, não remunera o capital."),
    QK("Traga cada entrada pela TMA e some. Entradas de R$ 1.100 em 1 ano e de R$ 1.210 em 2 anos, com TMA de 10% ao "
       "ano. Qual é o valor presente das entradas?",
       "R$ 2.000", ["R$ 2.310", "R$ 2.100"],
       "1.100 ÷ 1,1 + 1.210 ÷ 1,21 = 1.000 + 1.000 = 2.000. O R$ 2.310 soma sem descontar; o R$ 2.100 desconta só a "
       "segunda entrada.",
       step=3),
    PVS("As entradas de um projeto são de R$ 3.300,00 no fim do 1º ano e de R$ 3.630,00 no fim do 2º; o investimento "
        "inicial é de R$ 5.000,00. Descontadas pela TMA de 10% ao ano, essas entradas valem hoje",
        ["R$ 1.000,00", "R$ 5.000,00", "R$ 6.000,00", "R$ 6.300,00", "R$ 6.930,00"], "R$ 6.000,00",
        "3.300 ÷ 1,1 + 3.630 ÷ 1,21 = 3.000 + 3.000 = 6.000. O 1.000 é o VPL (entradas menos investimento), não o "
        "valor das entradas; o 6.300 desconta a 2ª entrada por um ano só; o 6.930 não desconta; o 5.000 é o "
        "investimento."),
    PV("Na análise de investimentos, a taxa mínima de atratividade (TMA) representa",
       "o retorno mínimo exigido, equivalente ao custo de oportunidade do capital.",
       ["a taxa que zera o valor presente líquido do projeto.", "a inflação oficial do período de análise.",
        "o prazo necessário para recuperar o investimento inicial.", "a taxa de juros paga pela poupança no período."],
       "A TMA é o retorno que se exige para aceitar o projeto, porque é o que o dinheiro renderia em outra alternativa. "
       "A taxa que zera o VPL é a TIR; o prazo de recuperação é outra medida; inflação e poupança não definem a TMA.",
       diff=1, style="literal", pos=4),
   ]),
  dict(title="Valor presente líquido (VPL)", subtopic="vpl",
   summary="VPL = valor presente das entradas − valor presente das saídas, descontados pela TMA. VPL > 0: o projeto "
           "rende mais que a TMA e deve ser aceito; VPL = 0: rende exatamente a TMA; VPL < 0: deve ser rejeitado. "
           "Entre projetos excludentes, escolha o de maior VPL. Quanto maior a TMA, menor o VPL.",
   items=[
    A1("Exemplo: investe R$ 1.000, recebe R$ 1.320 em 1 ano, TMA de 10%: VPL = 1.320 ÷ 1,1 − 1.000 = R$ 200. Agora: "
       "investe R$ 4.000 e recebe R$ 4.620 em 1 ano, com TMA de 10% ao ano. Qual é o VPL?",
       "R$ 200", ["R$ 620", "R$ 4.200"],
       "4.620 ÷ 1,1 = 4.200, e 4.200 − 4.000 = 200. O R$ 620 é o ganho sem descontar; o R$ 4.200 é o valor presente "
       "da entrada, sem tirar o investimento."),
    TF("Um projeto com VPL negativo, calculado pela TMA da empresa, deve ser aceito, porque ainda gera entradas "
       "positivas.", False,
       "VPL negativo significa que as entradas, trazidas a hoje pela TMA, não pagam o investimento: o projeto rende "
       "menos que a alternativa e deve ser rejeitado."),
    QK("Aumentar a TMA reduz o valor presente das entradas. Se o VPL de um projeto é R$ 500 com TMA de 8%, com TMA de "
       "12% o VPL tende a",
       "diminuir", ["aumentar", "continuar igual"],
       "Descontar as entradas por uma taxa maior dá valores presentes menores; o investimento, na data 0, não muda. "
       "Logo, o VPL cai.",
       style="conceitual"),
    QK("Entre projetos excludentes, escolha o de maior VPL. O projeto A tem VPL de R$ 3.000, e o B, de R$ 4.500; só um "
       "pode ser feito. A escolha é",
       "o projeto B", ["o projeto A", "os dois, somando os VPLs"],
       "Excludentes: só um entra, e o que mais agrega valor hoje é o de maior VPL. Somar não vale, porque fazer um "
       "impede o outro.",
       step=3),
    PVS("Um projeto exige investimento de R$ 10.000,00 hoje e gera entradas de R$ 6.600,00 em 1 ano e de R$ 7.260,00 em "
        "2 anos. Com TMA de 10% ao ano, o VPL do projeto é de",
        ["−R$ 2.000,00", "R$ 2.000,00", "R$ 3.860,00", "R$ 12.000,00", "R$ 13.860,00"], "R$ 2.000,00",
        "6.600 ÷ 1,1 + 7.260 ÷ 1,21 = 6.000 + 6.000 = 12.000; VPL = 12.000 − 10.000 = 2.000. O 3.860 não desconta as "
        "entradas; o 12.000 esquece o investimento; o 13.860 soma as entradas sem descontar; o −2.000 troca o sinal."),
    PV("Uma empresa com TMA de 12% ao ano avalia três projetos excludentes: X, com VPL de R$ 8.000,00; Y, com VPL de "
       "−R$ 1.000,00; e Z, com VPL de R$ 12.000,00. A decisão correta é",
       "fazer o projeto Z.",
       ["fazer o projeto X, que tem o menor investimento.", "fazer o projeto Y, porque ele ainda gera entradas.",
        "fazer os projetos X e Z, somando os seus VPLs.", "não fazer nenhum, porque um dos VPLs é negativo."],
       "Projetos excludentes: só um entra, e o de maior VPL (Z) é o que mais agrega valor. O investimento de X não foi "
       "informado; Y tem VPL negativo; somar X e Z é impossível, porque são excludentes; o VPL negativo de Y não "
       "afeta os outros.",
       style="caso_pratico", t="case"),
   ]),
  dict(title="Taxa interna de retorno (TIR)", subtopic="tir",
   summary="TIR (taxa interna de retorno) é a taxa que zera o VPL: com ela, o valor presente das entradas é igual ao do "
           "investimento. Regra: TIR > TMA, aceita; TIR < TMA, rejeita. Com uma única entrada, sai direto: investir "
           "1.000 para receber 1.150 em 1 ano dá TIR de 15%. Com várias entradas, teste as taxas das alternativas até o "
           "VPL dar zero.",
   items=[
    A1("Exemplo: investe R$ 1.000 e recebe R$ 1.150 em 1 ano: a TIR é 1.150 ÷ 1.000 − 1 = 15%. Agora: investe "
       "R$ 2.000 e recebe R$ 2.360 em 1 ano. Qual é a TIR?",
       "18%", ["36%", "15,25%"],
       "2.360 ÷ 2.000 = 1,18: TIR de 18%. O 15,25% divide o ganho pela entrada (360 ÷ 2.360), e não pelo investimento; "
       "o 36% dobra a taxa."),
    TF("Se a TIR de um projeto é de 14% ao ano e a TMA da empresa é de 10% ao ano, o projeto deve ser aceito.", True,
       "TIR maior que a TMA quer dizer que o projeto rende mais que o mínimo exigido; com a TMA, o VPL dá positivo."),
    QK("Com uma única entrada em 2 anos: (1 + TIR)² = entrada ÷ investimento. Investe R$ 10.000 e recebe R$ 12.100 em 2 "
       "anos. A TIR anual é",
       "10%", ["21%", "10,5%"],
       "12.100 ÷ 10.000 = 1,21 = 1,1²: TIR de 10% ao ano. O 21% é o ganho dos 2 anos; o 10,5% divide 21% por 2, a "
       "lógica dos juros simples."),
    QK("Pela definição da TIR, quando o fluxo é descontado por ela, o VPL é",
       "zero", ["igual ao investimento", "sempre positivo"],
       "A TIR é exatamente a taxa que iguala o valor presente das entradas ao investimento, e por isso zera o VPL.",
       step=3, style="conceitual"),
    PVS("Um projeto custa R$ 1.000,00 hoje e paga R$ 1.440,00 daqui a 2 anos, numa única entrada. A TIR anual do projeto "
        "é de",
        ["20%", "22%", "44%", "72%", "144%"], "20%",
        "(1 + TIR)² = 1.440 ÷ 1.000 = 1,44, então 1 + TIR = 1,2 e a TIR é 20% ao ano. O 22% divide 44% por 2; o 44% é o "
        "ganho dos 2 anos; o 72% e o 144% partem de 1.440 sem tirar o investimento.",
        t="mcq5"),
    PVS("Um projeto custa R$ 20.000,00 e gera R$ 11.500,00 em 1 ano e R$ 13.225,00 em 2 anos. Testando taxas, conclui-se "
        "que a TIR anual do projeto é de",
        ["10%", "15%", "18,5%", "20%", "23,6%"], "15%",
        "A 15%: 11.500 ÷ 1,15 + 13.225 ÷ 1,3225 = 10.000 + 10.000 = 20.000, e o VPL é zero. A 10% o VPL ainda é "
        "positivo, e a 20% já é negativo. O 23,6% é o ganho total sem descontar (24.725 ÷ 20.000 − 1)."),
   ]),
  dict(title="Capitalização contínua", subtopic="capitalização contínua",
   summary="Na capitalização contínua, o juro é incorporado a cada instante: M = C × eʳᵗ, com e ≈ 2,718 e r a taxa "
           "contínua por período. A taxa efetiva equivalente é eʳ − 1: 10% ao ano contínuos dão e^0,1 − 1 ≈ 10,52% ao "
           "ano. É o limite da capitalização composta quando as capitalizações ficam cada vez mais frequentes; com a "
           "mesma taxa nominal, rende mais que a mensal.",
   items=[
    A1("Exemplo: R$ 1.000 a 10% ao ano contínuos, por 1 ano: M = 1.000 × e^0,1 ≈ 1.000 × 1,1052 = R$ 1.105,20. Agora: "
       "R$ 5.000 a 10% ao ano contínuos, por 1 ano (e^0,1 ≈ 1,1052). Qual é o montante?",
       "R$ 5.526", ["R$ 5.500", "R$ 5.512,50"],
       "5.000 × 1,1052 = 5.526. O R$ 5.500 capitaliza uma vez no ano; o R$ 5.512,50, duas vezes (1,05²). A contínua "
       "rende um pouco mais que as duas."),
    TF("Com a mesma taxa nominal anual, a capitalização contínua rende mais que a capitalização mensal.", True,
       "Quanto mais frequentes as capitalizações, maior o montante; a contínua é o limite dessa sequência."),
    QK("Taxa efetiva da contínua: eʳ − 1. Dado e^0,2 ≈ 1,2214, a taxa efetiva anual equivalente a 20% ao ano "
       "contínuos é de",
       "22,14%", ["20%", "21%"],
       "1,2214 − 1 = 22,14% ao ano. O 20% é a própria taxa contínua; o 21% seria a efetiva com capitalização "
       "semestral (1,1²)."),
    QK("M = C × eʳᵗ: a taxa vezes o tempo vai no expoente. Dado e^0,3 ≈ 1,3499, R$ 10.000 a 10% ao ano contínuos, por "
       "3 anos, viram",
       "R$ 13.499", ["R$ 13.000", "R$ 13.310"],
       "r × t = 0,1 × 3 = 0,3, e 10.000 × 1,3499 = 13.499. O R$ 13.000 é o juro simples; o R$ 13.310, o composto "
       "anual (1,1³).",
       step=3),
    PVS("Um capital de R$ 20.000,00 é aplicado a 6% ao ano com capitalização contínua por 5 anos. Dado e^0,3 ≈ 1,35, o "
        "montante é de",
        ["R$ 21.200,00", "R$ 21.236,73", "R$ 26.000,00", "R$ 26.764,51", "R$ 27.000,00"], "R$ 27.000,00",
        "r × t = 0,06 × 5 = 0,3: M = 20.000 × 1,35 = 27.000. O 26.764,51 é o composto anual (1,06⁵); o 26.000, o "
        "simples; o 21.200 e o 21.236,73 param no 1º ano."),
    PV("Sobre a capitalização contínua, NÃO é correto afirmar que",
       "a taxa efetiva equivalente é igual à própria taxa contínua.",
       ["o montante é dado por C × eʳᵗ.", "a taxa efetiva equivalente a uma taxa contínua r é eʳ − 1.",
        "com a mesma taxa nominal, rende mais que a capitalização mensal.",
        "é o limite da capitalização composta com capitalizações cada vez mais frequentes."],
       "A efetiva é eʳ − 1, maior que r: 10% contínuos equivalem a cerca de 10,52% efetivos. As outras afirmações "
       "estão corretas.",
       style="excecao", pos=0),
   ]),
 ]),
]
