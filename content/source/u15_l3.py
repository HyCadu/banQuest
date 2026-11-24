# G5 Matemática financeira (u15), lição 3: Amortização. Gerado para o app por tools/gen_u16.py u15.
# Mesmo formato da lição 1 (certa primeiro; PVS ordena alternativas numéricas; um assert por conta e por distrator
# justificado; fatores dados no enunciado). Conteúdo de IA (não revisado), conferido contra a apostila do cargo
# (OP-121NV-26), usada só como referência. A apostila chama o sistema misto também de SACRE; como vários autores tratam
# o SACRE como outro sistema, aqui só se usa a definição que a própria apostila dá ao SAM (média de SAC e Price).
from fractions import Fraction as F


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

def PVS(stem, options, c, exp, diff=2, style="caso_pratico", t="case"):
    """Alternativas numéricas já em ordem crescente; a certa fica na posição natural."""
    assert c in options and len(options) == 5 and len(set(options)) == 5, stem
    return PV(stem, c, [o for o in options if o != c], exp, diff=diff, style=style, t=t, pos=options.index(c))


def r2(x):
    return round(float(x) + 1e-9, 2)


def price(pv, i, n):
    return pv * i / (1 - (1 + i) ** -n)


# --- contas conferidas ---------------------------------------------------------------------------------------------
assert 2300 - 20000 * F(15, 1000) == 2000 and 20000 - 2000 == 18000 and 20000 - 2300 == 17700
assert 50000 * F(2, 100) + 4000 == 5000 and 50000 - 4000 == 46000 and 4000 + 80 == 4080 and 46000 - 80 == 45920
assert 60000 / 10 == 6000 and 60000 / 5 == 12000
assert 6000 + 60000 * F(2, 100) == 7200 and 6000 + 54000 * F(2, 100) == 7080 and 6000 * F(2, 100) == 120
SAC30 = [5000 + F(3, 100) * (30000 - 5000 * k) for k in range(6)]
assert SAC30 == [5900, 5750, 5600, 5450, 5300, 5150] and SAC30[3] == 5450
assert 48000 / 24 == 2000 and F(3440 - 2000, 48000) == F(3, 100) and r2(F(3440, 48000) * 100) == 7.17
assert 20000 * F(103, 100) - 2600 == 18000 and 20000 - 2600 == 17400 and 20000 - 2000 + 600 == 18600
assert 17300 / F(346, 100) == 5000 and 17300 / 4 == 4325 and r2(4325 * 1.06) == 4584.50
assert 15000 * F(2, 100) == 300 and 15000 * F(24, 100) == 3600 and 15000 * F(4, 100) == 600
assert r2(6000 / 2.4869) == 2412.64 and abs(price(6000, 0.10, 3) - 2412.69) < 0.01 and 6000 * 0.1 == 600
assert 6000 / 3 + 600 == 2600 and 2000 + 200 == 2200
assert r2(10000 * 1.04 - 2246.27) == 8153.73 and r2(price(10000, 0.04, 5)) == 2246.27 and r2(10000 - 2246.27) == 7753.73
assert (7200 + 6680) / 2 == 6940 and (54000 + 54520) / 2 == 54260
assert r2(price(100000, 0.02, 10)) == 11132.65 and r2((12000 + 11132.65) / 2) == 11566.33
assert 12000 + 11132.65 == 23132.65
# Price × SAC: mesma dívida, mesma taxa e prazo
_sac = [100000 / 10 + 0.02 * (100000 - 10000 * k) for k in range(10)]
_p = price(100000, 0.02, 10)
assert _sac[0] > _p > _sac[-1] and sum(_sac) - 100000 < 10 * _p - 100000


