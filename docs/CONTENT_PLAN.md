# Plano de conteúdo — Específicos do Analista (U1–U10), Gerais (G1–G5) e Técnico Bancário

Mapa para planejar a geração. Não é conteúdo. Fonte do escopo: [EDITAL.md](EDITAL.md) (Anexo IV,
transcrito). Onde o PLAN.md §6 e o edital divergem, vale o edital.

Prova: 21/02/2027, 50 questões de Específicos (peso 2) + 30 de Gerais (peso 1), em 4h. Os Gerais valem 1/3 da nota e
também eliminam, porque a média ponderada mínima é 6,00. Por isso entram na trilha (ver "Conhecimentos Gerais" abaixo).

## Visão geral

| Un. | Tema | Lições | Questões | Estado | Densidade | Risco factual |
|---|---|---|---|---|---|---|
| 1 | Banco de dados | 5 | 50 | **pronta**, por conceitos (170 exercícios, 27 conceitos) | média | baixo |
| 2 | Programação e OO | 5 | 50 | **pronta (0.12.0)**, por conceitos (154 exercícios, 26 conceitos) | alta (l4) | baixo |
| 3 | Engenharia de software e ágil | 5 | 50 | a fazer | alta (l4) | **alto** (normas) |
| 4 | Web, APIs e arquitetura | 5 | 50 | a fazer | **muito alta** (l3, l5) | médio |
| 5 | DevOps e containers | 4 | 40 | a fazer | média | baixo |
| 6 | Segurança e LGPD | 6 | 60 | a fazer | **muito alta** (l4, l5) | **alto** (normas) |
| 7 | Governança de TI | 5 | 50 | a fazer | alta | **alto** (versões novas) |
| 8 | Sistemas operacionais | 4 | 40 | a fazer | média | baixo |
| 9 | Redes | 5 | 50 | a fazer | **muito alta** (l2, l4) | médio |
| 10 | Serviços, monitoramento e nuvem | 4 | 40 | a fazer | alta (l1, l2) | médio (RFCs) |
| | **Subtotal Específicos** | **48** | **480** | U1 e U2 prontas | | |
| G1 | Língua Portuguesa | 5 | 50 | **pronta (0.12.0)**, por conceitos (168 exercícios, 28 conceitos) | alta | baixo |
| G2 | Raciocínio lógico e estatística (`u12`) | 4 | 40 | a fazer, serve às duas provas | alta | baixo (cálculo conferível) |
| G3 | Atualidades do mercado financeiro | 4 | 40 | a fazer | alta | **alto** (envelhece rápido) |
| G4 | Ética, compliance e ESG | 4 | 40 | a fazer | média | médio (guia do Banestes) |
| G5 | Matemática financeira (`u15`; no Técnico, Unidade 3, lições 1–3) | 4 | 40 | a fazer, serve às duas provas | alta | baixo (cálculo conferível) |
| | **Subtotal Gerais** | **21** | **210** | G1 pronta | | |
| | **Total** | **69** | **690** | 3 unidades prontas (U1, U2, G1) | | |

Densidade = tópicos do edital por lição de 10 questões. Acima de ~8 tópicos, cada um ganha uma questão ou nenhuma.

## Normas e versões confirmadas no edital

Citar nas questões **só** estas versões, exatamente assim:

