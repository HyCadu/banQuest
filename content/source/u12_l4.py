# G2 Raciocínio lógico (u12), lição 4: Estatística e contagem. Gerado para o app por tools/gen_u16.py u12.
# Mesmo formato da lição 2 (certa primeiro; PVS ordena alternativas numéricas; um assert por conta). Conteúdo de IA (não
# revisado), conferido contra a apostila do cargo (OP-121NV-26), usada só como referência de fatos e métodos.
# Variância: a apostila define a amostral (divide por n − 1); as bancas costumam pedir a populacional (divide por n).
# Decisão do dono (24/11/2026): segue o padrão das bancas. A regra é a populacional; a amostral aparece só como
# exceção, quando a questão a pedir (e como distrator).
from fractions import Fraction as F
from itertools import permutations, product
from math import comb, factorial, perm, sqrt
from statistics import mean, median, mode, pstdev, pvariance, stdev


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
assert mean([12, 15, 9, 14, 10]) == 12
assert median([3, 8, 2, 9, 5]) == 5
assert median([7, 1, 4, 10, 3, 8]) == 5.5
assert F(6 * 1 + 9 * 2, 3) == 8
SAQUES = [50, 100, 50, 200, 80, 50, 120, 950]
assert mode(SAQUES) == 50 and median(SAQUES) == 90 and mean(SAQUES) == 200 and max(SAQUES) - min(SAQUES) == 900
assert 5 * F(74, 10) - 4 * 7 == 9
assert max([6, 15, 9, 3, 12]) - min([6, 15, 9, 3, 12]) == 12
assert mean([abs(x - 5) for x in [1, 5, 6, 8]]) == 2 and sum(x - 5 for x in [1, 5, 6, 8]) == 0
assert pvariance([1, 3, 5, 7]) == 5 and mean([abs(x - 4) for x in [1, 3, 5, 7]]) == 2
assert pvariance([3 * x for x in [1, 3, 5, 7]]) == 9 * pvariance([1, 3, 5, 7])
assert round(pstdev([2, 4, 4, 6]), 2) == 1.41 and round(stdev([2, 4, 4, 6]), 2) == 1.63 and pvariance([2, 4, 4, 6]) == 2
assert 360 * F(40, 100) == 144
assert F(10, 18 + 12 + 10) == F(25, 100)
assert 520 - 400 > 600 - 520
TRI = [240, 300, 180, 480]
assert sum(TRI) == 1200 and F(480, 1200) == F(40, 100) and mean(TRI) == 300 and F(240, 1200) == F(20, 100)
assert F(180, 300) == F(60, 100)                  # 40% abaixo, não 50%
assert F(360 - 216 - 90, 360) * 1200 == 180
assert 5 * 3 == 15
assert perm(10, 3) == 720
assert len([n for n in permutations("01234", 3) if n[0] != "0"]) == 48
assert 4 + 3 == 7
assert perm(5, 2) * 10 * 10 == 2000
assert len([n for n in permutations("12345", 3) if int(n[2]) % 2 == 0]) == 24
assert factorial(5) == 120
assert comb(6, 2) == 15 and perm(6, 2) == 30
assert factorial(6) // (factorial(3) * factorial(2)) == 60
assert len(set(permutations("BANANA"))) == 60
assert perm(7, 2) == 42 and comb(7, 2) == 21
assert comb(4, 2) * comb(6, 3) == 120 and perm(4, 2) * comb(6, 3) == 240 and comb(10, 5) == 252
assert 4 + 1 == 5                                 # 4 cores: pior caso, uma de cada
assert F(2, 6) == F(1, 3)
assert 5 + 7 + 2 == 14
assert F(2, 5) * F(1, 4) == F(1, 10)
assert F(6, 10) * F(4, 9) == F(4, 15) and F(6, 10) * F(4, 10) == F(6, 25)
assert F(sum(1 for a, b in product(range(1, 7), repeat=2) if a + b == 8), 36) == F(5, 36)


