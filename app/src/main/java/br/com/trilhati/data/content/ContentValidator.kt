package br.com.trilhati.data.content

import br.com.trilhati.domain.model.QuestionType

/**
 * Validação de pacotes dentro do app (importação de pacotes e carga dos assets). É a contraparte em Kotlin do
 * `tools/validate_content.py`: o script valida o repositório no build; esta classe protege o app em tempo de execução.
 */
object ContentValidator {
    private val banned = listOf("todas as anteriores", "nenhuma das anteriores", "todas as alternativas", "nenhuma das alternativas")

    fun validate(pack: PackDto): List<String> {
        val errors = mutableListOf<String>()
        if (pack.schema != 1) errors += "schema ${pack.schema} não suportado (esperado 1)"
        if (pack.lessons.isEmpty()) errors += "pacote sem lições"
        val seen = mutableSetOf<String>()
        pack.lessons.forEachIndexed { i, lesson ->
            if (lesson.id != "${pack.unit.id}l${i + 1}") errors += "lição ${lesson.id}: id fora do padrão"
            lesson.questions.forEach { q -> validateQuestion(q, seen, errors) }
        }
        return errors
    }

    private fun validateQuestion(q: QuestionDto, seen: MutableSet<String>, errors: MutableList<String>) {
        val at = q.id
        if (!seen.add(q.id)) errors += "$at: id duplicado"
        val type = parseType(q.type)
        if (type == null) errors += "$at: type inválido (${q.type})"
        if (parseStyle(q.style) == null) errors += "$at: style inválido (${q.style})"
        if (parseSource(q.source) == null) errors += "$at: source inválido (${q.source})"
        if (q.source == "ia" && q.reviewed) errors += "$at: questão de IA não pode vir revisada"
        if (q.stem.isBlank()) errors += "$at: enunciado vazio"
        if (q.explanation.isBlank()) errors += "$at: explicação vazia"
        when (type) {
            null -> Unit
            QuestionType.Mcq5,
            QuestionType.Case,
            QuestionType.CodeOutput -> options(q, 5..5, errors)
            QuestionType.FillBlank -> {
                options(q, 4..5, errors)
                if (q.code?.contains("___") != true) errors += "$at: fill_blank precisa de code com a lacuna ___"
            }
            QuestionType.TrueFalse ->
                if (q.options != listOf("Certo", "Errado") || q.answer !in 0..1) errors += "$at: truefalse inválido"
            QuestionType.Match ->
                if (q.pairs.size !in 3..5) errors += "$at: match precisa de 3 a 5 pares"
            QuestionType.OrderCode ->
                if (q.lines.size !in 3..8) errors += "$at: order_code precisa de 3 a 8 linhas"
        }
        if (type == QuestionType.CodeOutput && q.code.isNullOrBlank()) errors += "$at: code_output exige code"
    }

    private fun options(q: QuestionDto, range: IntRange, errors: MutableList<String>) {
        if (q.options.size !in range) errors += "${q.id}: esperava ${range.first}..${range.last} alternativas, achei ${q.options.size}"
        if (q.options.toSet().size != q.options.size) errors += "${q.id}: alternativas repetidas"
        if (q.answer !in q.options.indices) errors += "${q.id}: answer fora do intervalo"
        if (q.options.any { o -> banned.any { it in o.lowercase() } }) errors += "${q.id}: alternativa proibida (todas/nenhuma das anteriores)"
    }
}
