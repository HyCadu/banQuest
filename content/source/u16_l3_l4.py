# Unidade 1 do Técnico Bancário (u16), lições 3 e 4. Gerado para o app por tools/gen_u16.py.
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
 dict(order=3, title="Mercado financeiro e política monetária", concepts=[
  dict(title="Copom e taxa Selic", subtopic="Copom e Selic",
   summary="O Comitê de Política Monetária (Copom), formado pelo presidente e pelos diretores do Banco Central, se reúne "
           "a cada 45 dias e define a meta da taxa Selic, o principal instrumento para levar a inflação à meta fixada "
           "pelo CMN (medida pelo IPCA). Juros mais altos encarecem o crédito, reduzem o consumo e seguram os preços; "
           "juros mais baixos estimulam a economia.",
   items=[
    A1("Exemplo: com a inflação acima da meta, o Copom sobe a Selic para frear o consumo. Se a inflação estiver bem "
       "abaixo da meta e a economia fraca, o mais provável é que o Copom:",
       "reduza a meta da Selic", ["aumente a meta da Selic", "mude a meta de inflação"],
       "Juros menores barateiam o crédito e estimulam a economia. A meta de inflação não é do Copom, é do CMN."),
    TF("O Copom é formado pelo presidente e pelos diretores do Banco Central e se reúne, em regra, a cada 45 dias.", True,
       "São oito reuniões por ano, cerca de uma a cada 45 dias, com o presidente e os diretores do BC."),
    MT("Associe cada peça da política monetária ao seu papel:",
       [("CMN", "Fixa a meta de inflação"), ("Copom", "Fixa a meta da Selic"),
        ("Mesa de operações do BC", "Mantém a Selic diária perto da meta"), ("IPCA", "Índice oficial da meta de inflação")],
       "O CMN dá o alvo, o Copom escolhe o juro, a mesa do BC executa e o IPCA mede a inflação oficial."),
    QK("Depois de uma alta da Selic, o que tende a acontecer com o crédito ao consumidor?",
       "fica mais caro e a procura cai", ["fica mais barato", "não muda, a Selic só vale entre bancos"],
       "A Selic é a referência básica de juros: quando sobe, os bancos encarecem o crédito e a demanda diminui.",
       step=3),
    PV("A respeito da política monetária no Brasil, NÃO é correto afirmar que",
       "o Copom fixa a meta de inflação e o CMN, a taxa Selic.",
       ["o CMN fixa a meta de inflação, medida pelo IPCA.", "o Copom é formado pela diretoria do Banco Central.",
        "a meta da taxa Selic é definida pelo Copom.", "juros mais altos tendem a frear o consumo."],
       "Os papéis estão trocados: o CMN fixa a meta de inflação e o Copom, a meta da Selic. As outras frases estão "
       "corretas.", style="excecao"),
   ]),
  dict(title="Operações de mercado aberto", subtopic="mercado aberto",
   summary="Para manter a Selic diária perto da meta, a mesa de operações do Banco Central compra e vende títulos "
           "públicos (operações de mercado aberto). Comprar títulos põe dinheiro no mercado: a liquidez aumenta e o juro "
           "diário cai. Vender títulos tira dinheiro do mercado: a liquidez diminui e o juro sobe.",
   items=[
    A1("Exemplo: o BC vende títulos, os bancos pagam com suas reservas e sobra menos dinheiro no mercado: o juro sobe. "
       "Se o BC comprar títulos dos bancos, o efeito é:",
       "mais dinheiro no mercado e juro em queda", ["menos dinheiro e juro em alta", "nenhum efeito sobre o juro"],
       "Comprando títulos, o BC paga os bancos com reservas: a liquidez aumenta e o juro diário cai."),
    TF("Quando a Selic diária fica abaixo da meta, a mesa do Banco Central deve vender títulos para enxugar a liquidez.",
       True,
       "Juro abaixo da meta indica sobra de dinheiro; vendendo títulos, o BC recolhe esse excesso e o juro sobe à meta."),
    QK("A meta da Selic é 10% ao ano e a taxa diária está em 9,8%. Qual operação a mesa do BC deve fazer?",
       "vender títulos públicos", ["comprar títulos públicos", "emitir papel-moeda"],
       "Taxa abaixo da meta: há dinheiro sobrando. Vender títulos retira liquidez e faz a taxa subir."),
    OR("Ordene o que acontece quando o Banco Central vende títulos no mercado aberto:",
       ["O Banco Central vende títulos públicos aos bancos", "Os bancos pagam com suas reservas",
        "Sobra menos dinheiro para emprestar entre bancos", "A taxa de juros diária sobe"],
       "Venda de títulos → saída de reservas → menos liquidez → juro mais alto. Na compra, o caminho é o inverso."),
    PV("Num dia em que sobram reservas no sistema e a Selic diária cai abaixo da meta, a mesa de operações do Banco "
       "Central tende a",
       "vender títulos públicos para retirar liquidez do sistema.",
       ["comprar títulos públicos, injetando ainda mais liquidez no sistema.",
        "reduzir a meta da Selic na mesma hora, sem esperar o Copom.",
        "aumentar o recolhimento compulsório sobre os depósitos de poupança.",
        "emitir papel-moeda e entregá-lo aos bancos com falta de reservas."],
       "A mesa não muda a meta (isso é do Copom): ela vende títulos para enxugar o excesso de reservas e levar a taxa "
       "diária de volta à meta.", style="caso_pratico"),
   ]),
  dict(title="Compulsório e redesconto", subtopic="instrumentos de política monetária",
   summary="Além do mercado aberto, o Banco Central usa o depósito compulsório (parte dos depósitos que os bancos são "
           "obrigados a recolher ao BC: quanto maior, menos dinheiro para emprestar) e o redesconto (empréstimo do BC aos "
           "bancos para cobrir falta de liquidez). Compulsório maior contrai o crédito; menor, expande.",
   items=[
    A1("Exemplo: se o compulsório sobe, os bancos recolhem mais ao BC e têm menos para emprestar. Se o Banco Central "
       "reduzir o compulsório, o crédito tende a:",
       "aumentar", ["diminuir", "não mudar"],
       "Com compulsório menor, sobra mais dinheiro nos bancos para conceder crédito."),
    TF("No redesconto, o Banco Central empresta recursos a uma instituição financeira que precisa de liquidez.", True,
       "O redesconto é o empréstimo de assistência do BC aos bancos, o papel de banco dos bancos."),
    MT("Associe cada instrumento ao seu efeito:",
       [("Compulsório maior", "Menos dinheiro para emprestar"), ("Compra de títulos pelo BC", "Mais liquidez"),
        ("Venda de títulos pelo BC", "Menos liquidez"), ("Redesconto", "Socorro de liquidez a um banco")],
       "Compulsório e mercado aberto regulam a quantidade de dinheiro; o redesconto atende um banco com falta de caixa."),
    QK("Para conter uma alta forte do crédito sem mexer na Selic, o Banco Central pode:",
       "elevar o compulsório", ["reduzir o compulsório", "comprar títulos públicos"],
       "Compulsório maior prende recursos no BC e reduz o crédito. Reduzir o compulsório e comprar títulos fariam o "
       "contrário.", step=3),
    PV("Ao resumir os instrumentos de política monetária para um cliente, um analista escreveu cinco frases. Está "
       "correta a frase:",
       "O aumento do compulsório reduz os recursos que os bancos podem emprestar.",
       ["O redesconto é o empréstimo que o cliente faz diretamente ao Banco Central.",
        "A compra de títulos pelo Banco Central retira dinheiro da economia.",
        "O compulsório é recolhido pelos bancos ao Tesouro Nacional.",
        "A venda de títulos pelo Banco Central reduz a taxa de juros."],
       "O compulsório é recolhido ao Banco Central e, quanto maior, menos sobra para emprestar. O redesconto é para "
       "bancos; comprar títulos injeta dinheiro; vender títulos faz o juro subir.", style="caso_pratico"),
   ]),
  dict(title="Mercado de capitais e CVM", subtopic="ações, debêntures e CVM",
   summary="No mercado de capitais as empresas captam recursos de longo prazo emitindo valores mobiliários: ações (o "
           "investidor vira sócio) e debêntures (o investidor empresta à empresa). No mercado primário o título é "
           "emitido; no secundário, negociado entre investidores na bolsa. Quem regula e fiscaliza é a CVM: bolsas, "
           "fundos de investimento, auditores independentes e agências de rating. Seguradoras e sociedades de "
           "capitalização ficam com a Susep.",
   items=[
    A1("Exemplo: quem compra ação vira sócio e pode receber dividendos; quem compra debênture empresta à empresa e "
       "recebe juros. Um investidor quer emprestar a uma empresa e receber juros. Ele deve comprar:",
       "debêntures", ["ações", "cotas de consórcio"],
       "A debênture é título de dívida da empresa; a ação é participação no capital."),
    TF("Na abertura de capital (IPO), as ações são vendidas no mercado primário e o dinheiro vai para a empresa.", True,
       "No primário a empresa emite e recebe os recursos; depois, as ações circulam no secundário, entre investidores."),
    MT("Associe cada termo à sua definição:",
       [("Ação", "Participação no capital"), ("Debênture", "Dívida da empresa com o investidor"),
        ("Mercado primário", "Emissão de novos títulos"), ("Mercado secundário", "Negociação entre investidores")],
       "Ações e debêntures são os valores mobiliários típicos; primário é a emissão, secundário é a revenda."),
    QK("Uma agência de classificação de risco (rating) passa a avaliar debêntures. Quem a regula?",
       "a CVM", ["a Susep", "o Copom"],
       "Agências de rating, bolsas, fundos e auditores independentes atuam no mercado de valores mobiliários, sob a CVM.",
       step=3),
    PV("No mercado de capitais, a negociação, na bolsa, de ações já emitidas entre investidores ocorre no mercado",
       "secundário.", ["primário.", "monetário.", "de câmbio.", "interbancário."],
       "Ações já emitidas que trocam de mãos entre investidores estão no mercado secundário; o primário é a emissão.",
       style="literal", diff=1),
   ]),
  dict(title="Mercado de câmbio", subtopic="câmbio e swaps",
   summary="O mercado de câmbio troca reais por moedas estrangeiras. É regulado pelo CMN e fiscalizado pelo Banco "
           "Central; as taxas são livres, combinadas entre as partes, e pessoas e empresas podem operar sem limite de "
           "valor, por instituições autorizadas. Para segurar a alta do dólar, o BC vende dólares ou faz swap cambial "
           "tradicional (ele paga a variação do dólar); para segurar a queda, compra dólares ou faz swap reverso (ele "
           "recebe a variação).",
   items=[
    A1("Exemplo: com o dólar subindo rápido, o BC vende dólares e a cotação desacelera. Se o dólar estiver caindo demais "
       "e o BC quiser segurar a queda, ele deve:",
       "comprar dólares", ["vender dólares", "baixar o compulsório"],
       "Comprando dólares, o BC aumenta a procura pela moeda estrangeira e segura a queda da cotação."),
    TF("No Brasil, o Banco Central fixa a taxa de câmbio de cada operação feita pelos bancos.", False,
       "As taxas de câmbio são livres, combinadas entre as partes. O BC divulga a PTAX, uma média de referência, mas "
       "não fixa o preço das operações."),
    QK("Uma importadora teme a alta do dólar. O Banco Central anuncia leilões de swap cambial tradicional. Nesses "
       "contratos, o BC:",
       "paga a variação do dólar e recebe juros", ["recebe a variação do dólar e paga juros", "vende dólares em espécie"],
       "No swap tradicional o BC assume a posição vendida em dólar: paga a variação cambial e recebe juros. É o efeito "
       "de uma venda de dólar no futuro."),
    MT("Associe cada atuação do Banco Central ao seu efeito no dólar:",
       [("Venda de dólar à vista", "Segura a alta"), ("Compra de dólar à vista", "Segura a queda"),
        ("Swap tradicional", "BC paga a variação cambial"), ("Swap reverso", "BC recebe a variação cambial")],
       "Vender dólar e swap tradicional seguram a alta; comprar dólar e swap reverso seguram a queda.", step=3),
   ]),
 ]),

 dict(order=4, title="Pix, Open Finance e inovação", concepts=[
  dict(title="Pix", subtopic="Pix",
   summary="O Pix é o pagamento instantâneo criado pelo Banco Central: a transferência cai em segundos, 24 horas por dia, "
           "todos os dias, inclusive fins de semana e feriados. A chave (CPF, CNPJ, e-mail, celular ou aleatória) é "
           "opcional: dá para pagar com agência e conta ou por QR Code. Para pessoa física, enviar e receber é gratuito "
           "nos usos comuns; empresas podem ser tarifadas, inclusive ao receber.",
   items=[
    A1("Exemplo: um Pix enviado num domingo de Páscoa cai na hora, como numa segunda-feira. Uma cliente faz um Pix às "
       "23h de um feriado. O dinheiro chega:",
       "em segundos", ["no próximo dia útil", "em até dois dias úteis"],
       "O Pix funciona 24 horas por dia, todos os dias, sem esperar dia útil."),
    TF("Para receber um Pix, é obrigatório ter uma chave cadastrada.", False,
       "A chave só facilita; também se paga e recebe Pix informando agência e conta, ou por QR Code."),
    MT("Associe cada forma de identificar o recebedor no Pix:",
       [("Chave CPF", "Dado da pessoa física"), ("Chave CNPJ", "Dado da empresa"),
        ("Chave aleatória", "Código gerado sem dado pessoal"), ("Agência e conta", "Pagamento sem chave")],
       "As chaves são apelidos da conta; sem chave, o Pix vai com os dados bancários do recebedor."),
    QK("Uma loja (pessoa jurídica) quer saber se o banco pode cobrar tarifa quando ela recebe pagamentos via Pix. A "
       "resposta é:",
       "sim, empresas podem ser tarifadas ao receber", ["não, o Pix é sempre gratuito", "só à noite e nos feriados"],
       "A gratuidade obrigatória é para pessoas físicas nos usos comuns; empresas podem pagar tarifa, também no "
       "recebimento.", step=3),
    PV("Um cliente tirou dúvidas sobre o Pix com o técnico. Entre as informações a seguir, NÃO está correta a de que o Pix",
       "exige chave cadastrada de quem recebe o pagamento.",
       ["funciona 24 horas, inclusive em fins de semana e feriados.", "pode ser feito com agência e conta, sem chave.",
        "pode ser tarifado para empresas, também ao receber.", "foi criado e é regulado pelo Banco Central."],
       "A chave é opcional: dá para pagar e receber Pix com agência e conta ou QR Code. As outras afirmações estão "
       "corretas.", style="excecao"),
   ]),
  dict(title="Open Finance", subtopic="Open Finance",
   summary="No Open Finance o cliente autoriza o compartilhamento dos seus dados (cadastro, contas, crédito, "
           "investimentos) entre instituições autorizadas pelo Banco Central, para conseguir ofertas melhores. O "
           "consentimento é do cliente, tem prazo e finalidade e pode ser cancelado a qualquer momento. O Open Finance "
           "também permite iniciar um pagamento a partir de outro aplicativo. A participação é obrigatória para as "
           "maiores instituições e voluntária para as demais.",
   items=[
    A1("Exemplo: um cliente autoriza o banco B a ver seu histórico de crédito no banco A e recebe uma taxa menor. Quem "
       "decide se os dados vão ser compartilhados?",
       "o próprio cliente", ["o banco A", "o Banco Central"],
       "O compartilhamento depende do consentimento do cliente, não da vontade da instituição que guarda os dados."),
    TF("Depois de autorizar o compartilhamento no Open Finance, o cliente só pode cancelar no fim do prazo combinado.",
       False,
       "O consentimento pode ser revogado a qualquer momento pelo cliente."),
    QK("Uma cliente quer pagar uma compra pelo aplicativo de uma fintech, debitando a conta que tem em outro banco, sem "
       "abrir o app desse banco. No Open Finance, isso se chama:",
       "iniciação de pagamento", ["portabilidade de salário", "compensação de cheques"],
       "A iniciação de pagamento permite que uma instituição dispare um pagamento a partir da conta do cliente em "
       "outra, com o consentimento dele."),
    QK("Um banco pequeno quer saber se é obrigado a participar do Open Finance. Em regra:",
       "não, a obrigação é das maiores instituições", ["sim, todas as autorizadas", "não, só fintechs participam"],
       "A participação é obrigatória para as instituições de maior porte; as demais podem aderir voluntariamente.",
       step=3),
   ]),
  dict(title="Fintechs de crédito: SCD e SEP", subtopic="SCD e SEP",
   summary="As fintechs de crédito são autorizadas e reguladas pelo Banco Central. A Sociedade de Crédito Direto (SCD) "
           "empresta com capital próprio, por plataforma eletrônica, e não capta depósitos do público. A Sociedade de "
           "Empréstimo entre Pessoas (SEP) liga quem empresta a quem toma (peer-to-peer): pode receber recursos do "
           "público, mas só vinculados a cada operação de empréstimo.",
   items=[
    A1("Exemplo: na SEP, vários investidores financiam juntos o empréstimo de um tomador pela plataforma. Uma fintech "
       "empresta apenas com o próprio capital, por aplicativo. Ela é uma:",
       "SCD (Sociedade de Crédito Direto)", ["SEP (Empréstimo entre Pessoas)", "instituição de pagamento"],
       "Emprestar com capital próprio por meio eletrônico é o modelo da SCD."),
    TF("Fintechs de crédito, como SCD e SEP, podem funcionar sem autorização do Banco Central.", False,
       "SCD e SEP são instituições financeiras e precisam de autorização do Banco Central para funcionar."),
    MT("Associe cada instituição ao seu modelo de negócio:",
       [("SCD", "Empresta com capital próprio"), ("SEP", "Liga investidores e tomadores"),
        ("Instituição de pagamento", "Conta de pagamento e pré-pago"), ("Banco comercial", "Depósitos à vista e crédito")],
       "Cada tipo de instituição tem um modelo: a SCD usa capital próprio; a SEP intermedeia; a IP só faz pagamentos."),
    QK("Uma SEP recebe dinheiro de investidores para financiar o empréstimo de um pequeno comerciante. Essa captação é "
       "permitida?",
       "sim, vinculada a essa operação", ["não, a SEP nunca recebe recursos", "sim, e a SEP usa como quiser"],
       "A SEP só pode receber recursos vinculados às operações de empréstimo que intermedeia.", step=3),
    PV("Uma fintech conecta, pela sua plataforma, investidores que querem emprestar e pequenas empresas que precisam "
       "de crédito, sem usar capital próprio. Essa instituição é uma",
       "Sociedade de Empréstimo entre Pessoas (SEP).",
       ["Sociedade de Crédito Direto (SCD).", "instituição de pagamento emissora de moeda eletrônica.",
        "sociedade de crédito imobiliário.", "administradora de consórcio."],
       "Intermediar empréstimos entre investidores e tomadores, sem capital próprio, é o modelo da SEP. A SCD empresta "
       "com o próprio capital.", style="caso_pratico"),
   ]),
  dict(title="Instituições de pagamento", subtopic="instituições de pagamento",
   summary="Instituição de pagamento (IP) presta serviços de pagamento: conta de pagamento, cartão pré-pago, emissão de "
           "cartão, maquininha (credenciadora), transferências. A IP não é instituição financeira: não pode emprestar "
           "nem usar o saldo dos clientes, que fica separado do patrimônio dela, e esse saldo não tem garantia do FGC. "
           "Conforme o porte, precisa de autorização do Banco Central.",
   items=[
    A1("Exemplo: a carteira digital guarda o saldo do cliente numa conta de pagamento, mas não pode emprestar esse "
       "dinheiro. Uma empresa que só oferece maquininhas de cartão para lojistas é:",
       "uma instituição de pagamento", ["um banco comercial", "uma sociedade de crédito direto"],
       "Credenciar lojistas para aceitar cartões (maquininha) é serviço de pagamento, típico de IP."),
    TF("Uma instituição de pagamento pode conceder empréstimos usando o saldo das contas de pagamento dos clientes.",
       False,
       "A IP não é instituição financeira: o saldo dos clientes fica separado e não pode ser emprestado."),
    QK("Um cliente guarda dinheiro na carteira digital de uma instituição de pagamento. Esse saldo:",
       "fica separado do patrimônio da IP", ["é emprestado a outros clientes", "tem garantia do FGC"],
       "O saldo em conta de pagamento é separado do patrimônio da IP; não é emprestado e não é coberto pelo FGC."),
    QK("Uma IP quer passar a oferecer empréstimo pessoal aos clientes. O caminho regular é:",
       "parceria com instituição financeira ou nova autorização", ["emprestar o saldo das contas", "só pedir registro na CVM"],
       "Crédito é atividade de instituição financeira: a IP precisa de um parceiro autorizado ou de outra autorização "
       "do Banco Central.", step=3),
    PV("Sobre as instituições de pagamento (IPs), é correto afirmar que",
       "gerem contas de pagamento e emitem pré-pagos, mas não emprestam.",
       ["captam depósitos a prazo do público, como os bancos comerciais e múltiplos.",
        "não precisam de autorização do Banco Central em nenhum caso.",
        "têm o saldo das contas de pagamento garantido pelo FGC.",
        "podem aplicar o saldo dos clientes em empréstimos a terceiros."],
       "IP faz serviços de pagamento; não capta depósito, não empresta e o saldo dos clientes não tem FGC. Conforme o "
       "porte, depende de autorização do BC."),
   ]),
  dict(title="Criptoativos", subtopic="criptoativos",
   summary="Criptoativos (como o bitcoin) não são emitidos nem garantidos por um banco central e não têm lastro: o valor "
           "depende da confiança de quem os negocia e oscila muito. Pela Lei 14.478/2022, as prestadoras de serviços de "
           "ativos virtuais (corretoras de cripto) dependem de autorização, e o Banco Central é o regulador. Isso não "
           "torna o criptoativo moeda oficial nem garante o investimento.",
   items=[
    A1("Exemplo: o real é emitido e garantido pelo Banco Central; o bitcoin não tem emissor central nem lastro. Um "
       "cliente pergunta se o bitcoin tem garantia de conversão em reais. A resposta é:",
       "não, o valor depende da confiança do mercado", ["sim, pelo Banco Central", "sim, pelo FGC até R$ 250 mil"],
       "Criptoativo não tem garantia de conversão nem lastro; o preço é definido pela oferta e procura."),
    TF("Pela Lei 14.478/2022, as corretoras de criptoativos (prestadoras de serviços de ativos virtuais) passaram a "
       "depender de autorização para funcionar, e o Banco Central é o regulador.", True,
       "O marco legal dos criptoativos regula quem presta serviços com esses ativos; não garante o ativo em si."),
    QK("Um investidor perdeu 40% num criptoativo em uma semana. Ele pode acionar o FGC?",
       "não, cripto não tem cobertura do FGC", ["sim, até R$ 250 mil", "sim, se usou corretora autorizada"],
       "O FGC cobre depósitos e alguns títulos bancários, em caso de quebra da instituição; não cobre perda de valor "
       "de criptoativos."),
    QK("Ao orientar um cliente conservador interessado em criptoativos, o técnico deve destacar principalmente:",
       "a alta volatilidade e a falta de garantia", ["a isenção de imposto de renda", "a garantia do Banco Central"],
       "Para um perfil conservador, o ponto central é o risco: o preço oscila muito e não há garantia.", step=3),
    PV("Sobre os criptoativos no Brasil, assinale a afirmativa correta.",
       "Não têm emissor central nem lastro.",
       ["São moeda de curso legal, como o real.", "Contam com a garantia do FGC quando comprados em corretora.",
        "São emitidos e garantidos pelo Banco Central.", "Têm valor fixado diariamente pela CVM."],
       "Criptoativos não são moeda oficial, não têm garantia do FGC nem do Banco Central e não têm preço fixado por "
       "nenhum órgão."),
   ]),
 ]),
]
