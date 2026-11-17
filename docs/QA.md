# QA — roteiro para testar no celular

> Instale o APK mais recente por cima do anterior. Desde a versão 0.2, o progresso é mantido entre atualizações.
> **Exceção: da 0.4.x para a 0.5.0 (BanQuest)** é preciso sincronizar, desinstalar e instalar de novo uma vez, porque a
> assinatura passou a ser fixa. Veja o passo a passo em `docs/ASSINATURA.md`.
>
> **Versão 0.4.0 (social):** antes de instalar, faça os passos 1 a 3 de `docs/SUPABASE_SETUP.md` (SQL, login por código
> e SMTP). O roteiro da parte social está na seção "Versão social (0.4.0)", mais abaixo.

## Roteiro principal (≈15 minutos)

**1. Primeira tela = Trilha**
- [ ] O app abre direto na **Trilha**, com ofensiva, gemas, corações e "dias para a prova" no topo.
- [ ] O seletor **Específicos | Gerais** aparece abaixo do topo (ou dos avisos ativos), sem a antiga barra de XP com estrela nem espaço vazio no lugar dela. Confira nos temas claro e escuro e após concluir uma lição.
- [ ] Banner roxo "Unidade 1 · Banco de dados" **fixo no topo** enquanto você rola; o nó atual pulsa com o balão "Começar" e o
      personagem (Guaiamum) fica ao lado.
- [ ] Role até o fim: as Unidades 2 a 10 aparecem cinzas ("Conteúdo chegando em breve"). Toque num nó cinza: aparece uma
      mensagem explicando.

**2. Fazer uma lição**
- [ ] Toque no nó atual. A lição abre em **tela cheia**, com X, barra de progresso, corações (4/5 etc.) e o anel do cronômetro.
- [ ] Todo enunciado mostra o selo **"não revisada"** (as questões são geradas por IA e só você pode marcá-las revisadas).
- [ ] Responda e toque em **Verificar**. O painel sobe (verde ou vermelho) com a explicação e o personagem reagindo.
- [ ] **Erre de propósito uma questão**: perde 1 coração, o painel mostra a resposta correta, e a questão **volta no fim**
      ("Repetição de um erro… vale 5 XP").
- [ ] Acerte 3 seguidas: aparece **COMBO**. Deixe uma questão passar de 2:15: o anel fica **âmbar**; passe de 3:00: fica
      **vermelho e pisca** (e a resposta continua valendo).
- [ ] Você deve encontrar os tipos: múltipla escolha, caso prático, certo/errado, **associar pares** (toque num termo e na
      definição), **ordenar linhas** (toque nas linhas na ordem) e saída de código.
- [ ] Toque no **X**: pergunta se quer sair. Voltar do Android faz o mesmo.
- [ ] Terminando: tela de **resultado** com XP, precisão, melhor combo, **tempo médio por questão** (sempre aparece) e
      folga/atraso contra 3:00, ofensiva, gemas, "Para revisar".
- [ ] Volte à Trilha: a lição ganhou check (ou coroa, se perfeita) e a **próxima liberou**. Reabra o app: o progresso persiste.

**3. Corações**
- [ ] Erre de primeira até zerar: a lição termina e aparece **"Acabaram os corações!"**. Teste *Praticar erros* (volta 1
      coração ao terminar) e, em Configurações, o **modo sem vidas**.
- [ ] Toque no coração do topo da Trilha: mostra quando volta o próximo (30 min por coração).

**4. Aba Praticar**
- [ ] Depois de errar algo: mostra "vencidas" e "erros guardados". *Treinar erros* funciona; *Revisão livre* sorteia questões
      das lições concluídas. Seus pontos fracos aparecem quando há erros suficientes.

**5. Aba Ligas (missões + liga)**
- [ ] 3 missões do dia com barras de progresso. Ao completar uma, **Resgatar** dá as gemas.
- [ ] Liga Bronze com 30 participantes (você + 29 rivais fictícios), "termina em Xd Yh" e zonas de promoção/rebaixamento.
- [ ] Seu XP da semana sobe a cada lição.

**6. Aba Loja**
- [ ] Baú diário (uma vez por dia). Comprar *Congelar ofensiva* (200) e *Recarregar corações* (350); sem gemas suficientes,
      avisa quanto falta.

**7. Aba Perfil**
- [ ] **A escolha de personagem só existe aqui.** Troque entre Guaiamum, Tartaruga, Tamanduá e Preguiça: o personagem muda
      na Trilha, nas lições e no resultado.
