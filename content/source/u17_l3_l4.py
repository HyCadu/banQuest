# Unidade 2 do Técnico Bancário (u17), lições 3 e 4. Gerado para o app por tools/gen_u16.py u17.
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
 dict(order=3, title="Vendas, prospecção e fidelização", concepts=[
  dict(title="Venda consultiva e objeções", subtopic="foco na necessidade",
   summary="Na venda consultiva, o vendedor primeiro descobre a necessidade do cliente e só depois apresenta o produto que "
           "a resolve. Objeção é uma dúvida ou resistência ('está caro', 'vou pensar'); bem respondida, ajuda a fechar. "
           "Conhecer bem o produto dá segurança para responder sem inventar.",
   items=[
    A1("Exemplo: na venda consultiva, o vendedor descobre a necessidade e só então apresenta o produto que a atende. "
       "Helena conta a Diego que mora sozinha e se preocupa com roubo no apartamento. A abordagem consultiva é:",
       "mostrar a cobertura de roubo",
       ["listar todas as coberturas do seguro, uma a uma", "falar da promoção do mês antes de ouvir mais"],
       "Diego liga o produto à necessidade que Helena trouxe. Despejar todas as coberturas ou a promoção ignora o que "
       "ela disse."),
    TF("Objeção é uma dúvida ou resistência de Helena; quando bem respondida, pode ajudar Diego a fechar a venda.", True,
       "A objeção mostra o que ainda impede a compra. Respondê-la com clareza remove o obstáculo."),
    QK("Helena diz: 'Acho caro'. Qual resposta de Diego trata a objeção?",
       "comparar o custo mensal ao prejuízo de um roubo",
       ["mudar de assunto e falar de outro produto", "dizer que os outros clientes acham o seguro barato"],
       "Tratar a objeção é responder ao ponto: mostrar o custo em perspectiva. Fugir do assunto ou desqualificar a "
       "opinião dela não resolve a dúvida."),
    QK("Helena pergunta se o seguro cobre danos que um vazamento no apartamento dela causar ao vizinho. Diego não sabe. "
       "A melhor atitude é:",
       "conferir nas condições gerais e responder",
       ["dizer que sim, para não perder a venda", "dizer que não, por precaução"],
       "Inventar uma resposta, para mais ou para menos, engana a cliente. Conferir no documento mantém a confiança.",
       step=3),
    QK("Antes de qualquer oferta, Diego pergunta a Helena como é a rotina dela e o que mais a preocupa no apartamento. "
       "Essa conduta mostra que ele:",
       "está levantando a necessidade antes de oferecer",
       ["está atrasando a venda sem motivo", "está tentando conhecer a vida pessoal da cliente"],
       "Perguntar antes de apresentar é a base da venda consultiva: a oferta vem depois, sob medida.", step=3),
   ]),
  dict(title="Etapas da venda", subtopic="da prospecção ao pós-venda",
   summary="Etapas da venda: prospecção (achar clientes com potencial), abordagem (primeiro contato), sondagem (levantar "
           "necessidades), apresentação (mostrar a solução), tratamento de objeções, fechamento e pós-venda. A venda não "
           "acaba na assinatura: o pós-venda mantém o relacionamento.",
   items=[
    A1("Exemplo: as etapas são prospecção, abordagem, sondagem, apresentação, objeções, fechamento e pós-venda. Camila "
       "ligou para clientes com perfil para previdência (prospecção) e marcou uma conversa com Jorge (abordagem). Quando "
       "pergunta a Jorge sobre os planos dele para a aposentadoria, ela está na etapa de:",
       "sondagem", ["fechamento", "pós-venda"],
       "Perguntar para entender a necessidade é a sondagem. Fechamento e pós-venda vêm bem depois."),
    TF("Para Camila, o processo de vendas termina na assinatura do contrato; o que vem depois é responsabilidade de outra "
       "área.", False,
       "O pós-venda faz parte da venda: tirar dúvidas, acompanhar o uso e manter o contato evitam arrependimento e "
       "trazem novas vendas."),
    OR("Ordene as etapas da venda de Camila para Jorge:",
       ["Seleciona clientes com perfil para previdência", "Faz o primeiro contato com Jorge",
        "Pergunta sobre os planos de aposentadoria dele", "Apresenta o plano que se encaixa nesses planos",
        "Responde à dúvida de Jorge sobre o resgate", "Formaliza a contratação"],
       "Prospecção, abordagem, sondagem, apresentação, objeções e fechamento. O pós-venda vem depois.", step=2),
    QK("Três meses depois de Jorge contratar a previdência, Camila liga para saber se ele recebeu o extrato e tem "
       "dúvidas. Essa ligação é parte de qual etapa?",
       "pós-venda, para manter o relacionamento", ["prospecção, para encontrar novos clientes", "sondagem, antes da oferta"],
       "Acompanhar o cliente depois da compra é o pós-venda, que sustenta a relação de longo prazo.", step=3),
    QK("Camila apresentou o plano a Jorge antes de perguntar qualquer coisa sobre os objetivos dele. Que etapa ela pulou?",
       "a sondagem das necessidades", ["o fechamento da venda", "a prospecção de clientes"],
       "Sem sondagem, a apresentação é genérica e corre o risco de não servir ao cliente.", step=3),
   ]),
  dict(title="Mix de marketing (4 Ps)", subtopic="produto, preço, praça e promoção",
   summary="O mix de marketing tem quatro Ps. Produto: o que se oferece. Preço: quanto custa. Praça: onde e como chega "
           "ao cliente (canais e distribuição). Promoção: como a empresa comunica e estimula a compra, com propaganda, "
           "publicidade, promoção de vendas (incentivos temporários), relações públicas e venda pessoal.",
   items=[
    A1("Exemplo: Produto é o que se oferece, Preço é quanto custa, Praça é onde e como chega ao cliente, e Promoção é como "
       "a empresa comunica e incentiva a compra. O banco lança uma conta universitária e fixa a tarifa mensal em R$ 5. "
       "Essa decisão é do P de:",
       "Preço", ["Praça", "Produto"],
       "Definir quanto o cliente paga é decisão de Preço."),
    TF("No mix de marketing, permitir abrir a conta universitária pelo aplicativo, sem ir à agência, é uma decisão de "
       "Praça.", True,
       "Praça trata dos canais pelos quais o produto chega ao cliente: agência, app, correspondente."),
    MT("Associe cada decisão sobre a conta universitária ao P do mix de marketing:",
       [("Conta com cartão e limite para estudante", "Produto"), ("Tarifa de R$ 5 por mês", "Preço"),
        ("Abertura pelo app e em quiosques no campus", "Praça"), ("Anúncios nas redes sociais", "Promoção")],
       "O que é oferecido, quanto custa, por onde chega e como é comunicado."),
    QK("Dentro do P de Promoção, dar um bônus por tempo limitado a quem abrir a conta universitária até o fim do mês é "
       "exemplo de:",
       "promoção de vendas", ["relações públicas", "venda pessoal"],
       "Incentivo temporário para estimular a compra é promoção de vendas. Relações públicas cuidam da imagem; venda "
       "pessoal é o contato direto do vendedor.", step=3),
    QK("O banco passa a oferecer a conta universitária também por correspondentes perto das faculdades. Qual P mudou?",
       "Praça", ["Preço", "Promoção"],
       "Correspondentes são mais um canal de distribuição: é uma mudança de Praça.", step=3),
   ]),
  dict(title="Análise ambiental e planejamento comercial", subtopic="ambiente externo e interno",
   summary="Na análise ambiental, o banco olha o ambiente externo (oportunidades e ameaças, que ele não controla, como "
           "economia, concorrência, leis e tecnologia) e o interno (forças e fraquezas, que ele controla, como equipe, "
           "produtos, canais e localização). A matriz SWOT junta as quatro coisas e orienta o plano comercial.",
   items=[
    A1("Exemplo: o ambiente externo traz oportunidades e ameaças que o banco não controla; o interno, forças e fraquezas "
       "que ele controla. Uma fábrica que vai abrir no bairro da agência Jardim, trazendo 500 empregos, é:",
       "oportunidade do ambiente externo", ["força do ambiente interno", "fraqueza do ambiente interno"],
       "A fábrica é um fato de fora, que o banco não controla, e que favorece novos negócios: oportunidade."),
    TF("O horário de funcionamento da agência Jardim é uma variável que o banco pode controlar.", True,
       "Horário é decisão interna. Variáveis incontroláveis são as do ambiente, como inflação e concorrência."),
    MT("Associe cada fato da agência Jardim ao quadrante da matriz SWOT:",
       [("Equipe experiente em crédito rural", "Força"), ("Sistema lento na abertura de contas", "Fraqueza"),
        ("Safra recorde prevista na região", "Oportunidade"), ("Fintech com tarifa zero chegando à cidade", "Ameaça")],
       "Forças e fraquezas são internas; oportunidades e ameaças vêm de fora."),
    QK("A inflação sobe e as famílias do bairro passam a poupar menos. Para a agência Jardim, isso é:",
       "ameaça externa, fora do seu controle",
       ["fraqueza interna, que ela pode corrigir", "força interna, se ela reagir rápido"],
       "Inflação é variável do ambiente econômico: o banco não a controla, só reage a ela.", step=3),
    QK("A agência Jardim cruza a força 'equipe de crédito rural' com a oportunidade 'safra recorde'. A ação que sai "
       "desse cruzamento é:",
       "campanha de crédito de custeio",
       ["reduzir a equipe de crédito rural", "fechar o atendimento aos sábados"],
       "Usar uma força para aproveitar uma oportunidade é a leitura mais direta da SWOT no plano comercial.", step=3),
   ]),
  dict(title="Gestão e motivação da equipe comercial", subtopic="clima, metas e reconhecimento",
   summary="Cabe ao gestor criar o clima para a equipe se motivar: metas realistas e desafiadoras combinadas com a "
           "equipe, acompanhamento frequente, reconhecimento e confiança entre líder e liderados. Expor quem não bateu a "
           "meta, ou deixar conflitos crescerem, piora o resultado.",
   items=[
    A1("Exemplo: o gestor que combina metas com a equipe, acompanha o resultado toda semana e reconhece quem evolui cria "
       "um clima de confiança. Fábio decide divulgar só os nomes de quem ficou abaixo da meta, para 'pressionar'. Essa "
       "atitude tende a:",
       "piorar o clima e a motivação da equipe",
       ["melhorar a confiança entre líder e equipe", "aumentar a cooperação entre colegas"],
       "A exposição pública gera medo e ressentimento, não confiança. O reconhecimento motiva mais que a punição."),
    TF("Metas impossíveis de atingir costumam motivar mais a equipe de Fábio, porque exigem esforço máximo.", False,
       "Meta que ninguém alcança desanima. O que motiva é a meta desafiadora, mas possível."),
    QK("Dois vendedores da equipe de Fábio brigam pelo mesmo cliente. A atitude de líder que preserva o clima é:",
       "mediar e definir regras claras de carteira",
       ["deixar que disputem para ver quem vende mais", "dar o cliente a quem tem mais tempo de casa"],
       "Conflito sem regra cresce. O líder media e deixa claro quem atende cada cliente."),
    QK("Fábio quer que a equipe venda mais seguros no trimestre. Qual ação é mais adequada?",
       "metas claras, acompanhamento e reconhecimento",
       ["cortar a folga de quem não bater a meta", "aumentar a meta sem conversar antes com a equipe"],
       "Metas combinadas, acompanhadas de perto e com reconhecimento sustentam a motivação. Punir e impor desgastam.",
       step=3),
    QK("Um vendedor novo da equipe de Fábio está abaixo da meta porque ainda não conhece os produtos de seguro. O "
       "gestor deve:",
       "treiná-lo e acompanhar a evolução dele",
       ["transferi-lo para outra agência", "dar a ele a mesma meta dos mais antigos, sem apoio"],
       "A causa é falta de preparo: o caminho é capacitar e acompanhar, não punir.", step=3),
   ]),
 ]),

 dict(order=4, title="Negociação", concepts=[
  dict(title="Etapas da negociação", subtopic="preparação ao acompanhamento",
   summary="Negociar é buscar um acordo quando as partes têm interesses em comum e em conflito. Etapas: preparação "
           "(objetivos, informações, limites e alternativas), abertura (clima de confiança), exploração (descobrir "
           "necessidades), propostas e concessões, fechamento (formalizar o acordo) e acompanhamento (verificar o "
           "cumprimento).",
   items=[
    A1("Exemplo: antes de receber Otávio, que tem uma dívida no cartão, Lívia levanta o histórico dele, as opções de "
       "parcelamento e até onde pode reduzir os juros; isso é a preparação. Na reunião, ela começa cumprimentando Otávio "
       "e criando um clima tranquilo. Essa fase é a:",
       "abertura", ["exploração", "fechamento"],
       "Criar clima e confiança no início da conversa é a abertura. A exploração vem depois, quando ela ouve a situação "
       "de Otávio."),
    TF("A preparação é a etapa em que Lívia define objetivos, reúne informações e estabelece seus limites antes de "
       "conversar com Otávio.", True,
       "Sem preparação, o negociador não sabe até onde pode ir nem o que pedir em troca."),
    OR("Ordene as etapas da renegociação de Lívia com Otávio:",
       ["Estudar o histórico e as opções de parcelamento", "Criar um clima de confiança no início",
        "Ouvir quanto Otávio consegue pagar por mês", "Apresentar uma proposta de parcelamento",
        "Formalizar o acordo no sistema", "Verificar o pagamento das primeiras parcelas"],
       "Preparação, abertura, exploração, proposta, fechamento e acompanhamento.", step=2),
    QK("Otávio aceitou o parcelamento, mas o acordo não foi registrado e a cobrança continuou. Que etapa falhou?",
       "fechamento, que formaliza o acordo", ["abertura, que cria o clima", "exploração, que levanta as necessidades"],
       "O acordo só vale quando é formalizado. Sem registro, o combinado se perde.", step=3),
    PV("Na renegociação de um contrato de crédito empresarial, o gerente percebe, já durante a reunião, que não sabe qual "
       "desconto máximo a instituição autoriza nem conhece o faturamento atual do cliente. A falha ocorreu na etapa de",
       "preparação, quando se definem limites e se reúnem dados.",
       ["abertura, quando se cria o clima inicial da conversa.",
        "exploração, quando se descobrem os interesses do cliente.",
        "fechamento, quando se formaliza o que foi acordado.",
        "acompanhamento, quando se verifica o cumprimento."],
       "Conhecer os próprios limites e as informações da outra parte é tarefa da preparação, antes da reunião. As outras "
       "etapas acontecem durante ou depois da conversa.", style="caso_pratico"),
   ]),
  dict(title="Posições e interesses", subtopic="negociação baseada em princípios",
   summary="Posição é o que a parte pede; interesse é o motivo por trás do pedido. A negociação baseada em princípios "
           "propõe: separar as pessoas do problema, focar nos interesses e não nas posições, criar opções de ganho "
           "mútuo e usar critérios objetivos (como preços de mercado) para decidir.",
   items=[
    A1("Exemplo: posição é o que se pede ('quero tarifa zero'); interesse é o motivo ('preciso cortar custos fixos neste "
       "ano'). A Transportadora Rota diz ao gerente Nelson: 'Só fico no banco se a tarifa da folha cair pela metade, "
       "porque nosso caixa está apertado'. O interesse da empresa é:",
       "aliviar o caixa neste período", ["reduzir a tarifa pela metade", "trocar de banco"],
       "Cortar a tarifa pela metade é a posição. O que está por trás dela é o caixa apertado."),
    TF("Na negociação baseada em princípios, Nelson deve ser firme com o problema e respeitoso com as pessoas, separando "
       "uma coisa da outra.", True,
       "Separar as pessoas do problema evita que a discussão vire disputa pessoal."),
    QK("Sabendo que o interesse da Rota é aliviar o caixa, Nelson pode propor:",
       "carência na tarifa por seis meses",
       ["manter a tarifa e encerrar a conversa", "cortar a tarifa pela metade para sempre"],
       "A carência atende o interesse (caixa agora) sem ceder a posição de forma permanente: é uma opção de ganho mútuo."),
    MT("A negociação baseada em princípios tem quatro pontos. Associe cada um à sua prática:",
       [("Pessoas", "Separar as pessoas do problema"), ("Interesses", "Focar no motivo por trás das posições"),
        ("Opções", "Criar alternativas de ganho mútuo"), ("Critérios", "Usar padrões objetivos, como preço de mercado")],
       "Os quatro pontos levam a acordos mais sensatos e preservam a relação.", step=3),
    PV("Um cliente exige desconto nos juros de um empréstimo, e o gerente insiste em que a taxa é a da tabela. Para "
       "aplicar a negociação baseada em princípios, o gerente deveria",
       "entender o motivo do pedido e usar a taxa de mercado como base.",
       ["manter a taxa da tabela, para não abrir precedente na agência.",
        "conceder o desconto pedido, para preservar o relacionamento.",
        "transferir o cliente para outro gerente, que seja mais flexível.",
        "ameaçar reduzir o limite, para recuperar a posição de força."],
       "O método manda sair das posições (desconto × tabela), entender o interesse do cliente e decidir por critérios "
       "objetivos. Manter ou ceder sem análise continua preso às posições, e ameaçar mistura pessoas e problema.",
       style="caso_pratico", diff=3),
   ]),
  dict(title="Negociação distributiva e integrativa", subtopic="ganha-perde e ganha-ganha",
   summary="Na negociação distributiva (ganha-perde), as partes dividem um valor fixo: o que uma ganha, a outra perde, "
           "como quando só se discute o preço. Na integrativa (ganha-ganha), entram vários itens (prazo, garantia, "
           "taxa), e as partes trocam o que vale menos para uma e mais para a outra, ampliando o valor do acordo.",
   items=[
    A1("Exemplo: quando só se discute a taxa, cada ponto que o cliente ganha o banco perde: é negociação distributiva. "
       "Quando entram prazo, garantia e taxa, as partes podem trocar o que vale menos para uma e mais para a outra: é "
       "integrativa. O produtor Ivo e o banco ajustam a carência à colheita em troca de uma garantia melhor. Essa "
       "negociação é:",
       "integrativa", ["distributiva", "competitiva"],
       "Vários itens foram combinados para ganho dos dois lados: é integrativa. Distributiva e competitiva são nomes do "
       "ganha-perde."),
    TF("Na negociação integrativa entre Ivo e o banco, o objetivo é que uma parte ganhe o máximo possível às custas da "
       "outra.", False,
       "Esse é o espírito da distributiva. A integrativa busca ampliar o valor para os dois."),
    QK("Ivo pede taxa menor. O banco aceita reduzir a taxa se Ivo der em garantia uma máquina, o que diminui o risco da "
       "operação. Essa troca:",
       "cria valor para os dois lados", ["é ganha-perde, pois o banco cedeu", "só beneficia o banco"],
       "Ivo paga menos juros e o banco corre menos risco: os dois saem melhor, típico da integrativa."),
    QK("Em qual situação a negociação distributiva é mais comum?",
       "compra única, sem relação futura, só pelo preço",
       ["parceria de anos entre banco e cooperativa", "acordo com vários itens em discussão ao mesmo tempo"],
       "Um item só e nenhuma relação futura empurram para o ganha-perde. Relações longas e muitos itens favorecem a "
       "integrativa.", step=3),
    PV("Comparada à negociação distributiva, a negociação integrativa caracteriza-se por",
       "ampliar o valor, combinando vários itens.",
       ["discutir um único item, em geral o preço final.", "tratar a outra parte como adversária a vencer.",
        "esconder informações para obter vantagem.", "encerrar o relacionamento após o acordo."],
       "A integrativa combina itens para criar valor e preservar a relação. Item único, adversário e informação escondida "
       "são marcas da distributiva.", style="literal"),
   ]),
  dict(title="MAPAN e zona de acordo", subtopic="alternativas e limites",
   summary="MAPAN (melhor alternativa para um acordo negociado, em inglês BATNA) é o que você fará se não houver acordo; "
           "ela define quando vale a pena recusar. Preço de reserva é o limite de cada parte. A zona de possível acordo "
           "fica entre o limite de uma parte e o da outra; se os limites não se cruzam, não há acordo possível.",
   items=[
    A1("Exemplo: a MAPAN é o que você fará se não houver acordo. Teresa já tem uma proposta de outro banco a 1,8% ao mês "
       "para o consignado; essa é a MAPAN dela. Se o banco dela oferecer 2,0% ao mês, o mais racional para Teresa é:",
       "recusar e ficar com a proposta de 1,8%", ["aceitar, para não perder tempo", "aceitar, pois é o banco onde ela tem conta"],
       "Qualquer acordo pior que a MAPAN deve ser recusado: 2,0% é mais caro que a alternativa de 1,8%."),
    TF("Conhecer a própria MAPAN ajuda Teresa a saber quando é melhor não fechar o acordo.", True,
       "A MAPAN é o piso de comparação: abaixo dela, sair da negociação é melhor que aceitar."),
    QK("A zona de possível acordo fica entre o limite de uma parte e o da outra. O banco aceita baixar até 1,6% ao mês, "
       "e Teresa aceita pagar até 1,8%. Onde está a zona de acordo?",
       "entre 1,6% e 1,8% ao mês", ["abaixo de 1,6% ao mês", "acima de 1,8% ao mês"],
       "Qualquer taxa entre os dois limites é aceitável para ambos. Fora dessa faixa, uma das partes recusa."),
    QK("Se o menor juro que o banco aceita fosse 1,9% ao mês e Teresa só aceitasse até 1,8%, o que aconteceria?",
       "não haveria zona de acordo", ["o acordo sairia a 1,85%", "Teresa pagaria 1,9% ao mês"],
       "Os limites não se cruzam: o banco não desce de 1,9% e Teresa não sobe de 1,8%. Sem zona de acordo, Teresa fica "
       "com a MAPAN.", step=3),
    PV("Uma empresa negocia com o banco a taxa de antecipação de recebíveis. Outra instituição já lhe ofereceu formalmente "
       "1,5% ao mês, e o diretor financeiro sabe que pode recorrer a ela a qualquer momento. No vocabulário da "
       "negociação, essa proposta concorrente representa, para a empresa,",
       "a melhor alternativa sem acordo (MAPAN).",
       ["a zona de possível acordo entre as partes.", "a âncora fixada pelo banco na abertura.",
        "a concessão final obtida no fechamento.", "a posição inicial que o banco declarou."],
       "O que a empresa fará se não fechar com o banco é a MAPAN. A zona de acordo depende dos limites das duas partes, "
       "e âncora, concessão e posição do banco são outras coisas.", style="caso_pratico"),
   ]),
  dict(title="Concessões e técnicas de negociação", subtopic="troca, ancoragem e confirmação",
   summary="Cada concessão deve vir em troca de algo e de forma gradual, nunca de graça nem tudo de uma vez. Ancoragem: "
           "a primeira proposta vira referência para o resto da conversa. Ouvir e perguntar antes de propor, e resumir "
           "o que foi combinado ao final, evitam mal-entendidos.",
   items=[
    A1("Exemplo: cada concessão deve vir em troca de outra: 'reduzo a tarifa se a empresa trouxer a folha de pagamento "
       "para cá'. A clínica Vida pede a Márcia isenção da tarifa da maquininha. A resposta que segue a regra é:",
       "isento se concentrarem os recebíveis aqui",
       ["isento já, para agradar a clínica", "não isento, e encerra a conversa por ali"],
       "Márcia cede na tarifa e ganha volume de recebíveis: concessão com contrapartida."),
    TF("Ancoragem é o efeito da primeira proposta apresentada, que tende a servir de referência para o resto da "
       "negociação entre Márcia e a clínica Vida.", True,
       "A primeira proposta puxa a conversa para perto dela; por isso quem abre bem preparado tem vantagem."),
    QK("Ceder aos poucos dá espaço para trocar cada passo por uma contrapartida. A clínica pede 50% de desconto, e Márcia "
       "pode dar até 30%. Ela deve:",
       "começar com menos e ceder aos poucos",
       ["oferecer logo de uma vez os 30% de desconto", "recusar qualquer desconto"],
       "Dar tudo de uma vez acaba com a margem para negociar; recusar tudo pode encerrar a conversa."),
    QK("No fim, Márcia diz: 'Então combinamos: desconto de 20% e folha de pagamento conosco a partir de março, certo?'. "
       "Essa técnica serve para:",
       "confirmar o acordo e evitar mal-entendidos", ["reabrir a negociação de outros itens do pacote", "ancorar uma nova proposta"],
       "Resumir o combinado garante que as duas partes entenderam o mesmo antes de formalizar.", step=3),
    PV("Em uma negociação comercial com uma empresa cliente, são condutas adequadas do gerente as seguintes, EXCETO",
       "fazer concessões sem pedir nada em troca.",
       ["ouvir o cliente antes de apresentar propostas.", "resumir os pontos acordados ao final.",
        "planejar os seus limites antes da conversa.", "usar perguntas abertas para sondar."],
       "Concessão sem contrapartida reduz o valor do acordo e ensina a outra parte a pedir mais. As demais são boas "
       "práticas de preparação, escuta e fechamento.", style="excecao"),
   ]),
 ]),
]