| Área | Versão no edital |
|---|---|
| Processo | CMMI-DEV v2.0 · ABNT NBR ISO/IEC/IEEE 12207:2021 · MR-MPS-SW versão 2023 · UML 2.5 |
| Qualidade | ISO/IEC 25010:2024 |
| Governança | PMBOK 8ª edição · ITIL (Version 5) · COBIT 2019 · ABNT NBR ISO 31000:2018 · Lei nº 14.133/2021 · TOGAF (fundamentos) |
| Banco de dados | PostgreSQL 14 e 15 · Oracle 19c · H2 |
| Web | Bootstrap 5 · WCAG 2.1/2.2 · ABNT NBR 17225:2025 |
| SO | Red Hat e Oracle Linux · Windows 11 · Windows Server 2019 e 2022 |
| Redes | ABNT NBR 14565:2019 (cat. 5e, 6, 6a, 7) · IEEE 802.1D, 802.1Q/p, 802.1D-2004, 802.1Q-2005, 802.1X, 802.3, 802.3u, 802.3z, 802.3ae, 802.3af · 802.11n/ac/ax · RFC 2544 · RFC 2889 |
| Segurança | ABNT NBR ISO/IEC 27001:2024 · 27002:2022 · 27005:2023 · 27035-1:2023 · 27017:2016 · NIST SP 800-61 Rev. 3 · OWASP Top 10:2021 · LGPD (Lei nº 13.709/2018) |

## Diferenças do edital em relação ao PLAN.md §6

- **SOAP não aparece no edital.** O que aparece é **XML**: criação, declaração, elementos, atributos e XML Schema. U4l3
  troca o foco de SOAP para XML/XSD.
- **Tópicos que faltavam no PLAN** (entram nas lições indicadas abaixo):
  - Web e arquitetura: governança de APIs, autenticação/autorização em APIs, JSON em APIs públicas, integração com
    serviços externos, internet/extranet/intranet/portais, cliente-servidor, objetos distribuídos, legados × modernos.
  - Segurança: segurança Wi-Fi (EAP, WPA, WPA2, WPA3), antivírus/antispam, monitoramento de tráfego, VPN-SSL, defesa
    em profundidade, correlação de logs, segurança de APIs, LGPD no Poder Público.
  - Governança: stakeholders e comunicação, critérios de desempenho e conformidade em contratações, interoperabilidade e
    padronização no setor público, portfólio de **produtos digitais**.
  - Redes e serviços: comutação de circuitos, pacotes e células; WPAN/WLAN/WMAN/WWAN; redes de data center; RFC 2544 e
    2889; métodos de avaliação de desempenho; gerenciamento de usuários; logs de serviços.
  - SO: containers no SO, automação de tarefas administrativas, armazenamento secundário e terciário.
  - Nuvem: governança de nuvem, escalabilidade, serviços gerenciados (BD, mensageria, armazenamento, integração).
  - Processo: lista explícita de tipos de teste (funcionais, não funcionais, unitários, integração, sistema, aceitação,
    desempenho, carga, estresse, segurança, usabilidade); dados não estruturados.
- **Subnetting** não é citado, só "IPv4 e IPv6". Fica, mas com peso menor.
- **Inglês técnico** está nos Específicos do cargo 04 (confirmado).

## Por unidade

Legenda dos tipos: `mcq5`, `case`, `truefalse`, `match`, `order_code`, `fill_blank`, `code_output`, `quick` (escolha
rápida, 3 alternativas, só em exercícios de ensino).

### Formato das lições: por conceitos (decisão do dono, PR #8)
As lições passam a ser geradas no **formato por conceitos** (piloto na U1l1; regras em `CLAUDE.md` e `docs/DECISIONS.md`).
A coluna "Questões" da visão geral conta só as de **prova** (estilo FCC e oficiais); o banco da lição é maior:
- 4 a 7 conceitos pequenos por lição, cada um com exercícios de **aprender** e **praticar** em passos (`step` 1 = exemplo
  resolvido, 2 = aplicar, 3 = variar), um cenário por conceito, e ao menos uma questão de **prova**;
- exercício de ensino nunca cobra o que ainda não foi mostrado: a regra ou o exemplo vem no enunciado;
- exercícios feitos de questões oficiais levam `adaptedFrom` e nunca caem na mesma sessão da original;
- banco de 24 a 80 exercícios por lição (o validador já aceita), e a sessão sorteia 12 (3 conceitos × 3 passos + 3 de
  prova). Isso também atende às lições densas (ver abaixo): elas ganham mais conceitos, não mais questões soltas.