- [ ] Edite o nome, troque a meta diária, veja estatísticas, calendário de dias estudados, acerto por unidade e as 15
      conquistas. *Configurações*: cronômetro (Desligado/Discreto/Ritmo, 3:00 ou 2:30, caso prático 4:00), modo sem vidas,
      vibração, tema, apagar dados.

**8. Dia seguinte**
- [ ] Abra o app no dia seguinte (ou mude a data do aparelho): ofensiva continua se você estudou ontem; missões novas;
      baú diário disponível de novo. Na **segunda-feira**, a liga fecha e mostra o resultado da semana.

**9. Geral**
- [ ] Modo claro e escuro (Configurações > Tema). Gire a tela. Fonte grande do sistema. TalkBack (abas, alternativas e nós
      da trilha têm rótulos).

## Novidades da varredura (versão 0.3.0)

**10. Ícone**
- [ ] O ícone na tela inicial do celular é o **Guaiamum sorrindo sobre fundo turquesa**, na forma do seu launcher
      (redondo, quadrado etc.). Com ícones temáticos ligados (Android 13+), aparece a silhueta do caranguejo.

**11. Lembretes**
- [ ] Depois de concluir a primeira lição, a Trilha mostra o cartão "Quer que o Guaiamum te lembre…?". Toque em
      **Ativar lembrete** e permita as notificações.
- [ ] Em Configurações > Lembretes, mude o horário para daqui a 2 ou 3 minutos e **não faça lição**. A notificação deve
      chegar (alguns aparelhos atrasam alguns minutos). Tocar nela abre o app.
- [ ] Faça uma lição antes do horário: o lembrete do dia **não** deve chegar.
- [ ] Com ofensiva ativa e sem estudar no dia, às 21:30 chega "Sua ofensiva está em risco!".
- [ ] Reinicie o celular: o lembrete continua agendado.

**12. Sons e vibração**
- [ ] Sons de clique (ao escolher alternativa), acerto, erro, combo (no 3º acerto seguido), lição concluída e conquista.
      Desligue em Configurações > Estudo > Sons. No modo silencioso, não tocam.

**13. Dados**
- [ ] Configurações > Dados > **Exportar progresso**: salve o arquivo (por exemplo, no Drive ou em Downloads).
- [ ] **Zerar meu progresso** e depois **Importar progresso** com o arquivo salvo: XP, ofensiva, lições e conquistas voltam.
- [ ] Configurações > Conteúdo > **Importar pacote**: escolha um arquivo que não seja pacote; o app recusa e explica.

**14. Toques que agora fazem algo**
- [ ] Toque no chip de **gemas**: abre a Loja. No chip de **dias**: mostra o planejamento até a prova. No **banner da
      unidade**: abre o guia com as lições e os tópicos do edital.
- [ ] Antes de estudar no dia, a **chama** do topo fica cinza; depois de uma lição, volta a ficar laranja.
- [ ] Durante a lição (modo Ritmo), o chip **"Lição mm:ss"** mostra quanto resta do tempo da lição.

**15. Acessibilidade**
- [ ] Ligue "Remover animações" nas opções de acessibilidade do Android: os mascotes param de se mexer.
- [ ] Fonte do sistema no máximo: o cabeçalho da lição quebra linha sem cortar nada.

## Versão social (0.4.0)

**16. Entrar**
- [ ] Instale por cima da 0.3. Ao abrir, aparece **"Entre com seu e-mail"** (a trilha só abre com conta).
- [ ] Digite um e-mail inválido: o app avisa sem enviar nada. Digite o seu → **Receber código**.
- [ ] O e-mail chega com um **número de 6 dígitos** (se vier um link, falta o passo 3 do guia). Digite um código errado:
      "Código inválido ou vencido". Digite o certo → **Entrar**.
- [ ] "Reenviar (60)" conta para trás; **Trocar e-mail** volta ao primeiro passo.
- [ ] Escolha apelido e personagem → **Começar**. O progresso que você tinha na 0.3 **continua** (sobe para a nuvem).

**17. Ranking**
- [ ] Aba **Ranking** (antiga Ligas): "Ranking da semana", com tempo até o fim, e as missões do dia num cartão.
- [ ] Faça uma lição e volte: seu XP da semana aparece na lista **Geral** (pode levar alguns segundos; use **Atualizar**).
- [ ] No modo avião: faça uma lição. O Ranking mostra "Sem conexão agora…" e "1 sessão esperando internet". Desligue o
      modo avião e toque em **Atualizar**: o XP entra.

