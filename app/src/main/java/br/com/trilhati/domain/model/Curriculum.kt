package br.com.trilhati.domain.model

/**
 * Esqueleto da trilha (PLAN.md §6): 10 unidades e 48 lições dos Conhecimentos Específicos do cargo 04.
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
    )

    private fun unit(order: Int, title: String, vararg lessons: String) = UnitInfo("u$order", order, title, lessons.toList())

    fun lessonId(unitOrder: Int, lessonOrder: Int) = "u${unitOrder}l$lessonOrder"

    /** Todas as 48 lições da trilha. */
    val lessonIds: Set<String> by lazy {
        units.flatMap { u -> u.lessonTitles.indices.map { lessonId(u.order, it + 1) } }.toSet()
    }
}
