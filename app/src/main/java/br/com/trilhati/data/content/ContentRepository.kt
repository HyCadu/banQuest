package br.com.trilhati.data.content

import android.content.Context
import android.util.Log
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Lesson
import br.com.trilhati.domain.model.Question
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Resultado da leitura de um pacote: a unidade ou os erros que impediram o uso. */
sealed interface PackLoad {
    data class Ok(val pack: PackDto, val unit: CourseUnit) : PackLoad
    data class Invalid(val name: String, val errors: List<String>) : PackLoad
}

/**
 * Carrega os pacotes de conteúdo embutidos (arquivos .json em `assets/content`) e os importados (em `files/packs`).
 * Pacotes inválidos são ignorados (com log) em vez de derrubar o app.
 */
@Singleton
class ContentRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    /** A trilha completa; lições sem pacote vêm sem questões. Carregada uma vez. */
    val course: List<CourseUnit> by lazy { CourseAssembler.assemble(loadAll().filterIsInstance<PackLoad.Ok>().map { it.unit }) }

    private val lessonsById: Map<String, Lesson> by lazy { course.flatMap { it.lessons }.associateBy { it.id } }
    private val questionsById: Map<String, Question> by lazy { lessonsById.values.flatMap { it.questions }.associateBy { it.id } }

    fun lesson(id: String): Lesson? = lessonsById[id]
    fun question(id: String): Question? = questionsById[id]
    fun questions(ids: Collection<String>): List<Question> = ids.mapNotNull { questionsById[it] }
    fun allQuestions(): List<Question> = questionsById.values.toList()

    fun parse(name: String, text: String): PackLoad {
        val dto = try {
            json.decodeFromString(PackDto.serializer(), text)
        } catch (e: kotlinx.serialization.SerializationException) {
            return PackLoad.Invalid(name, listOf("JSON inválido: ${e.message}"))
        }
        val errors = ContentValidator.validate(dto)
        if (errors.isNotEmpty()) return PackLoad.Invalid(name, errors)
        val unit = dto.toDomain() ?: return PackLoad.Invalid(name, listOf("não foi possível mapear o pacote"))
        return PackLoad.Ok(dto, unit)
    }

    private fun loadAll(): List<PackLoad> {
        val results = mutableListOf<PackLoad>()
        val assetNames = context.assets.list(ASSET_DIR).orEmpty().filter { it.endsWith(".json") }.sorted()
        for (name in assetNames) {
            val text = context.assets.open("$ASSET_DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }
            results += parse(name, text)
        }
        val imported = File(context.filesDir, IMPORT_DIR).listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }
        for (file in imported) results += parse(file.name, file.readText(Charsets.UTF_8))
        results.filterIsInstance<PackLoad.Invalid>().forEach { Log.w(TAG, "Pacote ${it.name} ignorado: ${it.errors.take(3)}") }
        return results
    }

    companion object {
        const val ASSET_DIR = "content"
        const val IMPORT_DIR = "packs"
        private const val TAG = "ContentRepository"
    }
}