LESSONS = [
 dict(order=3, title="Amortização", concepts=[
  dict(title="Prestação, juros e amortização", subtopic="elementos",
   summary="Cada prestação tem duas partes: os juros, calculados sobre o saldo devedor do início do período, e a "
           "amortização, que reduz a dívida: prestação = juros + amortização. Novo saldo = saldo anterior − "
           "amortização. Quanto mais cedo a dívida cai, menos juros se pagam no total.",
   items=[
    A1("Exemplo: saldo de R$ 10.000 a 2% ao mês dá R$ 200 de juros no mês; com prestação de R$ 1.200, a amortização é "
       "1.200 − 200 = R$ 1.000. Agora: saldo de R$ 20.000 a 1,5% ao mês e prestação de R$ 2.300. Qual é a "
       "amortização?",
       "R$ 2.000", ["R$ 2.300", "R$ 300"],
       "Juros: 20.000 × 0,015 = 300; amortização: 2.300 − 300 = 2.000. O R$ 2.300 é a prestação inteira, e o R$ 300, "
       "só os juros."),
    TF("Os juros de cada prestação são calculados sobre o valor original do empréstimo, do começo ao fim.", False,
       "São calculados sobre o saldo devedor do início de cada período, que cai à medida que a dívida é amortizada.",
       style="literal"),
    QK("Novo saldo = saldo anterior − amortização. Com saldo de R$ 20.000 e amortização de R$ 2.000 no mês, o saldo "
       "depois da prestação é",
       "R$ 18.000", ["R$ 17.700", "R$ 20.300"],
       "Só a amortização reduz a dívida: 20.000 − 2.000 = 18.000. O R$ 17.700 tira a prestação inteira (os juros não "
       "abatem a dívida); o R$ 20.300 soma os juros."),
    MT("Associe cada termo ao seu papel no financiamento:",
       [("Saldo devedor", "O que ainda falta amortizar"), ("Juros do período", "Taxa × saldo do início do período"),
        ("Amortização", "Parte da prestação que reduz a dívida"), ("Prestação", "Juros mais amortização")],
       "A prestação se divide em juros (o custo do período, sobre o saldo) e amortização (o que reduz o saldo)."),
    PV("Um empréstimo de R$ 50.000,00 a 2% ao mês tem, na 1ª prestação, amortização de R$ 4.000,00. O valor da 1ª "
       "prestação e o saldo devedor logo depois dela são, respectivamente,",
       "R$ 5.000,00 e R$ 46.000,00.",
       ["R$ 4.000,00 e R$ 46.000,00.", "R$ 5.000,00 e R$ 45.000,00.", "R$ 1.000,00 e R$ 49.000,00.",
        "R$ 4.080,00 e R$ 45.920,00."],
       "Juros: 50.000 × 0,02 = 1.000; prestação: 1.000 + 4.000 = 5.000; saldo: 50.000 − 4.000 = 46.000. Tirar a "
       "prestação inteira do saldo (45.000) trata os juros como amortização; 4.000 é só a amortização.",
       style="caso_pratico", t="case", pos=4),
    PV("Em qualquer sistema de amortização, os juros de cada período são calculados sobre",
       "o saldo devedor do início do período.",
       ["o valor original do empréstimo, até a última prestação.", "o valor da prestação paga no período.",
        "a soma das amortizações já pagas.", "o total das prestações que ainda faltam pagar."],
       "Os juros remuneram o dinheiro que ainda está emprestado, isto é, o saldo devedor no começo do período. Por isso "
       "caem à medida que a dívida é amortizada.",
       diff=1, style="literal"),
   ]),
  dict(title="Sistema de amortização constante (SAC)", subtopic="sac",
   summary="SAC: amortização constante, A = valor financiado ÷ número de prestações. Como o saldo cai sempre o mesmo "
           "valor, os juros também caem, e as prestações diminuem em progressão aritmética: cada uma é a anterior menos "
           "A × i. Prestação de número k = A + i × saldo antes dela.",
   items=[
    A1("Exemplo: R$ 12.000 em 4 prestações pelo SAC: a amortização é 12.000 ÷ 4 = R$ 3.000 em todas. Agora: R$ 60.000 "
       "em 10 prestações pelo SAC. Qual é a amortização mensal?",
       "R$ 6.000", ["R$ 600", "R$ 12.000"],
       "60.000 ÷ 10 = 6.000 em todos os meses. O R$ 600 divide por 100, e o R$ 12.000, por 5."),
    TF("No SAC, as prestações são iguais do começo ao fim do financiamento.", False,
       "No SAC a amortização é que é constante; os juros caem com o saldo, e as prestações diminuem. Prestações "
       "iguais são da Price."),
    QK("Prestação k = A + i × saldo antes dela. R$ 60.000 em 10 prestações pelo SAC, a 2% ao mês. A 1ª prestação é",
       "R$ 7.200", ["R$ 6.000", "R$ 7.080"],
       "6.000 + 0,02 × 60.000 = 6.000 + 1.200 = 7.200. O R$ 6.000 é só a amortização; o R$ 7.080 é a 2ª prestação "
       "(juros sobre 54.000)."),
    QK("No SAC, cada prestação é A × i menor que a anterior. Com A = R$ 6.000 e i = 2% ao mês, quanto a 3ª prestação "
       "é menor que a 2ª?",
       "R$ 120", ["R$ 1.200", "R$ 6.000"],
       "O saldo cai 6.000 por mês, então os juros caem 6.000 × 0,02 = 120. O R$ 1.200 são os juros da 1ª prestação, e "
       "o R$ 6.000, a amortização.",
       step=3),
    PVS("Um financiamento de R$ 30.000,00 será pago em 6 prestações mensais pelo SAC, a 3% ao mês, a primeira um mês "
        "após a contratação. O valor da 4ª prestação é de",
        ["R$ 5.150,00", "R$ 5.300,00", "R$ 5.450,00", "R$ 5.600,00", "R$ 5.900,00"], "R$ 5.450,00",
        "A = 30.000 ÷ 6 = 5.000. Antes da 4ª, já foram amortizados 15.000: saldo de 15.000 e juros de 450. Prestação: "
        "5.450. As outras alternativas são a 6ª, a 5ª, a 3ª e a 1ª prestação."),
    PVS("Pelo SAC, um empréstimo de R$ 48.000,00 em 24 prestações mensais teve 1ª prestação de R$ 3.440,00. A taxa "
        "mensal de juros é de",
        ["2,00%", "2,50%", "3,00%", "3,50%", "7,17%"], "3,00%",
        "A = 48.000 ÷ 24 = 2.000; juros da 1ª: 3.440 − 2.000 = 1.440; taxa: 1.440 ÷ 48.000 = 3%. O 7,17% divide a "
        "prestação inteira pelo saldo, sem separar a amortização.",
        t="mcq5"),
   ]),
  dict(title="Sistema Price (francês)", subtopic="price",
   summary="Price (francês): prestações iguais, P = valor financiado ÷ a, com a = [1 − (1 + i)⁻ⁿ] ÷ i. No começo, a "
           "prestação é quase toda juros; com o tempo, os juros caem e a amortização cresce, e a soma fica constante. "
           "Saldo depois de cada prestação = saldo × (1 + i) − P. A “Tabela Price” costuma vir com taxa nominal anual "
           "capitalizada mensalmente: divida por 12.",
   items=[
    A1("Exemplo: saldo de R$ 10.000, 2% ao mês e prestação Price de R$ 1.500: juros 200, amortização 1.300, saldo novo "
       "R$ 8.700. Agora: saldo de R$ 20.000, 3% ao mês e prestação Price de R$ 2.600. Qual é o saldo depois da "
       "prestação?",
       "R$ 18.000", ["R$ 17.400", "R$ 18.600"],
       "Juros: 600; amortização: 2.600 − 600 = 2.000; saldo: 18.000. O R$ 17.400 tira a prestação inteira; o R$ 18.600 "
       "soma os juros em vez de descontá-los da prestação."),
    TF("Na Price, a amortização cresce ao longo do tempo, enquanto os juros de cada prestação diminuem.", True,
       "A prestação é fixa; como o saldo cai, os juros caem, e a parte que sobra para amortizar cresce."),
    QK("P = valor financiado ÷ a. Um empréstimo de R$ 17.300 em 4 prestações mensais pela Price tem fator a = 3,46 "
       "(4 meses, 6%). A prestação é",
       "R$ 5.000", ["R$ 4.325", "R$ 4.584,50"],
       "17.300 ÷ 3,46 = 5.000. O R$ 4.325 divide por 4 e esquece os juros; o R$ 4.584,50 só soma 6% a isso."),
    QK("Taxa nominal na Tabela Price: 24% ao ano com capitalização mensal dão 2% ao mês. Num saldo de R$ 15.000, os "
       "juros do mês são",
       "R$ 300", ["R$ 3.600", "R$ 600"],
       "24 ÷ 12 = 2% ao mês: 15.000 × 0,02 = 300. O R$ 3.600 aplica a taxa anual inteira; o R$ 600 usa 4% (24 ÷ 6).",
       step=3),
    PV("Um empréstimo de R$ 6.000,00 será pago em 3 prestações mensais iguais pela Tabela Price, a 10% ao mês, a "
       "primeira um mês após a contratação. Dado o fator a = 2,4869 (3 meses, 10%), o valor aproximado de cada "
       "prestação e os juros embutidos na 1ª são, respectivamente,",
       "R$ 2.412,64 e R$ 600,00.",
       ["R$ 2.000,00 e R$ 600,00.", "R$ 2.412,64 e R$ 241,26.", "R$ 2.600,00 e R$ 600,00.", "R$ 2.200,00 e R$ 200,00."],
       "P = 6.000 ÷ 2,4869 ≈ 2.412,64. Os juros da 1ª incidem sobre o saldo inteiro: 6.000 × 0,10 = 600. O 2.600 é a 1ª "
       "prestação do SAC; o 2.000 é só a amortização do SAC; o 241,26 aplica a taxa sobre a prestação.",
       style="caso_pratico", t="case", pos=0),
    PVS("Pela Tabela Price, um empréstimo de R$ 10.000,00 a 4% ao mês tem prestação de R$ 2.246,27. O saldo devedor "
        "logo após a 1ª prestação é de",
        ["R$ 7.753,73", "R$ 8.153,73", "R$ 9.600,00", "R$ 10.000,00", "R$ 10.400,00"], "R$ 8.153,73",
        "Saldo × (1 + i) − P: 10.000 × 1,04 − 2.246,27 = 8.153,73 (juros 400, amortização 1.846,27). O 7.753,73 tira "
        "a prestação inteira; o 9.600 tira só os juros; o 10.400 só os soma."),
   ]),
  dict(title="Sistema misto e comparação", subtopic="sam e comparação",
   summary="SAM (sistema misto): cada prestação é a média aritmética das prestações do SAC e da Price no mesmo mês; o "
           "mesmo vale para juros, amortização e saldo. Comparação, com mesmo valor, taxa e prazo: a 1ª prestação do "
           "SAC é maior que a da Price, e a última, menor; o SAC paga menos juros no total, porque amortiza mais cedo.",
   items=[
    A1("Exemplo: no mês 1, a prestação do SAC é R$ 3.000 e a da Price, R$ 2.600; no SAM, (3.000 + 2.600) ÷ 2 = "
       "R$ 2.800. Agora: no mês 1, SAC = R$ 7.200 e Price = R$ 6.680. Qual é a prestação do SAM?",
       "R$ 6.940", ["R$ 13.880", "R$ 6.680"],
       "(7.200 + 6.680) ÷ 2 = 6.940. O R$ 13.880 soma sem dividir; o R$ 6.680 é a Price."),
    TF("Com o mesmo valor, a mesma taxa e o mesmo prazo, o total de juros pagos no SAC é menor que na Price.", True,
       "No SAC a dívida cai mais depressa no começo, então os juros, que incidem sobre o saldo, somam menos."),
    QK("No SAM, o saldo também é a média dos dois sistemas. Depois da 1ª prestação, o saldo no SAC é R$ 54.000 e na "
       "Price, R$ 54.520. Qual é o saldo no SAM?",
       "R$ 54.260", ["R$ 108.520", "R$ 54.000"],
       "(54.000 + 54.520) ÷ 2 = 54.260. O R$ 108.520 soma sem dividir; o R$ 54.000 é o saldo do SAC."),
    MT("Associe cada sistema à sua característica:",
       [("SAC", "Amortização constante e prestações decrescentes"),
        ("Price", "Prestações constantes e amortização crescente"),
        ("SAM", "Prestação igual à média de SAC e Price")],
       "SAC fixa a amortização; Price fixa a prestação; SAM tira a média dos dois, mês a mês."),
    PVS("Um financiamento de R$ 100.000,00 em 10 prestações mensais, a 2% ao mês, tem 1ª prestação de R$ 11.132,65 pela "
        "Tabela Price. Pelo sistema misto (SAM), a 1ª prestação é de, aproximadamente,",
        ["R$ 10.000,00", "R$ 11.132,65", "R$ 11.566,33", "R$ 12.000,00", "R$ 23.132,65"], "R$ 11.566,33",
        "SAC: 10.000 de amortização + 2.000 de juros = 12.000. SAM: (12.000 + 11.132,65) ÷ 2 ≈ 11.566,33. O 12.000 e o "
        "11.132,65 são o SAC e a Price; o 10.000 é só a amortização do SAC; o 23.132,65 soma sem dividir."),
    PV("Comparando SAC e Price com o mesmo valor financiado, a mesma taxa e o mesmo prazo, NÃO é correto afirmar que",
       "a 1ª prestação da Price é maior que a 1ª prestação do SAC.",
       ["o SAC paga menos juros no total.", "no SAC, a amortização é igual em todos os meses.",
        "na Price, as prestações são iguais.", "a última prestação do SAC é menor que a última da Price."],
       "O SAC começa com a prestação maior, porque amortiza mais logo no início; a Price começa menor e fica igual até o "
       "fim. As outras afirmações estão corretas.",
       style="excecao"),
   ]),
 ]),
]
