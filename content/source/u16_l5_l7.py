# Unidade 1 do Técnico Bancário (u16), lições 5 a 7. Gerado para o app por tools/gen_u16.py.
# Em cada exercício a alternativa certa vem PRIMEIRO; o gerador espalha a posição. Conteúdo de IA (não revisado).

def A1(stem, c, w, exp, diff=1, style="caso_pratico"):
    return dict(type="quick", level="aprender", step=1, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def TF(stem, true, exp, diff=1, style="conceitual"):
    return dict(type="truefalse", level="aprender", step=2, stem=stem, true=true, exp=exp, diff=diff, style=style)

def QK(stem, c, w, exp, step=2, diff=2, style="caso_pratico"):
    return dict(type="quick", level="praticar", step=step, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def MT(stem, pairs, exp, step=2, diff=2):
    return dict(type="match", level="praticar", step=step, stem=stem, pairs=pairs, exp=exp, diff=diff, style="conceitual")

def OR(stem, lines, exp, step=3, diff=2):
    return dict(type="order_code", level="praticar", step=step, stem=stem, lines=lines, exp=exp, diff=diff, style="conceitual")

def PV(stem, c, w, exp, diff=2, style="conceitual", t="mcq5"):
    return dict(type=t, level="prova", stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)


LESSONS = [
 dict(order=5, title="Lavagem de dinheiro e COAF", concepts=[
  dict(title="Etapas da lavagem", subtopic="colocação, ocultação e integração",
   summary="Lavagem de dinheiro é dar aparência legal a recursos de origem ilícita. Costuma seguir três etapas: "
           "colocação (o dinheiro entra no sistema, por exemplo em depósitos fracionados em espécie), ocultação ou "
           "estratificação (muitas transações para apagar o rastro) e integração (o dinheiro volta à economia formal, em "
           "imóveis, empresas ou investimentos, já com aparência legal).",
   items=[
    A1("Exemplo: depositar R$ 9 mil por dia em contas diferentes para não chamar atenção é colocação. Comprar um imóvel "
       "com o dinheiro já com aparência legal corresponde a qual etapa?",
       "integração", ["colocação", "ocultação"],
       "Na integração o dinheiro volta à economia formal, em bens e negócios, como se tivesse origem legítima."),
    TF("Na etapa de ocultação (estratificação), o objetivo é dificultar o rastreamento do dinheiro por meio de muitas "
       "transações.", True,
       "Transferências em cadeia, entre contas, empresas e países, servem para afastar o dinheiro da sua origem."),
    MT("Associe cada ação à etapa da lavagem:",
       [("Depósitos fracionados em espécie", "Colocação"), ("Transferências em cadeia entre países", "Ocultação"),
        ("Compra de uma empresa com o dinheiro", "Integração")],
       "Entrar no sistema, apagar o rastro e voltar à economia: as três etapas, nessa ordem."),
    OR("Ordene as etapas clássicas da lavagem de dinheiro:",
       ["Colocação: o dinheiro ilícito entra no sistema", "Ocultação: muitas operações apagam o rastro",
        "Integração: o dinheiro volta à economia com aparência legal"],
       "Primeiro o dinheiro entra, depois é escondido em várias operações e, por fim, reaparece como se fosse legal."),
    PV("Um cliente faz vários depósitos em espécie, de valores um pouco abaixo do que chamaria atenção, em agências "
       "diferentes no mesmo dia. Essa conduta é típica da etapa de",
       "colocação.", ["integração.", "ocultação.", "compensação.", "custódia."],
       "Fracionar depósitos em espécie para introduzir o dinheiro no sistema financeiro é a colocação.",
       style="caso_pratico"),
   ]),
  dict(title="Lei 9.613/1998", subtopic="crime de lavagem",
   summary="A Lei 9.613/1998 define o crime de lavagem: ocultar ou dissimular a natureza, origem, localização, "
           "disposição, movimentação ou propriedade de bens, direitos ou valores vindos, direta ou indiretamente, de "
           "infração penal. Desde a Lei 12.683/2012 vale qualquer infração penal antecedente (antes havia uma lista "
           "fechada de crimes). A mesma lei criou o Coaf e listou as pessoas obrigadas a prevenir a lavagem, como bancos "
           "e corretoras.",
   items=[
    A1("Exemplo: quem esconde a origem de dinheiro desviado de um contrato público pratica lavagem, porque o dinheiro "
       "veio de infração penal. Esconder a origem de dinheiro de qualquer infração penal configura lavagem?",
       "sim, qualquer infração antecedente serve", ["não, só tráfico e terrorismo", "não, só crimes financeiros"],
       "Desde a Lei 12.683/2012 não há mais lista fechada: qualquer infração penal pode ser antecedente da lavagem."),
    TF("Para a Lei 9.613/1998, há lavagem mesmo quando o bem vem indiretamente da infração penal.", True,
       "A lei fala em bens provenientes, direta ou indiretamente, de infração penal."),
    QK("Antes de 2012, a Lei 9.613/1998 trazia uma lista fechada de crimes antecedentes. O que mudou com a Lei "
       "12.683/2012?",
       "qualquer infração penal passou a contar", ["a lavagem deixou de ser crime", "só o tráfico passou a contar"],
       "A lista de crimes antecedentes foi eliminada: agora basta que o dinheiro venha de qualquer infração penal."),
    QK("João recebe propina, compra gado em nome de terceiros e o revende logo depois para justificar o dinheiro. "
       "Segundo a Lei 9.613/1998, isso é:",
       "lavagem, pois dissimula a origem", ["apenas sonegação fiscal", "conduta lícita, houve venda real"],
       "A compra e venda serve para dar aparência legal ao dinheiro da propina: é dissimular a origem.", step=3),
    PV("De acordo com a Lei nº 9.613/1998, o crime de lavagem de dinheiro consiste em",
       "ocultar ou dissimular a origem de bens vindos de infração penal.",
       ["deixar de declarar ao fisco rendimentos de origem lícita recebidos no ano.",
        "movimentar em espécie, num único dia, valor acima de R$ 10 mil em agência.",
        "manter contas em nome próprio em mais de uma instituição financeira.",
        "fazer operações de câmbio por meio de instituição autorizada."],
       "O núcleo do crime é ocultar ou dissimular bens vindos de infração penal. As outras condutas, em si, não "
       "configuram lavagem.", style="literal"),
   ]),
  dict(title="Deveres das instituições", subtopic="conhecer, registrar e comunicar",
   summary="Bancos e demais pessoas obrigadas devem identificar e conhecer os clientes (cadastro atualizado), registrar "
           "as operações, monitorar as que fogem do perfil e comunicar ao Coaf as operações suspeitas, sem avisar o "
           "cliente. Os controles são proporcionais ao porte e ao risco da instituição (abordagem baseada em risco).",
   items=[
    A1("Exemplo: o banco nota depósitos incompatíveis com a renda de um cliente e comunica ao Coaf sem avisá-lo. Depois "
       "de comunicar uma operação suspeita, o banco deve:",
       "não avisar o cliente", ["avisar o cliente no mesmo dia", "bloquear todas as contas dele"],
       "A comunicação ao Coaf é sigilosa: dar ciência ao cliente atrapalharia a apuração."),
    TF("Manter o cadastro do cliente atualizado faz parte das obrigações de prevenção à lavagem de dinheiro.", True,
       "Sem conhecer o cliente (renda, atividade, origem dos recursos), não dá para saber o que foge do perfil."),
    MT("Associe cada dever à sua prática:",
       [("Conhecer o cliente", "Cadastro com renda e atividade"), ("Registrar", "Guardar os dados das operações"),
        ("Monitorar", "Alerta de movimentação fora do perfil"), ("Comunicar", "Envio ao Coaf")],
       "Os quatro deveres se completam: sem cadastro e registros, não há monitoramento nem comunicação."),
    QK("Um cliente pergunta ao gerente se o banco comunicou ao Coaf um depósito que ele fez. O gerente deve:",
       "não confirmar nem negar", ["confirmar, por transparência", "mostrar a comunicação enviada"],
       "As comunicações ao Coaf são sigilosas e não podem ser reveladas ao cliente.", step=3),
   ]),
  dict(title="Coaf e sanções", subtopic="Coaf",
   summary="O Coaf (Conselho de Controle de Atividades Financeiras), vinculado ao Banco Central como Unidade de "
           "Inteligência Financeira (UIF), recebe as comunicações, analisa e produz relatórios de inteligência para as "
           "autoridades. Quem descumpre os deveres da lei sofre sanções administrativas: advertência, multa, inabilitação "
           "de administradores e cassação ou suspensão da autorização para funcionar.",
   items=[
    A1("Exemplo: o banco envia a comunicação de operação suspeita; o Coaf analisa e, se for o caso, repassa às "
       "autoridades. Quem recebe as comunicações de operações suspeitas feitas pelos bancos?",
       "o Coaf", ["a Receita Federal, diretamente", "a Febraban"],
       "O Coaf é a unidade de inteligência financeira que centraliza as comunicações das instituições."),
    TF("O Coaf julga criminalmente os suspeitos de lavagem de dinheiro.", False,
       "O Coaf produz inteligência financeira. A investigação é da polícia e do Ministério Público, e o julgamento, do "
       "Poder Judiciário."),
    MT("Associe cada órgão ao seu papel no combate à lavagem:",
       [("Coaf", "Inteligência financeira"), ("Polícia e Ministério Público", "Investigação e acusação"),
        ("Poder Judiciário", "Julgamento do crime"), ("Banco Central", "Supervisão das instituições")],
       "Cada órgão age numa etapa: o Coaf analisa, a polícia e o MP investigam, o Judiciário julga e o BC fiscaliza os "
       "bancos."),
    QK("Um banco deixou de comunicar operações suspeitas por meses. Qual destas é sanção administrativa prevista?",
       "multa e inabilitação de administradores", ["prisão automática do gerente", "cancelamento das contas dos clientes"],
       "As sanções administrativas são advertência, multa, inabilitação de administradores e cassação ou suspensão da "
       "autorização.", step=3),
    PV("O Conselho de Controle de Atividades Financeiras (Coaf)",
       "é a unidade de inteligência financeira que analisa operações suspeitas.",
       ["julga os crimes de lavagem de dinheiro cometidos no território nacional.",
        "fiscaliza as seguradoras e as entidades de previdência aberta.",
        "define a meta de inflação junto com o Conselho Monetário Nacional.",
        "autoriza o funcionamento dos bancos comerciais e múltiplos."],
       "O Coaf é a UIF brasileira: recebe, analisa e repassa informações. Julgar é do Judiciário; seguros são da "
       "Susep; meta de inflação, do CMN; autorizar bancos, do Banco Central.", style="literal"),
   ]),
  dict(title="Sinais de alerta e PEP", subtopic="sinais de alerta e PEP",
   summary="Sinais de alerta: movimentação incompatível com a renda, muitos depósitos fracionados em espécie, entradas de "
           "várias origens seguidas de saques em dinheiro, preços de exportação ou importação muito fora do mercado, "
           "recusa em informar dados. Pessoa exposta politicamente (PEP) é quem ocupa ou ocupou, nos últimos cinco anos, "
           "cargo ou função pública relevante, além de familiares e colaboradores próximos: exige diligência reforçada. "
           "Dirigente de empresa privada não é PEP só por isso.",
   items=[
    A1("Exemplo: um deputado federal e o cônjuge dele são PEP, e o banco acompanha suas contas com mais cuidado. Quem "
       "destes é pessoa exposta politicamente?",
       "um ministro de Estado", ["o dono de uma rede de lojas", "um gerente de banco privado"],
       "PEP é quem exerce cargo ou função pública relevante. Empresários e funcionários privados não são PEP por isso."),
    TF("Quem deixou um cargo público relevante continua sendo tratado como PEP por cinco anos.", True,
       "A condição de PEP vale durante o exercício e nos cinco anos seguintes à saída do cargo."),
    QK("A conta de uma pessoa com renda de R$ 3 mil recebe dezenas de depósitos em espécie de R$ 4.900 num mês. O "
       "técnico deve:",
       "registrar para análise e comunicação", ["ignorar, depósito é sempre legal", "avisar o cliente da suspeita"],
       "Movimentação muito acima da renda, fracionada em espécie, é sinal de alerta: deve seguir para análise interna e "
       "possível comunicação ao Coaf, sem avisar o cliente."),
    QK("Qual destas situações é sinal de alerta para lavagem de dinheiro?",
       "empresa nova faturando muito acima do setor", ["cliente que paga faturas em dia", "salário depositado pelo empregador"],
       "Faturamento incompatível com a estrutura e o histórico da empresa pode indicar dinheiro ilícito misturado ao "
       "legal.", step=3),
    PV("Para fins de prevenção à lavagem de dinheiro, deve ser tratado como pessoa exposta politicamente (PEP) o "
       "cliente que",
       "exerceu, até dois anos atrás, o mandato de deputado federal.",
       ["é diretor-presidente de uma grande empresa privada de capital aberto.",
        "é gerente regional de um banco privado com atuação em vários estados.",
        "tem renda mensal acima de R$ 100 mil.", "é cliente da instituição há mais de dez anos."],
       "Ex-deputado federal continua PEP por cinco anos depois de deixar o mandato. Cargo em empresa privada, renda "
       "alta ou tempo de relacionamento não tornam ninguém PEP.", style="caso_pratico"),
   ]),
 ]),

 dict(order=6, title="Seguros, previdência, capitalização e consórcio", concepts=[
  dict(title="Sistema de seguros", subtopic="Susep e termos do seguro",
   summary="No sistema de seguros privados, o CNSP faz as regras e a Susep fiscaliza seguradoras, resseguradores, "
           "sociedades de capitalização e entidades de previdência complementar aberta. No contrato, o segurado paga o "
           "prêmio à seguradora; se ocorre o sinistro (o evento coberto), recebe a indenização, descontada a franquia "
           "quando houver. Beneficiário é quem recebe a indenização.",
   items=[
    A1("Exemplo: no seguro auto, o valor pago pelo cliente à seguradora é o prêmio; a batida coberta é o sinistro. No "
       "seguro residencial, o incêndio coberto que atinge a casa é chamado de:",
       "sinistro", ["prêmio", "franquia"],
       "Sinistro é a ocorrência do evento coberto pela apólice; prêmio é o preço do seguro."),
    TF("A Susep fiscaliza as seguradoras, as sociedades de capitalização e as entidades de previdência complementar "
       "aberta.", True,
       "Esses três mercados estão sob a Susep, que segue as normas do CNSP."),
    MT("Associe cada termo do seguro ao seu significado:",
       [("Prêmio", "Valor pago pelo segurado"), ("Sinistro", "Evento coberto que aconteceu"),
        ("Indenização", "Valor pago pela seguradora"), ("Franquia", "Parte do prejuízo que fica com o segurado")],
       "Prêmio vai do segurado para a seguradora; indenização vai da seguradora para o segurado ou beneficiário."),
    QK("O carro de um cliente, segurado com franquia de R$ 3.000, teve um conserto de R$ 10.000 coberto. Quanto a "
       "seguradora paga?",
       "R$ 7.000", ["R$ 10.000", "R$ 13.000"],
       "A franquia fica por conta do segurado: 10.000 − 3.000 = R$ 7.000.", step=3),
    PV("No sistema de seguros privados, NÃO está entre as atribuições da Susep",
       "fiscalizar os fundos de pensão de empresas.",
       ["fiscalizar as companhias seguradoras.", "fiscalizar as sociedades de capitalização.",
        "fiscalizar as entidades de previdência aberta.", "fiscalizar as empresas resseguradoras."],
       "Fundos de pensão são previdência fechada, fiscalizada pela Previc. Seguradoras, capitalização, previdência "
       "aberta e resseguradores estão sob a Susep.", style="excecao"),
   ]),
  dict(title="Seguros de pessoas e de danos", subtopic="tipos de seguro",
   summary="Seguros de pessoas cobrem vida e integridade: vida, acidentes pessoais e prestamista (quita ou paga parcelas "
           "de uma dívida em caso de morte, invalidez ou desemprego involuntário). Seguros de danos cobrem bens: "
           "automóvel, residencial, empresarial. No seguro auto, o valor de mercado referenciado segue uma tabela de "
           "preços de referência, com o percentual combinado; o valor determinado é uma quantia fixa definida na "
           "contratação.",
   items=[
    A1("Exemplo: o seguro de vida é seguro de pessoas; o seguro do carro é seguro de danos. Um seguro que quita o "
       "financiamento do carro se o devedor morrer é o seguro:",
       "prestamista", ["residencial", "de responsabilidade civil"],
       "O prestamista protege a dívida: em caso de morte ou invalidez (e, se contratado, desemprego), quita ou paga "
       "parcelas."),
    TF("No seguro auto com valor de mercado referenciado, a indenização segue uma tabela de preços de referência, com "
       "o percentual combinado no contrato.", True,
       "Por isso a indenização acompanha o preço de mercado do carro na data do sinistro, e não um valor fixo."),
    MT("Associe cada seguro ao que ele cobre:",
       [("Vida", "Morte do segurado"), ("Acidentes pessoais", "Morte ou invalidez por acidente"),
        ("Prestamista", "Dívida do segurado"), ("Residencial", "Danos ao imóvel")],
       "Os três primeiros são seguros de pessoas; o residencial é seguro de danos."),
    QK("Um cliente com empréstimo consignado quer que a dívida seja paga se ele for demitido sem justa causa. O "
       "prestamista indicado tem cobertura de:",
       "desemprego involuntário", ["desemprego voluntário", "acidente de trânsito"],
       "A cobertura de perda de renda é para desemprego involuntário; pedir demissão não está coberto.", step=3),
    PV("No seguro de automóvel contratado na modalidade valor determinado, a indenização integral corresponde",
       "à quantia fixa estabelecida pelas partes na contratação.",
       ["ao valor de uma tabela de referência na data do sinistro.", "ao total de prêmios pagos pelo segurado.",
        "à média de três orçamentos de oficinas credenciadas.", "ao valor da nota fiscal de compra corrigido."],
       "No valor determinado a quantia é fixada na contratação; a tabela de referência é a lógica do valor de mercado "
       "referenciado.", style="literal"),
   ]),
  dict(title="Previdência complementar", subtopic="PGBL, VGBL e fundos de pensão",
   summary="A previdência complementar aberta é vendida por seguradoras e entidades abertas, fiscalizadas pela Susep, a "
           "qualquer pessoa. No PGBL, as contribuições podem ser deduzidas da base do IR até 12% da renda bruta "
           "tributável (na declaração completa), e o IR incide sobre o valor total resgatado. No VGBL não há dedução e o "
           "IR incide só sobre os rendimentos. A previdência fechada (fundos de pensão) é restrita a empregados ou "
           "associados e é fiscalizada pela Previc. Num plano de contribuição definida, o benefício depende do saldo "
           "acumulado.",
   items=[
    A1("Exemplo: quem faz a declaração completa do IR e contribui ao PGBL deduz até 12% da renda bruta tributável. Um "
       "cliente usa a declaração simplificada. Qual plano costuma ser mais indicado para ele?",
       "VGBL", ["PGBL", "nenhum dos dois"],
       "Sem declaração completa, a dedução do PGBL não traz vantagem; no VGBL o IR incide só sobre os rendimentos."),
    TF("No VGBL, o imposto de renda incide apenas sobre os rendimentos, no resgate ou no recebimento da renda.", True,
       "Como as contribuições do VGBL não são deduzidas, só o ganho é tributado; no PGBL, o valor total."),
    QK("Um funcionário de uma estatal participa do fundo de pensão da empresa. Quem fiscaliza esse plano?",
       "a Previc", ["a Susep", "a CVM"],
       "Fundos de pensão são previdência fechada, fiscalizada pela Previc. A Susep cuida da previdência aberta."),
    QK("Num plano de contribuição definida (CD), o participante sabe com certeza:",
       "quanto contribui, não o benefício final", ["o valor exato do benefício futuro", "que o resgate é isento de IR"],
       "Na contribuição definida o benefício depende do saldo acumulado e da rentabilidade; não é garantido de antemão.",
       step=3),
   ]),
  dict(title="Capitalização", subtopic="títulos de capitalização",
   summary="Título de capitalização junta pagamentos (únicos ou mensais) com sorteios de prêmios. Parte do valor pago "
           "forma a reserva que será devolvida no resgate; parte paga os sorteios e as despesas. No fim do prazo o "
           "cliente resgata a reserva, corrigida conforme o título; resgatar antes pode devolver menos do que foi pago. "
           "Não é investimento: é um produto de sorteio com resgate, regulado pela Susep.",
   items=[
    A1("Exemplo: num título de 12 meses, parte de cada pagamento vai para os sorteios; por isso o resgate antecipado "
       "pode ser menor que o total pago. Um cliente quer que o dinheiro renda mais que a poupança. O título de "
       "capitalização é indicado?",
       "não, ele não é feito para render", ["sim, rende mais que a poupança", "sim, se o pagamento for mensal"],
       "A capitalização é um produto de sorteio com devolução da reserva; não foi feito para competir em rendimento."),
    TF("Os títulos de capitalização são regulados e fiscalizados pela Susep.", True,
       "As sociedades de capitalização estão no sistema de seguros privados, sob o CNSP e a Susep."),
    QK("Uma cliente pagou R$ 1.200 num título e quer resgatar no 3º mês. É possível que ela receba:",
       "menos do que pagou", ["o valor pago corrigido pela Selic", "o valor pago mais os prêmios"],
       "Parte dos pagamentos custeia sorteios e despesas, e pode haver carência: o resgate antecipado pode ser menor."),
    MT("Associe cada produto à sua lógica:",
       [("Título de capitalização", "Sorteio com resgate da reserva"), ("Poupança", "Rendimento definido em lei"),
        ("Consórcio", "Compra planejada em grupo"), ("Seguro de vida", "Indenização ao beneficiário")],
       "Produtos diferentes para objetivos diferentes: capitalização não substitui poupança, consórcio nem seguro.",
       step=3),
    PV("Ao oferecer um título de capitalização, o técnico bancário deve informar ao cliente que",
       "parte dos pagamentos custeia sorteios e o resgate antecipado pode ser menor.",
       ["o produto tem garantia do FGC até R$ 250 mil por cliente e por instituição.",
        "o rendimento acompanha sempre o CDI do período, como num CDB pós-fixado.",
        "o título é fiscalizado pela CVM, como as ações e as debêntures.",
        "não há sorteios, só rendimento mensal garantido pela seguradora."],
       "A informação correta sobre o produto evita venda inadequada: sorteios e resgate possivelmente menor. Não há FGC "
       "nem rendimento de CDI, e a fiscalização é da Susep."),
   ]),
  dict(title="Consórcio e bancassurance", subtopic="consórcio e bancassurance",
   summary="Consórcio é um grupo de pessoas que contribui mensalmente para um fundo comum para comprar bens ou serviços. "
           "Todo mês há contemplações por sorteio ou lance, e o contemplado recebe a carta de crédito. Não há juros; há "
           "taxa de administração e, em geral, fundo de reserva. As administradoras são autorizadas e fiscalizadas pelo "
           "Banco Central. Bancassurance é a venda de seguros, previdência e capitalização pelo banco, em seus canais, "
           "em parceria com seguradoras.",
   items=[
    A1("Exemplo: num consórcio de imóvel, quem dá o maior lance do mês é contemplado e recebe a carta de crédito. No "
       "consórcio, além do valor do bem dividido nas parcelas, o participante paga:",
       "taxa de administração", ["juros compostos mensais", "spread bancário"],
       "Consórcio não tem juros: o custo é a taxa de administração (e o fundo de reserva, quando houver)."),
    TF("As administradoras de consórcio são autorizadas e fiscalizadas pelo Banco Central.", True,
       "O consórcio integra o sistema financeiro sob o Banco Central, não sob a Susep."),
    MT("Associe cada termo do consórcio ao seu significado:",
       [("Contemplação", "Direito de usar a carta de crédito"), ("Lance", "Oferta para antecipar a contemplação"),
        ("Taxa de administração", "Remuneração da administradora"), ("Carta de crédito", "Valor para comprar o bem")],
       "A contemplação dá acesso à carta de crédito; o lance é a forma de antecipá-la; a taxa paga a administradora."),
    QK("Uma cliente compra um seguro residencial na agência do banco em que tem conta, de uma seguradora parceira do "
       "banco. Esse modelo de negócio é o:",
       "bancassurance", ["consórcio", "open banking"],
       "Bancassurance é usar a rede e os canais do banco para vender seguros, previdência e capitalização.", step=3),
    PV("Sobre o consórcio, NÃO é correto afirmar que",
       "o participante paga juros compostos sobre a carta de crédito.",
       ["a contemplação ocorre por sorteio ou por lance.", "as administradoras são fiscalizadas pelo Banco Central.",
        "o custo principal é a taxa de administração.", "o contemplado recebe uma carta de crédito."],
       "No consórcio não há juros: o custo é a taxa de administração. As outras afirmações estão corretas.",
       style="excecao"),
   ]),
 ]),

 dict(order=7, title="Regulação e perfil do cliente", concepts=[
  dict(title="Autorregulação bancária", subtopic="autorregulação",
   summary="A autorregulação bancária, criada pela Febraban em 2008, é um conjunto de normas que as próprias "
           "instituições assumem para ir além do que a lei exige, em temas como relacionamento com o consumidor e "
           "transparência. A adesão ao sistema é voluntária. Ela não substitui a regulação do CMN e do Banco Central, e "
           "o cumprimento das suas normas é acompanhado por órgãos da própria Febraban.",
   items=[
    A1("Exemplo: o banco que adere à autorregulação se compromete com regras de atendimento além das exigidas por lei. "
       "A adesão de um banco ao sistema de autorregulação bancária é:",
       "voluntária", ["obrigatória por resolução do CMN", "imposta pela CVM"],
       "Ninguém é obrigado a aderir; quem adere assume os compromissos do sistema."),
    TF("Para os bancos que aderem, as normas de autorregulação substituem as resoluções do CMN e do Banco Central.",
       False,
       "A autorregulação soma compromissos à regulação oficial, que continua valendo integralmente."),
    MT("Associe cada instância ao seu papel:",
       [("CMN", "Edita as normas do sistema"), ("Banco Central", "Supervisiona as instituições"),
        ("Febraban", "Conduz a autorregulação"), ("Ouvidoria", "Segunda instância do cliente no banco")],
       "Regulação oficial (CMN e BC), autorregulação (Febraban) e o canal interno de segunda instância (ouvidoria)."),
    QK("Um cliente teve a reclamação mal resolvida no SAC do banco. O próximo canal, dentro do próprio banco, é:",
       "a ouvidoria", ["a Febraban", "o Copom"],
       "A ouvidoria atua como segunda instância, depois do SAC e do atendimento comum.", step=3),
    PV("A respeito da autorregulação bancária no Brasil, é correto afirmar que",
       "reúne compromissos das próprias instituições, além da regulação oficial.",
       ["foi instituída por lei federal e substitui as resoluções do Banco Central.",
        "é conduzida pela CVM, com adesão obrigatória para todos os bancos.",
        "aplica-se apenas às cooperativas de crédito e às financeiras.",
        "dispensa as instituições de manter ouvidoria e SAC próprios."],
       "A autorregulação é da própria indústria (Febraban): soma compromissos à regulação oficial, sem substituí-la."),
   ]),
  dict(title="Relacionamento com clientes (Res. CMN 4.949/2021)", subtopic="Resolução CMN 4.949/2021",
   summary="A Resolução CMN nº 4.949/2021 trata do relacionamento das instituições com clientes e usuários. Exige "
           "tratamento justo, transparência, diligência e produtos adequados às necessidades do cliente, em todas as "
           "fases: pré-contratação (oferta e informação), contratação e pós-contratação (atendimento, cancelamento, "
           "reclamações). Induzir o cliente a erro ou prometer ganho sem risco contraria esses princípios.",
   items=[
    A1("Exemplo: anunciar um investimento com ganho garantido quando ele tem risco fere a política de relacionamento com "
       "clientes. Em qual fase da relação a Resolução 4.949/2021 se aplica?",
       "em todas: antes, durante e depois", ["só na contratação", "só depois da contratação"],
       "A norma cobre a pré-contratação, a contratação e a pós-contratação."),
    TF("Pela política de relacionamento com clientes, a instituição deve oferecer produtos adequados às necessidades e "
       "ao perfil de cada cliente.", True,
       "Adequação é um dos princípios da norma, ao lado de transparência, diligência e tratamento justo."),
    QK("Um gerente diz a um cliente: “Este fundo de ações não tem risco nenhum”. Essa conduta:",
       "é vedada, induz o cliente a erro", ["é permitida, se o fundo foi bem", "é permitida para cliente arrojado"],
       "Fundo de ações tem risco. Afirmar que não tem induz o cliente a erro, o que contraria a transparência e o "
       "tratamento justo exigidos na relação com o cliente."),
    QK("Um cliente quer cancelar um serviço e o banco cria obstáculos para dificultar. Isso fere a fase de:",
       "pós-contratação", ["pré-contratação", "nenhuma, cancelamento é livre"],
       "Cancelamento, atendimento e reclamações fazem parte da pós-contratação, também coberta pela norma.", step=3),
   ]),
  dict(title="Adequação ao perfil do cliente", subtopic="suitability",
   summary="Antes de recomendar investimentos, a instituição verifica se o produto é adequado ao cliente "
           "(suitability): objetivos, prazo, situação financeira, tolerância a risco e conhecimento. Com isso o cliente "
           "recebe um perfil (conservador, moderado ou arrojado). Se ele quiser algo fora do perfil, a instituição deve "
           "alertá-lo e registrar sua ciência antes de seguir.",
   items=[
    A1("Exemplo: a aposentada que não aceita perdas e pode precisar do dinheiro em 6 meses tem perfil conservador; um "
       "fundo de ações não é adequado para ela. Qual produto combina com um cliente conservador que pode precisar do "
       "dinheiro a qualquer momento?",
       "renda fixa com liquidez diária", ["ações de empresa iniciante", "fundo com carência de 2 anos"],
       "Conservador com necessidade de liquidez pede baixo risco e resgate a qualquer momento."),
    TF("O perfil do investidor considera, entre outros pontos, o objetivo do investimento, o prazo e a tolerância a "
       "perdas.", True,
       "Esses fatores mostram quanto risco o cliente pode e quer correr, e por quanto tempo."),
    MT("Associe cada perfil à sua descrição:",
       [("Conservador", "Prioriza segurança e liquidez"), ("Moderado", "Aceita algum risco por retorno maior"),
        ("Arrojado", "Aceita oscilações fortes buscando retorno alto")],
       "O perfil orienta quais produtos podem ser recomendados a cada cliente."),
    QK("Um cliente de perfil conservador insiste em aplicar todo o patrimônio num fundo de ações. O técnico deve:",
       "alertar e registrar a ciência do cliente", ["recusar qualquer aplicação dele", "aplicar sem comentar nada"],
       "A decisão é do cliente, mas a instituição precisa alertar sobre a incompatibilidade com o perfil e registrar "
       "que ele está ciente.", step=3),
    PV("Uma cliente de 70 anos, aposentada, diz que não aceita perder dinheiro e que pode precisar dos recursos para "
       "despesas de saúde. A recomendação mais adequada ao perfil dela é",
       "um investimento de renda fixa de baixo risco e liquidez diária.",
       ["um fundo de ações com foco em empresas iniciantes de tecnologia.",
        "um título de capitalização com sorteios mensais em dinheiro.",
        "um CDB de cinco anos sem liquidez antes do vencimento.",
        "uma carteira diversificada de criptoativos de grande liquidez."],
       "Ela é conservadora e precisa de liquidez: renda fixa de baixo risco com resgate diário. Ações e cripto são "
       "arriscados; o CDB longo prende o dinheiro; capitalização não é investimento.", style="caso_pratico"),
   ]),
  dict(title="Portabilidade", subtopic="portabilidade de crédito, salário e previdência",
   summary="Portabilidade é o direito de levar um produto para outra instituição. Na de crédito, o cliente transfere a "
           "dívida para um banco que ofereça condições melhores, sem custo; o novo banco quita a dívida no antigo, que "
           "pode fazer uma contraproposta, mas não pode impedir. Há também a de salário (o salário vai para a conta "
           "escolhida, sem custo) e a de previdência (a reserva passa de um plano para outro, sem IR na transferência).",
   items=[
    A1("Exemplo: o cliente tem empréstimo a 3% ao mês no banco A; o banco B oferece 2%. Pela portabilidade, B quita a "
       "dívida em A e o cliente passa a pagar a B. Quem paga tarifa por essa transferência?",
       "ninguém, ela não pode ser cobrada", ["o cliente, ao banco A", "o cliente, ao banco B"],
       "A portabilidade de crédito não pode gerar custo para o cliente."),
    TF("Na portabilidade de previdência entre planos, o valor transferido não sofre imposto de renda na transferência.",
       True,
       "O IR só incide no resgate ou no recebimento da renda; a transferência entre planos não é resgate."),
    MT("Associe cada portabilidade ao que ela faz:",
       [("De crédito", "Leva a dívida para outro banco"), ("De salário", "Leva o salário para a conta escolhida"),
        ("De previdência", "Leva a reserva para outro plano"), ("Refinanciamento", "Nova operação no mesmo banco")],
       "As portabilidades mudam o produto de instituição; o refinanciamento é uma nova operação no mesmo banco."),
    QK("Um cliente recebe uma proposta melhor de outro banco para o seu financiamento. O banco atual pode impedir a "
       "portabilidade?",
       "não, mas pode fazer contraproposta", ["sim, se o contrato for antigo", "sim, cobrando multa"],
       "O banco original pode tentar reter o cliente com uma proposta melhor, mas não pode impedir nem cobrar pela "
       "transferência.", step=3),
    PV("Sobre a portabilidade de crédito, é correto afirmar que",
       "permite levar a dívida a outra instituição, em condições melhores, sem custo.",
       ["exige que o cliente quite antes a dívida com recursos próprios, à vista.",
        "só vale para o crédito consignado de servidores públicos federais e estaduais.",
        "obriga o novo banco a manter a mesma taxa de juros do contrato original.",
        "depende de autorização prévia do Banco Central em cada caso concreto."],
       "Na portabilidade, o novo banco quita a dívida no antigo e o cliente leva a operação, sem custo, para condições "
       "melhores."),
  ]),
  dict(title="Resolução CMN 4.966/2021", subtopic="instrumentos financeiros e perda esperada",
   summary="A Resolução CMN nº 4.966/2021, em vigor desde 2025, muda a contabilidade dos instrumentos financeiros nos "
           "bancos e a aproxima da norma internacional IFRS 9. A provisão para crédito deixa de olhar sobretudo o atraso "
           "que já aconteceu e passa a considerar a perda esperada, em três estágios conforme o aumento do risco. Os "
           "ativos são classificados pelo modelo de negócio: custo amortizado, valor justo em outros resultados "
           "abrangentes ou valor justo no resultado.",
   items=[
    A1("Exemplo: antes, a provisão crescia sobretudo depois do atraso; com a Resolução 4.966, o banco provisiona a "
       "perda que espera ter, mesmo antes do atraso. A ideia central da nova regra para o crédito é:",
       "provisionar pela perda esperada", ["provisionar só após 90 dias de atraso", "eliminar as provisões"],
       "O modelo de perda esperada antecipa o reconhecimento do risco, em vez de esperar o atraso acontecer."),
    TF("A Resolução CMN 4.966/2021 aproxima a contabilidade dos instrumentos financeiros dos bancos brasileiros da "
       "norma internacional IFRS 9.", True,
       "A convergência com o IFRS 9 é um dos objetivos centrais da resolução."),
    MT("Associe cada categoria de classificação ao modelo de negócio:",
       [("Custo amortizado", "Manter o ativo para receber os fluxos"),
        ("Valor justo em outros resultados abrangentes", "Receber os fluxos e também vender"),
        ("Valor justo no resultado", "Negociar o ativo ativamente")],
       "A classificação depende de como o banco administra o ativo: só receber, receber e vender, ou negociar."),
    QK("O risco de um crédito aumentou muito desde a concessão, mas ainda não há atraso. Pela lógica da perda esperada, "
       "esse crédito:",
       "passa a exigir provisão maior", ["não exige provisão até atrasar", "deve sair do balanço"],
       "O aumento significativo do risco leva o crédito a um estágio com provisão maior, antes mesmo do atraso.",
       step=3),
    PV("A Resolução CMN nº 4.966/2021 trata principalmente",
       "dos critérios contábeis de instrumentos financeiros e da perda esperada.",
       ["das regras de funcionamento do Pix para pessoas jurídicas.",
        "da política de relacionamento das instituições com clientes e usuários.",
        "das normas de prevenção à lavagem de dinheiro e ao financiamento do terrorismo.",
        "do funcionamento das sociedades de crédito direto."],
       "A 4.966 é a norma contábil dos instrumentos financeiros. O relacionamento com clientes está na 4.949/2021; PLD, "
       "Pix e SCD têm normas próprias.", style="literal"),
   ]),
 ]),
]