**18. Amigos (precisa de um segundo celular ou de um amigo)**
- [ ] Lista **Amigos** → **Compartilhar código** abre o compartilhamento do Android com o seu `TRI-XXXXX`.
- [ ] No outro celular, **Tem o código de alguém?** → digite o código → "Pronto! … agora é seu amigo". Os dois passam a se
      ver em "Ranking dos amigos" e em "Ofensivas em dupla".
- [ ] Na lista **Geral**, toque em **+ Adicionar** em alguém: vira "Pedido enviado". O outro recebe uma **notificação**
      "Pedido de amizade" e vê **Aceitar / Recusar** na lista Amigos (e "Aceitar" na linha do ranking).
- [ ] Os dois fazem uma lição no mesmo dia: a ofensiva da dupla vira **1**. No dia seguinte, se só o amigo estudou, a
      Trilha mostra o banner laranja "A ofensiva em dupla com … acaba hoje…".
- [ ] **Desfazer amizade** pede confirmação e tira a pessoa das listas.

**19. Conta e privacidade**
- [ ] Configurações → **Conta** mostra o seu e-mail, o código de amigo e o estado da sincronização.
- [ ] Desligue **Aparecer no ranking geral**: no outro celular você some da lista Geral (mas continua na lista de amigos).
- [ ] **Trocar de celular:** em Configurações → Conta → **Sair da conta**, o aparelho fica zerado. Entre de novo com o
      mesmo e-mail: XP, lições, ofensiva e conquistas voltam.
- [ ] **Conflito:** com a mesma conta em dois celulares, estude nos dois **sem internet** e depois ligue a internet nos
      dois. Um deles pergunta **"Qual progresso fica?"**. Escolha uma opção e confira que ficou valendo.
- [ ] **Excluir minha conta** (use uma conta de teste): pede confirmação, volta para a tela de entrada, e no painel do
      Supabase o usuário some de Authentication → Users.

**20. Associar pares com erro corrigido (0.4.2)**
- [ ] Numa questão de associar colunas, erre um par de propósito (pisca vermelho) e depois complete todos certo.
- [ ] O painel fica **âmbar**: "Pares completos! +5 XP", e explica que a questão volta em Praticar erros.
- [ ] Os corações **não** diminuem e a questão **não** volta no fim da lição.
- [ ] No resultado, ela aparece em "Para revisar" como **"Corrigida no caminho"**, e a lição não conta como perfeita.

**21. Personagens (0.4.3)**
- [ ] Na Trilha, espere uns 10 s olhando o personagem: ele faz gestos sozinhos.
  - Guaiamum anda de lado e estala as garras.
  - Tartaruga recolhe a cabeça no casco.
  - Tamanduá fareja e mostra a língua.
  - Preguiça boceja.
- [ ] Os personagens têm sombra no chão; ao pular, a sombra encolhe.
- [ ] Na lição, o personagem pequeno ao lado dos chips **pensa** (balão "…").
  - No modo Ritmo, quando o anel fica âmbar ou vermelho, ele fica **nervoso** (gota de suor).
- [ ] Acertos seguidos até 5 de combo: o personagem do feedback fica **surpreso** ("!").
- [ ] Na tela de entrada, ele **acena**.
- [ ] No resultado com ofensiva nova, ele aparece **orgulhoso**.
- [ ] Ao trocar de pose (ex.: do feedback para a próxima questão), ele dá um "pulinho elástico".
- [ ] Com "Remover animações" ligado no Android, todos ficam parados.

## BanQuest 0.5.0

**22. Passagem da 0.4.x (uma vez)**
- [ ] Na 0.4.x: Configurações → Conta → **Sincronizar**, até aparecer "Tudo sincronizado".
- [ ] Tente instalar a 0.5.0 por cima: o Android recusa ("App não instalado"). É esperado.
- [ ] Desinstale, instale `banquest-0.5.0.apk`, entre com o mesmo e-mail: XP, lições, ofensiva e amigos voltam.

**23. Nome novo**
- [ ] O ícone na tela inicial se chama **BanQuest**, assim como a tela de entrada, o rodapé das Configurações e o texto
      de "Compartilhar código".

**24. Aviso de versão nova** (depois de rodar o `0002_releases.sql` e publicar uma versão de teste, conforme
`docs/PUBLICAR_VERSAO.md`)
- [ ] Com o app na 0.5.0, publique uma linha de teste com `version_code` 9. Abra Configurações → Sobre o app →
      **Verificar atualização**: aparece "BanQuest … disponível!".
