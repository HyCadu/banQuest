# Unidade 2 do Técnico Bancário (u17), lições 1 e 2. Gerado para o app por tools/gen_u16.py u17.
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
 dict(order=1, title="Atendimento ao cliente", concepts=[
  dict(title="Características dos serviços", subtopic="intangibilidade, inseparabilidade, variabilidade e perecibilidade",
   summary="Serviços têm quatro características. Intangibilidade: não dá para ver nem testar antes de comprar. "
           "Inseparabilidade: é produzido e consumido ao mesmo tempo, e quem atende faz parte do serviço. Variabilidade: "
           "a qualidade muda conforme quem, quando e onde se presta. Perecibilidade: não pode ser estocado; o horário "
           "vago se perde.",
   items=[
    A1("Exemplo: o caixa eletrônico parado de madrugada não guarda os atendimentos que deixou de fazer; isso é "
       "perecibilidade, porque serviço não se estoca. Na agenda da gerente Ana, um horário de consultoria que ninguém "
       "marcou ilustra qual característica?",
       "perecibilidade", ["intangibilidade", "inseparabilidade"],
       "O horário vago não pode ser guardado para outro dia: a capacidade de atender se perde, e isso é perecibilidade. "
       "Intangibilidade fala de não poder ver ou testar; inseparabilidade, de produção e consumo simultâneos."),
    TF("Variabilidade significa que a qualidade do serviço pode mudar conforme quem atende, quando e onde. Por isso, dois "
       "clientes atendidos por gerentes diferentes da agência de Ana podem ter experiências diferentes.", True,
       "É a definição de variabilidade (ou heterogeneidade): o serviço depende das pessoas e do momento, e por isso é "
       "difícil de padronizar."),
    MT("Associe cada situação da agência de Ana à característica do serviço:",
       [("O cliente não consegue testar a consultoria antes", "Intangibilidade"),
        ("A consultoria acontece com Ana e o cliente juntos", "Inseparabilidade"),
        ("Cada gerente conduz a consultoria de um jeito", "Variabilidade"),
        ("Horário vago na agenda não se recupera", "Perecibilidade")],
       "Não testar antes é intangibilidade; produzir junto com o cliente, inseparabilidade; mudar conforme quem atende, "
       "variabilidade; não poder estocar, perecibilidade."),
    QK("Para reduzir o efeito da intangibilidade, a agência de Ana pode:",
       "mostrar sinais concretos, como o ambiente",
       ["treinar todos no mesmo roteiro de atendimento", "abrir horários extras nos dias de maior movimento"],
       "Como o cliente não vê o serviço, ele julga pelos sinais tangíveis: ambiente, pessoas, folhetos, contrato claro. "
       "O roteiro único combate a variabilidade, e os horários extras lidam com a perecibilidade.", step=3),
    QK("Na agência de Ana, o mesmo atendimento de abertura de conta leva 10 minutos com um funcionário e 40 com outro. "
       "Qual característica dos serviços aparece aqui, e como reduzi-la?",
       "variabilidade; padronizar e treinar a equipe",
       ["perecibilidade; aumentar o número de caixas eletrônicos", "intangibilidade; distribuir folhetos na fila"],
       "A diferença vem de quem presta o serviço: é variabilidade, combatida com padrão de processo e treinamento.",
       step=3),
   ]),
  dict(title="Momentos da verdade", subtopic="pontos de contato",
   summary="Momento da verdade é cada contato do cliente com a empresa em que ele forma uma opinião sobre ela: na "
           "agência, no aplicativo, na central, na fila, numa reclamação. Todos contam, não só o primeiro, e dependem "
           "também das áreas internas e dos sistemas, não apenas de quem está na linha de frente.",
   items=[
    A1("Exemplo: Marcos ligou para a central do banco e foi bem atendido; foi um momento da verdade, porque ele formou "
       "uma opinião sobre o banco nesse contato. Depois, Marcos abre o aplicativo para ver o extrato. Isso também é um "
       "momento da verdade?",
       "sim, todo contato com a empresa conta", ["não, só o contato com um atendente conta", "não, só o primeiro contato conta"],
       "O aplicativo é um ponto de contato: se travar ou confundir, Marcos forma uma opinião ruim do banco do mesmo jeito."),
    TF("Os momentos da verdade dependem só dos funcionários da linha de frente; o que acontece nos sistemas e nas áreas "
       "internas não influencia a opinião de Marcos.", False,
       "Um sistema fora do ar ou um erro de cadastro feito numa área interna também viram experiência ruim para o "
       "cliente. Toda a empresa constrói os momentos da verdade."),
    QK("Marcos esperou 40 minutos na fila da agência e saiu sem ser atendido. Para o banco, esse episódio:",
       "foi um momento da verdade negativo", ["não conta, pois não houve atendimento", "só conta se ele registrar reclamação"],
       "A espera também é contato com a empresa. Marcos saiu com uma opinião pior do banco, mesmo sem falar com ninguém."),
    QK("Qual ação melhora os momentos da verdade de Marcos de forma mais ampla?",
       "cuidar de todos os pontos de contato",
       ["treinar apenas quem faz a abertura de contas", "concentrar o esforço no primeiro atendimento"],
       "Como todo contato conta, o cuidado precisa cobrir a jornada inteira: agência, app, central, cobrança, reclamação.",
       step=3),
    QK("Marcos recebeu uma ligação de cobrança por engano, de uma dívida que já tinha quitado. Esse contato:",
       "é momento da verdade e pesa contra o banco", ["não conta, pois foi feito por uma terceirizada",
                                                        "só pesa se ele mudar de banco"],
       "Mesmo feito por outra área ou empresa contratada, o contato fala em nome do banco e forma a opinião de Marcos.",
       step=3),
   ]),
  dict(title="Qualidade e satisfação", subtopic="expectativa e percepção",
   summary="A satisfação nasce da comparação entre o que o cliente percebeu do serviço e o que ele esperava. Percebeu "
           "menos que o esperado: insatisfeito. O mesmo: satisfeito. Mais: encantado. Prometer além do que se entrega "
           "aumenta a expectativa e gera frustração.",
   items=[
    A1("Exemplo: Lúcia esperava receber o cartão em 10 dias e recebeu em 5; o que ela percebeu superou o que esperava, "
       "e ela ficou encantada. Se o gerente tivesse prometido 3 dias e o cartão chegasse em 5, Lúcia ficaria:",
       "insatisfeita, pois esperava mais", ["encantada, pois 5 dias é rápido", "indiferente, pois o prazo foi o mesmo"],
       "O prazo real foi igual nos dois casos; mudou a expectativa. Com a promessa de 3 dias, 5 dias ficam abaixo do "
       "esperado."),
    TF("Prometer a Lúcia mais do que o banco consegue entregar costuma aumentar a satisfação dela, porque eleva a "
       "expectativa.", False,
       "Expectativa alta que não se cumpre vira insatisfação. Promessa realista, cumprida ou superada, é o que satisfaz."),
    QK("Lúcia esperava ser atendida em 15 minutos e foi atendida em 15 minutos, com cordialidade. O resultado mais "
       "provável é:",
       "satisfação: percebido igual ao esperado",
       ["encantamento: o serviço superou o esperado", "insatisfação: não houve surpresa"],
       "Percepção igual à expectativa gera satisfação. Encantar exige superar o esperado."),
    QK("O gerente quer melhorar a satisfação de Lúcia sem gastar mais. Qual atitude ajuda mais?",
       "informar prazos realistas e cumpri-los",
       ["prometer o prazo mais curto possível", "evitar falar de prazos com a cliente agora"],
       "Prazo realista ajusta a expectativa, e o cumprimento garante que a percepção fique à altura dela.", step=3),
    QK("Lúcia diz que o atendimento foi rápido, mas saiu sem entender as tarifas do pacote que contratou. Quanto à "
       "qualidade percebida:",
       "a rapidez não compensa a falta de clareza",
       ["o atendimento foi excelente, pois foi rápido", "a qualidade só depende do preço das tarifas"],
       "A cliente avalia o serviço inteiro. Se a necessidade dela (entender o que contratou) não foi atendida, a "
       "percepção cai, por mais rápido que tenha sido.", step=3),
   ]),
  dict(title="Comunicação e técnicas de atendimento", subtopic="escuta ativa, presencial e digital",
   summary="Escuta ativa: ouvir sem interromper e confirmar o que entendeu com as próprias palavras. Linguagem clara, sem "
           "jargão, adaptada a cada cliente. Perguntas abertas (o quê, por quê, como) fazem o cliente contar a "
           "necessidade. No digital vale o mesmo cuidado: cordialidade, objetividade e o passo a passo da solução.",
   items=[
    A1("Exemplo: na escuta ativa o atendente ouve sem interromper e confirma o que entendeu: 'Então a senhora quer um "
       "lugar seguro para guardar o 13º, é isso?'. Dona Rosa conta a Paulo que quer ajudar o neto a pagar a faculdade. "
       "Qual resposta de Paulo segue a escuta ativa?",
       "Se entendi, quer guardar para a faculdade dele?",
       ["Temos um ótimo título de capitalização hoje.", "A senhora deveria pensar antes na sua aposentadoria."],
       "Confirmar com as próprias palavras mostra que Paulo ouviu e evita oferecer algo que não atende Dona Rosa."),
    TF("Usar termos como 'liquidez' e 'marcação a mercado' com qualquer cliente transmite segurança e melhora a "
       "comunicação de Paulo com Dona Rosa.", False,
       "Jargão afasta e confunde. A linguagem deve ser clara e adaptada a quem está sendo atendido."),
    QK("Perguntas abertas fazem o cliente falar mais do que 'sim' ou 'não'. Paulo quer descobrir o que Dona Rosa espera "
       "do investimento. Qual pergunta ajuda mais?",
       "O que a senhora pretende fazer com esse dinheiro?",
       ["A senhora quer aplicar tudo na poupança, não é isso?", "A senhora já tem conta aqui no banco?"],
       "A pergunta aberta deixa Dona Rosa contar objetivo e prazo. As outras se respondem com sim ou não e induzem a "
       "resposta."),
    QK("À noite, Dona Rosa escreve no chat do banco: 'Não consigo ver meu saldo'. No atendimento digital, a melhor "
       "resposta é:",
       "cordial, objetiva e com o passo a passo",
       ["curta, apenas com o link da central de ajuda", "formal, pedindo que ela vá até a agência"],
       "O canal digital pede o mesmo cuidado do presencial: acolher, ser claro e resolver ali mesmo, quando possível.",
       step=3),
    QK("Paulo atende Dona Rosa e o celular dele toca várias vezes. Ele olha as mensagens enquanto ela fala. Essa "
       "atitude:",
       "prejudica a escuta e passa desinteresse",
       ["é aceitável, se ele responder às mensagens depois", "mostra agilidade no atendimento"],
       "Escuta ativa exige atenção total: olhar para o cliente, não se distrair e mostrar que está acompanhando.", step=3),
   ]),
  dict(title="Ética e conduta no atendimento", subtopic="transparência, venda casada e sigilo",
   summary="Os Códigos de Ética e de Conduta registram as regras de comportamento da empresa, inclusive a etiqueta "
           "empresarial no trato com colegas e clientes. No atendimento: informar custos e riscos com clareza, não "
           "induzir a erro, respeitar o sigilo dos dados e não condicionar um produto à compra de outro (venda casada, "
           "proibida pelo Código de Defesa do Consumidor).",
   items=[
    A1("Exemplo: condicionar a liberação de um empréstimo à compra de um seguro é venda casada, prática proibida pelo "
       "Código de Defesa do Consumidor. Bia diz a um cliente que só abre a conta se ele contratar também um título de "
       "capitalização. Isso é:",
       "venda casada, prática proibida", ["oferta cruzada, prática correta", "fidelização, prática recomendada"],
       "Bia condicionou a conta a outro produto. Oferecer é permitido; exigir a compra conjunta é venda casada."),
    TF("Oferecer um seguro a quem contrata um empréstimo, deixando claro que o cliente pode recusar, é venda casada.",
       False,
       "Ofertar outro produto é permitido. A venda casada existe quando um produto só é liberado se o cliente comprar "
       "o outro."),
    QK("Um cliente pergunta a Bia o saldo da conta do vizinho, 'só para saber se ele pode pagar uma dívida'. Bia deve:",
       "recusar, pois os dados são sigilosos",
       ["informar, se o valor for pequeno", "informar, se o vizinho for cliente antigo"],
       "Os dados bancários de um cliente não podem ser passados a terceiros, qualquer que seja o motivo."),
    QK("Bia vai oferecer um CDB a um cliente. Para agir com transparência, ela deve:",
       "explicar rendimento, prazo, liquidez e custos",
       ["destacar só o rendimento, que é o que atrai", "dizer que o rendimento sempre supera a inflação"],
       "Transparência é mostrar tudo o que pesa na decisão. Esconder prazo ou custo, ou prometer ganho que não é "
       "garantido, induz o cliente a erro.", step=3),
    QK("A agência de Bia tem meta alta de seguros no mês. Um cliente sem dependentes e com renda apertada pede só um "
       "cartão. A conduta ética é:",
       "atender o pedido e ofertar só o que servir",
       ["incluir o seguro de vida no pacote do cartão", "dizer que o cartão sai mais rápido com o seguro"],
       "A meta não justifica empurrar produto inadequado nem condicionar o cartão ao seguro. A oferta deve servir à "
       "necessidade do cliente.", step=3),
   ]),
 ]),

 dict(order=2, title="Comportamento do consumidor", concepts=[
  dict(title="Valor percebido", subtopic="benefícios e custos",
   summary="Para o cliente, um serviço tem valor quando os benefícios que ele percebe (financeiros, práticos e "
           "emocionais, como tranquilidade) superam os custos que ele tem (preço, tempo, esforço e aborrecimento). "
           "O mais barato nem sempre é o de maior valor.",
   items=[
    A1("Exemplo: para Carla, o pacote A custa R$ 30, mas evita idas à agência e dá tranquilidade; ela sente que ganha "
       "mais do que paga. O pacote B custa R$ 10, mas obriga Carla a enfrentar fila toda semana, o que ela detesta. "
       "Para Carla, o pacote B:",
       "pode ter menos valor, apesar de mais barato",
       ["tem mais valor, pois é mais barato", "tem o mesmo valor, pois os serviços são iguais"],
       "O valor soma todos os custos, inclusive tempo e aborrecimento. A fila semanal pesa para Carla mais que os R$ 20 "
       "de diferença."),
    TF("No valor que Carla percebe num serviço entram também custos não financeiros, como o tempo gasto e o "
       "aborrecimento.", True,
       "Custos de tempo, esforço e emoção entram na conta junto com o preço."),
    QK("Carla compara dois cartões sem anuidade. O cartão X tem aplicativo confuso e atendimento demorado; o Y resolve "
       "tudo pelo app em poucos minutos. Pelo conceito de valor percebido, ela tende a preferir:",
       "o Y, que lhe custa menos tempo e esforço",
       ["o X, pois o preço dos dois é igual", "qualquer um, pois a anuidade dos dois é zero"],
       "Com preço igual, decide o resto da conta: o Y entrega mais e custa menos tempo e aborrecimento."),
    QK("O banco de Carla quer aumentar o valor percebido do aplicativo sem baixar tarifas. Qual ação funciona?",
       "reduzir o tempo e o esforço para fazer um Pix",
       ["aumentar a publicidade do aplicativo na televisão", "trocar o app por outro com os mesmos passos"],
       "Valor sobe quando o benefício aumenta ou o custo do cliente cai. Menos passos reduzem tempo e esforço.", step=3),
    QK("Carla paga R$ 25 por mês de tarifa e diz que vale a pena, porque o gerente resolve seus problemas na hora. Uma "
       "amiga, com o mesmo pacote, acha caro. Isso mostra que o valor:",
       "depende da percepção de cada cliente",
       ["é fixado pela tabela de tarifas do banco", "é igual para todos que pagam o mesmo"],
       "O valor é percebido: cada cliente pesa benefícios e custos de um jeito, por isso a mesma oferta agrada um e não "
       "agrada outro.", step=3),
   ]),
  dict(title="Vantagem competitiva", subtopic="diferenciais",
   summary="Vantagem competitiva é o diferencial que faz o cliente preferir uma empresa às concorrentes, porque ela cria "
           "mais valor para ele. Dura mais quando é difícil de copiar, como relacionamento e conhecimento do cliente. As "
           "estratégias mais citadas para obtê-la são custo baixo, diferenciação e foco num nicho.",
   items=[
    A1("Exemplo: o Banco Litoral conhece de perto os produtores rurais da região e libera crédito de safra mais rápido "
       "que os concorrentes; esse diferencial, que faz os produtores o preferirem, é uma vantagem competitiva. Uma "
       "cooperativa que atende pescadores com horário adaptado à rotina deles, e por isso é a preferida da categoria, "
       "tem:",
       "uma vantagem competitiva", ["uma ameaça de mercado", "um custo emocional"],
       "O horário adaptado cria valor para os pescadores e faz a cooperativa ser preferida: é um diferencial competitivo."),
    TF("Um diferencial fácil de copiar, como tarifa zero por um mês, tende a dar ao Banco Litoral uma vantagem "
       "competitiva duradoura.", False,
       "O concorrente copia a promoção no mês seguinte. Vantagem duradoura vem de algo difícil de imitar."),
    QK("Qual destes diferenciais do Banco Litoral é o mais difícil de os concorrentes copiarem rapidamente?",
       "relacionamento de anos com a comunidade local",
       ["desconto de 10% na tarifa durante o mês que vem", "brinde na abertura de conta"],
       "Relacionamento e conhecimento do cliente levam tempo para construir. Desconto e brinde se copiam em dias."),
    MT("As três estratégias genéricas para obter vantagem competitiva são custo, diferenciação e foco. Associe cada uma "
       "a um exemplo bancário:",
       [("Liderança em custo", "Tarifas baixas graças a uma operação enxuta"),
        ("Diferenciação", "Atendimento ou produto único, valorizado pelo cliente"),
        ("Foco", "Atender muito bem um nicho, como o agronegócio")],
       "Custo: ser o mais eficiente. Diferenciação: oferecer algo que o cliente valoriza e os outros não têm. Foco: "
       "concentrar-se num segmento específico.", step=3),
   ]),
  dict(title="Processo de decisão de compra", subtopic="etapas",
   summary="O cliente costuma passar por cinco etapas: reconhecer a necessidade, buscar informações, avaliar as "
           "alternativas, decidir a compra e o comportamento pós-compra (satisfação, arrependimento, recomendação). O "
           "atendente ajuda mais quando sabe em que etapa o cliente está.",
   items=[
    A1("Exemplo: as etapas são necessidade, busca de informações, avaliação de alternativas, decisão e pós-compra. Pedro "
       "percebeu que precisa de um carro para trabalhar (necessidade) e pesquisou financiamentos em três bancos (busca). "
       "Ao comparar o CET de cada proposta, ele está na etapa de:",
       "avaliação de alternativas", ["decisão de compra", "pós-compra"],
       "Comparar as opções encontradas é a avaliação de alternativas. A decisão vem depois, quando ele escolhe uma."),
    TF("A etapa de pós-compra não interessa ao banco de Pedro, porque a venda já foi feita.", False,
       "No pós-compra o cliente confirma ou não a escolha: daí vêm arrependimento, recomendação a outras pessoas e nova "
       "compra. O banco tem muito a ganhar acompanhando."),
    OR("Ordene as etapas da decisão de Pedro sobre o financiamento:",
       ["Percebe que precisa de um carro para trabalhar", "Pesquisa financiamentos em três bancos",
        "Compara o CET das propostas", "Assina o financiamento escolhido",
        "Avalia se fez um bom negócio nos meses seguintes"],
       "Necessidade, busca, avaliação, decisão e pós-compra, nessa ordem.", step=2),
    QK("Dois meses depois de assinar, Pedro se pergunta se não teria feito melhor negócio em outro banco. Em que etapa "
       "ele está, e o que o banco pode fazer?",
       "pós-compra; contatá-lo e tirar as dúvidas",
       ["busca; enviar novas propostas de crédito", "avaliação; oferecer outro financiamento igual"],
       "A dúvida depois da compra é típica do pós-compra. Um contato do banco, tirando dúvidas, reduz o arrependimento.",
       step=3),
    PV("Um cliente procura a agência dizendo que o filho começará a faculdade em dois anos e que precisa juntar dinheiro. "
       "Ele já pesquisou na internet e trouxe anotações sobre três aplicações. Considerando as etapas do processo de "
       "decisão de compra, o atendente que quer ajudar o cliente neste momento deve",
       "comparar com ele as opções, de acordo com o prazo e o objetivo.",
       ["oferecer o produto que tem a maior meta de venda da agência no mês.",
        "despertar nele a necessidade de poupar, mostrando por que guardar.",
        "encerrar a conversa e retomar o contato depois da aplicação feita.",
        "insistir numa decisão imediata, antes que ele procure concorrentes."],
       "O cliente já reconheceu a necessidade e buscou informações: está avaliando alternativas. Ajudar a comparar, "
       "pelo prazo e pelo objetivo, é o que serve agora. Despertar a necessidade é uma etapa já vencida, e empurrar a meta "
       "ou pressionar a decisão ignora o que ele precisa.", style="caso_pratico"),
   ]),
  dict(title="Fatores que influenciam o consumidor", subtopic="culturais, sociais, pessoais e psicológicos",
   summary="Quatro grupos de fatores influenciam a compra. Culturais: valores e costumes da sociedade. Sociais: família, "
           "amigos e grupos de referência. Pessoais: idade, fase da vida, ocupação, renda e estilo de vida. "
           "Psicológicos: motivação, percepção, experiências passadas, crenças e atitudes.",
   items=[
    A1("Exemplo: Rafael abriu conta num banco digital porque os amigos da faculdade usam e recomendam; é um fator social "
       "(grupo de referência). Depois do primeiro emprego, ele passou a procurar cartão com limite maior, porque a renda "
       "subiu. Esse fator é:",
       "pessoal (situação econômica)", ["cultural (valores do país)", "psicológico (percepção)"],
       "Renda e ocupação são fatores pessoais. A recomendação dos amigos é social."),
    TF("A fase da vida de Rafael, como casar ou ter filhos, é um fator pessoal que muda as necessidades financeiras "
       "dele.", True,
       "O ciclo de vida é fator pessoal: com filhos, por exemplo, ganham peso a reserva, o seguro e a previdência."),
    MT("Associe cada situação de Rafael ao fator que influencia a compra:",
       [("Os amigos recomendam um banco", "Social"), ("Medo de investir após perder dinheiro", "Psicológico"),
        ("Renda maior depois de uma promoção", "Pessoal"), ("Costume do país de parcelar compras", "Cultural")],
       "Grupo de referência é social; experiência e atitude são psicológicas; renda é pessoal; costume da sociedade é "
       "cultural."),
    QK("Rafael perdeu dinheiro numa aplicação no ano passado e agora sente medo de investir. Para o atendente, o fator a "
       "considerar é:",
       "psicológico: experiência e atitude", ["cultural: costume da região", "social: opinião dos amigos"],
       "A experiência ruim mudou a atitude de Rafael diante do risco. É um fator psicológico, e o atendente precisa "
       "explicar riscos com calma, sem pressionar.", step=3),
    PV("Na análise do comportamento do consumidor de produtos financeiros, são exemplos de fatores pessoais que "
       "influenciam a decisão de compra, EXCETO",
       "a opinião dos colegas de trabalho.",
       ["a idade e a fase do ciclo de vida.", "a ocupação e a renda disponível.", "o estilo de vida do cliente.",
        "a situação econômica do momento."],
       "Idade, fase da vida, ocupação, renda, estilo de vida e situação econômica são fatores pessoais. A opinião dos "
       "colegas vem de um grupo de referência: é fator social.", style="excecao"),
   ]),
  dict(title="Adequação e orientação ao cliente", subtopic="características, benefícios e educação financeira",
   summary="Característica é o que o produto é ('a conta tem Pix ilimitado'); benefício é o que ele faz pelo cliente "
           "('você recebe sem pagar tarifa'). A oferta certa parte da necessidade, do prazo e do risco que o cliente "
           "aceita. Orientar também é educação financeira: explicar custos e desaconselhar o uso inadequado do crédito.",
   items=[
    A1("Exemplo: 'a maquininha aceita aproximação' é característica; 'o senhor atende a fila mais rápido na hora do "
       "pico' é benefício. Sérgio, dono de padaria, ouve: 'a conta PJ tem Pix ilimitado'. O benefício correspondente "
       "para ele é:",
       "receber sem pagar tarifa por Pix", ["a conta ter um aplicativo moderno", "o banco ter muitas agências"],
       "O benefício traduz a característica no ganho do cliente: sem custo por Pix recebido, a padaria economiza."),
    TF("Orientar Sérgio a usar o cheque especial como complemento de caixa todo mês é uma boa prática de educação "
       "financeira.", False,
       "O cheque especial é um crédito caro, para emergência curta. Usá-lo todo mês indica problema de caixa que precisa "
       "de outra solução."),
    QK("Sérgio quer guardar dinheiro para trocar o forno daqui a um ano e não aceita perder valor. A oferta mais "
       "adequada é:",
       "renda fixa conservadora com prazo de um ano",
       ["ações de uma grande empresa do setor de alimentos", "consórcio de imóvel com prazo de 15 anos"],
       "Prazo curto e nenhuma tolerância a perda pedem renda fixa conservadora no prazo do objetivo. Ações oscilam, e o "
       "consórcio não serve ao prazo nem ao bem."),
    QK("Sérgio pede um empréstimo para cobrir a falta de caixa que aparece todo fim de mês. A orientação mais "
       "responsável é:",
       "analisar o fluxo de caixa e buscar a causa",
       ["liberar um limite maior no cheque especial", "oferecer o crédito de maior valor disponível"],
       "Falta de caixa repetida tem causa (prazo de recebimento, estoque, retiradas). Crédito sem diagnóstico só adia e "
       "encarece o problema.", step=3),
    PV("Uma cliente de 68 anos, aposentada, quer aplicar uma reserva da qual pode precisar a qualquer momento para "
       "despesas de saúde. O técnico bancário que age com responsabilidade na oferta deve recomendar",
       "aplicação de baixo risco e com resgate rápido.",
       ["título de capitalização, pelo sorteio mensal.", "ações, pelo potencial de ganho no longo prazo.",
        "previdência com prazo longo de carência.", "consórcio, pela disciplina de pagamento."],
       "A reserva pode ser usada a qualquer hora: precisa de liquidez e de baixo risco. Ações oscilam, previdência e "
       "consórcio prendem o dinheiro por prazos longos, e capitalização não é investimento para reserva.",
       style="caso_pratico"),
   ]),
 ]),
]
