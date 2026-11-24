# G2 Raciocínio lógico (u12), lição 3: Proporção e porcentagem. Gerado para o app por tools/gen_u16.py u12.
# Mesmo formato da lição 2 (certa primeiro; PVS ordena alternativas numéricas; um assert por conta). Conteúdo de IA (não
# revisado), conferido contra a apostila do cargo (OP-121NV-26), usada só como referência de fatos e métodos.
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


# --- contas conferidas ---------------------------------------------------------------------------------------------
assert 8 * 25_000 == 200_000                      # cm = 2 km
assert F(12, 18) == F(2, 3)
assert 35 * 8 / 5 == 56
assert (11 - 1) * 30 == 300
assert 315 * F(2, 9) == 70
assert 160 * F(5, 8) == 100                       # t + 3/5 t = 160 min
assert 1200 * F(5, 8) == 750
assert 500 * F(3, 5) == 300                       # inversos de 2 e 3, × 6 = 3 e 2
assert F(2000 * 3, 2000 * 3 + 3000 * 4) * 3000 == 1000 and F(2, 5) * 3000 == 1200
assert F(4400, 16) * 5 == 1375
assert F(520, 13) * 6 == 240                      # 1/2, 1/3, 1/4 × 12 = 6, 4, 3
assert 40 * 3 / 6 == 20
assert 30 * F(4800, 2400) * F(4, 6) == 40
assert 240 / (120 / 60 + 120 / 40) == 48
assert 6 * F(540, 360) * F(4, 3) == 12
assert F(4320, 900 * F(6, 5)) == 4 and F(4320, 900) == F(24, 5)    # 4 h (antes, 4h48min)
assert F(35, 100) * 240 == 84
assert F(60, 100) * F(50, 100) == F(30, 100)
assert F(9, 25) == F(36, 100)
assert F(60, 100) * F(1, 2) + F(10, 100) == F(40, 100)
assert 180 + 150 == F(60, 100) * (400 + 150)
assert F(40 * 25, 40 * 25 + 60 * 50) == F(1, 4)
assert 40 * F(112, 100) == F(448, 10)
assert F(6, 5) * F(4, 5) == F(96, 100)
assert F(210, F(105, 100)) == 200
assert F(11, 10) ** 2 == F(121, 100)
assert F(5, 4) * F(4, 5) == 1
assert 600 - F(20, 100) * 600 == 480 and F(600, F(6, 5)) == 500


