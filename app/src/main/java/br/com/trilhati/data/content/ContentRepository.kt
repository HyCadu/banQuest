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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/** Resultado da leitura de um pacote: a unidade ou os erros que impediram o uso. */
sealed interface PackLoad {
    data class Ok(val pack: PackDto, val unit: CourseUnit) : PackLoad
    data class Invalid(val name: String, val errors: List<String>) : PackLoad
}

/** Pacote importado pelo aluno (fica em `files/packs`). */
data class ImportedPack(val fileName: String, val pack: String, val version: Int, val unitTitle: String, val questions: Int)

/**
 * Carrega os pacotes de conteúdo embutidos (arquivos .json em `assets/content`) e os importados (em `files/packs`).
 * Para cada unidade vale o pacote de versão mais alta; no empate, o importado. Pacotes inválidos são ignorados (com log).
 */
@Singleton
class ContentRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val importDir get() = File(context.filesDir, IMPORT_DIR)

    private val _course: MutableStateFlow<List<CourseUnit>> by lazy { MutableStateFlow(build()) }

    /** A trilha completa, reemitida quando um pacote é importado ou removido. */
    val courseFlow: StateFlow<List<CourseUnit>> get() = _course.asStateFlow()

    /** Valor atual da trilha. Lições sem pacote vêm sem questões. */
    val course: List<CourseUnit> get() = _course.value

    private var lessonsById: Map<String, Lesson> = emptyMap()
    private var questionsById: Map<String, Question> = emptyMap()

    fun lesson(id: String): Lesson? = index().let { lessonsById[id] }
    fun question(id: String): Question? = index().let { questionsById[id] }
    fun questions(ids: Collection<String>): List<Question> = index().let { ids.mapNotNull { questionsById[it] } }
    fun allQuestions(): List<Question> = index().let { questionsById.values.toList() }

    private fun index() {
        if (lessonsById.isEmpty()) reindex(course)
    }

    private fun reindex(course: List<CourseUnit>) {
        lessonsById = course.flatMap { it.lessons }.associateBy { it.id }
        questionsById = lessonsById.values.flatMap { it.questions }.associateBy { it.id }
    }

    fun parse(name: String, text: String): PackLoad {
        val dto = try {
            json.decodeFromString(PackDto.serializer(), text)
        } catch (e: kotlinx.serialization.SerializationException) {
            return PackLoad.Invalid(name, listOf("JSON inválido: ${e.message?.take(120)}"))
        } catch (e: IllegalArgumentException) {
            return PackLoad.Invalid(name, listOf("JSON inválido: ${e.message?.take(120)}"))
        }
        val errors = ContentValidator.validate(dto)
        if (errors.isNotEmpty()) return PackLoad.Invalid(name, errors)
        val unit = dto.toDomain() ?: return PackLoad.Invalid(name, listOf("não foi possível mapear o pacote"))
        return PackLoad.Ok(dto, unit)
    }

    /** Valida e guarda um pacote escolhido pelo aluno. Devolve o resultado da validação. */
    fun importPack(text: String): PackLoad {
        val result = parse("importado", text)
        if (result is PackLoad.Ok) {
            importDir.mkdirs()
            File(importDir, "${result.pack.pack}.json").writeText(text, Charsets.UTF_8)
            reload()
        }
        return result
    }

    fun importedPacks(): List<ImportedPack> = importDir.listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }.mapNotNull { f ->
        (parse(f.name, f.readText(Charsets.UTF_8)) as? PackLoad.Ok)?.let {
            ImportedPack(f.name, it.pack.pack, it.pack.version, it.unit.title, it.unit.lessons.sumOf { l -> l.questions.size })
        }
    }

    fun removeImported(fileName: String) {
        File(importDir, fileName).takeIf { it.parentFile == importDir }?.delete()
        reload()
    }

    private fun reload() {
        _course.value = build()
    }

    private fun build(): List<CourseUnit> {
        val embedded = context.assets.list(ASSET_DIR).orEmpty().filter { it.endsWith(".json") }.sorted().map { name ->
            parse(name, context.assets.open("$ASSET_DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }) to false
        }
        val imported = importDir.listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }.map { f ->
            parse(f.name, f.readText(Charsets.UTF_8)) to true
        }
        (embedded + imported).map { it.first }.filterIsInstance<PackLoad.Invalid>()
            .forEach { Log.w(TAG, "Pacote ${it.name} ignorado: ${it.errors.take(3)}") }
        val chosen = (embedded + imported)
            .mapNotNull { (load, isImported) -> (load as? PackLoad.Ok)?.let { Triple(it, isImported, it.pack.version) } }
            .groupBy { it.first.unit.id }
            .values.map { candidates -> candidates.maxWith(compareBy({ it.third }, { it.second })).first.unit }
        return CourseAssembler.assemble(chosen).also { reindex(it) }
    }

    companion object {
        const val ASSET_DIR = "content"
        const val IMPORT_DIR = "packs"
        private const val TAG = "ContentRepository"
    }
}
