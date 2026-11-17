package br.com.trilhati.data.content

import android.content.Context
import android.util.Log
import br.com.trilhati.domain.exam.PastExam
import br.com.trilhati.domain.exam.PastExams
import br.com.trilhati.domain.exam.ExamCourse
import br.com.trilhati.domain.exam.TargetExam
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
 * As questões de provas anteriores (`assets/provas`) entram nas lições indicadas em cada uma ([CourseAssembler]).
 */
@Singleton
class ContentRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val importDir get() = File(context.filesDir, IMPORT_DIR)

    private val _catalog: MutableStateFlow<List<CourseUnit>> by lazy { MutableStateFlow(build()) }

    /**
     * O catálogo completo (todas as unidades das duas provas), reemitido quando um pacote é importado ou removido. A
     * trilha de cada prova sai dele em [course].
     */
    val catalogFlow: StateFlow<List<CourseUnit>> get() = _catalog.asStateFlow()

    /** Valor atual do catálogo. Lições sem pacote vêm sem questões. */
    val catalog: List<CourseUnit> get() = _catalog.value

    /** A trilha da prova [exam] (Específicos e Gerais), montada a partir do catálogo atual. */
    fun course(exam: TargetExam): List<CourseUnit> = ExamCourse.courseFor(exam, catalog)

    /**
     * As trilhas de todas as provas, juntas (uma unidade pode aparecer mais de uma vez). Serve para contar unidades
     * concluídas nas conquistas sem depender da prova escolhida ([br.com.trilhati.domain.progress.TrailEngine]).
     */
    val allCourses: List<CourseUnit> get() = TargetExam.entries.flatMap { course(it) }

    private var lessonsById: Map<String, Lesson> = emptyMap()
    private var questionsById: Map<String, Question> = emptyMap()

    /** Questões de provas anteriores do pacote embutido (lidas uma vez). */
    private val examPool: List<Question> by lazy { examQuestions() }

    /**
     * Provas anteriores para o modo da aba Praticar, montadas com todas as questões do pacote de provas, inclusive as que
     * ainda esperam o conteúdo da lição (como as dos Gerais). A parte dos Gerais de cada prova é a do cargo dela.
     */
    val exams: List<PastExam> by lazy {
        // Gerais e Específicos de cada prova seguem o cargo dela: a Matemática financeira é dos Específicos no Técnico.
        PastExams.from(examPool) { q ->
            val target = TargetExam.forCargo(q.ref?.cargo.orEmpty())
            target.general.any { it.unitId == q.lessonId.substringBefore('l') }
        }
    }

    /** As provas anteriores que valem para quem estuda para [target] ([PastExams.forTarget]). */
    fun exams(target: TargetExam): List<PastExam> = PastExams.forTarget(exams, target)

    fun exam(id: String, target: TargetExam): PastExam? = exams(target).firstOrNull { it.id == id }

    fun lesson(id: String): Lesson? = index().let { lessonsById[id] }
    fun question(id: String): Question? = index().let { questionsById[id] }
    fun questions(ids: Collection<String>): List<Question> = index().let { ids.mapNotNull { questionsById[it] } }
    fun allQuestions(): List<Question> = index().let { questionsById.values.toList() }

    private fun index() {
        if (lessonsById.isEmpty()) reindex(catalog)
    }

    private fun reindex(course: List<CourseUnit>) {
        lessonsById = course.flatMap { it.lessons }.associateBy { it.id }
        // As questões de prova que ainda não estão numa lição também entram: os erros da prova anterior vão para
        // "Praticar erros", que busca as questões por id.
        questionsById = examPool.associateBy { it.id } + lessonsById.values.flatMap { it.questions }.associateBy { it.id }
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
        _catalog.value = build()
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
        return CourseAssembler.assemble(chosen, examPool).also { reindex(it) }
    }

    /** Questões de provas anteriores embutidas. Um pacote inválido é ignorado inteiro (com log). */
    private fun examQuestions(): List<Question> =
        context.assets.list(EXAM_DIR).orEmpty().filter { it.endsWith(".json") }.sorted().flatMap { name ->
            val text = context.assets.open("$EXAM_DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseExam(name, text)
        }

    private fun parseExam(name: String, text: String): List<Question> {
        val dto = try {
            json.decodeFromString(ExamPackDto.serializer(), text)
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.w(TAG, "Pacote de provas $name ignorado: ${e.message?.take(120)}")
            return emptyList()
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Pacote de provas $name ignorado: ${e.message?.take(120)}")
            return emptyList()
        }
        val errors = ContentValidator.validate(dto)
        if (errors.isNotEmpty()) {
            Log.w(TAG, "Pacote de provas $name ignorado: ${errors.take(3)}")
            return emptyList()
        }
        return dto.toDomain()
    }

    companion object {
        const val ASSET_DIR = "content"
        const val IMPORT_DIR = "packs"
        const val EXAM_DIR = "provas"
        private const val TAG = "ContentRepository"
    }
}
