# G5 Matemática financeira (u15), lição 2: Descontos e séries. Gerado para o app por tools/gen_u16.py u15.
# Mesmo formato da lição 1 (certa primeiro; PVS ordena alternativas numéricas; um assert por conta e por distrator
# justificado; potências e fatores dados no enunciado). Conteúdo de IA (não revisado), conferido contra a apostila do
# cargo (OP-121NV-26), usada só como referência.
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
assert 5000 * (1 - F(2, 100) * 3) == 4700 and 5000 * F(98, 100) == 4900 and r2(5000 / F(106, 100)) == 4716.98
assert 6000 * F(4, 100) * F(15, 30) == 120 and 6000 * F(4, 100) == 240 and 6000 * F(4, 100) * 15 == 3600
assert 10000 - 10000 * F(2, 100) - 10000 * F(1, 100) == 9700 and 10000 * F(98, 100) * F(99, 100) == 9702
assert 15000 * (1 - F(3, 100) * F(40, 30)) == 14400 and r2(15000 / F(104, 100)) == 14423.08
assert 15000 * F(97, 100) == 14550 and 15000 * (1 - F(3, 100) * F(40, 360)) == 14950 and 15000 + 600 == 15600
assert 7200 / F(90, 100) == 8000 and 7200 * F(105, 100) == 7560 and 7200 * F(110, 100) == 7920
assert 7200 * F(90, 100) == 6480 and 7200 * 2 == 14400
assert 2400 / F(12, 10) == 2000 and 2400 * F(8, 10) == 1920 and 2400 * F(9, 10) == 2160
assert 5300 - 5300 / F(106, 100) == 300 and 5300 * F(6, 100) == 318
assert 200 * F(11, 10) == 220
assert 10600 * F(6, 100) - (10600 - 10600 / F(106, 100)) == 36 and 10600 * F(6, 100) + 600 == 1236
assert 5000 * F(8, 10) ** 2 == 3200 and 5000 * (1 - F(4, 10)) == 3000 and r2(5000 / F(144, 100)) == 3472.22
assert 12100 / F(121, 100) == 10000 and 12100 * F(81, 100) == 9801 and r2(12100 / F(12, 10)) == 10083.33
assert r2(900 / F(9, 10) / F(11, 10)) == 909.09 and 900 * F(11, 10) == 990 and r2(900 / F(11, 10)) == 818.18
assert 20000 * F(729, 1000) == 14580 and 20000 * F(7, 10) == 14000 and r2(20000 / F(1331, 1000)) == 15026.30
assert 20000 * F(81, 100) == 16200
assert 6400 * F(125, 100) ** 2 == 10000 and 10000 * F(75, 100) ** 2 == 5625 and 6400 * F(5625, 10000) == 3600
assert 10000 * (1 - F(25, 100) * 2) == 5000
assert 6050 / F(121, 100) == 5000 and 6050 * F(8, 10) == 4840 and r2(6050 / F(12, 10)) == 5041.67
assert 1200 / F(121, 100) < 1000
assert (2100 - 1000) * F(11, 10) == 1210 and 1100 + 2100 * F(1, 10) == 1310
assert 2420 / F(11, 10) + 2420 / F(121, 100) == 4200 and 4840 / F(121, 100) == 4000
assert r2(2420 / F(11, 10) + 2420 / F(12, 10)) == 4216.67 and 2 * 2420 / F(11, 10) == 4400
assert 3000 * F(11, 10) + 3630 / F(11, 10) == 6600 and 6600 / F(11, 10) == 6000 and 3000 + 3630 / F(11, 10) == 6300
assert 6600 * F(11, 10) == 7260
assert 1210 / F(11, 10) + 1210 / F(121, 100) == 2100 and 2 * 1210 / F(121, 100) == 2000
assert 1000 * F(433, 100) == 4330 and r2(5000 / F(105, 100)) == 4761.90
assert 8660 / F(433, 100) == 2000 and r2(F(8660, 5)) == 1732 and 8660 / 4 == 2165
assert 500 * (F(121, 100) + F(11, 10) + 1) == 1655 and 500 * (F(12, 10) + F(11, 10) + 1) == 1650
assert 500 * (F(1331, 1000) + F(121, 100) + F(11, 10)) == F(182050, 100) and 1500 * F(1331, 1000) == F(199650, 100)
assert 43300 / F(866, 100) == 5000 and 43300 / 10 == 4330 and 4330 * F(102, 100) == F(441660, 100)
assert 4330 + 43300 * F(2, 100) == 5196 and 43300 * F(2, 100) == 866
assert 550 + 550 / F(11, 10) == 1050 and r2(550 / F(11, 10) + 550 / F(121, 100)) == 954.55
assert 12100 / F(121, 100) == 10000 and 12100 / F(11, 10) == 11000 and r2(12100 / F(1331, 1000)) == 9090.91
assert 1100 / F(11, 10) + 2420 / F(121, 100) == 3000 and 3520 / F(11, 10) == 3200
assert 1210 + 1210 / F(11, 10) + 1210 / F(121, 100) == 3310 and r2(1100 + 1000 + 1000 / F(11, 10)) == 3009.09
assert 3630 / F(11, 10) == 3300 and 3310 * F(11, 10) == 3641
P_CARENCIA = 10000 / (1 / F(121, 100) + 1 / F(1331, 1000))
assert r2(P_CARENCIA) == 6338.10 and r2(10000 / (1 / F(11, 10) + 1 / F(121, 100))) == 5761.90
assert 5000 * F(121, 100) == 6050 and 5000 * F(1331, 1000) == 6655