- [ ] A Trilha mostra o aviso com **Baixar** e **Agora não**. "Agora não" esconde o aviso dessa versão.
- [ ] Chega **uma** notificação "BanQuest … disponível". Tocar nela abre o download.
- [ ] Teste `min_version_code` = 9: aparece o aviso **obrigatório**, que não fecha. Depois apague a linha de teste
      (`delete from public.app_releases where version_code = 9;`).

## BanQuest 0.6.0 — provas anteriores na trilha

**25. Questões de provas reais nas lições**
- [ ] Na Trilha, as lições da Unidade 1 mostram mais questões que antes (ex.: "16 q." em vez de "10 q.").
- [ ] Faça algumas lições da Unidade 1. Entre as 8 questões, algumas trazem a etiqueta azul-esverdeada com banca, órgão,
      ano e número (ex.: "FGV · Banestes 2021 · questão 48") e o selo amarelo **"explicação não revisada"**.
- [ ] As questões da Prefeitura de Teresina mostram "· gabarito preliminar" na etiqueta, e as da DPE-RR, "· gabarito
      não confirmado".
- [ ] Nas questões com tabela ou figura (ex.: T1, T2 e T3 da FGV 2021; a estrutura SOAP de Teresina), o texto da
      tabela/figura aparece inteiro e legível, com rolagem se precisar.
- [ ] Erre uma questão de prova: ela volta no fim da lição e aparece depois em **Praticar erros**.
- [ ] Configurações → Conteúdo: "… questões no app, 26 delas de provas anteriores …".
- [ ] As unidades 2 a 10 continuam "Conteúdo chegando em breve" (as questões de prova delas esperam o pacote da
      unidade).

## BanQuest 0.7.1 — aprender fazendo, meio coração e "Não sei"

**26. Lição "Modelo ER e normalização" em blocos** (a 0.7.0 de teste foi substituída por esta)
- [ ] Na primeira vez, a lição passa por **3 conceitos na ordem** (Chaves e atributos → Cardinalidade → Entidade fraca),
      3 exercícios seguidos de cada, e termina com 3 questões de prova.
- [ ] O primeiro exercício de cada conceito mostra **"Novo conceito: …"** já aberto.
- [ ] Os exercícios de ensino trazem a regra ou um exemplo no enunciado: dá para chegar à resposta lendo, sem chutar.
- [ ] Na segunda vez, a lição continua de onde parou (conceitos 4 a 6). Na terceira, o 7 e revisão.

**27. Meio coração**
- [ ] Erre um exercício de aprender ou praticar: o contador vai para **4,5** e o coração aparece **rachado** (e treme).
      Erre outro de ensino: o coração quebra e o contador vai a 4. Erro em questão de prova tira 1 inteiro.
- [ ] Na Trilha, o chip de corações mostra "4,5" com o coração rachado, e o diálogo mostra o coração da vez rachado.

**28. Botão "Não sei"**
- [ ] Aparece nos exercícios de aprender e praticar (não na prova), com o estoque: "Não sei (3)".
- [ ] Tocar mostra "Agora você sabe!", a resposta e a explicação, **sem perder coração**, e o exercício volta no fim.
- [ ] O estoque cai para 2. No dia seguinte volta a 3 (o presente diário só enche até 3).
- [ ] Loja → **Não sei** por 50 gemas; dá para guardar até 9.
- [ ] Atualizando por cima da 0.6/0.7: corações e progresso continuam iguais, e o estoque começa em 3.

## BanQuest 0.7.2 — menos texto nas telas

**29. Telas mais limpas**
- [ ] **Ranking:** no título, só "Termina em …" e o botão ↻. Sem parágrafo de instruções. O aviso da semana passada
      cabe numa linha.
- [ ] **Ranking › Amigos:** cada ofensiva em dupla é uma linha (nome, situação de hoje, chama com o número e ⋯).
      "Desfazer amizade" fica no ⋯. O ⓘ ao lado de "Ofensivas em dupla" explica a regra.
- [ ] **Loja:** um item por linha, com o preço à direita.
- [ ] **Praticar:** o "Como funciona" está no ⓘ do título.
- [ ] **Resultado da lição:** tempo médio com "alvo 3:00" e fichas de recompensa no lugar das faixas.
- [ ] **Configurações:** sem parágrafos; cada seção com explicação tem um ⓘ.
- [ ] Com o TalkBack ligado, os ícones ↻, ⋯ e ⓘ e as fichas dizem o que são.

## BanQuest 0.7.3 — tela de exercício enxuta

**30. Antes do enunciado, uma faixa curta**
- [ ] No topo: X, barra de progresso e corações; embaixo, o personagem e o anel do tempo. As fichas "Folga" e "Lição"
      não aparecem mais durante a questão.