- Os "Tipos indicados" das tabelas abaixo valem para as questões de prova; nos exercícios de ensino entram também
  `quick`, `truefalse`, `match`, `fill_blank` e `order_code`.

### U1 — Banco de dados
- **l1 (Modelo ER e normalização): formato por conceitos** (7 conceitos, 50 exercícios no pacote + 6 oficiais),
  revisão em `content/review/u1-banco-de-dados.md`.
- **l2–l5: a converter** para o formato por conceitos, usando as 10 questões de cada uma como prova e as oficiais que já
  estão nelas.
- Pendências: o dono revisar as questões, com atenção a u1l4q06 (MERGE no PostgreSQL 15) e u1l2q06 (isolamento).
  Cobertura confere com o edital, incluindo governança e qualidade de dados.

### U2 — Programação e orientação a objetos
Decisão: **as cinco linguagens do edital (PHP, Python, C, Java, C#) e o .NET com peso igual.** Os trechos de código
das lições 1 e 2 alternam entre as linguagens; nenhuma é a "padrão".
**Feita na 0.12.0** (pacote `u2-programacao`, revisão em `content/review/u2-programacao.md`): 26 conceitos, 154
exercícios e as 22 oficiais ligadas a conceitos.

| Lição | Tópicos do edital | Tipos indicados | Observação |
|---|---|---|---|
| l1 Fundamentos | sintaxe, estrutura, compilação e execução, tipos primitivos, variáveis, literais, strings, operadores e precedência, controle e repetição, exceções, acesso a BD | `code_output`, `fill_blank` | rodízio de linguagens |
| l2 Orientação a objetos | classes, interfaces, métodos, atributos, encapsulamento, herança, polimorfismo, packages, sobrecarga × sobrescrita | `code_output`, `case`, `match` | Java e C# rendem mais aqui |
| l3 SOLID | os 5 princípios | `case`, `match` | 2 por princípio |
| l4 Linguagens e .NET | PHP, Python, C, Java, C#, .NET | `code_output`, `literal` | **densa:** 6 itens. Mínimo de 2 por linguagem |
| l5 Testes e Git | testes unitários e de integração, TDD, BDD, Git, GitFlow, PR, code review, IA assistente de código | `order_code`, `case` | IA responsável: só conceitual |

### U3 — Engenharia de software e ágil
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Processos | CMMI-DEV v2.0, 12207:2021, MR-MPS-SW 2023 | `mcq5`, `literal`, `match` | **risco alto:** níveis e nomes de processos mudam entre versões |
| l2 Requisitos e modelagem | engenharia de requisitos, UML 2.5, BPMN, modelagem de processos, dados estruturados/semiestruturados (XML, JSON)/não estruturados | `match`, `case` | |
| l3 Scrum, Kanban, XP | papéis, eventos, artefatos, WIP, práticas XP | `case`, `excecao` | Guia do Scrum 2020 como referência |
| l4 Testes e configuração | 11 tipos de teste, gestão de configuração, SemVer, code review | `case`, `fill_blank` (SemVer) | **densa** pelos tipos de teste. U2l5 = prática, U3l4 = processo |
| l5 Qualidade e documentação | ISO/IEC 25010:2024, low-code/no-code, documentação técnica, dicionário de dados, rastreabilidade | `match` | 25010:2024: conferir as características na própria norma |

### U4 — Web, APIs e arquitetura
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 HTML, CSS e JS | HTML5, CSS3, Bootstrap 5, JavaScript, TypeScript, mobile-first | `code_output`, `fill_blank` | |
| l2 Frameworks e plataformas | React, React Native, Angular, Vue.js, Node.js, Python e .NET na web, internet/extranet/intranet/portais | `match`, `mcq5` | densa |
| l3 APIs e XML | REST/RESTful, XML (declaração, elementos, atributos, XSD), JSON, OpenAPI, versionamento, idempotência, paginação, autenticação/autorização, governança de APIs, integração externa | `case`, `fill_blank` (XML), `match` | **densa.** Sem SOAP |
| l4 Usabilidade e acessibilidade | usabilidade, UX, WCAG 2.1/2.2, NBR 17225:2025 | `case`, `excecao` | NBR 17225 é nova: só o que ela compartilha com a WCAG |
| l5 Arquitetura | multicamadas, cliente-servidor, objetos distribuídos, SOA, eventos/filas/mensageria, MVC, DDD, hexagonal, cloud-native, API Gateway, service discovery, circuit breaker, retries, timeouts, legados × modernos, microsserviços | `case` | **a mais densa da trilha:** 16 conceitos |

### U5 — DevOps e containers
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 CI/CD e IaC | CI, CD, pipelines de build/teste/deploy, IaC, testes de regressão | `order_code`, `case` | |
| l2 Containers | containers e imagens, Docker, cluster, Kubernetes, orquestração, Rancher | `fill_blank` (Dockerfile), `match` | |
| l3 Implantação | blue/green, canary, deploy | `case` | rolling e feature flag só como distratores |
| l4 Observabilidade e DevSecOps | logs, métricas, traces, monitoramento contínuo, segurança em pipelines | `match`, `case` | ferramentas ficam na U10l2 |

### U6 — Segurança da informação e LGPD
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Normas e riscos | CID, 27001:2024, 27002:2022, 27005:2023, defesa em profundidade | `literal`, `match` | não citar número de controle |
| l2 Criptografia | simétrica, assimétrica, hash, certificados, assinatura digital, SSL/TLS | `case`, `match` | |
| l3 Identidade e acesso | autenticação, autorização, auditoria, RBAC, MFA, IAM, SSO, OAuth 2.0, OIDC | `case`, `order_code` | |
| l4 Proteção de redes | firewall, WAF, UTM, DMZ, proxy, NAC, antivírus, antispam, IDS/IPS, monitoramento de tráfego, Wi-Fi (EAP, WPA/2/3), VPN, VPN-SSL, ZTNA, malware, DoS/DDoS | `match`, `case` | **densa:** cerca de 16 itens |
| l5 Operação e incidentes | SOC, SIEM, EDR, endpoints, correlação de logs, 27035-1:2023, NIST SP 800-61 Rev. 3, Zero Trust, 27017:2016, OWASP Top 10:2021, segurança de APIs | `match`, `order_code` | **densa. Risco alto:** a Rev. 3 do NIST mudou a estrutura em relação à Rev. 2; não usar as fases antigas |
| l6 LGPD | fundamentos, princípios, bases legais, Poder Público, direitos, agentes, ANPD, anonimização, pseudonimização | `case`, `excecao` | sem número de artigo |

### U7 — Governança de TI
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Projetos e portfólio | PMBOK 8ª ed., portfólio de projetos e produtos digitais, stakeholders e comunicação | `mcq5`, `case` | **risco alto:** edição recente |
| l2 Serviços | ITIL Version 5, SLA, OLA, melhoria contínua | `match` | **risco alto:** não afirmar estrutura da v5 sem fonte |
| l3 COBIT e planejamento | COBIT 2019, PETIC, PDTIC, alinhamento estratégico | `literal`, `match` | |
| l4 Finanças e riscos | TCO, ROI, CAPEX, OPEX, FinOps, ISO 31000:2018 | `case` (cálculo simples) | |
| l5 Contratações e arquitetura | Lei 14.133/2021 em TIC, contratos e fornecedores, critérios de desempenho e conformidade, TOGAF, arquitetura corporativa, interoperabilidade no setor público, redação técnica e normativa | `case`, `excecao` | densa. Lei sem artigo |

### U8 — Sistemas operacionais
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Processos | processos, threads, alocação de CPU, sincronização, deadlock, starvation | `case`, `match` | |
| l2 Memória e E/S | segmentação, memória virtual, paginação, E/S, armazenamento secundário e terciário | `code_output` (substituição de páginas), `mcq5` | |
| l3 Linux | RHEL e Oracle Linux: instalação, configuração, administração, comandos, automação | `fill_blank`, `code_output` | |
| l4 Windows e virtualização | Windows 11, Server 2019/2022, AD, PowerShell, virtualização, containers no SO, hardening | `match`, `fill_blank` | |

### U9 — Redes de computadores
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Fundamentos | meios de transmissão, comutação de circuitos/pacotes/células, PAN/LAN/MAN/WAN, WPAN/WLAN/WMAN/WWAN, interconexão | `match` | |
| l2 TCP/IP | TCP/IP, IPv4, IPv6, TCP, UDP, IPsec, ARP, SSH, SMTP, HTTP, FTP, LDAP, DNS, DHCP, POP, IMAP | `match` (protocolo × função/porta), `code_output` | **densa:** 16 itens |
| l3 Roteamento e data center | MPLS, OSPF, BGP, VLAN, VXLAN, SDN, NFV, redes de data center | `match`, `case` | |
| l4 Cabeamento e padrões | NBR 14565:2019 (5e, 6, 6a, 7), fibra mono e multimodo, 10 padrões IEEE, 802.11n/ac/ax | `match` (padrão × função) | **densa.** Velocidades e distâncias: conferir |
| l5 QoS e voz | QoS para voz/vídeo/dados, VoIP, ToIP, videoconferência, SIP, H.323, multicast, IGMP | `match`, `mcq5` | no edital fica em "Serviços"; mantida aqui por afinidade |

### U10 — Serviços, monitoramento e nuvem
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Servidores e serviços | e-mail, DHCP, web proxy, WildFly, JBoss EAP, Apache HTTP Server, IIS, usuários, logs, VPN corporativa, acesso remoto seguro | `match`, `case` | densa |
| l2 Desempenho e monitoramento | SNMP (agentes, gerentes, MIBs), métricas, níveis de serviço, avaliação de desempenho, RFC 2544, RFC 2889, Zabbix, Elasticsearch, Logstash, Kibana, Grafana, Prometheus, Fluentd | `match` | **densa. RFCs: só o propósito de cada uma**, sem detalhe de métrica não conferido |
| l3 Nuvem | pública/privada/híbrida/multicloud, IaaS/PaaS/SaaS, migração, governança, custos, escalabilidade, alta disponibilidade, serviços gerenciados | `case` | |
| l4 Inglês técnico | leitura e interpretação de texto técnico | `mcq5` com trecho | ver abaixo |

**Inglês técnico (decidido): leitura e interpretação.** Cada questão traz um trecho curto em inglês (cerca de 60 a 120
palavras, tema de TI) dentro do enunciado, e pergunta sobre ideia principal, informação explícita, inferência, referência
de pronome ou sentido de palavra no contexto. Usa o tipo `mcq5` atual, sem mudar o app. Os trechos são autorais, não
copiados de sites ou documentação.

## Conhecimentos Gerais (G1–G5)

**0.13.0:** a antiga G2 (8 lições) foi dividida em G2 Raciocínio lógico e estatística (`u12`, lições 1–4) e G5
Matemática financeira (`u15`, as lições 5–8 de antes, agora 1–4). Assim as mesmas lições servem às duas provas: no
Técnico Bancário a matemática financeira é dos Específicos. Nenhuma questão oficial precisou mudar de lição (todas as da
G2 estavam nas lições 1–4).

Pedido do colaborador: incluir as matérias comuns. **Muda o escopo da v1** (CLAUDE.md, regra 5, e PLAN §2 diziam "só
Específicos"); o merge deste plano é a aprovação do dono. IDs nos pacotes: `u11` a `u14`, porque o validador e o
`Curriculum.kt` já aceitam esse formato. Na tela aparecem como G1 a G4.

Na prova: Português 10, Raciocínio e Estatística 10, Atualidades 5, Ética/ESG 5.

### G1 — Língua Portuguesa (`u11`)
**Feita na 0.12.0** (pacote `u11-lingua-portuguesa`, revisão em `content/review/u11-lingua-portuguesa.md`): 28
conceitos, 168 exercícios e as 35 oficiais da FGV ligadas a conceitos. Textos de interpretação autorais e curtos.

| Lição | Tópicos do edital | Tipos | Observação |
|---|---|---|---|
| l1 Ortografia e crase | ortografia, acentuação, crase | `mcq5`, `literal` | |
| l2 Interpretação de texto | compreensão, gêneros, contexto histórico, denotação e conotação, discursos direto/indireto/indireto livre, intertextualidade, figuras de linguagem | `mcq5` com trecho | textos autorais curtos, mesmo formato do Inglês |
| l3 Morfologia | estrutura e formação de palavras, sinonímia e antonímia, pronomes, flexão nominal e verbal, vozes do verbo, tempos e modos | `mcq5`, `match` | |
| l4 Sintaxe | morfossintaxe, concordância nominal e verbal, regência nominal e verbal, coordenação e subordinação, conectivos | `mcq5`, `excecao` | a mais cobrada da FCC em Português |
| l5 Pontuação e reescrita | pontuação, redação (frases corretas e incorretas, reorganização de períodos, equivalência de estruturas) | `mcq5` | estilo "reescrita mantém o sentido e a correção" |

### G2 — Raciocínio lógico e estatística (`u12`) e G5 — Matemática financeira (`u15`)

Na tabela, l5 a l8 são as lições 1 a 4 da `u15`.
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Lógica de relações | relações arbitrárias, dedução, raciocínio verbal, sequencial, espacial e temporal | `case` | estilo FCC clássico |
| l2 Aritmética | inteiros, racionais, frações, expressões, múltiplos e divisores | `mcq5` | |
| l3 Proporção e porcentagem | razão, proporção, divisão proporcional, regra de três, porcentagem, acréscimos e descontos | `case` | |
| l4 Estatística e contagem | média, moda, mediana, desvio, variância, gráficos e tabelas, princípio multiplicativo, permutação, combinação | `case` | gráficos descritos em texto (o app não mostra imagem) |
| l5 Juros e taxas | juros simples e compostos, montante, nominal × efetiva × real, equivalência de taxas e capitais, capitalização contínua | `case` | contas que dão para fazer à mão; a explicação mostra o cálculo |
| l6 Descontos e séries | desconto simples e composto (comercial e racional), séries uniformes e não uniformes | `case` | |
| l7 Amortização | Price, SAC, Sistema Misto | `case`, `match` | |
| l8 Avaliação de projetos | fluxo de caixa, valor presente e futuro, VPL, TIR | `case` | liga com U7l4 (ROI, TCO) |

Matemática financeira tem 4 lições porque é a parte mais larga do edital de Raciocínio. O Técnico Bancário vê só as 3
primeiras (o edital dele não cita VPL, TIR nem capitalização contínua). Questões de cálculo: conferir cada
resultado com script antes de gravar (o validador não confere contas).

### G3 — Atualidades do mercado financeiro (`u13`)
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Sistema Financeiro Nacional | estrutura, órgãos normativos, supervisores, operadores | `match` | estável |
| l2 Política monetária | Copom, Selic, inflação, juros e impactos | `case` | **nunca citar valor de taxa**, só o mecanismo |
| l3 Mercado e inovação | mercado financeiro e de capitais, produtos e indicadores, Open Finance, Pix, moedas digitais, fintechs, bancos digitais, instituições de pagamento | `match`, `case` | |
| l4 Cidadania financeira e cenários | educação e inclusão financeira, proteção do consumidor, finanças sustentáveis, ESG no mercado, cenários e tendências | `mcq5` | **risco alto:** "atualidades" envelhece. Só tendências estruturais, sem notícias datadas |

O app é offline e não atualiza sozinho. Atualidades de verdade (notícias de dez./2026 a fev./2027) ficam fora; o dono
estuda por fora ou importa um pacote novo perto da prova.

### G4 — Ética, compliance e ESG (`u14`)
| Lição | Tópicos | Tipos | Observação |
|---|---|---|---|
| l1 Ética e integridade | ética e moral, ética no serviço público e em instituições financeiras, integridade, transparência, conflito de interesses, Guia de Conduta Ética do Banestes | `case` | **o guia do Banestes precisa estar no repositório** antes de gerar (ver decisões) |
| l2 Governança e compliance | governança corporativa, compliance, controles internos, riscos, canais de denúncia, programas de integridade | `match`, `case` | |
| l3 Corrupção, fraude e dados | anticorrupção, fraudes, lavagem de dinheiro, privacidade, noções de LGPD | `case`, `excecao` | LGPD reaproveita o nível da U6l6, mais básico |
| l4 ESG e sustentabilidade | ESG, responsabilidade socioambiental, ESG no setor financeiro, Agenda 2030, ODS | `match` | número e nome dos ODS: conferir |

### Impacto no app

A trilha hoje é **uma fila só**: a primeira lição sem conteúdo trava tudo depois dela
(`domain/progress/TrailEngine.kt`). Se G1–G4 entrarem depois da U10, ficam travadas até o fim de TI. Opções:
1. **★ Duas trilhas independentes** (Específicos e Gerais), com uma troca no topo da Trilha. **Feito na 0.9.0.** Muda `Curriculum`,
   `TrailEngine`, a tela da Trilha e testes. Cada trilha desbloqueia na sua própria ordem.
2. Intercalar unidades Gerais entre as de TI (ex.: G1 depois da U2). Não muda código, mas obriga uma ordem fixa.
3. Desbloqueio livre por unidade. Perde a sensação de trilha.

Também muda o Perfil (precisão por tema) e, na Fase 10, o simulado passa a poder ter 80 questões (30 + 50), como a prova.

## Técnico Bancário (0.13.0)

Segunda prova no app, escolhida no chip do topo da Trilha. Edital transcrito em [EDITAL_TECNICO_BANCARIO.md](EDITAL_TECNICO_BANCARIO.md);
prova em 24/01/2027 (70 questões, 4 h). Os Gerais são os mesmos do Analista (G1–G4). Os Específicos, com peso 2:

| Un. | Id | Tema | Lições | Questões na prova | Estado | Risco factual |
|---|---|---|---|---|---|---|
| 1 | `u16` | Conhecimentos bancários e securitários | 7 | 15 | a fazer | **alto** (Res. CMN 4.966/2021, PLD/FT, COAF) |
| 2 | `u17` | Vendas, negociação e atendimento | 5 | 10 | a fazer | baixo |
| 3 | `u15` | Matemática financeira (lições 1–3, a mesma G5 do Analista) | 3 | 5 | a fazer | baixo |
| 4 | `u18` | Probabilidade e estatística | 4 | 5 | a fazer | baixo |
| 5 | `u19` | Informática e comportamentos digitais | 6 | 5 | a fazer | médio (Google Workspace e IA mudam rápido) |

Ordem sugerida, pelos pontos: `u16` (30 de 110 pontos), `u17`, a G2 e a G5 (servem às duas provas), `u18`, `u19`.
`u16` e a G3 (`u13`) cobrem o mesmo Sistema Financeiro Nacional: gere as duas juntas, sem repetir conceitos.

Provas anteriores: as do pacote são de cargos de TI; quem estuda para o Técnico vê só a parte de Conhecimentos Gerais das
provas do Banestes. Provas de Técnico Bancário entram quando o dono enviar os PDFs.

## Lições densas

Lições com mais de ~8 tópicos: U2l4, U3l4, U4l2, U4l3, U4l5, U6l4, U6l5, U7l5, U9l2, U9l4, U10l1, U10l2.

A decisão está com o dono na [issue #1](https://github.com/HyCadu/trilha-ti/issues/1). No formato por conceitos, a
opção ★ 1 já está coberta: o validador aceita de 24 a 80 exercícios por lição, e a lição densa ganha mais conceitos.
Opções:
1. **★ Banco maior nas lições densas (15–20 questões).** O app já sorteia 8 de qualquer tamanho; só o validador exige 10
   exatas (`QUESTIONS_PER_LESSON` em `tools/validate_content.py`). Não muda IDs nem progresso.
2. Dividir as lições. Muda `Curriculum.kt` e o número de lições.
3. Manter 10 e aceitar cobertura rasa.

## Ordem de criação sugerida

Mínimo para lançar (PLAN §6): U1–U4. Depois, por largura no edital:

1. ~~**U2** (base, risco baixo)~~ feita na 0.12.0, junto com a G1
2. **U4** (densa)
3. **U3** (normas a conferir)
4. **U6** (segurança, grande)
5. **U9** (redes)
6. **U5**, **U8**, **U10**
7. **U7** (maior risco de versão)

Gerais, em paralelo (depois que existir a segunda trilha): **G2** (matemática financeira, a mais larga e conferível),
**G1**, **G4**, e **G3** por último (envelhece mais rápido).

O dono pode reordenar pelo próprio ponto fraco.

## Calendário

Hoje 15/11/2026, prova em 21/02/2027: cerca de 14 semanas. Faltam 13 unidades, uns 640 a 760 questões.

- Geração: 2 a 3 unidades por semana. Todas prontas até ~03/01/2027.
- **O gargalo é a revisão humana**, não a geração. Sugestão: revisar cada unidade enquanto a estuda, priorizando as
  marcadas como risco alto.

## Checklist de geração (cada unidade)

1. Conferir os tópicos em [EDITAL.md](EDITAL.md) e contar as questões oficiais que já esperam pelas lições da unidade
   (`content/provas`, campo `lesson`): elas viram a prova de algum conceito e calibram o estilo.
2. Gerar o JSON no formato por conceitos (`concepts`, `concept`, `level`, `step`; `source: "ia"`, `reviewed: false`),
   com os tipos indicados acima. Dar `concept` às oficiais da lição em `content/provas` e regerar com
   `python3 tools/build_provas.py`.
3. `python3 tools/validate_content.py app/src/main/assets/content/` e `python3 tools/validate_provas.py`.
4. Segunda passada ("ache erros factuais e alternativas ambíguas"), relatório em `content/review/uN-*.md`.
5. Uma unidade por branch e por pull request (ver [CONTRIBUTING.md](../CONTRIBUTING.md)).

## Pendências do edital no conteúdo já existente

- **SOAP e WSDL não estão no edital.** As questões oficiais FGV18 Q76 e Q78, TER16 Q22 e DPE15 Q47 (todas da U4l3)
  devem ganhar `fora_do_edital` na PR da U4, quando a l3 virar "APIs e XML".

## Decisões abertas para o dono

1. **Lições densas:** [issue #1](https://github.com/HyCadu/trilha-ti/issues/1).
2. ~~**Gerais no app:** duas trilhas (★), intercalar, ou desbloqueio livre.~~ **Decidido (0.9.0):** duas trilhas
   independentes, com o seletor "Específicos | Gerais" no topo da Trilha (ver `docs/DECISIONS.md`).
3. **Guia de Conduta Ética do Banestes:** o edital indica o PDF no site do banco. O app não acessa a rede; alguém precisa
   baixá-lo e colocá-lo em `docs/` para G4l1 ser gerada sem inventar conteúdo.
4. **Ordem das unidades** conforme o ponto fraco.
5. **Questões reais da FCC** em `content/inbox/` para calibrar o estilo.

## Colaboração DCM

Cada unidade tem uma Issue com seu roteiro de lições e prompt. Contributors podem dividir as lições e definir os conceitos, respeitando o edital e o guia. Matemática financeira usa um único pacote compartilhado. PR pequeno por lição; revisão factual e validação obrigatórias.
