package shot

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.db.MIGRATION_2_3
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Atualização do app 0.3 (banco v2, com a liga simulada) para a 0.4 (v3, social): o progresso do aluno precisa
 * sobreviver. Cria um banco v2 exatamente como o Room da 0.3 criava (app/schemas/…/2.json) e abre com a 0.4.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {
    private val schemaDir = File("../../../app/schemas/br.com.trilhati.data.db.AppDatabase")

    private fun createV2(file: File) {
        val schema = Json.parseToJsonElement(File(schemaDir, "2.json").readText()).jsonObject.getValue("database").jsonObject
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        schema.getValue("entities").jsonArray.forEach { e ->
            val o = e.jsonObject
            val table = o.getValue("tableName").jsonPrimitive.content
            db.execSQL(o.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table))
            o["indices"]?.jsonArray?.forEach { i -> db.execSQL(i.jsonObject.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table)) }
        }
        schema.getValue("setupQueries").jsonArray.forEach { db.execSQL(it.jsonPrimitive.content) }
        db.execSQL(
            "INSERT INTO stats VALUES (1, 640, 215, 3, 99, 3, 5, 20000, 1, 2, 1, 1, 0, 0, 18, 2520000, 16, 13, 2)",
        )
        db.execSQL("INSERT INTO league_week (weekStart, tier, userXp, closed, finalRank, outcome) VALUES (20408, 2, 320, 0, 0, 0)")
        db.version = 2
        db.close()
    }

    @Test fun v2ParaV3MantemProgressoECriaFila() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val file = context.getDatabasePath("migracao-teste.db").also { it.parentFile?.mkdirs(); it.delete() }
        createV2(file)
        val room = Room.databaseBuilder(context, AppDatabase::class.java, file.absolutePath)
            .addMigrations(MIGRATION_2_3).allowMainThreadQueries().build()
        val dao = room.progressDao()
        val stats = runBlocking { dao.stats() }!!
        assertEquals(640, stats.totalXp); assertEquals(215, stats.gems); assertEquals(3, stats.streakCurrent)
        assertEquals(0, stats.podiums)
        assertTrue(runBlocking { dao.outbox(10) }.isEmpty())
        val tables = room.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'").use { c ->
            buildList { while (c.moveToNext()) add(c.getString(0)) }
        }
        assertFalse("league_week deveria sumir", "league_week" in tables)
        assertTrue("outbox_session" in tables)
        room.close()
    }
}