- [ ] Fique abaixo do ritmo (demore em algumas questões): aparece a ficha de **atraso**. Em dia, não aparece nada.
- [ ] Antes do enunciado, uma linha: origem curta ("FCC · Teresina 2016 · Q22") e o selo "não revisada". Tocando nela,
      abre **Sobre esta questão** (prova completa, gabarito preliminar quando for o caso e o que não foi revisado).
- [ ] No primeiro exercício de um conceito novo aparece a ficha **"Novo: …"** fechada; tocando, abre o resumo.
- [ ] Repetição de um erro mostra a ficha "Repetição · 5 XP".
- [ ] Com o estoque de "Não sei" em zero, o botão some.

## BanQuest 0.9.0 — temas e trilha dos Gerais

**31. Escolha do tema na primeira entrada**
- [ ] Ao abrir depois de atualizar (ou numa instalação nova, depois do login), aparece **"Escolha o seu tema"** com
      quatro cartões: Claro, Escuro, Elétrico e Chiclete. Cada um mostra uma miniatura e a faixa 60/30/10.
- [ ] Vem marcado o tema que você já usava (ou o claro/escuro do aparelho). Tocar num cartão já pinta a tela com ele.
      Embaixo aparece "Dá para trocar quando quiser em Configurações › Tema."
- [ ] Toque em **Começar**: vai para a Trilha. Feche e abra o app: a escolha não aparece de novo e o tema continua.

**32. Temas nas Configurações**
- [ ] Configurações › **Tema** mostra os quatro temas em duas colunas; o atual vem marcado. Trocar muda o app inteiro
      na hora.
- [ ] Elétrico: fundo branco, cor principal azul (botões, lição atual, faixa da unidade) e limão nas lições concluídas.
- [ ] Chiclete: fundo creme, cor principal rosa com texto escuro e azul nas lições concluídas.
- [ ] Nos dois temas novos, passe por Trilha, uma lição (acerto e erro), Resultado, Praticar, Ranking, Loja e Perfil:
      o fundo é sempre claro e nenhum texto fica difícil de ler.

**33. Trilha dos Gerais**
- [ ] No topo da Trilha, o seletor **Específicos | Gerais**. Toque em Gerais: a trilha desliza para o lado, as cores
      mudam aos poucos e o botão do seletor desliza junto.
- [ ] Os Gerais mostram G1 Língua Portuguesa, G2 Raciocínio lógico, estatística e matemática financeira, G3
      Atualidades do mercado financeiro e G4 Ética, compliance e ESG, todas como "Conteúdo chegando em breve".
- [ ] As cores dos Gerais são outras: Claro e Escuro verde-água, Elétrico limão, Chiclete azul.
- [ ] Volte para Específicos: a U1 continua de onde estava. Feche e abra o app: a Trilha abre na última trilha usada.
- [ ] Com "Remover animações" ligado no Android (Acessibilidade), a troca é imediata.
- [ ] Com o TalkBack, o seletor diz "Trilha Específicos" e "Trilha Gerais" e qual está selecionada.

## BanQuest 0.9.1 — lições por conceitos no ranking

Antes de testar: o SQL `0001_social.sql` atualizado precisa estar rodado no Supabase (`docs/PUBLICAR_VERSAO.md`).

**35. Lição boa entra no ranking**
- [ ] Anote seu XP da semana na aba **Ranking**.
- [ ] Faça uma lição da U1 acertando quase tudo de primeira (9 ou mais dos 12 exercícios). No resultado, o XP fica
      entre 160 e 210.
- [ ] Volte ao Ranking e toque em ↻: o XP da semana subiu exatamente o XP da lição. Na 0.9.0 ele não mudava.
- [ ] Num segundo celular, com uma conta amiga, o seu XP aparece atualizado. Se os dois fizeram lição hoje, a
      ofensiva em dupla conta o dia.

## BanQuest 0.10.0 — provas anteriores na aba Praticar

Antes de testar: o SQL `0001_social.sql` atualizado precisa estar rodado no Supabase (`docs/PUBLICAR_VERSAO.md`).

**36. Provas anteriores**
- [ ] Na aba **Praticar**, abaixo de "Revisão livre", aparece **Provas anteriores** com quatro provas: FGV Banestes 2021
      e 2018, FCC Prefeitura de Teresina 2016 (gabarito preliminar) e FCC DPE-RR 2015 (gabarito não confirmado).
- [ ] Toque em FGV Banestes 2018: aparece o aviso, ainda sem gastar nada. Ele mostra o número de questões, o tempo
      estimado, "Começar gasta 1 coração", "Errar não tira coração" e "O XP só vem se você responder todas".
