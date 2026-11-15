# QA — roteiro para testar no celular

> Instale o APK mais recente por cima do anterior. O banco de dados é recriado uma vez (a versão anterior só tinha uma
> tabela vazia), então você começa do zero com 5 corações, 0 XP e nenhuma lição feita.

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

## O que ainda NÃO existe (não é bug)
Notificações de lembrete, sons, exportar/importar progresso, importar pacotes de conteúdo, e conteúdo das Unidades 2 a 10.

## Verificações automáticas já executadas
`./gradlew check assembleRelease lintRelease` sem erros nem warnings; **119 testes unitários**; validador de conteúdo sem
erros; e 11 testes de integração no harness (`tools/preview-harness`, ver README dele).

> Limite: nada disso substitui abrir o app num aparelho. Animações, vibração, toque, TalkBack e a virada de dia real só
> você consegue validar.