LESSONS = [
 dict(order=2, title="Descontos e séries", concepts=[
  dict(title="Desconto comercial simples", subtopic="desconto comercial",
   summary="O desconto comercial (bancário, “por fora”) incide sobre o valor nominal N: D = N × d × t, e o valor atual, "
           "que o cliente recebe, é A = N − D = N × (1 − d × t). Taxa e prazo na mesma unidade (mês comercial de 30 "
           "dias). Tarifas e IOF calculados sobre o nominal também saem do valor recebido.",
   items=[
    A1("Exemplo: título de R$ 1.000, descontado 2 meses antes a 3% ao mês: D = 1.000 × 0,03 × 2 = 60, e o cliente "
       "recebe R$ 940. Agora: uma duplicata de R$ 5.000 é descontada 3 meses antes do vencimento, desconto comercial "
       "simples de 2% ao mês. Quanto o cliente recebe?",
       "R$ 4.700", ["R$ 4.900", "R$ 4.716,98"],
       "D = 5.000 × 0,02 × 3 = 300, e 5.000 − 300 = 4.700. O R$ 4.900 desconta só um mês; o R$ 4.716,98 é o desconto "
       "racional (5.000 ÷ 1,06)."),
    TF("No desconto comercial simples, a taxa incide sobre o valor nominal do título.", True,
       "É o desconto “por fora”: D = N × d × t. O racional, ao contrário, incide sobre o valor atual.",
       style="literal"),
    QK("Prazo em dias: use o mês comercial de 30 dias. Um título de R$ 6.000 é descontado 15 dias antes do vencimento, "
       "desconto comercial simples de 4% ao mês. Qual é o desconto?",
       "R$ 120", ["R$ 240", "R$ 3.600"],
       "15 dias = meio mês: 6.000 × 0,04 × 0,5 = 120. O R$ 240 cobra o mês inteiro; o R$ 3.600 trata os 15 dias como "
       "15 meses."),
    QK("Tarifas sobre o nominal também saem do valor recebido. Título de R$ 10.000, desconto comercial de 2% ao mês "
       "por 1 mês e tarifa de 1% sobre o nominal. O cliente recebe",
       "R$ 9.700", ["R$ 9.800", "R$ 9.702"],
       "Desconto 200 e tarifa 100, ambos sobre 10.000: recebe 9.700. O R$ 9.800 esquece a tarifa; o R$ 9.702 aplica a "
       "tarifa sobre o valor já descontado.",
       step=3),
    PVS("Um título de R$ 15.000,00 foi descontado 40 dias antes do vencimento, pelo desconto bancário simples, à taxa de "
        "3% ao mês. O valor líquido recebido foi de",
        ["R$ 14.400,00", "R$ 14.423,08", "R$ 14.550,00", "R$ 14.950,00", "R$ 15.600,00"], "R$ 14.400,00",
        "40 dias = 4/3 de mês: D = 15.000 × 0,03 × 4/3 = 600, e 15.000 − 600 = 14.400. O 14.423,08 é o desconto "
        "racional; o 14.550 desconta só 30 dias; o 14.950 lê 3% como taxa anual; o 15.600 soma o desconto."),
    PVS("Ao descontar uma nota promissória 2 meses antes do vencimento, à taxa de desconto comercial simples de 5% ao "
        "mês, um cliente recebeu R$ 7.200,00. O valor nominal do título era de",
        ["R$ 6.480,00", "R$ 7.560,00", "R$ 7.920,00", "R$ 8.000,00", "R$ 14.400,00"], "R$ 8.000,00",
        "A = N × (1 − 0,05 × 2) = 0,9 N = 7.200, então N = 8.000. O 7.920 soma 10% sobre o valor recebido (a base "
        "certa é o nominal); o 7.560 usa um mês só; o 6.480 aplica o desconto de novo; o 14.400 dobra o valor."),
   ]),
  dict(title="Desconto racional simples", subtopic="desconto racional",
   summary="O desconto racional (“por dentro”) trata o valor atual A como um capital que, a juros simples, vira o "
           "nominal: N = A × (1 + i × t), então A = N ÷ (1 + i × t). O desconto incide sobre A, que é menor; por isso, "
           "com a mesma taxa e o mesmo prazo, o racional é menor que o comercial. Relação útil: Dc = Dr × (1 + i × t).",
   items=[
    A1("Exemplo: título de R$ 1.100, 1 mês antes, racional simples de 10% ao mês: A = 1.100 ÷ 1,1 = R$ 1.000. Agora: "
       "título de R$ 2.400, 2 meses antes, desconto racional simples de 10% ao mês. Qual é o valor atual?",
       "R$ 2.000", ["R$ 1.920", "R$ 2.160"],
       "1 + 0,1 × 2 = 1,2, e 2.400 ÷ 1,2 = 2.000. O R$ 1.920 é o comercial (2.400 × 0,8); o R$ 2.160 desconta só um "
       "mês por fora."),
    TF("Com a mesma taxa e o mesmo prazo, o desconto racional simples é maior que o comercial simples.", False,
       "É o contrário: o racional incide sobre o valor atual, menor que o nominal, então o desconto é menor."),
    QK("A = N ÷ (1 + i × t). Um título de R$ 5.300 é descontado 3 meses antes, racional simples de 2% ao mês. O "
       "desconto é",
       "R$ 300", ["R$ 318", "R$ 5.000"],
       "A = 5.300 ÷ 1,06 = 5.000, e D = 5.300 − 5.000 = 300. O R$ 318 é o desconto comercial (5.300 × 0,06); o "
       "R$ 5.000 é o valor atual, não o desconto."),
    QK("O desconto comercial é o racional mais o juro sobre ele: Dc = Dr × (1 + i × t). Se Dr = R$ 200 e i × t = 0,1, "
       "quanto é Dc?",
       "R$ 220", ["R$ 180", "R$ 200"],
       "200 × 1,1 = 220: o comercial é sempre o maior dos dois. O R$ 180 inverteria essa relação.",
       step=3, style="literal"),
    PVS("Um título de R$ 10.600,00 é descontado 4 meses antes do vencimento, à taxa de 1,5% ao mês. A diferença entre o "
        "desconto comercial simples e o desconto racional simples é de",
        ["R$ 0,00", "R$ 36,00", "R$ 600,00", "R$ 636,00", "R$ 1.236,00"], "R$ 36,00",
        "i × t = 0,06. Comercial: 10.600 × 0,06 = 636. Racional: 10.600 − 10.600 ÷ 1,06 = 10.600 − 10.000 = 600. "
        "Diferença: 36. O 600 e o 636 são cada desconto sozinho; o 1.236 soma os dois; o 0 supõe que são iguais."),
    PV("Sobre o desconto simples, NÃO é correto afirmar que",
       "o desconto racional incide sobre o valor nominal do título.",
       ["o desconto comercial incide sobre o valor nominal do título.",
        "no desconto racional, o valor atual é tratado como capital aplicado a juros simples.",
        "com a mesma taxa e o mesmo prazo, o desconto comercial é maior que o racional.",
        "no desconto comercial, o valor atual é N × (1 − d × t)."],
       "O racional incide sobre o valor atual, não sobre o nominal (quem incide sobre o nominal é o comercial). As "
       "outras afirmações estão corretas.",
       style="excecao"),
   ]),
  dict(title="Descontos compostos", subtopic="desconto composto",
   summary="Racional composto: A = N ÷ (1 + i)ⁿ, trazer o nominal a valor presente a juros compostos. Comercial "
           "composto: cada período tira d% do valor anterior, A = N × (1 − d)ⁿ. Com a mesma taxa, o comercial composto "
           "dá o menor valor atual. Atenção: 10% ao mês comercial composto por 2 meses é 0,9² = 0,81, ou seja, 19% de "
           "desconto, e não 20%.",
   items=[
    A1("Exemplo: N = R$ 1.000, comercial composto de 10% ao mês, 2 meses: A = 1.000 × 0,9 × 0,9 = R$ 810. Agora: N = "
       "R$ 5.000, desconto comercial composto de 20% ao mês, 2 meses antes. Qual é o valor atual?",
       "R$ 3.200", ["R$ 3.000", "R$ 3.472,22"],
       "5.000 × 0,8 × 0,8 = 3.200. O R$ 3.000 é o comercial simples (tira 40% de uma vez); o R$ 3.472,22 é o racional "
       "composto (5.000 ÷ 1,44)."),
    TF("No desconto racional composto, o valor atual é o nominal dividido por (1 + i)ⁿ.", True,
       "É o valor presente a juros compostos: o valor atual, aplicado à taxa i por n períodos, vira o nominal.",
       style="literal"),
    QK("Fórmula do racional composto: A = N ÷ (1 + i)ⁿ. Faltando 2 meses para o vencimento, uma duplicata de "
       "R$ 12.100 é descontada a 10% ao mês. Pelo desconto racional composto, o cliente recebe",
       "R$ 10.000", ["R$ 9.801", "R$ 10.083,33"],
       "12.100 ÷ 1,21 = 10.000. O R$ 9.801 é o comercial composto (12.100 × 0,81); o R$ 10.083,33 é o racional "
       "simples (12.100 ÷ 1,2)."),
    QK("Do comercial para o racional: ache o nominal primeiro. Um título descontado 1 mês antes, comercial composto de "
       "10%, valeu R$ 900. Com desconto racional à mesma taxa, valeria",
       "R$ 909,09", ["R$ 990,00", "R$ 818,18"],
       "N × 0,9 = 900, então N = 1.000; racional: 1.000 ÷ 1,1 ≈ 909,09. O 990 aplica 10% sobre o valor atual; o "
       "818,18 divide o valor atual, em vez do nominal, por 1,1.",
       step=3),
    PVS("Uma duplicata de R$ 20.000,00 é descontada 3 meses antes do vencimento, desconto comercial composto de 10% ao "
        "mês. Dado 0,9³ = 0,729, o valor atual é de",
        ["R$ 14.000,00", "R$ 14.580,00", "R$ 15.026,30", "R$ 16.200,00", "R$ 18.000,00"], "R$ 14.580,00",
        "20.000 × 0,729 = 14.580. O 14.000 é o comercial simples (30% de uma vez); o 15.026,30 é o racional composto "
        "(÷ 1,331); o 16.200 e o 18.000 descontam só 2 meses e 1 mês."),
    PVS("Um título descontado 2 meses antes do vencimento, à taxa de 25% ao mês no desconto racional composto, valeu "
        "R$ 6.400,00. Se o desconto fosse comercial composto, à mesma taxa, o valor atual seria de",
        ["R$ 3.600,00", "R$ 5.000,00", "R$ 5.625,00", "R$ 6.400,00", "R$ 10.000,00"], "R$ 5.625,00",
        "Nominal: 6.400 × 1,25² = 6.400 × 1,5625 = 10.000. Comercial composto: 10.000 × 0,75² = 5.625. O 3.600 aplica "
        "0,75² sobre o valor atual; o 5.000 usa o comercial simples; o 10.000 é o nominal."),
   ]),
  dict(title="Valor presente, valor futuro e equivalência", subtopic="equivalência de capitais",
   summary="Valor futuro: VF = VP × (1 + i)ⁿ; valor presente: VP = VF ÷ (1 + i)ⁿ. Capitais de datas diferentes só se "
           "comparam depois de levados à mesma data (data focal). No regime composto, qualquer data focal dá a mesma "
           "conclusão; escolha a que facilita a conta. Equação de valor: o que se deve e o que se paga, levados à mesma "
           "data, têm de ser iguais.",
   items=[
    A1("Exemplo: R$ 1.210 daqui a 2 meses, a 10% ao mês, valem hoje 1.210 ÷ 1,21 = R$ 1.000. Agora: um pagamento de "
       "R$ 6.050 daqui a 2 meses, a 10% ao mês compostos, vale quanto hoje?",
       "R$ 5.000", ["R$ 4.840", "R$ 5.041,67"],
       "6.050 ÷ 1,21 = 5.000. O R$ 4.840 tira 20% do valor; o R$ 5.041,67 divide por 1,2 (juros simples)."),
    TF("No regime composto, a escolha da data focal muda a conclusão sobre qual de dois capitais vale mais.", False,
       "No composto, levar os dois para qualquer data multiplica ambos pelo mesmo fator, e a comparação não muda. No "
       "simples, a data focal pode mudar o resultado."),
    QK("Leve tudo para a mesma data. A 10% ao mês compostos, o que vale mais: R$ 1.000 hoje ou R$ 1.200 daqui a 2 "
       "meses?",
       "R$ 1.000 hoje", ["R$ 1.200 daqui a 2 meses", "os dois valem o mesmo"],
       "Trazendo para hoje: 1.200 ÷ 1,21 ≈ 991,74, menos que 1.000. Ou levando para o mês 2: 1.000 × 1,21 = 1.210, "
       "mais que 1.200. As duas datas dão a mesma conclusão."),
    QK("Equação de valor: a dívida e os pagamentos, na mesma data. Uma dívida de R$ 2.100 vence hoje. O cliente paga "
       "R$ 1.000 hoje e o resto daqui a 1 mês, a 10% ao mês. Quanto paga daqui a 1 mês?",
       "R$ 1.210", ["R$ 1.100", "R$ 1.310"],
       "Ficam 1.100 devendo hoje; daqui a 1 mês, 1.100 × 1,1 = 1.210. O R$ 1.100 esquece os juros; o R$ 1.310 cobra "
       "10% sobre a dívida inteira.",
       step=3),
    PVS("Uma dívida será paga em duas parcelas de R$ 2.420,00, daqui a 1 e a 2 meses, a juros compostos de 10% ao mês. "
        "O cliente quer trocá-las por um único pagamento hoje. Esse pagamento é de",
        ["R$ 4.000,00", "R$ 4.200,00", "R$ 4.216,67", "R$ 4.400,00", "R$ 4.840,00"], "R$ 4.200,00",
        "2.420 ÷ 1,1 + 2.420 ÷ 1,21 = 2.200 + 2.000 = 4.200. O 4.216,67 usa juros simples; o 4.400 desconta as duas "
        "por um mês só; o 4.000 desconta as duas por dois meses; o 4.840 não desconta nada."),
    PVS("Um cliente deve R$ 3.000,00 para daqui a 1 mês e R$ 3.630,00 para daqui a 3 meses. Ele quer pagar tudo numa só "
        "parcela daqui a 2 meses, a juros compostos de 10% ao mês. O valor dessa parcela é de",
        ["R$ 6.000,00", "R$ 6.300,00", "R$ 6.600,00", "R$ 6.630,00", "R$ 7.260,00"], "R$ 6.600,00",
        "Na data 2: 3.000 × 1,1 + 3.630 ÷ 1,1 = 3.300 + 3.300 = 6.600. O 6.630 soma sem levar à data; o 6.300 corrige "
        "só a segunda dívida; o 6.000 e o 7.260 calculam o valor nas datas 1 e 3, não na 2."),
   ]),
  dict(title="Séries uniformes", subtopic="séries uniformes",
   summary="Série uniforme postecipada: n parcelas iguais P, a primeira um período depois. Valor presente: VP = P × a, "
           "em que a = [1 − (1 + i)⁻ⁿ] ÷ i é o fator de valor presente (dado na prova ou numa tabela). Para achar a "
           "parcela: P = VP ÷ a. Valor futuro de depósitos iguais: VF = P × s, com s = [(1 + i)ⁿ − 1] ÷ i. Sem fator "
           "dado, traga parcela por parcela.",
   items=[
    A1("Exemplo: 2 parcelas de R$ 121, em 1 e 2 meses, a 10%: 121 ÷ 1,1 + 121 ÷ 1,21 = 110 + 100 = R$ 210. Agora: 2 "
       "parcelas de R$ 1.210, em 1 e 2 meses, a 10% ao mês. Qual é o valor à vista equivalente?",
       "R$ 2.100", ["R$ 2.420", "R$ 2.000"],
       "1.210 ÷ 1,1 + 1.210 ÷ 1,21 = 1.100 + 1.000 = 2.100. O R$ 2.420 não desconta nada; o R$ 2.000 desconta as duas "
       "por 2 meses."),
    TF("Na série postecipada, a primeira parcela é paga no ato da compra.", False,
       "Postecipada: a primeira parcela vence um período depois. Com pagamento no ato, a série é antecipada."),
    QK("VP = P × a. O fator a para 5 parcelas mensais a 5% é 4,33. Uma compra em 5 parcelas de R$ 1.000 vale à vista",
       "R$ 4.330", ["R$ 5.000", "R$ 4.761,90"],
       "1.000 × 4,33 = 4.330. O R$ 5.000 soma as parcelas sem descontar; o R$ 4.761,90 desconta o total por um mês só."),
    QK("Para achar a parcela, divida pelo fator: P = VP ÷ a. Um empréstimo de R$ 8.660 em 5 parcelas mensais, com "
       "fator a = 4,33. A parcela é",
       "R$ 2.000", ["R$ 1.732", "R$ 2.165"],
       "8.660 ÷ 4,33 = 2.000. O R$ 1.732 divide por 5, esquecendo os juros; o R$ 2.165 divide por 4.",
       step=3),
    PVS("Uma pessoa deposita R$ 500,00 no fim de cada mês, durante 3 meses, numa aplicação que rende 10% ao mês em juros "
        "compostos. Logo após o 3º depósito, o saldo é de",
        ["R$ 1.500,00", "R$ 1.650,00", "R$ 1.655,00", "R$ 1.820,50", "R$ 1.996,50"], "R$ 1.655,00",
        "O 1º depósito rende 2 meses, o 2º, 1 mês, e o 3º, nada: 500 × (1,21 + 1,1 + 1) = 500 × 3,31 = 1.655. O 1.650 "
        "usa juros simples; o 1.820,50 trata os depósitos como feitos no início do mês; o 1.996,50 capitaliza tudo "
        "por 3 meses; o 1.500 esquece os juros."),
    PVS("Um carro de R$ 43.300,00 à vista será financiado em 10 prestações mensais iguais, a primeira um mês após a "
        "compra, a 2% ao mês. Dado o fator de valor presente a = 8,66 (10 meses, 2%), cada prestação vale",
        ["R$ 866,00", "R$ 4.330,00", "R$ 4.416,60", "R$ 5.000,00", "R$ 5.196,00"], "R$ 5.000,00",
        "P = 43.300 ÷ 8,66 = 5.000. O 4.330 divide por 10 e esquece os juros; o 4.416,60 soma 2% a isso; o 5.196 "
        "soma o juro de um mês sobre o total; o 866 é só esse juro."),
   ]),
  dict(title="Séries antecipadas, com carência e não uniformes", subtopic="séries especiais",
   summary="Antecipada: a primeira parcela é paga no ato; vale a postecipada vezes (1 + i), ou a entrada mais n − 1 "
           "parcelas postecipadas. Com carência (diferida): as parcelas começam depois de alguns períodos; ache o valor "
           "da série um período antes da 1ª parcela e desconte até hoje. Não uniforme: traga cada parcela a valor "
           "presente e some.",
   items=[
    A1("Exemplo: 2 parcelas de R$ 110, a 1ª no ato e a 2ª em 1 mês, a 10%: 110 + 110 ÷ 1,1 = R$ 210. Agora: 2 parcelas "
       "de R$ 550, a 1ª no ato e a 2ª em 1 mês, a 10% ao mês. Qual é o valor à vista?",
       "R$ 1.050", ["R$ 1.100", "R$ 954,55"],
       "550 + 550 ÷ 1,1 = 550 + 500 = 1.050. O R$ 1.100 não desconta a 2ª parcela; o R$ 954,55 trata a série como "
       "postecipada, descontando também a 1ª."),
    TF("Uma compra em “1 + 2” (entrada mais duas parcelas mensais iguais à entrada) é uma série antecipada de 3 "
       "parcelas.", True,
       "A 1ª parcela é paga no ato e as outras vêm a cada mês: é a definição de série antecipada."),
    QK("Com carência: ache o valor da série um período antes da 1ª parcela e desconte até hoje. Uma série vale "
       "R$ 12.100 na data 2, um mês antes da 1ª parcela, que vence no 3º mês. A 10% ao mês, quanto ela vale hoje?",
       "R$ 10.000", ["R$ 11.000", "R$ 9.090,91"],
       "Da data 2 até hoje são 2 meses: 12.100 ÷ 1,21 = 10.000. O R$ 11.000 desconta só 1 mês; o R$ 9.090,91 desconta "
       "3, contando até a 1ª parcela."),
    QK("Não uniforme: traga cada parcela e some. Parcelas de R$ 1.100 em 1 mês e de R$ 2.420 em 2 meses, a 10% ao mês. "
       "Qual é o valor presente?",
       "R$ 3.000", ["R$ 3.520", "R$ 3.200"],
       "1.100 ÷ 1,1 + 2.420 ÷ 1,21 = 1.000 + 2.000 = 3.000. O R$ 3.520 soma sem descontar; o R$ 3.200 desconta tudo "
       "por um mês só.",
       step=3),
    PVS("Uma loja vende um produto em 3 parcelas mensais de R$ 1.210,00, a 1ª no ato da compra. A juros compostos de 10% "
        "ao mês, o valor à vista equivalente é de",
        ["R$ 3.009,09", "R$ 3.300,00", "R$ 3.310,00", "R$ 3.630,00", "R$ 3.641,00"], "R$ 3.310,00",
        "1.210 + 1.210 ÷ 1,1 + 1.210 ÷ 1,21 = 1.210 + 1.100 + 1.000 = 3.310. O 3.009,09 trata a série como "
        "postecipada; o 3.300 desconta o total por um mês; o 3.630 não desconta; o 3.641 leva a série para o mês 1."),
    PVS("Um empréstimo de R$ 10.000,00 será pago em 2 parcelas iguais, no 2º e no 3º mês (carência de 1 mês), a juros "
        "compostos de 10% ao mês. Dados 1,1² = 1,21 e 1,1³ = 1,331, cada parcela vale, aproximadamente,",
        ["R$ 5.000,00", "R$ 5.761,90", "R$ 6.050,00", "R$ 6.338,10", "R$ 6.655,00"], "R$ 6.338,10",
        "P ÷ 1,21 + P ÷ 1,331 = 10.000, ou seja, P × (0,8264 + 0,7513) ≈ P × 1,5778 = 10.000, e P ≈ 6.338,10. O "
        "5.761,90 esquece a carência (parcelas no 1º e no 2º mês); o 6.050 e o 6.655 corrigem metade da dívida por 2 "
        "ou 3 meses; o 5.000 esquece os juros."),
   ]),
 ]),
]
