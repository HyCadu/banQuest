# QA — roteiro para testar no celular

> Instale o APK mais recente por cima do anterior. Desde a versão 0.2, o progresso é mantido entre atualizações.
> **Exceção: da 0.4.x para a 0.5.0 (BanQuest)** é preciso sincronizar, desinstalar e instalar de novo uma vez, porque a
> assinatura passou a ser fixa. Veja o passo a passo em `docs/ASSINATURA.md`.
>
> **Versão 0.4.0 (social):** antes de instalar, faça os passos 1 a 3 de `docs/SUPABASE_SETUP.md` (SQL, login por código
> e SMTP). O roteiro da parte social está na seção "Versão social (0.4.0)", mais abaixo.

## Roteiro principal (≈15 minutos)

**1. Primeira tela = Trilha**
- [ ] O app abre direto na **Trilha**, com ofensiva, gemas, corações e "dias para a prova" no topo, e a barra da meta do dia.
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

## O que ainda NÃO existe (não é bug)
Conteúdo das Unidades 2 a 10, simulado de prova, teste de nivelamento e missão mensal (v1.1 do PLAN).

## Verificações automáticas já executadas
Versão 0.4.0: `./gradlew check assembleDebug assembleRelease lintRelease` sem erros nem warnings; **135 testes
unitários**; validador de conteúdo sem erros; `tools/sql-tests/run.sh` (SQL aplicado duas vezes + testes de RLS e regras)
passando; e na harness (`tools/preview-harness`): 14 testes de integração, **10 de integração social** contra um Supabase
falso, 1 de migração v2→v3, 12 de acessibilidade e as renderizações das telas.

> Limite: nada disso substitui abrir o app num aparelho. Animações, vibração, toque, TalkBack e a virada de dia real só
> você consegue validar.
