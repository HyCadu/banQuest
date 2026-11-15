# QA — passo a passo para testar no celular

## Fase 0 — Esqueleto do projeto

**Gerar o APK** (na máquina com Android SDK e JDK 17+):

```bash
./gradlew assembleDebug
```

O arquivo sai em `app/build/outputs/apk/debug/app-debug.apk`.

**Instalar no celular:**
1. Ative "Opções do desenvolvedor" e "Depuração USB" no aparelho e conecte por USB (ou copie o APK e permita "instalar apps desconhecidos").
2. Com ADB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

**Checklist**
- [ ] O ícone "Trilha TI" (triângulo âmbar sobre azul-petróleo, provisório) aparece na lista de apps.
- [ ] Ao abrir, o app mostra a **tela de diagnóstico**: título "Trilha TI", "Fase 0 — esqueleto funcionando", a data de hoje (Brasília) e "Banco de dados: aberto, sem perfil ainda". Se aparecer isso, Compose, Hilt, Clock e Room estão funcionando. Vale para modo claro e escuro.
- [ ] Se o app **fechar sozinho ao abrir**, anote o modelo do celular e a versão do Android e me avise.
- [ ] Girar a tela não fecha o app.
- [ ] Em Configurações do Android > Apps > Trilha TI > Permissões: **nenhuma permissão** listada.
- [ ] O app funciona com o modo avião ligado (não usa rede).

**Verificações automáticas (já executadas na entrega):**

```bash
./gradlew assembleDebug test lint
./gradlew assembleRelease lintRelease
```

> Observação: a Fase 0 foi verificada por compilação, testes unitários e lint. O app **não foi aberto em um aparelho/emulador** no ambiente de desenvolvimento (nuvem, sem emulador), então o checklist acima é a primeira execução real.

---

## Fase 1 — Design system, mascotes e galeria

**Instalar:** use o APK `trilha-ti-fase1-debug.apk` (ou `./gradlew assembleDebug`). Ele substitui a versão da Fase 0 sem apagar dados.

O app agora abre no **catálogo de design**: é a vitrine de como o app vai parecer. Role a tela de cima para baixo.

**Checklist**
- [ ] No topo: título "Trilha TI", chips de ofensiva (12), gemas (340), corações (5) e "98 d" (dias para a prova; confira se bate com a data de hoje).
- [ ] Botões "Tema do sistema / Claro / Escuro" trocam o tema na hora. Confira **os dois temas** em todas as telas.
- [ ] **Mascotes:** escolha cada personagem e cada pose (Parado, Feliz, Triste, Comemorando, Sonolento). Eles devem **se mexer** (respirar, piscar, pular quando comemora, "Z" quando sonolento, lágrima quando triste, confete ao comemorar).
- [ ] **Todas as poses:** os 4 personagens × 5 poses aparecem lado a lado, sem cortes.
- [ ] **Botões:** tocar e segurar faz o botão "afundar". "Bloqueado" não reage.
- [ ] **Cronômetro de ritmo:** três anéis (turquesa, âmbar, vermelho que pisca) e as pílulas "Folga 0:42" e "Atraso 0:15".
- [ ] **Trilha:** nós de lição (coroa dourada, check turquesa, atual roxo **pulsando** com balão "Começar", bloqueados) e a tartaruga ao lado do nó atual.
- [ ] **Tela de questão:** toque em uma alternativa (ela destaca), "Verificar" habilita; ao verificar, a certa fica verde, a errada vermelha e sobe o painel com o mascote reagindo. "Continuar" reinicia o exemplo.
- [ ] **Barra inferior:** 5 abas; a ativa ganha pílula roxa e o ícone dá um pulinho; a aba Praticar tem uma bolinha vermelha.
- [ ] **TalkBack** ligado: as abas dizem "selecionada"; as alternativas leem "Alternativa A" e o estado; os nós leem "Lição 3, disponível".
- [ ] **Fonte grande** (Configurações do Android > Tela > Tamanho da fonte): nada fica cortado nem sobreposto.
- [ ] Gire a tela: o app não fecha e mantém o tema escolhido.

**O que eu quero saber de você (feedback de design)**
- As cores e o "clima" estão como você imaginou?
- Os 4 mascotes agradam? Algum precisa de retoque (já sei que o Tamanduá é o mais fraco)?
- Os botões, a barra inferior e os nós da trilha parecem "de jogo" o bastante?

**Verificações automáticas (já executadas):** `./gradlew assembleDebug test lint` e `./gradlew assembleRelease lintRelease`, sem warnings. 11 testes unitários (relógio, contagem regressiva da prova, contraste de cores).

> Limite: o app **não foi aberto em aparelho real** no ambiente de desenvolvimento. O visual foi conferido por renderização de imagem (claro e escuro), mas animações, toque e TalkBack só você consegue validar no celular.
