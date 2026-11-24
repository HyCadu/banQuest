# G5 Matemática financeira (u15), lição 1: Juros e taxas. Gerado para o app por tools/gen_u16.py u15.
# Em cada exercício a alternativa certa vem PRIMEIRO; o gerador espalha a posição. Nas de 5 alternativas numéricas, PVS
# põe as opções em ordem crescente e fixa a posição da certa. Cada conta (e cada distrator que a explicação justifica)
# tem um assert: se um resultado estiver errado, a geração para. Potências e fatores vêm dados no enunciado, como nas
# provas. Conteúdo de IA (não revisado), conferido contra a apostila do cargo (OP-121NV-26), usada só como referência.
from fractions import Fraction as F


def A1(stem, c, w, exp, diff=1, style="caso_pratico"):
    return dict(type="quick", level="aprender", step=1, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def TF(stem, true, exp, diff=1, style="conceitual"):
    return dict(type="truefalse", level="aprender", step=2, stem=stem, true=true, exp=exp, diff=diff, style=style)

def QK(stem, c, w, exp, step=2, diff=2, style="caso_pratico"):
    return dict(type="quick", level="praticar", step=step, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def PV(stem, c, w, exp, diff=2, style="conceitual", t="mcq5", pos=None):
    d = dict(type=t, level="prova", stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)
    if pos is not None:
        d["pos"] = pos
    return d

def PVS(stem, options, c, exp, diff=2, style="caso_pratico", t="case"):
    """Alternativas numéricas já em ordem crescente; a certa fica na posição natural."""
    assert c in options and len(options) == 5 and len(set(options)) == 5, stem
    return PV(stem, c, [o for o in options if o != c], exp, diff=diff, style=style, t=t, pos=options.index(c))


def r2(x):
    return round(float(x) + 1e-9, 2)


# --- contas conferidas ---------------------------------------------------------------------------------------------
assert 2500 * F(3, 100) * 4 == 300 and 2500 * F(3, 100) == 75
assert F(15, 10) * 12 == 18
assert 1200 * (1 + F(6, 100) * F(10, 30)) == 1224 and 1200 * F(106, 100) == 1272
assert 1320 / F(110, 100) == 1200 and 1320 * F(9, 10) == 1188
assert 8000 * (1 + F(15, 1000) * 7) == 8840 and r2(8000 * 1.015 ** 7) == 8878.76
assert 8000 * (1 + F(18, 1000) * 7) == 9008 and 8000 * F(118, 100) == 9440 and 8000 * (1 + F(18, 100) * 7) == 18080
assert 1 / F(4, 100) == 25 and 1.04 ** 17 < 2 < 1.04 ** 18 and 1 / F(2, 100) == 50 and 1 / F(5, 100) == 20
assert 5000 * F(11, 10) ** 2 == 6050 and 5000 * F(12, 10) == 6000
assert 4410 / F(11025, 10000) == 4000 and 4410 * F(9, 10) == 3969 and 4410 / F(105, 100) == 4200
assert 1000 * F(11, 10) ** 4 == F(146410, 100)
assert 10000 * F(1061208, 10 ** 6) - 10000 == F(61208, 100) and 10000 * F(2, 100) * 3 == 600
assert 10404 * F(2, 100) == F(20808, 100)
assert (5000 * F(121, 100) - 1050) * F(11, 10) == 5500 and 5000 * F(1331, 1000) == 6655 and 6655 - 1050 == 5605
assert F(11, 10) ** 3 == F(1331, 1000)
assert 1.02 ** 12 > 1.268 and F(105, 100) ** 2 == F(11025, 10000)
assert F(12, 10) ** 2 == F(144, 100)
assert F(11, 10) ** 2 == F(121, 100)
assert F(106, 100) ** 2 == F(11236, 10000)
assert F(11, 10) ** 2 == F(121, 100)                     # 20% a.a. cap. semestral: 10% ao semestre
assert 1.4 ** 0.25 - 1 > 0.0877 and 1.4 ** 0.25 - 1 < 0.0879
assert F(1124864, 10 ** 6) - 1 == F(124864, 10 ** 6)     # ≈ 12,49%
assert 2000 * F(112, 100) == 2240 and 2000 * F(1144, 1000) == 2288 and 2000 * F(124, 100) == 2480
assert 2000 * F(144, 100) == 2880 and 2000 * F(244, 100) == 4880
assert F(11025, 10000) / F(105, 100) == F(105, 100) and F(11025, 10000) * F(105, 100) > F(11576, 10000)
assert F(103, 100) * F(106, 100) == F(10918, 10000)
assert (3000 * F(10, 100) + 1000 * F(2, 100)) / 4000 == F(8, 100)
assert r2((1.08 / 1.10 - 1) * 100) == -1.82
assert F(7, 10) * 12 + F(3, 10) * 2 == 9 and F(3, 10) * 12 + F(7, 10) * 2 == 5


LESSONS = [
 dict(order=1, title="Juros e taxas", concepts=[
  dict(title="Juros simples", subtopic="juros simples",
   summary="J = C × i × t, com a taxa e o prazo na mesma unidade; o montante é M = C + J = C × (1 + i × t). O juro "
           "incide sempre sobre o capital inicial, então cresce em linha reta. Mês comercial: 30 dias; ano comercial: "
           "360 dias. No regime simples, taxas proporcionais se equivalem: 2% ao mês = 24% ao ano.",
   items=[
    A1("Exemplo: R$ 1.000 a 2% ao mês por 3 meses rendem 1.000 × 0,02 × 3 = R$ 60 de juros simples. Agora: R$ 2.500 a "
       "3% ao mês, juros simples, por 4 meses. Quanto rendem de juros?",
       "R$ 300", ["R$ 75", "R$ 2.800"],
       "2.500 × 0,03 × 4 = 300. R$ 75 é o juro de um só mês, e R$ 2.800 é o montante (capital mais juros)."),
    TF("No regime de juros simples, a taxa de 1,5% ao mês é proporcional a 18% ao ano.", True,
       "Basta multiplicar pelo número de meses do ano: 1,5% × 12 = 18%. No simples, taxas proporcionais rendem o mesmo.",
       style="literal"),
    QK("Taxa e prazo na mesma unidade; o mês comercial tem 30 dias. Um boleto de R$ 1.200 pago com 10 dias de atraso, "
       "com juros simples de 6% ao mês, custa",
       "R$ 1.224", ["R$ 1.272", "R$ 1.920"],
       "10 dias = 1/3 do mês: 1.200 × 0,06 × 1/3 = 24, total 1.224. R$ 1.272 cobra o mês inteiro; R$ 1.920 trata os "
       "10 dias como 10 meses."),
    QK("Para achar o capital, divida pelo fator: C = M ÷ (1 + i × t). Que capital, a juros simples de 2% ao mês, vira "
       "R$ 1.320 em 5 meses?",
       "R$ 1.200", ["R$ 1.188", "R$ 1.100"],
       "1 + 0,02 × 5 = 1,10, e 1.320 ÷ 1,10 = 1.200. O R$ 1.188 tira 10% do montante (base errada); o R$ 1.100 "
       "dividiria por 1,20, como se a taxa fosse 4% ao mês.",
       step=3),
    PVS("Um capital de R$ 8.000,00 foi aplicado a juros simples de 18% ao ano durante 7 meses. O montante foi de",
        ["R$ 8.840,00", "R$ 8.878,76", "R$ 9.008,00", "R$ 9.440,00", "R$ 18.080,00"], "R$ 8.840,00",
        "18% ao ano = 1,5% ao mês (proporcional). M = 8.000 × (1 + 0,015 × 7) = 8.000 × 1,105 = 8.840. O 8.878,76 usa "
        "juros compostos; o 9.008 usa 1,8% ao mês; o 9.440 cobra o ano inteiro; o 18.080 aplica 18% por mês."),
    PVS("Aplicado a juros simples de 4% ao mês, um capital dobra de valor em",
        ["12,5 meses", "18 meses", "20 meses", "24 meses", "25 meses"], "25 meses",
        "Dobrar é render juros iguais ao capital: C × 0,04 × t = C, então t = 1 ÷ 0,04 = 25 meses. O 18 é o prazo "
        "aproximado em juros compostos; o 20 seria com 5% ao mês, e o 12,5, com 8%; em 24 meses os juros somam 96%, "
        "ainda abaixo do dobro.",
        style="conceitual", t="mcq5"),
   ]),
  dict(title="Juros compostos", subtopic="juros compostos",
   summary="Juro sobre juro: M = C × (1 + i)ⁿ. Cada período multiplica o saldo pelo mesmo fator (1 + i): a 10% ao mês, "
           "em 3 meses, 1,1 × 1,1 × 1,1 = 1,331. Para achar o capital, divida o montante pelo fator; para achar o "
           "prazo, compare potências do mesmo fator. Nas provas, as potências vêm dadas no enunciado.",
   items=[
    A1("Exemplo: R$ 1.000 a 10% ao mês, juros compostos, por 2 meses: 1.000 × 1,1 × 1,1 = R$ 1.210. Agora: R$ 5.000 a "
       "10% ao mês, juros compostos, por 2 meses. Qual é o montante?",
       "R$ 6.050", ["R$ 6.000", "R$ 5.500"],
       "5.000 × 1,1 = 5.500 no 1º mês e 5.500 × 1,1 = 6.050 no 2º. R$ 6.000 é o juro simples, e R$ 5.500 para no 1º "
       "mês."),
    TF("Com a mesma taxa e o mesmo prazo, desde que maior que um período, os juros compostos rendem mais que os "
       "simples.", True,
       "No composto, o juro de cada período também rende juros. Com prazo menor que um período ocorre o contrário: o "
       "simples rende mais."),
    QK("Para achar o capital, divida pelo fator: C = M ÷ (1 + i)ⁿ. Dado 1,05² = 1,1025, que capital, a 5% ao mês, "
       "juros compostos, vira R$ 4.410 em 2 meses?",
       "R$ 4.000", ["R$ 3.969", "R$ 4.200"],
       "4.410 ÷ 1,1025 = 4.000. O R$ 4.200 desconta só um mês (÷ 1,05); o R$ 3.969 tira 10% de 4.410, que não é a "
       "operação inversa."),
    QK("Para achar o prazo, compare potências do mesmo fator. A 10% ao mês, 1,1⁴ = 1,4641. Em quantos meses R$ 1.000 "
       "viram R$ 1.464,10?",
       "4", ["3", "5"],
       "1.464,10 ÷ 1.000 = 1,4641 = 1,1⁴: são 4 meses. Com 3 meses seriam 1.331, e com 5, 1.610,51.",
       step=3),
    PVS("Um capital de R$ 10.000,00 é aplicado a juros compostos de 2% ao mês por 3 meses. Dado 1,02³ = 1,061208, os "
        "juros obtidos são de",
        ["R$ 208,08", "R$ 600,00", "R$ 612,08", "R$ 1.061,21", "R$ 10.612,08"], "R$ 612,08",
        "Montante: 10.000 × 1,061208 = 10.612,08; juros: 10.612,08 − 10.000 = 612,08. O 600 é o juro simples; o "
        "10.612,08 é o montante; o 208,08 é só o juro do 3º mês; o 1.061,21 lê mal o fator.",
        style="literal", t="mcq5"),
    PVS("Um investidor aplicou R$ 5.000,00 a juros compostos de 10% ao mês. Após 2 meses, sacou R$ 1.050,00, e o saldo "
        "continuou aplicado nas mesmas condições por mais 1 mês. O saldo final foi de",
        ["R$ 5.000,00", "R$ 5.500,00", "R$ 5.605,00", "R$ 6.050,00", "R$ 6.655,00"], "R$ 5.500,00",
        "Em 2 meses: 5.000 × 1,21 = 6.050. Depois do saque: 5.000. Mais 1 mês: 5.000 × 1,1 = 5.500. O 6.655 ignora o "
        "saque; o 5.605 tira o saque só no fim; o 5.000 esquece o último mês; o 6.050 para antes do saque."),
   ]),
  dict(title="Taxas equivalentes", subtopic="taxas equivalentes",
   summary="No regime composto, taxas equivalentes levam ao mesmo montante no mesmo prazo: (1 + i_ano) = (1 + i_mês)¹². "
           "10% ao mês, em 3 meses, equivalem a 1,1³ − 1 = 33,1% ao trimestre, e não a 30%. Do período maior para o "
           "menor, use a raiz: 21% ao ano equivalem a 10% ao semestre, porque 1,1² = 1,21. No regime simples, basta a "
           "proporção.",
   items=[
    A1("Exemplo: 10% ao mês, juros compostos, em 2 meses dão 1,1² − 1 = 21% ao bimestre. Agora: qual é a taxa "
       "trimestral equivalente a 10% ao mês, juros compostos? (1,1³ = 1,331)",
       "33,1%", ["30%", "31,3%"],
       "1,331 − 1 = 0,331 = 33,1% ao trimestre. O 30% é a taxa proporcional, que só vale no regime simples."),
    TF("Em juros compostos, a taxa de 2% ao mês equivale a 24% ao ano.", False,
       "1,02¹² ≈ 1,268, cerca de 26,8% ao ano. O 24% é a taxa proporcional, que vale no regime simples."),
    QK("Do período maior para o menor, use a raiz. Uma aplicação rende 44% em 2 anos, juros compostos. A taxa anual "
       "equivalente é",
       "20%", ["22%", "12%"],
       "√1,44 = 1,2, ou seja, 20% ao ano (1,2 × 1,2 = 1,44). O 22% divide 44 por 2, que é a lógica do regime simples."),
    QK("Qual é a taxa semestral equivalente a 21% ao ano, juros compostos? (√1,21 = 1,1)",
       "10%", ["10,5%", "11%"],
       "1,1² = 1,21, então 10% ao semestre. O 10,5% é a metade de 21%, a taxa proporcional.",
       step=3, style="literal"),
    PVS("A taxa anual equivalente, em juros compostos, a 5% ao semestre é de",
        ["10,00%", "10,25%", "10,50%", "11,00%", "25,00%"], "10,25%",
        "1,05² = 1,1025: 10,25% ao ano. O 10% é a taxa proporcional (regime simples); as outras não saem de nenhuma "
        "conta com 5% em dois semestres.",
        style="literal", t="mcq5"),
    PV("Sobre taxas de juros no regime composto, NÃO é correto afirmar que",
       "2% ao mês equivalem a 24% ao ano.",
       ["10% ao mês equivalem a 33,1% ao trimestre.", "21% ao ano equivalem a 10% ao semestre.",
        "taxas equivalentes levam ao mesmo montante no mesmo prazo.",
        "a taxa proporcional só equivale à original no regime simples."],
       "2% ao mês compostos equivalem a 1,02¹² − 1 ≈ 26,8% ao ano; 24% é a proporcional, que só vale no regime "
       "simples. As outras afirmações estão corretas (1,1³ = 1,331 e 1,1² = 1,21).",
       style="excecao", pos=4),
   ]),
  dict(title="Taxa nominal e taxa efetiva", subtopic="nominal e efetiva",
   summary="Taxa nominal é anunciada num período mas capitalizada em outro: “24% ao ano com capitalização mensal”. "
           "Primeiro divida pelo número de capitalizações (24% ÷ 12 = 2% ao mês, a taxa efetiva do período); depois "
           "componha para o período pedido: 1,02¹² − 1 ≈ 26,8% ao ano. Com mais de uma capitalização, a efetiva é "
           "sempre maior que a nominal.",
   items=[
    A1("Exemplo: 36% ao ano com capitalização mensal dão 36 ÷ 12 = 3% ao mês, efetivos. Agora: 18% ao ano com "
       "capitalização mensal correspondem a quantos por cento ao mês, efetivos?",
       "1,5%", ["18%", "1,39%"],
       "18 ÷ 12 = 1,5% ao mês: a taxa nominal se divide pelo número de capitalizações. O 1,39% seria a raiz 12ª de "
       "1,18, conta de taxa equivalente, que não se usa para taxa nominal."),
    TF("Uma taxa nominal de 12% ao ano com capitalização semestral corresponde a 12% ao ano efetivos.", False,
       "São 6% ao semestre, efetivos; no ano, 1,06² − 1 = 12,36%. Com mais de uma capitalização, a efetiva passa da "
       "nominal."),
    QK("Primeiro divida, depois componha. Qual é a taxa efetiva anual de 20% ao ano capitalizados semestralmente?",
       "21%", ["20%", "10%"],
       "20 ÷ 2 = 10% ao semestre; 1,1² − 1 = 21% ao ano. O 20% é a nominal, e o 10% é a efetiva do semestre."),
    QK("Uma taxa nominal de 40% ao ano tem capitalização trimestral. A taxa efetiva trimestral é",
       "10%", ["40%", "8,78%"],
       "40 ÷ 4 = 10% ao trimestre. O 8,78% é a raiz 4ª de 1,40, que trataria 40% como taxa efetiva anual.",
       step=3, style="literal"),
    PVS("Um empréstimo tem taxa nominal de 48% ao ano com capitalização mensal. Dado 1,04³ = 1,124864, a taxa efetiva "
        "trimestral é de, aproximadamente,",
        ["12,00%", "12,49%", "13,00%", "16,00%", "48,00%"], "12,49%",
        "48 ÷ 12 = 4% ao mês; no trimestre, 1,04³ − 1 ≈ 12,49%. O 12% é a proporcional (3 × 4%); o 16% divide 48 por 3 "
        "como se a capitalização fosse quadrimestral; o 48% é a nominal anual."),
    PVS("Uma fatura de R$ 2.000,00 ficou um mês sem pagamento num cartão que cobra 144% ao ano com capitalização "
        "mensal, em juros compostos. O valor devido após esse mês é de",
        ["R$ 2.240,00", "R$ 2.288,00", "R$ 2.480,00", "R$ 2.880,00", "R$ 4.880,00"], "R$ 2.240,00",
        "144 ÷ 12 = 12% ao mês: 2.000 × 1,12 = 2.240. O 2.288 lê 14,4% ao mês; o 2.480 divide 144 por 6; o 2.880 "
        "aplica 44%; o 4.880 aplica a taxa anual inteira num só mês."),
   ]),
  dict(title="Taxa real e rentabilidade", subtopic="taxa real",
   summary="Taxa aparente (a do contrato), inflação e taxa real se ligam por multiplicação: (1 + aparente) = (1 + real) × "
           "(1 + inflação). Rendeu 12% com inflação de 4%: real = 1,12 ÷ 1,04 − 1 ≈ 7,7%, e não 8%. Rentabilidade de uma "
           "carteira: média das taxas ponderada pelo valor aplicado em cada parte.",
   items=[
    A1("Exemplo: uma aplicação rendeu 12% num ano de inflação de 4%: real = 1,12 ÷ 1,04 − 1 ≈ 7,7%. Agora: um salário "
       "foi reajustado em 10,25% num ano de inflação de 5%. Qual foi o ganho real?",
       "5%", ["5,25%", "15,76%"],
       "1,1025 ÷ 1,05 = 1,05: ganho real de 5%. O 5,25% subtrai as taxas; o 15,76% multiplica em vez de dividir."),
    TF("Se uma aplicação rende exatamente a inflação do período, o ganho real é zero.", True,
       "1 + aparente = 1 + inflação, então (1 + real) = 1 e a taxa real é 0%: o poder de compra só foi mantido."),
    QK("(1 + aparente) = (1 + real) × (1 + inflação). Para ganhar 3% reais num ano de inflação de 6%, a taxa aparente "
       "precisa ser de",
       "9,18%", ["9%", "3%"],
       "1,03 × 1,06 = 1,0918: 9,18%. Somar (9%) esquece o ganho real sobre a parte corrigida pela inflação."),
    QK("Carteira: média das taxas ponderada pelo valor aplicado. R$ 3.000 renderam 10% e R$ 1.000 renderam 2% no ano. "
       "A rentabilidade da carteira foi de",
       "8%", ["6%", "12%"],
       "Juros: 300 + 20 = 320 sobre 4.000 = 8%. O 6% é a média simples das taxas, que ignora quanto foi aplicado em "
       "cada uma; o 12% soma as taxas.",
       step=3),
    PVS("Um investimento rendeu 8% num ano em que a inflação foi de 10%. A taxa real do investimento foi de, "
        "aproximadamente,",
        ["−2,00%", "−1,82%", "1,82%", "2,00%", "18,80%"], "−1,82%",
        "1,08 ÷ 1,10 − 1 ≈ −0,0182: perda real de 1,82%. O −2% subtrai as taxas; os valores positivos esquecem que a "
        "inflação foi maior que o rendimento; o 18,80% multiplica os fatores."),
    PVS("Uma pessoa aplicou 70% do capital num fundo que rendeu 12% no ano e o restante numa aplicação que rendeu 2%. "
        "A rentabilidade total da carteira foi de",
        ["5%", "7%", "9%", "12%", "14%"], "9%",
        "0,7 × 12% + 0,3 × 2% = 8,4% + 0,6% = 9%. O 5% troca os pesos; o 7% é a média simples; o 14% soma as taxas; o "
        "12% é só a do fundo."),
   ]),
 ]),
]
assert 1320 / F(12, 10) == 1100
assert 1 / F(8, 100) == F(25, 2) and F(4, 100) * 24 == F(96, 100)
