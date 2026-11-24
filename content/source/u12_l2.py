# G2 Raciocínio lógico (u12), lição 2: Aritmética. Gerado para o app por tools/gen_u16.py u12.
# Em cada exercício a alternativa certa vem PRIMEIRO; o gerador espalha a posição. Nas de 5 alternativas numéricas, PVS
# põe as opções em ordem crescente e fixa a posição da certa. Cada conta tem um assert logo abaixo: se um resultado
# estiver errado, a geração para. Conteúdo de IA (não revisado), conferido contra a apostila do cargo (OP-121NV-26),
# usada só como referência de fatos e métodos; textos, cenários e números são próprios.
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


# --- contas conferidas ---------------------------------------------------------------------------------------------
assert 20 - 12 / 4 == 17
assert 2 * 3 ** 2 == 18
assert -2 ** 4 + (-2) ** 4 == 0
assert 5 ** 0 + 2 ** 3 * 10 == 81
assert 18 - 2 * (7 - 4) ** 2 / 6 + 5 == 20
assert (150 - 4 * 18 + 2 * 25) / 4 == 32
assert F(2, 5) * 1500 == 600
assert 1200 - 1200 // 4 - (1200 - 1200 // 4) // 3 == 600 and 1200 * (1 - F(1, 4) - F(1, 3)) == 500
assert F(5, 2) / F(1, 4) == 10
assert 1800 * F(2, 3) * F(3, 4) == 900
assert (F(2, 3) + F(1, 6)) / F(5, 4) == F(2, 3)
assert 18 % 6 == 0 and 18 % 9 == 0 and all(n % 6 or n % 9 for n in range(1, 18))
assert 24 % 12 == 0 and 36 % 12 == 0 and 24 // 12 + 36 // 12 == 5
assert max(d for d in range(1, 49) if 48 % d == 0 and 60 % d == 0) == 12
assert min(n for n in range(1, 2000) if n % 8 == 0 and n % 12 == 0 and n % 15 == 0) == 120
assert max(d for d in range(1, 37) if 36 % d == 0 and 60 % d == 0) == 12
assert min(n for n in range(1, 2200) if n % 36 == 0 and n % 60 == 0) == 180
assert 3 * 15 + 4 == 49
assert (80 + 20) / 2 == 50
assert (130 + 24) / 2 == 77
assert 30 + 6 == 3 * (6 + 6)
assert 54 + 38 == 92 and 54 - 8 == 38 + 8
assert 14 + 26 == 40 and 20 * 14 + 50 * 26 == 1580
assert (9 * 60 + 48 + 35) == 10 * 60 + 23
assert 2.4 * 1000 == 2400
assert 14 * 18 == 252 and (22 * 60 + 40 + 252) % (24 * 60) == 2 * 60 + 52
assert 250 * 180 / 10000 == 4.5
assert 30 % 7 == 2
assert 25 % 5 == 0
assert 15 + 19 * 3 == 72
assert sum(range(1, 41)) == 820
assert 500 % 6 == 2
assert sum(10 + 5 * i for i in range(20)) == 1150


