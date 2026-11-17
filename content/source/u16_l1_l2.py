# Unidade 1 do Técnico Bancário (u16), lições 1 e 2. Gerado para o app por tools/gen_u16.py.
# Em cada exercício a alternativa certa vem PRIMEIRO; o gerador espalha a posição. Conteúdo de IA (não revisado).

def A1(stem, c, w, exp, diff=1, style="caso_pratico"):
    return dict(type="quick", level="aprender", step=1, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def TF(stem, true, exp, diff=1, style="conceitual"):
    return dict(type="truefalse", level="aprender", step=2, stem=stem, true=true, exp=exp, diff=diff, style=style)

def QK(stem, c, w, exp, step=2, diff=2, style="caso_pratico"):
    return dict(type="quick", level="praticar", step=step, stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)

def MT(stem, pairs, exp, step=2, diff=2):
    return dict(type="match", level="praticar", step=step, stem=stem, pairs=pairs, exp=exp, diff=diff, style="conceitual")

def PV(stem, c, w, exp, diff=2, style="conceitual", t="mcq5"):
    return dict(type=t, level="prova", stem=stem, correct=c, wrong=w, exp=exp, diff=diff, style=style)


LESSONS = [
 dict(order=1, title="Sistema Financeiro Nacional", concepts=[
  dict(title="Estrutura do SFN", subtopic="normativos, supervisores e operadores",
   summary="O SFN tem três níveis. Normativos fazem as regras: CMN (moeda, crédito e mercado de capitais), CNSP (seguros "
           "privados) e CNPC (previdência fechada). Supervisores fiscalizam: Banco Central e CVM (sob o CMN), Susep (sob o "
           "CNSP) e Previc (sob o CNPC). Operadores fazem os negócios: bancos, cooperativas, corretoras, seguradoras.",
   items=[
    A1("Exemplo: o CMN cria a regra, o Banco Central fiscaliza se ela é cumprida e o banco que capta e empresta é "
       "operador. Um banco abre uma agência e passa a captar depósitos. No SFN, ele é:",
       "operador", ["órgão normativo", "órgão supervisor"],
       "Quem faz as operações com o público (captar, emprestar, intermediar) é operador. Normativos fazem regras e "
       "supervisores fiscalizam."),
    TF("Os supervisores ficam sob um normativo: Banco Central e CVM sob o CMN, Susep sob o CNSP. Por essa estrutura, "
       "a Susep responde ao CNSP, e não ao CMN.", True,
       "A Susep fiscaliza seguros, capitalização e previdência aberta seguindo as regras do Conselho Nacional de Seguros "
       "Privados (CNSP)."),
    MT("Associe cada entidade ao seu papel no SFN:",
       [("CMN", "Normativo da moeda e do crédito"), ("CVM", "Supervisora do mercado de valores mobiliários"),
        ("Susep", "Supervisora de seguros e capitalização"), ("Cooperativa de crédito", "Operadora")],
       "CMN dita as regras; CVM e Susep fiscalizam cada um o seu mercado; a cooperativa opera, atendendo os cooperados."),
    QK("Uma seguradora lança um plano de previdência aberta, vendido a qualquer pessoa. Quem fiscaliza essa seguradora?",
       "Susep", ["Previc", "CVM"],
       "Previdência aberta (seguradoras e entidades abertas) é fiscalizada pela Susep. A Previc cuida da previdência "
       "fechada, os fundos de pensão.", step=3),
    PV("Na estrutura do Sistema Financeiro Nacional, são órgãos normativos:",
       "CMN, CNSP e CNPC.",
       ["CMN, Banco Central e CVM.", "Banco Central, CVM e Susep.", "CNSP, Susep e Previc.", "CMN, Copom e Febraban."],
       "Os três conselhos (CMN, CNSP e CNPC) são os normativos. Banco Central, CVM, Susep e Previc são supervisores; o "
       "Copom é comitê do Banco Central e a Febraban é associação dos bancos.", style="literal"),
   ]),
  dict(title="Conselho Monetário Nacional", subtopic="CMN",
   summary="O CMN é o órgão máximo do SFN: fixa as diretrizes das políticas monetária, de crédito e cambial e define a "
           "meta de inflação. É formado pelo Ministro da Fazenda, que o preside, pelo Ministro do Planejamento e "
           "Orçamento e pelo Presidente do Banco Central. O CMN decide; quem executa é o Banco Central.",
   items=[
    A1("Exemplo: o CMN decide a regra (a meta de inflação) e o Banco Central a executa. Quem define a meta de inflação "
       "que o Banco Central deve perseguir?",
       "o CMN", ["o Copom", "a Febraban"],
       "A meta de inflação é fixada pelo CMN. O Copom define a Selic para cumprir essa meta; a Febraban é associação "
       "de bancos."),
    TF("O CMN tem três membros: o Ministro da Fazenda (presidente), o Ministro do Planejamento e Orçamento e o Presidente "
       "do Banco Central. Logo, o CMN é presidido pelo Presidente do Banco Central.", False,
       "Quem preside o CMN é o Ministro da Fazenda; o Presidente do Banco Central é um dos membros."),
    QK("O governo quer mudar uma regra geral sobre crédito que vale para todas as instituições financeiras. Normas "
       "desse tipo costumam sair como:",
       "resolução do CMN", ["circular da Febraban", "ata do Copom"],
       "O CMN edita resoluções com as regras gerais do sistema. Atas do Copom tratam da Selic; a Febraban não edita "
       "normas oficiais."),
    MT("Associe cada órgão ao que ele faz:",
       [("CMN", "Fixa diretrizes e a meta de inflação"), ("Copom", "Define a meta da Selic"),
        ("Banco Central", "Executa a política monetária"), ("CVM", "Fiscaliza o mercado de valores mobiliários")],
       "Cada órgão tem seu papel: o CMN decide as diretrizes, o Copom fixa a Selic, o Banco Central executa e a CVM "
       "cuida do mercado de capitais.", step=3),
    PV("O Conselho Monetário Nacional (CMN) definiu a meta de inflação dos próximos anos. Sobre esse órgão, é correto "
       "afirmar que",
       "é presidido pelo Ministro da Fazenda e inclui o Presidente do Banco Central.",
       ["executa diretamente as operações de mercado aberto para manter a taxa Selic na meta.",
        "é formado pelos diretores do Banco Central e se reúne a cada quarenta e cinco dias.",
        "fiscaliza as seguradoras e as entidades de previdência complementar aberta do país.",
        "tem como membros os presidentes dos bancos públicos federais e da Febraban."],
       "O CMN tem três membros (Fazenda, Planejamento e Banco Central). Quem opera o mercado aberto é a mesa do Banco "
       "Central; o comitê de diretores que se reúne a cada 45 dias é o Copom; seguros e previdência aberta ficam com a "
       "Susep.", style="literal"),
   ]),
  dict(title="Banco Central do Brasil", subtopic="funções e autonomia",
   summary="O Banco Central executa a política monetária, emite a moeda, guarda as reservas dos bancos (banco dos "
           "bancos), regula e fiscaliza as instituições financeiras e administra o sistema de pagamentos. Pela Lei "
           "Complementar 179/2021, tem autonomia: presidente e diretores com mandatos fixos de 4 anos, não coincidentes "
           "com o do Presidente da República. Sua missão inclui a estabilidade do poder de compra da moeda e um sistema "
           "financeiro sólido e eficiente.",
   items=[
    A1("Exemplo: quando falta dinheiro a um banco no fim do dia, ele pode recorrer ao Banco Central, que funciona como "
       "banco dos bancos. Qual destas funções também é do Banco Central?",
       "emitir o papel-moeda", ["fixar a meta de inflação", "fiscalizar as seguradoras"],
       "O Banco Central emite a moeda. A meta de inflação é do CMN e as seguradoras são fiscalizadas pela Susep."),
    TF("A Lei Complementar 179/2021 deu mandatos fixos de 4 anos ao presidente e aos diretores do Banco Central. Por "
       "isso, um novo Presidente da República não troca a diretoria do BC logo que assume.", True,
       "Os mandatos não coincidem com o do Presidente da República: essa é a base da autonomia do Banco Central."),
    QK("Um banco descumpre uma regra prudencial e sofre processo administrativo. Qual órgão fiscaliza e pune o banco?",
       "o Banco Central", ["o CMN", "o Copom"],
       "Fiscalizar e punir as instituições financeiras é papel do Banco Central. O CMN faz as regras e o Copom fixa a "
       "Selic."),
    QK("Ao explicar o papel do Banco Central a um cliente, o técnico deve dizer que a missão do BC é garantir:",
       "a estabilidade do poder de compra da moeda", ["o lucro dos bancos públicos", "os juros de cada contrato"],
       "A missão do Banco Central é a estabilidade da moeda e um sistema financeiro sólido; ele não define o juro de "
       "cada contrato nem garante lucro de bancos.", step=3),
    PV("Sobre o Banco Central do Brasil, assinale a afirmativa correta.",
       "Executa a política monetária e fiscaliza os bancos.",
       ["Define a meta de inflação, que depois é só homologada pelo CMN.",
        "Supervisiona as seguradoras, junto com a Susep.",
        "Regula as ofertas públicas de ações, no lugar da CVM.",
        "Tem a diretoria trocada a cada novo Presidente da República."],
       "O BC executa a política monetária e supervisiona as instituições financeiras. A meta de inflação é do CMN, "
       "seguros são da Susep, ações são da CVM e, desde a LC 179/2021, os mandatos não coincidem com o presidencial."),
   ]),
  dict(title="Instituições bancárias e não bancárias", subtopic="tipos de instituição",
   summary="Instituições bancárias (monetárias) recebem depósitos à vista e, ao emprestar, criam moeda: bancos "
           "comerciais, bancos múltiplos com carteira comercial, caixas econômicas e cooperativas de crédito. As não "
           "bancárias não captam depósito à vista: bancos de investimento, financeiras (sociedades de crédito, "
           "financiamento e investimento), sociedades de crédito imobiliário e de arrendamento mercantil, corretoras e "
           "distribuidoras.",
   items=[
    A1("Exemplo: o banco comercial recebe depósitos à vista (conta corrente), por isso é instituição bancária; a "
       "financeira capta só a prazo e não é. Uma cooperativa de crédito que mantém contas à vista dos cooperados é:",
       "instituição bancária", ["instituição não bancária", "órgão supervisor"],
       "Cooperativas de crédito captam depósitos à vista dos cooperados: entram no grupo das instituições bancárias."),
    TF("A sociedade de arrendamento mercantil (leasing) é instituição não bancária. Portanto, ela capta depósitos à "
       "vista, como um banco comercial.", False,
       "Instituição não bancária justamente não capta depósitos à vista. O leasing se financia de outras formas."),
    MT("Associe cada instituição à sua característica:",
       [("Caixa econômica", "Bancária, capta depósitos à vista"), ("Banco de investimento", "Financia capital de giro e fixo"),
        ("Financeira (SCFI)", "Crédito direto ao consumidor"), ("Corretora", "Intermedeia títulos e valores mobiliários")],
       "Caixa econômica é bancária; banco de investimento, financeira e corretora são não bancárias, cada uma com seu "
       "foco de negócio."),
    QK("Uma empresa precisa de financiamento de longo prazo para máquinas e quer contratar com instituição que não "
       "capta depósito à vista. Qual destas atende ao critério?",
       "um banco de investimento", ["uma caixa econômica", "um banco comercial"],
       "Banco de investimento financia empresas a médio e longo prazo e não capta depósitos à vista. Caixa econômica e "
       "banco comercial captam.", step=3),
    PV("Entre as instituições a seguir, é instituição não bancária, isto é, não capta depósitos à vista:",
       "a sociedade de crédito imobiliário.",
       ["o banco comercial.", "a caixa econômica.", "a cooperativa de crédito.", "o banco múltiplo com carteira comercial."],
       "A sociedade de crédito imobiliário financia imóveis com recursos a prazo e não recebe depósitos à vista. As "
       "demais captam depósitos à vista e são bancárias."),
   ]),
  dict(title="Banco múltiplo", subtopic="carteiras",
   summary="Banco múltiplo é uma única instituição com pelo menos duas carteiras: comercial, de investimento, de crédito "
           "imobiliário, de crédito, financiamento e investimento, ou de arrendamento mercantil. Uma delas tem de ser "
           "comercial ou de investimento. Com carteira comercial, pode captar depósitos à vista.",
   items=[
    A1("Exemplo: um banco com carteiras comercial e de crédito imobiliário é múltiplo: tem duas carteiras e uma é "
       "comercial. Qual combinação também forma um banco múltiplo?",
       "investimento e arrendamento mercantil", ["crédito imobiliário e arrendamento", "só a carteira comercial"],
       "Investimento + arrendamento são duas carteiras, e uma delas (investimento) é das obrigatórias. Só uma carteira "
       "não basta, e imobiliário + arrendamento não têm comercial nem investimento."),
    TF("Para ser banco múltiplo, basta ter duas carteiras quaisquer, como crédito imobiliário e arrendamento mercantil.",
       False,
       "Além de ter duas carteiras, uma delas precisa ser comercial ou de investimento."),
    QK("Um grupo quer criar um banco múltiplo com as carteiras de crédito imobiliário e de crédito, financiamento e "
       "investimento. O pedido:",
       "não atende, falta carteira comercial ou de investimento", ["atende, porque são duas carteiras", "atende, se tiver agências"],
       "As duas carteiras escolhidas não incluem a comercial nem a de investimento, exigidas para o banco múltiplo."),
    QK("Um banco múltiplo tem as carteiras de investimento e de financiamento, mas não a comercial. Ele pode oferecer "
       "conta corrente (depósito à vista)?",
       "não, isso exige carteira comercial", ["sim, todo banco múltiplo pode", "sim, se tiver três carteiras"],
       "Captar depósitos à vista é próprio da carteira comercial; sem ela, o banco múltiplo não oferece conta corrente.",
       step=3),
    PV("O grupo financeiro Delta quer reunir suas operações num banco múltiplo. Entre as combinações de carteiras a "
       "seguir, a única que atende às regras é",
       "comercial e crédito imobiliário.",
       ["crédito imobiliário e arrendamento mercantil.", "crédito, financiamento e investimento, apenas.",
        "arrendamento mercantil e corretora de câmbio.", "crédito imobiliário, apenas."],
       "São necessárias pelo menos duas carteiras, uma delas comercial ou de investimento. Só a combinação comercial + "
       "crédito imobiliário cumpre as duas condições (corretora de câmbio nem é carteira de banco múltiplo).",
       style="caso_pratico"),
   ]),
 ]),

 dict(order=2, title="Produtos e serviços bancários", concepts=[
  dict(title="Contas e poupança", subtopic="conta corrente e poupança",
   summary="Na conta corrente (depósito à vista) o dinheiro é movimentado livremente. A poupança rende a TR mais 0,5% ao "
           "mês quando a meta da Selic é maior que 8,5% ao ano, ou a TR mais 70% da meta da Selic quando ela está em 8,5% "
           "ou menos. Para pessoa física é isenta de imposto de renda, e o rendimento é creditado no aniversário mensal "
           "do depósito.",
   items=[
    A1("Exemplo: com a meta da Selic em 10% ao ano (acima de 8,5%), a poupança rende TR + 0,5% ao mês. Se a meta da "
       "Selic for 7% ao ano, o adicional da poupança passa a ser:",
       "70% da meta da Selic", ["0,5% ao mês, sempre", "100% do CDI"],
       "Com a meta em até 8,5% ao ano, o adicional é 70% da meta da Selic: 4,9% ao ano, além da TR."),
    TF("A poupança de pessoa física paga imposto de renda pela tabela regressiva, como o CDB.", False,
       "O rendimento da poupança é isento de IR para pessoa física. O CDB é que segue a tabela regressiva."),
    QK("Um cliente depositou na poupança no dia 10. Quando o rendimento desse depósito é creditado?",
       "no dia 10 do mês seguinte", ["no último dia útil do mês", "todos os dias, como no CDB"],
       "A poupança rende uma vez por mês, na data de aniversário do depósito."),
    QK("Com a meta da Selic em 12% ao ano, quanto a poupança rende além da TR?",
       "0,5% ao mês", ["70% de 12% ao ano", "12% ao ano"],
       "Meta acima de 8,5%: vale a regra antiga, TR + 0,5% ao mês.", step=3),
   ]),
  dict(title="Cartões e meios de pagamento", subtopic="cartões e rotativo",
   summary="No cartão de crédito o cliente compra até o limite e paga na fatura; pagar menos que o total leva o saldo ao "
           "crédito rotativo, que só pode ser usado até a fatura seguinte; depois, o banco deve oferecer um parcelamento "
           "com juros menores. No cartão de débito o valor sai da conta na hora. O pré-pago usa saldo carregado antes.",
   items=[
    A1("Exemplo: a fatura é de R$ 1.000. Pagando R$ 1.000 não há juros; pagando só R$ 300, os R$ 700 restantes entram "
       "no crédito rotativo. Um cliente com fatura de R$ 2.000 paga R$ 2.000 na data. Ele entra no rotativo?",
       "não, ele pagou o total", ["sim, sempre que usa o cartão", "sim, se parcelou alguma compra"],
       "Só entra no rotativo o saldo da fatura que não foi pago. Quem paga o total em dia não paga juros."),
    TF("No cartão de débito, o valor da compra sai da conta corrente do cliente no momento da compra.", True,
       "O débito usa o saldo da conta na hora; quem paga depois, na fatura, é o crédito."),
    MT("Associe cada forma de pagamento à sua característica:",
       [("Cartão de crédito", "Paga depois, na fatura"), ("Cartão de débito", "Sai da conta na hora"),
        ("Cartão pré-pago", "Usa saldo carregado antes"), ("Crédito rotativo", "Saldo da fatura não pago")],
       "Crédito é pagamento posterior; débito é imediato; o pré-pago depende de recarga; o rotativo financia o que "
       "ficou da fatura."),
    QK("Uma cliente pagou só o mínimo da fatura de março. Em abril, o saldo que estava no rotativo e continua sem "
       "pagamento:",
       "deve ir para um parcelamento com juros menores", ["fica no rotativo sem prazo", "é perdoado pelo banco"],
       "O rotativo só pode ser usado até o vencimento da fatura seguinte; depois, o banco deve oferecer parcelamento "
       "em condições mais vantajosas.", step=3),
   ]),
  dict(title="Crédito e CET", subtopic="linhas de crédito",
   summary="Linhas comuns: crédito pessoal, consignado (parcela descontada na folha ou no benefício, por isso juros "
           "menores), CDC (financia a compra de um bem) e cheque especial (limite automático na conta, juros altos). "
           "Antes de contratar, o banco deve informar o Custo Efetivo Total (CET), que soma juros, tarifas, tributos e "
           "seguros. É o CET que permite comparar ofertas.",
   items=[
    A1("Exemplo: a oferta A tem juros de 2% ao mês e tarifas altas; a B, 2,1% sem tarifas. Para comparar, use o CET, "
       "que soma juros e todos os encargos. Qual número o cliente deve comparar entre as propostas?",
       "o CET de cada proposta", ["só a taxa de juros", "o valor da primeira parcela"],
       "O CET inclui juros, tarifas, impostos e seguros; a taxa de juros sozinha pode esconder custos."),
    TF("No consignado a parcela é descontada direto da folha de pagamento ou do benefício. Isso reduz o risco do banco "
       "e costuma baratear os juros.", True,
       "Com o desconto em folha, a inadimplência é menor e o banco pode cobrar juros menores que no crédito pessoal."),
    MT("Associe cada linha de crédito à sua característica:",
       [("Consignado", "Parcela descontada na folha"), ("CDC", "Financia a compra de um bem"),
        ("Cheque especial", "Limite automático na conta"), ("Crédito pessoal", "Empréstimo sem destino definido")],
       "Cada linha tem uma garantia e um uso; o cheque especial é das mais caras e deve ser usado por pouco tempo."),
    QK("Um cliente usa o cheque especial há meses para fechar o mês. Qual a orientação mais adequada?",
       "trocar a dívida por uma linha mais barata", ["manter, pois não tem parcelas", "aumentar o limite do cheque especial"],
       "Trocar uma dívida cara (cheque especial) por uma mais barata, como consignado ou crédito pessoal, reduz o custo; "
       "aumentar o limite só aumenta a dívida.", step=3),
   ]),
  dict(title="Investimentos bancários", subtopic="CDB, LCI, LCA e IR",
   summary="CDB e RDB são depósitos a prazo emitidos por bancos; o CDB pode ser negociado com outro investidor, o RDB "
           "não. LCI e LCA têm lastro em crédito imobiliário e do agronegócio e são isentas de IR para pessoa física. "
           "Letras de câmbio são emitidas por financeiras. CDB e letras de câmbio pagam IR pela tabela regressiva: 22,5% "
           "até 180 dias, 20% até 360, 17,5% até 720 e 15% acima disso.",
   items=[
    A1("Exemplo: um CDB resgatado com 100 dias paga 22,5% de IR sobre o rendimento; com 800 dias, 15%. Um CDB "
       "resgatado com 500 dias paga:",
       "17,5%", ["22,5%", "15%"],
       "500 dias está na faixa de 361 a 720 dias: alíquota de 17,5%."),
    TF("Para pessoa física, LCI e LCA são isentas de imposto de renda.", True,
       "A isenção vale para pessoa física; por isso LCI e LCA costumam pagar taxas nominais menores que o CDB."),
    QK("Um cliente quer um título bancário que possa vender para outro investidor antes do vencimento. Entre CDB e RDB:",
       "o CDB pode ser negociado", ["o RDB pode ser negociado", "nenhum dos dois"],
       "O CDB é transferível; o RDB é nominativo e não pode ser negociado com terceiros."),
    MT("Associe cada aplicação à sua característica:",
       [("LCA", "Lastro no agronegócio"), ("LCI", "Lastro em crédito imobiliário"),
        ("RDB", "Não pode ser transferido"), ("Letra de câmbio", "Emitida por financeiras")],
       "LCA e LCI se diferenciam pelo lastro; o RDB não circula; a letra de câmbio é o título típico das financeiras.",
       step=3),
    PV("Um cliente aplicou R$ 10.000,00 num CDB e resgatou após 400 dias, com rendimento bruto de R$ 1.200,00. O "
       "imposto de renda retido foi de",
       "R$ 210,00.", ["R$ 180,00.", "R$ 240,00.", "R$ 270,00.", "R$ 1.750,00."],
       "400 dias cai na faixa de 361 a 720 dias, alíquota de 17,5%, aplicada só sobre o rendimento: "
       "0,175 × 1.200 = R$ 210,00.", style="caso_pratico"),
   ]),
  dict(title="Fundo Garantidor de Créditos", subtopic="FGC",
   summary="O FGC é uma entidade privada, mantida pelas instituições associadas, que devolve ao cliente depósitos e "
           "alguns títulos quando a instituição sofre intervenção, liquidação ou falência. Cobre até R$ 250 mil por CPF "
           "ou CNPJ em cada instituição ou conglomerado, com teto de R$ 1 milhão a cada 4 anos. Garante conta corrente, "
           "poupança, CDB, RDB, LCI, LCA e letras de câmbio; não garante fundos de investimento, ações, títulos públicos "
           "nem previdência.",
   items=[
    A1("Exemplo: quem tem R$ 300 mil em CDB de um banco que quebra recebe do FGC até R$ 250 mil. Um cliente tem R$ 100 "
       "mil na poupança e R$ 200 mil em CDB no mesmo banco, que é liquidado. Quanto o FGC garante?",
       "R$ 250 mil", ["R$ 300 mil", "R$ 100 mil"],
       "O limite é de R$ 250 mil por CPF na instituição, somando todos os produtos garantidos que ele tem nela."),
    TF("Cotas de fundos de investimento têm garantia do FGC até R$ 250 mil por cotista.", False,
       "Fundos de investimento não têm cobertura do FGC: o risco das aplicações do fundo é do cotista."),
    QK("Uma cliente quer aplicar R$ 400 mil com garantia do FGC sobre todo o valor. Ela deve:",
       "dividir entre instituições de grupos diferentes", ["deixar tudo na poupança", "aplicar num fundo de renda fixa"],
       "O limite é por instituição ou conglomerado: dividindo entre grupos diferentes, cada parcela fica coberta até "
       "R$ 250 mil."),
    QK("Qual destas aplicações não tem cobertura do FGC?",
       "título do Tesouro Direto", ["LCA", "RDB"],
       "Títulos públicos são garantidos pelo Tesouro Nacional, não pelo FGC. LCA e RDB são cobertos.", step=3),
   ]),
  dict(title="Fiança bancária", subtopic="carta de fiança",
   summary="Na fiança bancária o banco garante, por uma carta de fiança, que a empresa cliente vai cumprir uma obrigação "
           "com terceiros (um fornecedor, um contrato público, um aluguel). Se a empresa não cumprir, o banco paga e "
           "depois cobra dela. Pelo serviço, o banco cobra uma comissão da empresa afiançada.",
   items=[
    A1("Exemplo: a empresa X compra matéria-prima a prazo e o fornecedor pede garantia; o banco emite a carta de "
       "fiança. Se X não pagar, quem paga o fornecedor?",
       "o banco, que depois cobra de X", ["ninguém, a fiança é só para licitação", "o próprio fornecedor"],
       "O banco é o fiador: honra a obrigação perante o fornecedor e depois cobra o valor da empresa afiançada."),
    TF("Na fiança bancária, o banco cobra uma comissão da empresa afiançada pela garantia que presta.", True,
       "A comissão remunera o risco que o banco assume ao garantir a obrigação do cliente."),
    QK("Numa licitação, o órgão público exige garantia de que a empresa vencedora vai entregar a obra. Que produto "
       "bancário atende a essa exigência?",
       "carta de fiança bancária", ["título de capitalização", "CDB"],
       "A carta de fiança é usada como garantia em licitações e contratos; CDB e capitalização não garantem obrigações "
       "de terceiros."),
    QK("Na fiança concedida à empresa Alfa num contrato com a empresa Beta, o banco assume o risco de:",
       "Alfa não cumprir o contrato com Beta", ["Beta não pagar o banco", "o banco quebrar"],
       "O banco garante a obrigação da Alfa (afiançada) perante a Beta.", step=3),
   ]),
 ]),
]
