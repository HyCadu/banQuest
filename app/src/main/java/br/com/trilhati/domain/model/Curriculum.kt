package br.com.trilhati.domain.model

/**
 * Catálogo de todas as unidades e lições do app, das duas provas (docs/CONTENT_PLAN.md):
 * - `u1`–`u10`: Conhecimentos Específicos do Analista de TI (cargo 04);
 * - `u11`–`u15`: Conhecimentos Gerais (Português, Raciocínio, Atualidades, Ética e Matemática financeira);
 * - `u16`–`u19`: Conhecimentos Específicos do Técnico Bancário.
 *
 * Em qual trilha e em que ordem cada unidade aparece depende da prova escolhida ([br.com.trilhati.domain.exam.TargetExam]):
 * a Matemática financeira (`u15`) é dos Gerais no Analista e dos Específicos no Técnico.
 * As questões chegam nos pacotes de conteúdo; lições sem pacote aparecem como "em breve".
 */
data class UnitInfo(val id: String, val order: Int, val title: String, val lessonTitles: List<String>)

object Curriculum {
    val units: List<UnitInfo> = listOf(
        unit(1, "Banco de dados", "Modelo ER e normalização", "SQL: DML, DDL, DCL e transações", "Subconsultas, CTEs e funções analíticas", "PL/SQL, PL/pgSQL e SGBDs", "Modelagem dimensional, DW e NoSQL"),
        unit(2, "Programação e orientação a objetos", "Fundamentos de programação", "Orientação a objetos", "Princípios SOLID", "Linguagens e plataformas", "Testes, TDD e Git"),
        unit(3, "Engenharia de software e ágil", "Processos de software", "Requisitos, UML e BPMN", "Scrum, Kanban e XP", "Testes e gestão de configuração", "Qualidade e rastreabilidade"),
        unit(4, "Web, APIs e arquitetura", "HTML, CSS e JavaScript", "Frameworks front-end e Node.js", "Web services REST e SOAP", "Usabilidade e acessibilidade", "Arquitetura de software"),
        unit(5, "DevOps e containers", "CI/CD e infraestrutura como código", "Docker e Kubernetes", "Estratégias de implantação", "Observabilidade e DevSecOps"),
        unit(6, "Segurança da informação e LGPD", "Conceitos e gestão de riscos", "Criptografia e certificados", "Controle de acesso e identidade", "Proteção de redes e ataques", "Monitoramento e resposta a incidentes", "LGPD"),
        unit(7, "Governança de TI", "Projetos e portfólio", "ITIL e melhoria contínua", "COBIT e planejamento de TI", "Gestão financeira e de riscos", "Contratações, arquitetura corporativa e TOGAF"),
        unit(8, "Sistemas operacionais", "Processos, threads e sincronização", "Memória, E/S e armazenamento", "Linux", "Windows, AD e virtualização"),
        unit(9, "Redes de computadores", "Fundamentos e meios de transmissão", "TCP/IP e protocolos", "Roteamento, VLAN e SDN", "Cabeamento e Wi-Fi", "QoS, VoIP e multicast"),
        unit(10, "Serviços, monitoramento e nuvem", "Servidores e serviços", "Gerência de redes e monitoramento", "Computação em nuvem", "Inglês técnico"),
        // Conhecimentos Gerais (G1–G5). A G2 foi dividida na 0.13.0: Raciocínio (u12) e Matemática financeira (u15).
        unit(11, "Língua Portuguesa", "Ortografia e crase", "Interpretação de texto", "Morfologia", "Sintaxe", "Pontuação e reescrita"),
        unit(12, "Raciocínio lógico e estatística", "Lógica de relações", "Aritmética", "Proporção e porcentagem", "Estatística e contagem"),
        unit(13, "Atualidades do mercado financeiro", "Sistema Financeiro Nacional", "Política monetária", "Mercado e inovação", "Cidadania financeira e cenários"),
        unit(14, "Ética, compliance e ESG", "Ética e integridade", "Governança e compliance", "Corrupção, fraude e dados", "ESG e sustentabilidade"),
        unit(15, "Matemática financeira", "Juros e taxas", "Descontos e séries", "Amortização", "Avaliação de projetos"),
        // Conhecimentos Específicos do Técnico Bancário (Anexo IV do edital de nível médio).
        unit(16, "Conhecimentos bancários e securitários", "Sistema Financeiro Nacional", "Produtos e serviços bancários", "Mercado financeiro e política monetária", "Pix, Open Finance e inovação", "Lavagem de dinheiro e COAF", "Seguros, previdência, capitalização e consórcio", "Regulação e perfil do cliente"),
        unit(17, "Vendas, negociação e atendimento", "Atendimento ao cliente", "Comportamento do consumidor", "Vendas, prospecção e fidelização", "Negociação", "CRM, reclamações e canais digitais"),
        unit(18, "Probabilidade e estatística", "Dados, amostragem e gráficos", "Medidas de posição e dispersão", "Probabilidade", "Condicional e distribuição normal"),
        unit(19, "Informática e comportamentos digitais", "LGPD no setor bancário", "Hardware, software e sistemas", "Internet, redes e nuvem", "Google Workspace", "Segurança digital e golpes", "Inteligência artificial"),
    )

    private val byId: Map<String, UnitInfo> by lazy { units.associateBy { it.id } }

    fun unit(id: String): UnitInfo? = byId[id]

    private fun unit(order: Int, title: String, vararg lessons: String) = UnitInfo("u$order", order, title, lessons.toList())

    fun lessonId(unitOrder: Int, lessonOrder: Int) = "u${unitOrder}l$lessonOrder"

    /** Todas as lições do catálogo, das duas provas. */
    val lessonIds: Set<String> by lazy {
        units.flatMap { u -> u.lessonTitles.indices.map { lessonId(u.order, it + 1) } }.toSet()
    }
}