LESSONS = [
 dict(order=3, title="Proporção e porcentagem", concepts=[
  dict(title="Razão, proporção e escala", subtopic="razão e proporção",
   summary="Razão compara por divisão: 12 caixas e 18 atendentes estão na razão 12 : 18 = 2 : 3. Proporção é a "
           "igualdade de duas razões (a/b = c/d) e se resolve pelo produto cruzado: a × d = b × c. Escala 1 : 50.000 "
           "quer dizer que 1 cm no mapa vale 50.000 cm (500 m) no real. Em pontos igualmente espaçados, conte os "
           "intervalos, e não os pontos.",
   items=[
    A1("Exemplo: numa escala 1 : 10.000, 3 cm no mapa valem 30.000 cm = 300 m. Agora: num mapa em escala 1 : 25.000, "
       "duas agências estão a 8 cm uma da outra. Qual é a distância real?",
       "2 km", ["200 m", "20 km"],
       "8 × 25.000 = 200.000 cm. Como 100.000 cm = 1 km, são 2 km. 200 m e 20 km erram a conversão de centímetros."),
    TF("Se a razão entre caixas e atendentes é 2 : 3 e há 12 caixas, então há 18 atendentes.", True,
       "12/x = 2/3, e pelo produto cruzado 2x = 36, x = 18. Conferindo: 12 : 18 simplifica para 2 : 3."),
    QK("Produto cruzado: a/b = c/d ⇒ a × d = b × c. Se 5 cartões custam R$ 35, quanto custam 8 cartões do mesmo tipo?",
       "R$ 56", ["R$ 40", "R$ 48"],
       "5/35 = 8/x, então 5x = 280 e x = 56 (cada cartão custa R$ 7). O R$ 40 soma 5 ao preço; o R$ 48 não segue a "
       "proporção."),
    QK("Em pontos igualmente espaçados, conte os intervalos: do 1º ao 5º ponto há 4 intervalos. Numa rua, há um poste a "
       "cada 30 m. Qual é a distância do 1º ao 11º poste?",
       "300 m", ["330 m", "270 m"],
       "Do 1º ao 11º há 10 intervalos: 10 × 30 = 300 m. O 330 conta 11 intervalos (os postes); o 270 conta 9.",
       step=3),
    PVS("Numa agência, a razão entre clientes pessoa física e pessoa jurídica é de 7 para 2. Se há 315 clientes ao "
        "todo, o número de clientes pessoa jurídica é",
        ["45", "63", "70", "90", "245"], "70",
        "A razão 7 : 2 divide o total em 9 partes: 315 ÷ 9 = 35 por parte. PJ = 2 × 35 = 70; PF = 7 × 35 = 245. O 63 "
        "e o 90 tomam 2/10 e 2/7 do total, e o 245 é o número de pessoas físicas."),
    PVS("Uma viagem de 2h40min foi feita em dois trechos, e o segundo trecho durou 3/5 do tempo do primeiro. O primeiro "
        "trecho durou",
        ["1h04min", "1h20min", "1h36min", "1h40min", "2h00min"], "1h40min",
        "2h40min = 160 min. Se o primeiro dura t, o segundo dura 3t/5, e t + 3t/5 = 8t/5 = 160, então t = 100 min = "
        "1h40min (o segundo, 60 min). O 1h36min toma 3/5 do total; o 1h04min, 2/5; o 1h20min divide ao meio."),
   ]),
  dict(title="Divisão proporcional", subtopic="divisão proporcional",
   summary="Direta: divida o total pela soma das partes. R$ 900 em partes proporcionais a 2, 3 e 4: 900 ÷ 9 = 100, e "
           "as partes são 200, 300 e 400. Inversa: use os inversos (1/2, 1/3, 1/6) e multiplique pelo MMC dos "
           "denominadores para tirar as frações (3, 2 e 1); quem tem o número maior recebe menos. Proporcional a duas "
           "grandezas ao mesmo tempo: multiplique-as (capital × tempo).",
   items=[
    A1("Exemplo: R$ 600 em partes proporcionais a 1 e 2 dá 600 ÷ 3 = 200 por parte: R$ 200 e R$ 400. Agora: um prêmio "
       "de R$ 1.200 será dividido entre dois caixas em partes proporcionais a 3 e 5. Quanto recebe quem tem a parte 5?",
       "R$ 750", ["R$ 450", "R$ 600"],
       "3 + 5 = 8 partes; 1.200 ÷ 8 = 150; 5 × 150 = 750. R$ 450 é a parte 3, e R$ 600, a divisão ao meio."),
    TF("Na divisão inversamente proporcional, quem tem o maior número recebe a maior parte.", False,
       "É o contrário: divide-se pelos inversos, e o maior número tem o menor inverso. R$ 120 em partes inversas a 1 e "
       "2 dão R$ 80 (para o 1) e R$ 40 (para o 2)."),
    QK("Inversa a 2 e 3: os inversos 1/2 e 1/3, multiplicados por 6, viram 3 e 2. R$ 500 divididos em partes "
       "inversamente proporcionais a 2 e 3 dão, respectivamente,",
       "R$ 300 e R$ 200", ["R$ 200 e R$ 300", "R$ 250 e R$ 250"],
       "Partes 3 e 2 somam 5: 500 ÷ 5 = 100, então 300 para o 2 e 200 para o 3. R$ 200 e R$ 300 seria a divisão "
       "direta."),
    QK("Proporcional a duas grandezas ao mesmo tempo: multiplique-as. Dois sócios dividem um lucro de R$ 3.000 "
       "proporcionalmente a capital × tempo: A aplicou R$ 2.000 por 3 meses e B, R$ 3.000 por 4 meses. Quanto recebe A?",
       "R$ 1.000", ["R$ 1.200", "R$ 1.500"],
       "Pesos: A = 2.000 × 3 = 6.000 e B = 3.000 × 4 = 12.000, razão 1 : 2. A recebe 1/3 de 3.000 = 1.000. O R$ 1.200 "
       "olha só o capital (2 : 3) e esquece o tempo.",
       step=3),
    PVS("Uma bonificação de R$ 4.400,00 será repartida entre três equipes em partes diretamente proporcionais ao número "
        "de metas batidas: 3, 5 e 8. A equipe que bateu 5 metas recebe",
        ["R$ 1.375,00", "R$ 1.466,67", "R$ 2.200,00", "R$ 2.750,00", "R$ 3.025,00"], "R$ 1.375,00",
        "3 + 5 + 8 = 16 partes; 4.400 ÷ 16 = 275; 5 × 275 = 1.375. O 2.200 é a parte da equipe de 8 metas; o 1.466,67 "
        "divide o total por 3, ignorando a proporção; o 2.750 toma 5/8 do total, esquecendo a equipe de 3 metas; o "
        "3.025 é o que vai para as outras duas equipes."),
    PVS("Três agências receberão 520 talões em quantidades inversamente proporcionais ao número de reclamações de cada "
        "uma: 2, 3 e 4. A agência com 2 reclamações recebe",
        ["120", "160", "173", "240", "260"], "240",
        "Inversos 1/2, 1/3 e 1/4; vezes 12 (o MMC), viram 6, 4 e 3, que somam 13. 520 ÷ 13 = 40, e a agência com 2 "
        "reclamações recebe 6 × 40 = 240 (as outras, 160 e 120). O 173 divide em três partes iguais; o 260 dá metade "
        "do total a quem reclamou menos; o 120 é a parte da agência com 4 reclamações."),
   ]),
  dict(title="Regra de três", subtopic="regra de três",
   summary="Monte uma tabela com as grandezas e compare cada uma com a que se procura. Diretamente proporcionais: se uma "
           "dobra, a outra dobra (mais extratos, mais tempo). Inversamente: se uma dobra, a outra cai à metade (mais "
           "caixas, menos tempo). Na regra de três composta, analise cada grandeza separadamente em relação à "
           "incógnita. Velocidade média = distância total ÷ tempo total.",
   items=[
    A1("Exemplo: se 4 caixas atendem uma fila em 30 min, 2 caixas levam 60 min: menos caixas, mais tempo (inversa). "
       "Agora: 3 caixas atendem uma fila em 40 min. Em quanto tempo 6 caixas, no mesmo ritmo, atendem a mesma fila?",
       "20 min", ["80 min", "40 min"],
       "O dobro de caixas faz o trabalho na metade do tempo: 40 × 3 ÷ 6 = 20 min. O 80 trata a relação como direta."),
    TF("Se 5 impressoras imprimem 1.000 extratos em 2 horas, as mesmas 5 impressoras imprimem 3.000 extratos em 6 "
       "horas.", True,
       "Com as mesmas máquinas, extratos e tempo são diretamente proporcionais: o triplo de extratos leva o triplo do "
       "tempo."),
    QK("Regra de três composta: compare cada grandeza com o tempo. 4 máquinas contam 2.400 cédulas em 30 min. Em "
       "quantos minutos 6 máquinas contam 4.800 cédulas?",
       "40", ["90", "60"],
       "O dobro de cédulas dobra o tempo (direta); 6 máquinas em vez de 4 multiplicam o tempo por 4/6 (inversa): "
       "30 × 2 × 4/6 = 40. O 90 trata as máquinas como diretas; o 60 as ignora."),
    QK("Velocidade média é a distância total dividida pelo tempo total, não a média das velocidades. Um carro vai 120 "
       "km a 60 km/h e volta os mesmos 120 km a 40 km/h. Qual é a velocidade média na viagem toda?",
       "48 km/h", ["50 km/h", "45 km/h"],
       "Ida: 2 h; volta: 3 h. 240 km ÷ 5 h = 48 km/h. O 50 é a média das duas velocidades, que ignora que a volta "
       "demorou mais.",
       step=3),
    PVS("Numa central de atendimento, 6 atendentes respondem 360 mensagens em 4 horas. Mantido o ritmo, quantos "
        "atendentes são necessários para responder 540 mensagens em 3 horas?",
        ["8", "9", "10", "12", "15"], "12",
        "Mais mensagens pedem mais atendentes (direta: × 540/360 = 1,5); menos tempo também pede mais atendentes "
        "(inversa: × 4/3). 6 × 1,5 × 4/3 = 12. O 9 considera só as mensagens; o 8 só o tempo; o 15 erra a inversão."),
    PVS("Um equipamento de compensação processava 900 cheques por hora. Depois de uma atualização, passou a processar "
        "20% a mais por hora. O tempo para processar 4.320 cheques passou a ser de",
        ["3h12min", "3h36min", "4h00min", "4h20min", "4h48min"], "4h00min",
        "Nova capacidade: 900 × 1,2 = 1.080 cheques por hora. 4.320 ÷ 1.080 = 4 h. O 4h48min é o tempo antigo "
        "(4.320 ÷ 900 = 4,8 h); o 3h36min tira 25% do tempo antigo, como se a relação fosse direta."),
   ]),
  dict(title="Porcentagem", subtopic="porcentagem",
   summary="x% de um valor é x ÷ 100 vezes o valor: 15% de 80 = 0,15 × 80 = 12. Porcentagem de porcentagem multiplica: "
           "40% de 70% = 0,4 × 0,7 = 28%. “Quantos por cento A é de B”: faça A ÷ B. Em grupos cruzados (homens e "
           "mulheres, com e sem curso), monte uma tabela de dupla entrada e use um total de 100.",
   items=[
    A1("Exemplo: 20% de 350 = 0,2 × 350 = 70. Agora: 35% dos 240 clientes atendidos hoje usaram o aplicativo. "
       "Quantos clientes usaram o aplicativo?",
       "84", ["35", "96"],
       "0,35 × 240 = 84. O 35 é a taxa, não a quantidade, e o 96 seria 40% de 240."),
    TF("Se 60% dos clientes têm cartão e, desses, metade usa a função crédito, então 30% de todos os clientes usam a "
       "função crédito.", True,
       "Porcentagem de porcentagem multiplica: 0,6 × 0,5 = 0,3 = 30% do total."),
    QK("“Quantos por cento A é de B”: faça A ÷ B. Numa equipe de 25 pessoas, 9 são analistas. Que porcentagem da "
       "equipe são analistas?",
       "36%", ["9%", "64%"],
       "9 ÷ 25 = 0,36 = 36%. O 9% confunde quantidade com porcentagem, e 64% é a parte que não é analista."),
    QK("Use um total de 100 e uma tabela. Numa empresa, 60% dos funcionários são mulheres, e metade delas trabalha no "
       "atendimento. Os homens do atendimento são 10% do total de funcionários. Que porcentagem do total trabalha no "
       "atendimento?",
       "40%", ["30%", "70%"],
       "Em 100 funcionários: 60 mulheres, 30 delas no atendimento, mais 10 homens = 40. O 30% conta só as mulheres; o "
       "70% soma 60% e 10%.",
       step=3),
    PVS("Numa carteira de 400 clientes, 45% são do varejo. Depois da entrada de novos clientes, todos do varejo, o "
        "varejo passou a representar 60% da carteira. Quantos clientes novos entraram?",
        ["60", "90", "120", "150", "240"], "150",
        "Varejo antes: 180. Com x novos: 180 + x = 0,6 × (400 + x), então 180 + x = 240 + 0,6x, 0,4x = 60 e x = 150. "
        "Conferindo: 330 de 550 é 60%. O 60 aplica os 15 pontos de diferença sobre 400; o 240 é 60% de 400."),
    PVS("Num banco, 40% dos funcionários são da área de TI. Entre os de TI, 25% são mulheres; entre os demais, 50% são "
        "mulheres. Que porcentagem das mulheres do banco trabalha na área de TI?",
        ["10%", "20%", "25%", "40%", "50%"], "25%",
        "Em 100 funcionários: 40 de TI, com 10 mulheres; 60 de outras áreas, com 30 mulheres. São 40 mulheres, e 10 "
        "delas na TI: 10 ÷ 40 = 25%. O 10% é a parte do total de funcionários, não das mulheres; o 40% é a fatia da TI "
        "no banco."),
   ]),
  dict(title="Acréscimos, descontos e variações", subtopic="acréscimos e descontos",
   summary="Acréscimo de x%: multiplique por (1 + x/100); desconto de x%: por (1 − x/100). Variações sucessivas "
           "multiplicam os fatores: +10% e depois −10% dá 1,1 × 0,9 = 0,99, ou seja, 1% a menos. Para achar o valor "
           "original, divida pelo fator: R$ 220 com 10% de acréscimo vieram de 220 ÷ 1,1 = R$ 200. Lucro “sobre a "
           "venda” usa o preço de venda como base, e não o custo.",
   items=[
    A1("Exemplo: um produto de R$ 200 com 15% de desconto sai por 200 × 0,85 = R$ 170. Agora: uma tarifa de R$ 40,00 "
       "teve reajuste de 12%. Qual é o novo valor?",
       "R$ 44,80", ["R$ 52,00", "R$ 35,20"],
       "40 × 1,12 = 44,80. O R$ 35,20 aplica desconto em vez de acréscimo, e o R$ 52,00 soma 12 reais, não 12%."),
    TF("Um aumento de 20% seguido de um desconto de 20% traz o preço de volta ao valor inicial.", False,
       "1,2 × 0,8 = 0,96: o preço termina 4% abaixo do inicial, porque o desconto incide sobre um valor já aumentado."),
    QK("Para voltar ao valor original, divida pelo fator. Uma conta paga com 5% de multa custou R$ 210,00. Qual era o "
       "valor original?",
       "R$ 200,00", ["R$ 199,50", "R$ 205,00"],
       "210 ÷ 1,05 = 200. O R$ 199,50 tira 5% de 210, que é uma base diferente da usada para a multa."),
    QK("Variações sucessivas multiplicam os fatores. Dois aumentos seguidos de 10% equivalem a um aumento único de",
       "21%", ["20%", "11%"],
       "1,1 × 1,1 = 1,21: aumento de 21%. O segundo aumento incide também sobre o primeiro, por isso passa de 20%.",
       step=3, style="conceitual"),
    PV("O preço de um serviço subiu 25% em janeiro e caiu 20% em fevereiro. Em relação ao preço do início de janeiro, o "
       "preço no fim de fevereiro",
       "ficou igual.", ["ficou 5% maior.", "ficou 5% menor.", "ficou 4% menor.", "ficou 45% maior."],
       "1,25 × 0,8 = 1,00: o preço volta ao inicial. Somar e subtrair as porcentagens (25 − 20 = 5%) ignora que a "
       "queda incide sobre um valor maior.",
       style="caso_pratico", t="case"),
    PVS("Um comerciante vendeu um produto por R$ 600,00, com lucro de 20% sobre o preço de venda. O custo do produto "
        "foi de",
        ["R$ 480,00", "R$ 500,00", "R$ 520,00", "R$ 576,00", "R$ 720,00"], "R$ 480,00",
        "O lucro é 20% da venda: 0,2 × 600 = 120, então o custo é 600 − 120 = 480. O R$ 500 seria o custo com lucro "
        "de 20% sobre o custo (600 ÷ 1,2); o R$ 720 soma 20% à venda."),
   ]),
 ]),
]

# distratores conferidos
assert 4400 * F(5, 8) == 2750 and 4400 - 1375 == 3025 and 4400 * F(8, 16) == 2200