LESSONS = [
 dict(order=4, title="Estatística e contagem", concepts=[
  dict(title="Média, mediana e moda", subtopic="tendência central",
   summary="Média = soma ÷ quantidade; média ponderada = soma de (valor × peso) ÷ soma dos pesos. Mediana: ordene os "
           "dados e pegue o do meio (com quantidade par, a média dos dois do meio). Moda: o valor que mais se repete "
           "(pode haver mais de uma ou nenhuma). A média é puxada por valores extremos; a mediana, não.",
   items=[
    A1("Exemplo: os atendimentos 4, 6 e 8 têm média (4 + 6 + 8) ÷ 3 = 6. Agora: um caixa atendeu 12, 15, 9, 14 e 10 "
       "clientes em cinco dias. Qual foi a média diária?",
       "12", ["13", "60"],
       "Soma: 12 + 15 + 9 + 14 + 10 = 60; dividindo pelos 5 dias, 12. O 60 é a soma, antes de dividir."),
    TF("Nos dados 3, 8, 2, 9 e 5, a mediana é 2, o valor que está no meio da lista como ela foi escrita.", False,
       "Primeiro se ordena: 2, 3, 5, 8, 9. A mediana é o termo do meio da lista ordenada, 5."),
    QK("Com quantidade par de dados, a mediana é a média dos dois valores do meio, depois de ordenar. Qual é a mediana "
       "de 7, 1, 4, 10, 3 e 8?",
       "5,5", ["4", "7"],
       "Ordenando: 1, 3, 4, 7, 8, 10. Os do meio são 4 e 7, e (4 + 7) ÷ 2 = 5,5. Ficar só com o 4 ou só com o 7 "
       "esquece que há dois termos centrais."),
    QK("Média ponderada: some valor × peso e divida pela soma dos pesos. Numa avaliação, Gerais tem peso 1 e "
       "Específicos, peso 2. Um candidato tirou 6 em Gerais e 9 em Específicos. Qual é a média ponderada?",
       "8", ["7,5", "8,5"],
       "(6 × 1 + 9 × 2) ÷ (1 + 2) = 24 ÷ 3 = 8. O 7,5 é a média simples, que ignora os pesos.",
       step=3),
    PV("Os valores, em reais, de oito saques feitos num caixa eletrônico foram: 50, 100, 50, 200, 80, 50, 120 e 950. "
       "Sobre esses valores, NÃO é correto afirmar que",
       "a média é menor que a mediana.",
       ["a moda é 50.", "a mediana é 90.", "a média é 200.", "a amplitude é 900."],
       "Ordenando: 50, 50, 50, 80, 100, 120, 200, 950. Moda: 50 (aparece três vezes). Mediana: (80 + 100) ÷ 2 = 90. "
       "Média: 1.600 ÷ 8 = 200. Amplitude: 950 − 50 = 900. A média fica acima da mediana, puxada pelo saque de 950; "
       "por isso a afirmação de que ela é menor está errada.",
       style="excecao", t="case"),
    PVS("A média das notas de 4 funcionários numa avaliação foi 7. Um quinto funcionário fez a mesma avaliação, e a "
        "média dos 5 passou a ser 7,4. A nota do quinto funcionário foi",
        ["7,4", "8", "8,6", "9", "9,4"], "9",
        "Soma antes: 4 × 7 = 28. Soma depois: 5 × 7,4 = 37. A nota nova é 37 − 28 = 9. O 7,4 é a nova média, e o 9,4 "
        "soma 2 à média em vez de trabalhar com as somas."),
   ]),
  dict(title="Medidas de dispersão", subtopic="dispersão",
   summary="Amplitude = maior − menor. Desvio de um valor = valor − média; a soma dos desvios é sempre zero. Desvio "
           "médio: média dos desvios sem sinal. Variância: média dos quadrados dos desvios, dividindo por n (a "
           "populacional, padrão das bancas); só se a questão pedir a amostral, divide-se por n − 1. Desvio padrão = raiz da variância, na mesma unidade dos "
           "dados. Somar uma constante a todos não muda a dispersão; multiplicar por k multiplica o desvio padrão por k "
           "e a variância por k².",
   items=[
    A1("Exemplo: em 4, 9 e 11, a amplitude é 11 − 4 = 7. Agora: os tempos de atendimento foram 6, 15, 9, 3 e 12 "
       "minutos. Qual é a amplitude?",
       "12", ["9", "15"],
       "Maior menos menor: 15 − 3 = 12. O 15 é só o maior valor, e o 9, a diferença entre o primeiro e o último da "
       "lista como foi escrita."),
    TF("Se todos os salários de uma equipe sobem R$ 300, o desvio padrão dos salários não muda.", True,
       "Somar a mesma quantia desloca todos os valores juntos: a média sobe R$ 300, mas a distância de cada salário "
       "até a média continua a mesma."),
    QK("Desvio médio: a média das distâncias, sem sinal, até a média. Para 1, 5, 6 e 8, a média é 5. Qual é o desvio "
       "médio?",
       "2", ["0", "8"],
       "As distâncias são 4, 0, 1 e 3, que somam 8; divididas por 4, dão 2. O 0 é a soma dos desvios com sinal "
       "(sempre zero), e o 8 esquece de dividir."),
    QK("Variância populacional: a média dos quadrados dos desvios. Para 1, 3, 5 e 7 (média 4), os desvios são −3, −1, "
       "1 e 3. Qual é a variância populacional?",
       "5", ["2", "0"],
       "Quadrados: 9, 1, 1 e 9, que somam 20; 20 ÷ 4 = 5. O 2 é o desvio médio (sem elevar ao quadrado), e o 0, a soma "
       "dos desvios com sinal. O desvio padrão seria √5 ≈ 2,24.",
       step=3),
    PV("Sobre as medidas de dispersão de um conjunto de dados, NÃO é correto afirmar que",
       "multiplicar todos os dados por 3 multiplica a variância por 3.",
       ["o desvio padrão é a raiz quadrada positiva da variância.",
        "somar 10 a todos os dados não altera o desvio padrão.",
        "a amplitude é a diferença entre o maior e o menor valor.",
        "a soma dos desvios em relação à média aritmética é zero."],
       "Multiplicar os dados por 3 multiplica o desvio padrão por 3 e a variância por 3² = 9, porque ela é feita de "
       "quadrados. As outras afirmações estão corretas.",
       style="excecao"),
    PVS("Os saldos, em milhares de reais, de quatro contas são 2, 4, 4 e 6. O desvio padrão populacional desses saldos, "
        "em milhares de reais, é aproximadamente",
        ["1,00", "1,41", "1,63", "2,00", "4,00"], "1,41",
        "Média 4; desvios −2, 0, 0 e 2; quadrados somam 8. Variância populacional: 8 ÷ 4 = 2; desvio padrão: √2 ≈ "
        "1,41. O 1,63 é o desvio padrão amostral (√(8 ÷ 3)); o 2,00 é a variância; o 1,00 é o desvio médio; o 4,00 é a "
        "média."),
   ]),
  dict(title="Gráficos e tabelas", subtopic="gráficos",
   summary="Antes dos números, leia o título, as unidades e a escala. Setores: cada fatia é parte do todo, e 25% "
           "ocupam 360° × 0,25 = 90°. Histograma: barras encostadas para faixas de valores; a altura (ou a área) mostra "
           "a frequência. Linha: evolução no tempo; compare as variações entre os pontos, e não só os valores. Não "
           "conclua nada sobre o que o gráfico não mostra.",
   items=[
    A1("Exemplo: num gráfico de setores, uma fatia de 25% ocupa 360° × 0,25 = 90°. Agora: numa pesquisa, 40% dos "
       "clientes preferem o aplicativo. Quantos graus ocupa essa fatia?",
       "144°", ["40°", "160°"],
       "360° × 0,4 = 144°. O 40° confunde porcentagem com grau, e o 160° usa 400° como volta inteira."),
    TF("Um gráfico de linha mostra o saldo de uma conta só em janeiro (R$ 500) e em março (R$ 800). Com base nele, "
       "pode-se afirmar que o saldo de fevereiro foi de R$ 650.", False,
       "O gráfico não traz fevereiro: o saldo pode ter subido, caído ou ficado parado no meio. A média dos dois meses "
       "é uma suposição, não um dado."),
    QK("Numa tabela de frequências, some as frequências para ter o total. Atendimentos por tempo de espera: até 5 min, "
       "18; de 5 a 10 min, 12; mais de 10 min, 10. Que porcentagem dos clientes esperou mais de 10 min?",
       "25%", ["10%", "30%"],
       "Total: 18 + 12 + 10 = 40; 10 ÷ 40 = 25%. O 10% usa a frequência como se fosse porcentagem, e o 30% é a faixa "
       "de 5 a 10 min."),
    QK("Compare variações, não só valores. Clientes no aplicativo: 2021, 400 mil; 2022, 520 mil; 2023, 600 mil. Em "
       "qual período o número de clientes cresceu mais?",
       "de 2021 para 2022", ["de 2022 para 2023", "igual nos dois períodos"],
       "De 2021 para 2022 entraram 120 mil; de 2022 para 2023, 80 mil. O maior valor está em 2023, mas o maior "
       "crescimento foi antes.",
       step=3),
    PV("O número de contas abertas numa agência, por trimestre de um ano, foi: 1º trimestre, 240; 2º, 300; 3º, 180; "
       "4º, 480. Com base nesses dados, é correto afirmar que",
       "o 4º trimestre concentrou 40% das contas abertas no ano.",
       ["a média trimestral foi de 240 contas abertas.", "o 3º trimestre ficou 50% abaixo do 2º.",
        "o 1º trimestre teve 25% das contas abertas no ano.",
        "a maior alta de um trimestre para o seguinte foi do 1º para o 2º."],
       "Total do ano: 1.200 contas; 480 ÷ 1.200 = 40%. A média é 1.200 ÷ 4 = 300; o 3º trimestre ficou 40% abaixo do "
       "2º (180 ÷ 300 = 0,6); o 1º teve 240 ÷ 1.200 = 20%; e a maior alta foi do 3º para o 4º (+300).",
       style="caso_pratico", t="case"),
    PVS("Num gráfico de setores sobre o canal preferido de 1.200 clientes, o aplicativo ocupa 216°, o internet banking, "
        "90°, e a agência, o restante do círculo. Quantos clientes preferem a agência?",
        ["150", "180", "216", "300", "720"], "180",
        "A agência ocupa 360° − 216° − 90° = 54°, isto é, 54 ÷ 360 = 15% do círculo; 15% de 1.200 = 180. O 720 é o "
        "aplicativo (60%), o 300 é o internet banking (25%) e o 216 confunde graus com clientes."),
   ]),
  dict(title="Princípio multiplicativo e aditivo", subtopic="contagem",
   summary="Multiplicativo: se uma etapa tem m opções e a seguinte tem n, as duas juntas têm m × n (etapas que "
           "acontecem juntas, “e”). Aditivo: casos que não se misturam se somam (“ou”). Sem repetição, as opções "
           "diminuem a cada etapa: 10 × 9 × 8. Comece pela etapa que tem restrição (por exemplo, o 0 não pode iniciar "
           "um número).",
   items=[
    A1("Exemplo: com 3 camisas e 4 calças há 3 × 4 = 12 combinações de roupa. Agora: um cartão pode ter 5 cores de "
       "fundo e 3 desenhos. Quantos modelos diferentes de cartão existem?",
       "15", ["8", "125"],
       "Cada cor combina com cada desenho: 5 × 3 = 15. O 8 soma as opções, como se fosse escolher cor ou desenho; o "
       "125 é 5³."),
    TF("Uma senha de 3 dígitos distintos, escolhidos de 0 a 9, tem 10 × 10 × 10 = 1.000 possibilidades.", False,
       "Com dígitos distintos, as opções caem a cada posição: 10 × 9 × 8 = 720. O 1.000 vale quando os dígitos podem "
       "se repetir."),
    QK("Comece pela posição com restrição. Quantos números de 3 algarismos distintos podem ser formados com os "
       "algarismos 0, 1, 2, 3 e 4?",
       "48", ["60", "100"],
       "A centena não pode ser 0: 4 opções. Depois sobram 4 para a dezena (o 0 volta a valer) e 3 para a unidade: "
       "4 × 4 × 3 = 48. O 60 (5 × 4 × 3) deixa o número começar por 0."),
    QK("Casos que não se misturam se somam. Um cliente vai escolher um único investimento entre 4 de renda fixa ou 3 de "
       "renda variável. Quantas escolhas ele tem?",
       "7", ["12", "1"],
       "É um investimento de um grupo ou do outro: 4 + 3 = 7. Multiplicar (12) contaria pares de investimentos, que "
       "ele não vai escolher.",
       step=3, style="conceitual"),
    PVS("Um código de acesso tem 2 letras distintas, escolhidas entre A, B, C, D e E, seguidas de 2 algarismos de 0 a "
        "9, que podem se repetir. Quantos códigos diferentes podem ser formados?",
        ["1.800", "2.000", "2.500", "9.000", "10.000"], "2.000",
        "Letras: 5 × 4 = 20 (distintas). Algarismos: 10 × 10 = 100 (podem repetir). Total: 20 × 100 = 2.000. O 2.500 "
        "deixa as letras repetirem (5 × 5); o 1.800 proíbe repetir os algarismos (10 × 9)."),
    PVS("Quantos números pares de 3 algarismos distintos podem ser formados com os algarismos 1, 2, 3, 4 e 5?",
        ["12", "20", "24", "30", "60"], "24",
        "Comece pela unidade, que tem a restrição: 2 ou 4 (2 opções). Sobram 4 algarismos para a centena e 3 para a "
        "dezena: 2 × 4 × 3 = 24. O 60 conta todos os números de 3 algarismos distintos; o 12 só os terminados em 2.",
        style="literal", t="mcq5"),
   ]),
  dict(title="Permutações e combinações", subtopic="permutação e combinação",
   summary="Permutação: arrumar n itens distintos em fila dá n! (4! = 4 × 3 × 2 × 1 = 24). Com letras repetidas, divida "
           "pelo fatorial de cada repetição: ARARA = 5! ÷ (3! × 2!) = 10. Arranjo (a ordem importa): A(n, p) = n! ÷ "
           "(n − p)!. Combinação (a ordem não importa, é um grupo): C(n, p) = n! ÷ [p! × (n − p)!]. Pergunta-chave: "
           "trocar a ordem muda o resultado?",
   items=[
    A1("Exemplo: as letras de SOL formam 3! = 3 × 2 × 1 = 6 anagramas. Agora: quantos anagramas tem a palavra PRAZO, "
       "que tem 5 letras diferentes?",
       "120", ["25", "60"],
       "5! = 5 × 4 × 3 × 2 × 1 = 120. O 25 é 5², e o 60 só valeria se houvesse uma letra repetida duas vezes."),
    TF("Escolher 3 de 8 funcionários para formar uma comissão, sem cargos, é um caso de combinação, porque a ordem da "
       "escolha não muda a comissão.", True,
       "A comissão Ana, Beto e Caio é a mesma que Caio, Ana e Beto: a ordem não importa, então é C(8, 3) = 56."),
    QK("Combinação: C(n, p) = n! ÷ [p! × (n − p)!]. Entre 6 agências, a diretoria vai sortear 2 para uma auditoria. "
       "Quantos pares diferentes são possíveis?",
       "15", ["30", "12"],
       "C(6, 2) = 6 × 5 ÷ 2 = 15. O 30 conta cada par duas vezes (A e B, B e A), como num arranjo."),
    QK("Com letras repetidas, divida pelo fatorial de cada repetição. Quantos anagramas tem a palavra BANANA?",
       "60", ["720", "120"],
       "6 letras, com 3 A e 2 N: 6! ÷ (3! × 2!) = 720 ÷ 12 = 60. O 720 trata todas as letras como diferentes.",
       step=3),
    PVS("De um grupo de 7 analistas, serão escolhidos um coordenador e um vice-coordenador para um projeto. De quantas "
        "maneiras essa escolha pode ser feita?",
        ["7", "13", "14", "21", "42"], "42",
        "Os cargos são diferentes, então a ordem importa: 7 opções para coordenador e 6 para vice, 7 × 6 = 42. O 21 é "
        "a combinação C(7, 2), que esquece os cargos; o 13 soma 7 + 6 em vez de multiplicar; o 14 é 7 × 2; o 7 conta "
        "só a escolha do coordenador."),
    PVS("Uma equipe de 5 pessoas será formada com 2 gerentes, escolhidos entre 4, e 3 analistas, escolhidos entre 6. O "
        "número de equipes diferentes que podem ser formadas é",
        ["26", "120", "240", "252", "1.440"], "120",
        "Gerentes: C(4, 2) = 6. Analistas: C(6, 3) = 20. As duas escolhas acontecem juntas: 6 × 20 = 120. O 26 soma "
        "em vez de multiplicar; o 240 trata os gerentes como cargos ordenados; o 252 é C(10, 5), que ignora a "
        "exigência de 2 gerentes e 3 analistas."),
   ]),
  dict(title="Casa dos pombos e chance", subtopic="probabilidade",
   summary="Para ter certeza, pense no pior caso: com 3 cores de meia, tirando 4 meias, com certeza duas são da mesma "
           "cor (n gavetas e n + 1 objetos). Probabilidade = casos favoráveis ÷ casos possíveis, quando todos são "
           "igualmente prováveis: num dado, sair par é 3/6 = 1/2. Em retiradas seguidas sem reposição, multiplique as "
           "chances, atualizando o total a cada retirada.",
   items=[
    A1("Exemplo: numa urna com bolas de 2 cores, tirando 3 bolas no escuro, com certeza 2 têm a mesma cor (no pior "
       "caso, uma de cada cor e a 3ª repete). Agora: uma caixa tem cartões de 4 cores. Quantos cartões é preciso tirar, "
       "sem olhar, para ter certeza de 2 da mesma cor?",
       "5", ["2", "8"],
       "No pior caso, os 4 primeiros são um de cada cor; o 5º repete uma delas. Com 2 cartões, podem vir cores "
       "diferentes; 8 é mais do que o necessário."),
    TF("Ao lançar um dado honesto, a probabilidade de sair um número maior que 4 é 2/6, isto é, 1/3.", True,
       "Os casos favoráveis são 5 e 6, dois entre seis resultados igualmente prováveis: 2/6 = 1/3."),
    QK("Quando se pede uma cor específica, o pior caso tira antes todas as outras. Uma gaveta tem 5 cartões azuis, 7 "
       "verdes e 3 vermelhos. Quantos é preciso tirar, sem olhar, para ter certeza de pegar 2 vermelhos?",
       "14", ["2", "4"],
       "No pior caso saem antes os 5 azuis e os 7 verdes (12); depois, 2 vermelhos: 12 + 2 = 14. O 4 garante duas da "
       "mesma cor, não duas vermelhas."),
    QK("Sem reposição, o total diminui a cada retirada. Uma urna tem 3 bolas brancas e 2 pretas. Tirando duas, sem "
       "repor a primeira, qual é a probabilidade de as duas serem pretas?",
       "1/10", ["4/25", "1/5"],
       "1ª preta: 2/5; 2ª preta, com 4 bolas e 1 preta restante: 1/4. 2/5 × 1/4 = 2/20 = 1/10. O 4/25 repõe a "
       "primeira bola (2/5 × 2/5).",
       step=3),
    PVS("Uma urna tem 4 fichas premiadas e 6 não premiadas. Um cliente tira duas fichas, uma depois da outra, sem "
        "devolver a primeira. A probabilidade de apenas a segunda ficha ser premiada é",
        ["2/15", "6/25", "4/15", "2/5", "8/15"], "4/15",
        "1ª não premiada: 6/10; 2ª premiada, com 9 fichas e 4 premiadas: 4/9. 6/10 × 4/9 = 24/90 = 4/15. O 6/25 repõe "
        "a primeira ficha; o 8/15 é a chance de exatamente uma ser premiada, em qualquer ordem; o 2/5 olha só a "
        "segunda retirada."),
    PVS("Num sorteio de brindes, o cliente lança dois dados honestos e ganha se a soma dos pontos for 8. A "
        "probabilidade de ganhar é",
        ["5/36", "1/6", "2/9", "1/4", "5/18"], "5/36",
        "São 6 × 6 = 36 resultados igualmente prováveis. Somam 8: (2, 6), (3, 5), (4, 4), (5, 3) e (6, 2), 5 casos: "
        "5/36. O 1/6 é a chance da soma 7; o 5/18 conta cada caso duas vezes; o 2/9 (8/36) usa o próprio 8 como "
        "número de casos; o 1/4 (9/36) conta também (1, 7) e (7, 1), que não existem num dado."),
   ]),
 ]),
]

# distratores conferidos
assert comb(7, 2) == 21 and 7 + 6 == 13 and F(10, 36) == F(5, 18) and F(8, 36) == F(2, 9) and F(9, 36) == F(1, 4)
