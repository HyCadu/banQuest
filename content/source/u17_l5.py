# Unidade 2 do Técnico Bancário (u17), lição 5. Gerado para o app por tools/gen_u16.py u17.
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
 dict(order=5, title="CRM, reclamações e canais digitais", concepts=[
  dict(title="CRM e marketing de relacionamento", subtopic="histórico e personalização",
   summary="CRM (gestão do relacionamento com clientes) é a estratégia, apoiada num sistema, de registrar dados e cada "
           "contato do cliente (pedidos, compras, reclamações) para personalizar o atendimento. Só funciona com dados "
           "atualizados e registrados por todos. O marketing de relacionamento usa essas informações para manter o "
           "cliente fiel no longo prazo.",
   items=[
    A1("Exemplo: no CRM, a agência registra cada contato do cliente; quem atende depois já conhece o histórico. Gustavo "
       "ligou ontem reclamando de uma tarifa e hoje foi à agência. Com o CRM bem usado, Renata:",
       "já conhece a reclamação e retoma o assunto",
       ["pede que ele conte tudo de novo", "oferece um produto novo sem olhar o histórico"],
       "O registro do contato de ontem permite continuar de onde parou, sem fazer Gustavo repetir a história."),
    TF("CRM é apenas um software; para funcionar, não depende de os funcionários registrarem as informações dos "
       "clientes como Gustavo.", False,
       "O sistema só mostra o que foi registrado. Sem registro e atualização pela equipe, o CRM fica vazio ou errado."),
    QK("Pelo CRM, Renata vê que Gustavo recebe o salário na conta, usa pouco o cartão e perguntou duas vezes sobre "
       "investimento. A ação de relacionamento mais adequada é:",
       "ligar e oferecer orientação sobre investimentos",
       ["oferecer um aumento do limite do cartão de crédito", "enviar a mesma oferta que todos recebem"],
       "O histórico mostra o interesse dele. Personalizar a oferta é o uso certo dos dados do CRM."),
    QK("Qual destas práticas é de marketing de relacionamento, e não de venda pontual?",
       "acompanhar Gustavo e ajustar as ofertas",
       ["vender o máximo no primeiro contato", "dar um desconto só no dia da abertura da conta"],
       "Relacionamento é contínuo: conhecer o cliente ao longo do tempo e oferecer o que faz sentido em cada momento.",
       step=3),
    QK("Gustavo mudou de telefone e de endereço, mas ninguém atualizou o cadastro. Para o CRM, a consequência é:",
       "as ações de relacionamento não chegam a ele",
       ["a tarifa de Gustavo passa a aumentar sozinha", "o sistema corrige os dados sozinho"],
       "Com dados desatualizados, ligações, cartas e ofertas se perdem. O cadastro é a base do CRM.", step=3),
   ]),
  dict(title="Segmentação da carteira", subtopic="grupos com características comuns",
   summary="Segmentar é dividir a carteira em grupos com características parecidas (renda, idade, atividade, momento de "
           "vida, canal preferido) para tratar cada grupo de um jeito. A mesma oferta para todos desperdiça esforço e "
           "agrada pouca gente.",
   items=[
    A1("Exemplo: segmentar é dividir a carteira em grupos parecidos para tratar cada um de um jeito, como aposentados, "
       "jovens no primeiro emprego e pequenos empresários. A gerente Sandra tem 400 clientes e separa os que são donos "
       "de comércio. O critério usado foi:",
       "atividade econômica", ["faixa etária", "região de moradia"],
       "Ser dono de comércio é uma atividade econômica. Idade e região seriam outros critérios."),
    TF("Na segmentação, Sandra envia exatamente a mesma oferta a todos os clientes da carteira, para não haver tratamento "
       "diferente.", False,
       "Segmentar serve justamente para adaptar a oferta a cada grupo."),
    MT("Associe cada segmento da carteira de Sandra à oferta mais adequada:",
       [("Jovens no primeiro emprego", "Primeiro cartão e reserva de emergência"),
        ("Pequenos comerciantes", "Maquininha e capital de giro"),
        ("Aposentados", "Recebimento do benefício e prevenção a golpes"),
        ("Clientes de alta renda", "Consultoria de investimentos")],
       "Cada grupo tem necessidades típicas; a oferta acompanha o momento e a atividade do cliente."),
    QK("Sandra percebe que 60 clientes jovens usam só o app e nunca vão à agência. A melhor forma de falar com esse "
       "segmento é:",
       "ofertas personalizadas pelo próprio app",
       ["cartas impressas pelo correio a cada trimestre", "convites para palestras na agência"],
       "O canal preferido também é critério de segmentação: fala-se com o cliente onde ele está.", step=3),
    QK("Sandra separa os clientes com a conta parada há seis meses. Esse grupo pede uma ação de:",
       "reativação, com contato e oferta sob medida",
       ["encerramento imediato das contas", "a mesma campanha enviada aos clientes ativos"],
       "Clientes inativos formam um segmento próprio: o objetivo é entender por que pararam e trazê-los de volta.",
       step=3),
   ]),
  dict(title="Tratamento de reclamações", subtopic="ouvir, registrar, resolver e dar retorno",
   summary="Reclamação bem tratada recupera o cliente e mostra onde o processo falha. Passos: ouvir sem interromper, "
           "registrar com número de protocolo, resolver ou encaminhar, e dar retorno ao cliente. Se o atendimento "
           "habitual (agência, SAC) não resolver, o cliente pode recorrer à ouvidoria da instituição.",
   items=[
    A1("Exemplo: a reclamação bem tratada segue quatro passos: ouvir, registrar com protocolo, resolver ou encaminhar, e "
       "dar retorno. Neide reclama de uma tarifa cobrada indevidamente. O técnico ouviu e registrou o protocolo. O "
       "próximo passo é:",
       "resolver ou encaminhar a correção", ["encerrar o atendimento", "pedir que ela volte outro dia"],
       "Depois de ouvir e registrar, vem a solução (ou o encaminhamento a quem resolve). O retorno fecha o ciclo."),
    TF("Quando a reclamação de Neide não é resolvida no atendimento habitual (agência ou SAC), ela pode recorrer à "
       "ouvidoria da instituição.", True,
       "A ouvidoria atua depois do atendimento habitual, como instância para os casos que não foram resolvidos ali."),
    QK("A tarifa de Neide foi estornada. O que falta para fechar bem o atendimento?",
       "dar retorno a Neide e confirmar a solução",
       ["nada, o estorno já resolveu o caso", "aproveitar e oferecer outro produto na mesma ligação"],
       "Sem retorno, Neide pode nem saber que o problema foi resolvido. O retorno é o último passo."),
    QK("Neide ligou três vezes e, em cada ligação, teve de explicar tudo de novo. O que falhou?",
       "o registro do histórico da reclamação",
       ["a cordialidade dos atendentes", "o horário de funcionamento do atendimento telefônico"],
       "Com o protocolo e o histórico registrados, cada atendente continuaria de onde o anterior parou.", step=3),
   ]),
  dict(title="Resolução de conflitos e mediação", subtopic="cliente irritado e neutralidade",
   summary="Diante de um cliente irritado: manter a calma, ouvir, mostrar empatia ('entendo o transtorno'), não levar "
           "para o lado pessoal, conversar em local reservado e focar na solução possível, sem prometer o que não pode "
           "cumprir. Ao mediar um conflito entre pessoas, ouvir os dois lados e aplicar a regra com neutralidade.",
   items=[
    A1("Exemplo: diante de um cliente irritado, o atendente mantém a calma, ouve, mostra que entende e passa para a "
       "solução. Wagner fala alto na fila porque o caixa eletrônico reteve o cartão dele. A primeira resposta adequada "
       "é:",
       "ouvir e dizer que entende o transtorno", ["pedir que ele baixe o tom ou saia", "explicar que a culpa é da máquina, não do banco"],
       "Acolher primeiro baixa a tensão. Repreender ou se justificar aumenta o conflito."),
    TF("Ao atender um cliente exaltado como Wagner, convém levá-lo para um local mais reservado, longe da fila, para "
       "conversar com calma.", True,
       "O local reservado tira a plateia, acalma e dá privacidade ao cliente."),
    QK("Wagner exige o cartão de volta na hora, mas o caixa só será aberto pela manutenção amanhã. O atendente deve:",
       "dizer o prazo real e oferecer bloqueio e segunda via",
       ["prometer que o cartão será devolvido em uma hora, no máximo", "dizer que não há nada a fazer"],
       "Prometer o impossível gera nova frustração; negar tudo também. O caminho é a verdade e uma solução possível."),
    QK("Dois clientes discutem na agência por causa da ordem da fila preferencial. Como mediador, o técnico deve:",
       "ouvir os dois e aplicar a regra com neutralidade",
       ["dar razão a quem estiver falando mais alto na fila", "pedir que os dois resolvam a questão entre eles"],
       "O mediador não toma partido: escuta as partes e decide pela regra, que vale para todos.", step=3),
    PV("Um cliente chega à agência muito irritado, falando alto, porque um Pix agendado não foi feito e ele pagou multa "
       "num boleto. Diante da situação, a conduta mais adequada do técnico bancário é",
       "acolher o cliente, ouvir o relato e buscar uma solução.",
       ["pedir que o cliente se acalme antes de iniciar o atendimento.",
        "explicar que o agendamento é de responsabilidade do cliente.",
        "encaminhar o cliente à ouvidoria, sem analisar o caso antes.",
        "garantir o ressarcimento da multa antes de apurar o ocorrido."],
       "Acolher e ouvir baixa a tensão e permite entender o que houve. Exigir calma ou culpar o cliente acirra o conflito; "
       "a ouvidoria vem depois do atendimento habitual; e prometer ressarcimento sem apurar pode criar uma promessa que "
       "não se cumpre.", style="caso_pratico"),
   ]),
  dict(title="Canais de atendimento e transformação digital", subtopic="multicanal e omnichannel",
   summary="No multicanal, o banco tem vários canais (agência, app, internet banking, central, correspondentes), mas "
           "eles não se falam. No omnichannel, os canais são integrados: o cliente começa num e termina no outro sem "
           "repetir nada. O digital resolve o simples; o atendimento humano segue essencial nos casos complexos e para "
           "quem tem dificuldade com tecnologia.",
   items=[
    A1("Exemplo: no multicanal, há vários canais que não se comunicam; no omnichannel, os canais são integrados e o "
       "cliente continua de onde parou. Yara simulou um financiamento no app e foi à agência; o gerente abriu a mesma "
       "simulação na tela dele. Isso é:",
       "omnichannel", ["multicanal", "canal único"],
       "O gerente viu o que Yara fez no app: os canais compartilham o histórico, característica do omnichannel."),
    TF("A transformação digital do setor bancário eliminou a necessidade de atendimento humano, inclusive nos casos "
       "complexos como o financiamento de Yara.", False,
       "O digital absorve as operações simples. Casos complexos, orientação e clientes com dificuldade ainda pedem "
       "atendimento humano."),
    QK("Yara começou um pedido de cartão no chat, mas a conexão caiu. Ao ligar para a central de um banco omnichannel, "
       "ela:",
       "continua o pedido de onde parou", ["refaz o pedido desde o início", "precisa ir à agência para concluir"],
       "Com canais integrados, a central vê o pedido iniciado no chat e segue dali."),
    QK("Um cliente idoso tem dificuldade com o app. A atitude que combina inclusão e transformação digital é:",
       "orientar o uso do app e manter outros canais",
       ["exigir que ele passe a usar apenas o aplicativo", "desativar o acesso digital dele"],
       "Ensinar o uso ajuda a incluir, e manter agência e telefone garante o atendimento enquanto ele se adapta.", step=3),
    PV("Um banco percebe que os clientes que usam o aplicativo e depois ligam para a central precisam repetir todos os "
       "dados, e que o gerente da agência não vê as simulações feitas no aplicativo. Para melhorar a experiência do "
       "cliente, o banco deve",
       "integrar os canais e o histórico do cliente.",
       ["criar novos canais, cada um com equipe própria.", "reduzir o horário de atendimento da central.",
        "levar todo o atendimento de volta para a agência.", "limitar o aplicativo às consultas de saldo."],
       "O problema é a falta de integração (multicanal). A saída é o omnichannel: canais conectados, com o mesmo "
       "histórico. Criar mais canais isolados ou cortar canais piora a experiência.", style="caso_pratico"),
   ]),
 ]),
]