- [ ] Escolha **Específicos**: o número de questões e o tempo mudam (37 questões, cerca de 1h51). Volte ao app sem
      começar: os corações continuam os mesmos.
- [ ] Toque em **Começar prova**: o contador de corações cai 1. Na prova não aparece coração no topo. Erre uma questão:
      nenhum coração some, e a questão não volta no fim.
- [ ] No meio da prova, toque no X: o aviso diz que você não ganha XP e que o coração não volta. Saia: o XP do Perfil
      não muda.
- [ ] Faça uma prova curta até o fim (FCC DPE-RR 2015): aparece "Prova concluída!" com XP, precisão e acertos. As
      erradas aparecem em **Praticar erros**. A ofensiva do dia conta, e o XP entra no Ranking depois de sincronizar.
- [ ] Com só meio coração (ou nenhum), **Começar** leva à tela "Acabaram os corações", que explica que abrir uma
      prova gasta 1 coração inteiro. No **Modo sem vidas**, o aviso diz que começar não gasta coração.
- [ ] **Silenciar notificações:** ligue o interruptor. Na primeira vez, o Android abre a tela de acesso ao Não perturbe:
      permita para o BanQuest e volte. Comece a prova: o Não perturbe liga (ícone na barra do Android). Termine ou saia:
      ele desliga. Se o Não perturbe já estava ligado antes, ele continua ligado depois.

## BanQuest 0.10.1 — treino de erros sem farm

**37. Só a revisão das vencidas dá XP e coração**
- [ ] Erre 2 questões numa lição. Na aba Praticar: "Vencidas 0", "Erros guardados 2" e o texto "Cada erro sai daqui
      depois de 4 revisões certas, em dias diferentes". O botão diz **"Treinar os mais antigos (sem XP)"**.
- [ ] Faça esse treino duas vezes seguidas, acertando tudo: nenhuma resposta mostra "+XP", o resultado diz "Treino
      para estudar: sem XP e sem coração", e o XP do Perfil e os corações não mudam. Os erros continuam guardados.
- [ ] **Revisão livre (sem XP)**: mesma coisa, sem XP e sem coração.
- [ ] No dia seguinte, os 2 erros aparecem como **Vencidas**. "Treinar erros (2)" dá XP e devolve 1 coração. Depois
      disso, as vencidas voltam a 0 e os erros continuam guardados (a próxima revisão é em 3 dias).
- [ ] Fique sem corações num dia sem erro vencido: a tela "Acabaram os corações" não mostra o botão de revisar erros.
      Com erro vencido, mostra "Revisar erros vencidos (ganhe 1 coração)".

## BanQuest 0.12.0 — Unidade 2 e G1 Língua Portuguesa

**35. Unidade 2: Programação e orientação a objetos**
- [ ] Conclua as 5 lições da U1 (ou já tenha concluído): a **Unidade 2** abre, com as lições Fundamentos de programação,
      Orientação a objetos, Princípios SOLID, Linguagens e plataformas e Testes, TDD e Git.