LESSONS = [
 dict(order=2, title="Aritmética", concepts=[
  dict(title="Operações e expressões", subtopic="expressões",
   summary="Ordem de cálculo: parênteses; depois potências; depois multiplicação e divisão, da esquerda para a "
           "direita; por fim soma e subtração. Sinais: (−) × (−) = (+). Sem parênteses, o sinal fica fora da potência: "
           "−3² = −9, mas (−3)² = 9. Todo número diferente de zero elevado a 0 vale 1 (5⁰ = 1).",
   items=[
    A1("Exemplo: em 8 + 2 × 5, a multiplicação vem antes da soma: 8 + 10 = 18. Agora: no controle de senhas, quanto "
       "vale 20 − 12 ÷ 4?",
       "17", ["2", "3"],
       "A divisão vem antes: 12 ÷ 4 = 3, e 20 − 3 = 17. O 2 sai de fazer a subtração primeiro, (20 − 12) ÷ 4, e o 3 é "
       "só o resultado da divisão."),
    TF("A potência vem antes da multiplicação: em 2 × 3², calcula-se primeiro 3² = 9. Logo, 2 × 3² = 36.", False,
       "2 × 9 = 18. O 36 seria (2 × 3)², que eleva o produto inteiro; sem parênteses, só o 3 é elevado ao quadrado."),
    QK("Sem parênteses, o sinal fica fora da potência: −3² = −9, e (−3)² = 9. Quanto vale −2⁴ + (−2)⁴?",
       "0", ["32", "−32"],
       "−2⁴ = −16 e (−2)⁴ = +16 (expoente par com base negativa dá positivo). A soma é zero. 32 e −32 aparecem quando "
       "os dois termos recebem o mesmo sinal."),
    QK("Todo número diferente de zero elevado a 0 vale 1, e a potência vem antes da multiplicação. Quanto vale "
       "5⁰ + 2³ × 10?",
       "81", ["90", "80"],
       "5⁰ = 1 e 2³ × 10 = 8 × 10 = 80; total 81. O 90 sai de somar antes de multiplicar, (1 + 8) × 10, e o 80, de "
       "tratar 5⁰ como zero.",
       step=3, style="literal"),
    PVS("O valor da expressão 18 − 2 × (7 − 4)² ÷ 6 + 5 é",
        ["5", "10", "20", "22", "29"], "20",
        "Parênteses: 7 − 4 = 3. Potência: 3² = 9. Multiplicação e divisão, da esquerda para a direita: 2 × 9 = 18 e "
        "18 ÷ 6 = 3. Por fim, 18 − 3 + 5 = 20. O 29 sai de fazer tudo da esquerda para a direita; o 22, de esquecer o "
        "quadrado; o 5, de dividir (18 − 18) por 6; o 10, de pôr o 5 dentro do que é subtraído.",
        style="literal", t="mcq5"),
    PVS("Uma agência tinha 150 senhas impressas. Pela manhã, distribuiu 4 lotes de 18 senhas e, à tarde, recebeu da "
        "gráfica 2 lotes de 25 senhas. As senhas que sobraram foram repartidas igualmente entre 4 guichês. Cada guichê "
        "recebeu",
        ["7", "18", "25", "28", "32"], "32",
        "Sobraram 150 − 4 × 18 + 2 × 25 = 150 − 72 + 50 = 128 senhas, e 128 ÷ 4 = 32. O 7 subtrai os lotes recebidos "
        "em vez de somar; o 28 faz o mesmo e esquece de dividir; o 25 ignora a manhã e ainda subtrai as recebidas, "
        "(150 − 50) ÷ 4; o 18 divide só as senhas distribuídas."),
   ]),
  dict(title="Frações e decimais", subtopic="frações",
   summary="Fração de um total: 2/5 de 300 = 300 ÷ 5 × 2 = 120. Para somar ou subtrair, iguale os denominadores "
           "(1/2 + 1/3 = 3/6 + 2/6 = 5/6); para multiplicar, numerador com numerador e denominador com denominador; para "
           "dividir, multiplique pelo inverso (6 ÷ 3/4 = 6 × 4/3 = 8). “Do restante” muda a base: depois de gastar 1/4, "
           "um terço do restante é 1/3 de 3/4, ou seja, 1/4 do total.",
   items=[
    A1("Exemplo: 3/4 de R$ 200 = 200 ÷ 4 × 3 = R$ 150. Agora: um cliente separou 2/5 de um bônus de R$ 1.500 para a "
       "poupança. Quanto foi para a poupança?",
       "R$ 600", ["R$ 300", "R$ 900"],
       "1.500 ÷ 5 = 300, e 300 × 2 = 600. R$ 300 é só um quinto, e R$ 900 são os 3/5 que sobraram."),
    TF("Para somar frações, somam-se os numeradores e os denominadores: 1/2 + 1/4 = 2/6.", False,
       "É preciso igualar os denominadores: 1/2 = 2/4, e 2/4 + 1/4 = 3/4. Repare que 2/6 = 1/3 é menor que 1/2, o que "
       "já mostra o erro."),
    QK("“Do restante” muda a base do cálculo. Um caixa tinha R$ 1.200; pagou 1/4 desse valor e, depois, 1/3 do que "
       "sobrou. Quanto restou no caixa?",
       "R$ 600", ["R$ 500", "R$ 700"],
       "Pagou 300 e ficou com 900; um terço de 900 é 300, e restam 600. O R$ 500 sai de tirar 1/4 e 1/3 do valor "
       "inicial, como se os dois pagamentos tivessem a mesma base."),
    QK("Dividir por uma fração é multiplicar pelo inverso: 6 ÷ 3/4 = 6 × 4/3 = 8. Quantos copos de 1/4 de litro enchem "
       "uma garrafa de 2,5 litros?",
       "10", ["5", "8"],
       "2,5 ÷ 1/4 = 2,5 × 4 = 10 copos. Multiplicar 2,5 por 1/4 daria uma fração de litro, não um número de copos.",
       step=3),
    PVS("Um cliente sacou 1/3 do saldo da conta e, em seguida, 1/4 do que restou, ficando com R$ 900,00. O saldo "
        "inicial era de",
        ["R$ 1.800,00", "R$ 2.160,00", "R$ 2.700,00", "R$ 3.600,00", "R$ 5.400,00"], "R$ 1.800,00",
        "Depois do 1º saque sobram 2/3 do saldo; do 2º, 3/4 disso: 2/3 × 3/4 = 1/2 do saldo inicial. Se metade é 900, "
        "o saldo era 1.800. O 2.160 sai de somar 1/3 + 1/4 sobre o saldo inicial (sobrariam 5/12); o 2.700 trata 900 "
        "como 1/3 do saldo; o 3.600, como 1/4; o 5.400, como 1/3 de 1/2."),
    PV("O valor de (2/3 + 1/6) ÷ 5/4 é",
       "2/3", ["25/24", "1/2", "3/2", "5/6"],
       "2/3 = 4/6, e 4/6 + 1/6 = 5/6. Dividir por 5/4 é multiplicar por 4/5: 5/6 × 4/5 = 20/30 = 2/3. O 25/24 sai de "
       "multiplicar por 5/4 em vez de dividir; 3/2 é o inverso da resposta; 5/6 esquece a divisão.",
       style="literal"),
   ]),
  dict(title="Múltiplos e divisores", subtopic="mmc e mdc",
   summary="Múltiplos de 6: 6, 12, 18… Divisores de 12: 1, 2, 3, 4, 6 e 12. MMC (menor múltiplo comum) responde "
           "“quando os eventos coincidem de novo”; MDC (maior divisor comum), “dividir em partes iguais, as maiores "
           "possíveis”. Fatorando 12 = 2² × 3 e 18 = 2 × 3²: MDC = 2 × 3 = 6 (menores expoentes) e MMC = 2² × 3² = 36 "
           "(maiores). Paridade: par + par e ímpar + ímpar dão par; par + ímpar dá ímpar.",
   items=[
    A1("Exemplo: um alarme toca a cada 4 dias e outro a cada 6; voltam a tocar juntos no MMC(4, 6) = 12 dias. Agora: o "
       "malote da agência A sai a cada 6 dias e o da agência B, a cada 9. Saíram juntos hoje. Daqui a quantos dias "
       "saem juntos de novo?",
       "18", ["54", "3"],
       "Os múltiplos de 9 são 9, 18, 27…; o primeiro que também é múltiplo de 6 é 18. O 54 é o produto 6 × 9, que "
       "também é múltiplo comum, mas não o menor; o 3 é o MDC."),
    TF("Para cortar duas fitas de 24 m e de 36 m em pedaços iguais, do maior tamanho possível e sem sobra, usa-se o MDC: "
       "pedaços de 12 m.", True,
       "12 é o maior número que divide 24 e 36 ao mesmo tempo; saem 2 + 3 = 5 pedaços. O MMC (72) seria maior do que "
       "as próprias fitas."),
    QK("O MDC resolve “grupos iguais, os maiores possíveis”. Uma agência tem 48 cartões azuis e 60 verdes e quer "
       "embalá-los em pacotes com a mesma quantidade, sem misturar cores, sem sobras e com o maior pacote possível. "
       "Quantos cartões vão em cada pacote?",
       "12", ["6", "240"],
       "48 = 2⁴ × 3 e 60 = 2² × 3 × 5: MDC = 2² × 3 = 12 (4 pacotes azuis e 5 verdes). Pacotes de 6 também funcionam, "
       "mas não são os maiores; 240 é o MMC."),
    QK("Paridade: ímpar + ímpar dá par, e par + ímpar dá ímpar. A soma de três números ímpares é",
       "sempre ímpar", ["sempre par", "par ou ímpar, conforme os números"],
       "Os dois primeiros ímpares somam um par; par + o terceiro ímpar dá ímpar. Exemplo: 1 + 3 + 5 = 9. Não depende "
       "de quais ímpares são.",
       step=3, style="conceitual"),
    PVS("Três caixas eletrônicos passam por manutenção a cada 8, 12 e 15 dias, respectivamente. A manutenção dos três "
        "coincidiu numa segunda-feira. A próxima coincidência dos três ocorrerá depois de",
        ["60 dias", "120 dias", "180 dias", "240 dias", "1.440 dias"], "120 dias",
        "8 = 2³, 12 = 2² × 3 e 15 = 3 × 5. O MMC pega os maiores expoentes: 2³ × 3 × 5 = 120. O 60 não é múltiplo de "
        "8; 240 é múltiplo comum, mas não o menor; 1.440 é o produto 8 × 12 × 15."),
    PV("Sobre os números 36 e 60, NÃO é correto afirmar que",
       "o MMC entre eles é 2.160, o produto dos dois números.",
       ["o MDC entre eles é 12.", "o MMC entre eles é 180.", "60 é múltiplo de 12.",
        "a fatoração de 36 é 2² × 3²."],
       "36 = 2² × 3² e 60 = 2² × 3 × 5. MDC: menores expoentes dos fatores comuns, 2² × 3 = 12. MMC: todos os "
       "fatores com os maiores expoentes, 2² × 3² × 5 = 180. O produto 36 × 60 = 2.160 é múltiplo comum, mas não o "
       "menor; só coincide com o MMC quando o MDC é 1.",
       style="excecao"),
   ]),
  dict(title="Problemas com equações", subtopic="equações",
   summary="Traduza o texto em conta: chame o valor desconhecido de x e escreva cada frase como equação. “A tem 5 a "
           "mais que B”: A = B + 5. Soma e diferença conhecidas: o maior é (soma + diferença) ÷ 2. Em idades, daqui a n "
           "anos todo mundo tem n anos a mais. No fim, confira a resposta voltando ao enunciado.",
   items=[
    A1("Exemplo: “o dobro de um número mais 6 dá 20” vira 2x + 6 = 20, então 2x = 14 e x = 7. Agora: “o triplo das "
       "senhas chamadas, mais 4, dá 49”. Quantas senhas foram chamadas?",
       "15", ["45", "53"],
       "3x + 4 = 49, então 3x = 45 e x = 15. O 45 é o triplo, antes de dividir por 3; o 53 soma 4 em vez de "
       "subtrair."),
    TF("Se A + B = 80 e A − B = 20, então A = 50 e B = 30.", True,
       "A = (80 + 20) ÷ 2 = 50 e B = 80 − 50 = 30. Conferindo a diferença: 50 − 30 = 20."),
    QK("Com soma e diferença, o maior é (soma + diferença) ÷ 2. Dois caixas atenderam juntos 130 clientes, e um deles "
       "atendeu 24 a mais que o outro. Quantos clientes atendeu o que atendeu mais?",
       "77", ["65", "53"],
       "(130 + 24) ÷ 2 = 77, e o outro atendeu 53 (77 − 53 = 24). O 65 é só a metade do total, e o 53 é o que "
       "atendeu menos."),
    QK("Daqui a n anos, cada pessoa terá n anos a mais. Hoje, Ana tem 30 anos e o filho, 6. Daqui a quantos anos Ana "
       "terá o triplo da idade do filho?",
       "6", ["4", "8"],
       "30 + n = 3 × (6 + n), então 30 + n = 18 + 3n, 12 = 2n e n = 6. Conferindo: Ana terá 36, e o filho, 12.",
       step=3),
    PVS("Uma agência tem 92 funcionários entre caixas e atendentes. Se 8 caixas fossem transferidos para o "
        "atendimento, os dois grupos ficariam com o mesmo número de pessoas. O número de caixas é",
        ["38", "46", "50", "54", "62"], "54",
        "Depois da troca, cada grupo teria 92 ÷ 2 = 46 pessoas; antes dela, havia 46 + 8 = 54 caixas e 46 − 8 = 38 "
        "atendentes. O 46 esquece a transferência; o 62 soma 8 duas vezes; o 38 é o número de atendentes."),
    PVS("Um cofre tem 40 cédulas, apenas de R$ 20,00 e de R$ 50,00, num total de R$ 1.580,00. Quantas cédulas de "
        "R$ 50,00 há no cofre?",
        ["14", "22", "26", "28", "30"], "26",
        "Se as 40 fossem de R$ 20, seriam R$ 800. Faltam 780, e cada troca de uma cédula de 20 por uma de 50 acrescenta "
        "30: 780 ÷ 30 = 26 cédulas de R$ 50 (e 14 de R$ 20). Conferindo: 26 × 50 + 14 × 20 = 1.300 + 280 = 1.580. O "
        "14 é a quantidade de cédulas de R$ 20."),
   ]),
  dict(title="Unidades de medida e tempo", subtopic="unidades",
   summary="Comprimento: 1 km = 1.000 m e 1 m = 100 cm. Área tem duas dimensões: 1 m² = 10.000 cm², e 1 hectare = "
           "10.000 m². Volume: 1 m³ = 1.000 litros e 1 litro = 1.000 cm³. Tempo é de base 60: 1 h = 60 min e 1 min = "
           "60 s. Para somar horários, some segundos, minutos e horas e “vá um” a cada 60.",
   items=[
    A1("Exemplo: 1h50min + 25min = 1h75min = 2h15min, porque 60 minutos viram 1 hora. Agora: um atendimento começou às "
       "9h48min e durou 35 minutos. A que horas terminou?",
       "10h23min", ["10h13min", "10h33min"],
       "48 + 35 = 83 minutos = 1h23min, então 9h + 1h23min = 10h23min. Os outros horários erram o “vai um” de 60 "
       "minutos."),
    TF("Como 1 m = 100 cm, então 1 m² = 100 cm².", False,
       "Área tem duas dimensões: 1 m² = 100 cm × 100 cm = 10.000 cm². No volume, são três: 1 m³ = 1.000.000 cm³."),
    QK("1 m³ = 1.000 litros. Um reservatório de 2,4 m³ guarda quantos litros?",
       "2.400", ["240", "24.000"],
       "2,4 × 1.000 = 2.400 litros. 240 e 24.000 deslocam a vírgula uma casa a menos ou a mais."),
    QK("Tempo é de base 60, não de base 10. Quanto é 2,5 horas?",
       "2h30min", ["2h50min", "2h05min"],
       "0,5 hora é metade de 60 minutos: 30 min. O 2h50min lê a parte decimal como minutos, como se a hora tivesse "
       "100 minutos.",
       step=3, style="literal"),
    PVS("Um processamento noturno tem 14 lotes de 18 minutos cada, rodados um depois do outro, a partir das 22h40min. "
        "O processamento termina às",
        ["1h32min", "2h12min", "2h52min", "3h12min", "4h12min"], "2h52min",
        "14 × 18 = 252 min = 4h12min. 22h40min + 4h12min = 26h52min, isto é, 2h52min do dia seguinte. O 1h32min lê "
        "252 minutos como 2h52min; o 4h12min é a duração, não o horário de término."),
    PVS("Um terreno retangular de 250 m por 180 m será vendido por hectare (1 hectare = 10.000 m²). A área do terreno, "
        "em hectares, é",
        ["0,45", "4,5", "45", "450", "4.500"], "4,5",
        "250 × 180 = 45.000 m², e 45.000 ÷ 10.000 = 4,5 hectares. As outras alternativas deslocam a vírgula, como se o "
        "hectare tivesse 1.000 ou 100.000 m².",
        style="literal", t="mcq5"),
   ]),
  dict(title="Restos, ciclos e sequências", subtopic="sequências",
   summary="Em padrões que se repetem, divida a posição pelo tamanho do ciclo e olhe o resto: num ciclo de 7 dias, o "
           "dia 100 dá 100 ÷ 7 = 14, resto 2, e cai no 2º dia do ciclo. Resto 0 é o último elemento do ciclo. Numa "
           "progressão aritmética (sempre o mesmo acréscimo r), o termo n é a₁ + (n − 1) × r, e a soma dos n primeiros "
           "é (a₁ + aₙ) × n ÷ 2.",
   items=[
    A1("Exemplo: hoje é segunda; daqui a 10 dias, 10 ÷ 7 dá resto 3, e três dias depois de segunda é quinta. Agora: "
       "hoje é quarta-feira. Que dia da semana será daqui a 30 dias?",
       "sexta-feira", ["quinta-feira", "sábado"],
       "30 ÷ 7 = 4 semanas inteiras, resto 2. Dois dias depois de quarta é sexta. As semanas inteiras não mudam o dia."),
    TF("Num ciclo de 5 cores (azul, verde, amarelo, vermelho, branco) repetido sem parar, a 25ª posição é branco, "
       "porque 25 ÷ 5 dá resto 0, e resto 0 corresponde à última cor do ciclo.", True,
       "As posições 5, 10, 15, 20 e 25 fecham ciclos e são sempre a 5ª cor, branco."),
    QK("Termo geral da progressão aritmética: aₙ = a₁ + (n − 1) × r. Uma fila começou na senha 15 e anda de 3 em 3 "
       "(15, 18, 21…). Qual é a 20ª senha chamada?",
       "72", ["75", "60"],
       "15 + 19 × 3 = 72: do 1º ao 20º termo há 19 saltos. O 75 conta 20 saltos; o 60 é só 20 × 3."),
    QK("Soma da progressão aritmética: (primeiro + último) × quantidade ÷ 2. Quanto é 1 + 2 + 3 + … + 40?",
       "820", ["800", "1.640"],
       "(1 + 40) × 40 ÷ 2 = 41 × 20 = 820. O 1.640 esquece de dividir por 2; o 800 usa 40 no lugar de 41.",
       step=3, style="literal"),
    PV("Os códigos de um lote seguem o padrão A, B, C, D, E, F, A, B, C… repetido sem parar. A letra do código de "
       "número 500 é",
       "B", ["A", "C", "D", "E"],
       "O ciclo tem 6 letras: 500 ÷ 6 = 83, resto 2. O código 500 é a 2ª letra do ciclo, B. Resto 0 seria F, que nem "
       "está entre as alternativas.",
       style="caso_pratico", t="case", pos=1),
    PVS("Um cliente depositou R$ 10 no 1º dia do mês, R$ 15 no 2º, R$ 20 no 3º e assim por diante, sempre R$ 5 a mais "
        "que no dia anterior. Ao fim do 20º dia, o total depositado foi de",
        ["R$ 575", "R$ 1.045", "R$ 1.050", "R$ 1.100", "R$ 1.150"], "R$ 1.150",
        "O 20º depósito é 10 + 19 × 5 = 105. A soma é (10 + 105) × 20 ÷ 2 = 1.150. O 1.100 usa 100 como último termo "
        "(18 saltos em vez de 19); o 1.045 soma só 19 dias; o 1.050 multiplica o último depósito por 10, a metade dos "
        "dias; o 575 divide por 2 duas vezes."),
   ]),
 ]),
]

# distratores conferidos
assert (150 - 72 - 50) / 4 == 7 and 150 - 72 - 50 == 28 and (150 - 50) / 4 == 25 and 72 / 4 == 18
assert 900 * 3 == 2700 and 900 * 4 == 3600 and 900 * 6 == 5400 and 900 * F(12, 5) == 2160
assert (10 + 100) * 20 / 2 == 1100 and (10 + 100) * 19 / 2 == 1045 and 105 * 10 == 1050 and 1150 / 2 == 575
