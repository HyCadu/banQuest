# QA — roteiro para testar no celular

> Instale o APK mais recente por cima do anterior. Desde a versão 0.2, o progresso é mantido entre atualizações.

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
- [ ] **Apagar todos os dados** e depois **Importar progresso** com o arquivo salvo: XP, ofensiva, lições e conquistas voltam.
- [ ] Configurações > Conteúdo > **Importar pacote**: escolha um arquivo que não seja pacote; o app recusa e explica.

**14. Toques que agora fazem algo**
- [ ] Toque no chip de **gemas**: abre a Loja. No chip de **dias**: mostra o planejamento até a prova. No **banner da
      unidade**: abre o guia com as lições e os tópicos do edital.
- [ ] Antes de estudar no dia, a **chama** do topo fica cinza; depois de uma lição, volta a ficar laranja.
- [ ] Durante a lição (modo Ritmo), o chip **"Lição mm:ss"** mostra quanto resta do tempo da lição.

**15. Acessibilidade**
- [ ] Ligue "Remover animações" nas opções de acessibilidade do Android: os mascotes param de se mexer.
- [ ] Fonte do sistema no máximo: o cabeçalho da lição quebra linha sem cortar nada.

## O que ainda NÃO existe (não é bug)
Conteúdo das Unidades 2 a 10, simulado de prova, teste de nivelamento e missão mensal (v1.1 do PLAN).

## Verificações automáticas já executadas
`./gradlew check assembleRelease lintRelease` sem erros nem warnings; **137 testes unitários**; validador de conteúdo sem
erros; e no harness (`tools/preview-harness`): 14 testes de integração, 10 de acessibilidade e as renderizações das telas.

> Limite: nada disso substitui abrir o app num aparelho. Animações, vibração, toque, TalkBack e a virada de dia real só
> você consegue validar.