- [ ] Na lição 1, o primeiro bloco começa com "Novo: Tipos e representação de números" e um exemplo resolvido.
      Exercícios com código (Java, C#, Python, PHP e C) mostram o trecho com quebra de linha e recuo legíveis.
- [ ] Depois dos 9 exercícios de ensino, vêm 3 questões de prova. Algumas são oficiais ("FGV · Banestes 2018 · …",
      "FCC · …"), por exemplo a do try/catch/finally (FGV 2018 q68) e a do ciclo do TDD (FCC Teresina 2016 q40).
- [ ] Na lição 4, cada linguagem (Java, C#, Python, PHP e C) tem o seu conceito; no exercício de completar do C, a
      lacuna é o `%d` do printf.

**36. G1: Língua Portuguesa**
- [ ] Na Trilha, toque em **Gerais**: G1 não aparece mais como "Conteúdo chegando em breve", e a lição 1 (Ortografia e
      crase) já vem aberta. G2 a G4 continuam "chegando".
- [ ] Os textos com aspas curvas (“ ”), travessão (—) e crase aparecem sem caracteres estranhos.
- [ ] Na lição 2 (Interpretação de texto), as questões oficiais da FGV Banestes 2018 e 2021 aparecem depois dos blocos
      de ensino, com o texto-base no enunciado.
- [ ] Respostas: o exercício de associar e o de completar funcionam como na U1; o "Não sei" aparece só nos de ensino.

**37. As duas unidades juntas**
- [ ] Depois de jogar uma lição de cada, o Perfil e o Praticar mostram os números das duas unidades novas.
- [ ] O selo "não revisada" aparece em todos os exercícios novos (são de IA).
- [ ] Um erro numa lição da U2 ou da G1 vai para o banco de erros e volta no treino, como na U1.

## BanQuest 0.12.0 — provas Banestes 2012 e 2024

**38. Questões das provas IDECAN 2012 e Instituto Access 2024**
- [ ] Nas lições da U1, da U2 e da G1, entre as questões de prova aparecem algumas com a origem "IDECAN · Banestes 2012 ·
      …" (com "gabarito preliminar") e "Instituto Access · Banestes 2024 · …".
- [ ] As de 2024 têm **4 alternativas** (A a D), como na prova. Responder uma delas funciona como as outras: acerto, erro
      e explicação.
- [ ] Exemplos: G1 lição 1 (acentuação e hífen, 2024 Q13); G1 lição 2 (texto sobre neutralidade no jornalismo, 2024 Q1–3);
      U1 lição 2 (comandos SQL, 2012 Q40); U2 lição 1 (vetor em C++, 2024 Q59, com o código formatado).
- [ ] Tocar na origem abre "Sobre esta questão", com a prova, a situação do gabarito e o aviso de explicação não revisada.

## O que ainda NÃO existe (não é bug)
Conteúdo das Unidades 3 a 10 e dos Gerais G2 a G4, simulado de prova, teste de nivelamento e missão mensal (v1.1 do
PLAN). Questões de prova das unidades sem pacote (por exemplo, Raciocínio de 2024) só aparecem quando a unidade ganhar
conteúdo.

## Verificações automáticas já executadas
Versão 0.4.0: `./gradlew check assembleDebug assembleRelease lintRelease` sem erros nem warnings; **135 testes
unitários**; validador de conteúdo sem erros; `tools/sql-tests/run.sh` (SQL aplicado duas vezes + testes de RLS e regras)
passando; e na harness (`tools/preview-harness`): 14 testes de integração, **10 de integração social** contra um Supabase
falso, 1 de migração v2→v3, 12 de acessibilidade e as renderizações das telas.

Versão 0.6.0: `./gradlew check` sem erros nem warnings (inclui `validateProvas` e `checkProvasPack`); harness com
85 testes (integração confere as 26 questões de prova na Unidade 1) e renders `s_exam*.png`; APK assinado conferido
(`dist/banquest-0.6.0.apk`, versionCode 9, com `assets/provas/provas-anteriores.json`).

Versão 0.7.1: `./gradlew check` sem erros nem warnings; 184 testes unitários (inclui `LessonPlannerTest` em blocos,
`HeartsEngineTest` com meio coração e `HelpEngineTest`); harness com 95 testes (migração 3→4, "Não sei" e meio coração
numa lição de verdade) e renders `s_learn_new.png`, `s_learn_quick.png`, `s_learn_skipped.png` e `s_practice_*.png`;
validador de conteúdo sem erros nem avisos; revisão em `content/review/u1-banco-de-dados.md`.

Versão 0.7.2: `./gradlew check` sem erros nem warnings; harness com 95 testes (inclui acessibilidade dos botões novos
↻, ⋯ e ⓘ) e renders `t_ranking.png`, `r_friends.png`, `t_shop.png`, `t_practice.png`, `s_result.png` e
`t_settings_full.png`.

Versão 0.7.3: `./gradlew check` sem erros nem warnings; 185 testes unitários; harness com 96 testes (inclui
`t_path_half_heart.png`, com o coração rachado na Trilha) e renders da lição `s_learn_*.png`, `s_exam*.png`,
`s_case.png` e `s_match.png`.

> Limite: nada disso substitui abrir o app num aparelho. Animações, vibração, toque, TalkBack e a virada de dia real só
> você consegue validar.


### Banco de dados por conceitos — pacote v4

- Abrir as lições 2 a 5 de Banco de Dados (concluir a anterior para liberar a próxima). Cada uma tem 5 conceitos e 30 exercícios no banco.
- Conferir o resumo de novo conceito e os blocos: exemplo resolvido, aplicação guiada e variação, seguidos por questões de prova. A sessão continua selecionando 3 conceitos e até 12 exercícios, além das repetições de erros.
- Repetir a lição: os 2 conceitos ainda não vistos devem receber prioridade. Conferir também os pares e as explicações de acerto/erro.
- Conferir que o histórico das questões q01–q10 foi mantido e que os itens novos mostram o selo de não revisada.
- Validação estrutural adicional em JavaScript: 170 IDs únicos, conceitos, níveis, passos, alternativas, pares e balanceamento de escolhas rápidas sem erros. O validador Python e o build Android não foram executados neste ambiente, que não disponibiliza Python utilizável.

## BanQuest 0.8.2 — questões oficiais nas lições 2 a 5

**34. Questões de prova de verdade nas lições 2 a 5 de Banco de Dados**
- [ ] Na lição 2 (SQL), depois dos blocos de ensino, aparecem questões com a origem "FCC · …" ou "FGV · Banestes …".
      Exemplos: ALTER TABLE (FCC DPE-RR 2015), classificação de COMMIT/CREATE/REVOKE (FCC Teresina 2016), JOIN com
      UNION (FGV Banestes 2018).
- [ ] Nas lições 3, 4 e 5 também aparecem questões oficiais (subconsultas, PostgreSQL e Oracle, star schema).
- [ ] A questão oficial só aparece quando o conceito dela já foi ensinado nesta sessão ou numa anterior.
- [ ] Um exercício adaptado de uma questão oficial nunca aparece na mesma sessão que a original.

Versão 0.8.2: `./gradlew check` sem erros nem warnings, com 185 testes unitários; validador de conteúdo sem erros (1
aviso); harness com 98 testes, incluindo a conferência de que toda questão de prova das lições por conceitos tem
conceito da lição.

Versão 0.9.0 (temas e trilha dos Gerais juntos com a 0.8.2): `./gradlew check` sem erros nem warnings; 195 testes
unitários (inclui `ContrastTest` nos quatro temas e nas trilhas dos Gerais, `TrailEngineTest` com as duas trilhas e
`LegacyPaletteTest`); validador de conteúdo sem erros (1 aviso); pacote de provas com 205 questões (123 dos Específicos
e 82 dos Gerais, esperando conteúdo); harness com 117 testes e renders `th_*.png` e `site_*.png`.

Versão 0.9.1 (lições por conceitos no ranking): `./gradlew check` sem erros nem warnings, com 198 testes unitários
(`SocialRulesTest` confere que o limite de XP da lição é exatamente o XP da melhor lição de 12 exercícios);
`tools/sql-tests/run.sh` passando, inclusive reaplicando o 0001 sobre um banco com o limite antigo; harness com 118
testes, entre eles `licaoPorConceitosPerfeitaEntraNoRanking` (lição real de 12 exercícios, 210 XP, chega ao ranking).

Versão 0.10.0 (provas anteriores): `./gradlew check` sem erros nem warnings; testes unitários de `PastExamTest`
(agrupamento, partes, regras de abrir e tempo), `LessonRunTest` (prova sem repetir erros), `ProgressProcessorTest` (XP
e ofensiva da prova, sem devolver coração) e `SocialRulesTest` (limites do tipo `exam`); `tools/sql-tests/run.sh` com
as provas no servidor e na ofensiva em dupla; harness com integração da prova (aviso, coração ao começar, XP só ao
terminar, abandonar, partes, modo sem vidas e Não perturbe) e renders `s_exam_intro*.png`, `s_exam_mode.png`,
`s_exam_result.png`, `s_nohearts_exam.png` e `t_practice.png`.

Versão 0.10.1 (treino sem farm): `SrsEngineTest` (acerto antes do dia não adianta), `LessonRunTest` (treino sem XP) e
`ProgressProcessorTest` (treino livre sem XP, coração, missão nem meta); harness com
`treinoForaDoPrazoERevisaoLivreNaoDaoXpNemCoracaoENaoAdiantamARevisao` e render `s_nohearts_nada_vencido.png`.

Versão 0.12.0 (U2 e G1): `./gradlew check` sem erros, com 196 testes unitários (inclui o teste que valida todos os
pacotes embutidos e o conceito de cada questão da U2 e da G1); validador de conteúdo com 3 pacotes e 492 exercícios,
sem erros (1 aviso antigo, u1l2q09); `validate_provas` com 218 questões OK; pacote de provas do app regerado (205
questões, 83 com conceito); harness com 117 testes, todos passando. Saídas de código da U2 conferidas rodando Java 17,
.NET 6 e Python 3.12.

Versão 0.12.0 (provas Banestes 2012 e 2024): `./gradlew check` sem erros, com 197 testes unitários (inclui o de 4
alternativas só em prova real); `validate_provas` com 353 questões OK; letras conferidas por script contra os dois
gabaritos oficiais; pacote de provas do app com 326 questões; harness com 117 testes, todos passando.
