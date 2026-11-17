package shot

import br.com.trilhati.data.remote.HttpResponse
import br.com.trilhati.data.remote.HttpTransport
import br.com.trilhati.data.remote.NetworkException
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/**
 * Supabase falso, em memória, falando o mesmo HTTP que o app usa (Auth em /auth/v1 e funções em /rest/v1/rpc).
 * Serve para testar o cliente inteiro (login, renovação de token, fila, retrato, conflito, amigos) sem rede.
 * As regras imitam as do SQL de supabase/migrations/0001_social.sql no essencial.
 */
class FakeSupabase(private val clock: Clock) : HttpTransport {
    var offline = false
    var otp = "123456"
    val paths = mutableListOf<String>()
    val emailsSent = mutableListOf<String>()

    private val json = Json { ignoreUnknownKeys = true }
    private val users = mutableMapOf<String, String>() // e-mail -> id
    private val accessTokens = mutableMapOf<String, Pair<String, Long>>() // token -> (id, vence em segundos)
    private val refreshTokens = mutableMapOf<String, String>()

    data class Profile(val id: String, var name: String, var mascot: String, val code: String, var showInGlobal: Boolean = true, var streak: Int = 0)
    data class Session(val id: String, val user: String, val day: LocalDate, val kind: String, val xp: Int)
    data class Snap(val version: Long, val device: String, val payload: JsonElement, val updatedAt: String)
    data class Request(val id: String, val from: String, val to: String, var status: String = "pending")

    val profiles = mutableMapOf<String, Profile>()
    val sessions = mutableListOf<Session>()
    val snapshots = mutableMapOf<String, Snap>()
    val friendships = mutableSetOf<Set<String>>()
    val requests = mutableListOf<Request>()
    var pushes = 0

    /** Versões publicadas (tabela app_releases do 0002). */
    data class Release(val code: Int, val name: String, val url: String, val notes: String = "", val minCode: Int = 1)
    val releases = mutableListOf<Release>()

    /** Outro aluno já cadastrado (para amizade). */
    fun seedUser(name: String, code: String, mascot: String = "Tartaruga", showInGlobal: Boolean = true): String {
        val id = UUID.randomUUID().toString()
        profiles[id] = Profile(id, name, mascot, code, showInGlobal)
        return id
    }

    fun userIdOf(email: String): String? = users[email]

    /** Faz o token atual de todo mundo vencer (o próximo pedido recebe 401). */
    fun expireAllTokens() = accessTokens.replaceAll { _, v -> v.first to 0L }

    private fun today(): LocalDate = LocalDate.now(clock)

    override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse {
        if (offline) throw NetworkException(java.io.IOException("offline"))
        val path = url.substringAfter(".co").substringBefore('?')
        paths += path
        if (headers["apikey"].isNullOrBlank()) return HttpResponse(401, """{"message":"No API key found in request"}""")
        val args = runCatching { json.parseToJsonElement(body).jsonObject }.getOrDefault(JsonObject(emptyMap()))
        return when {
            path == "/auth/v1/otp" -> otp(args)
            path == "/auth/v1/verify" -> verify(args)
            path == "/auth/v1/token" -> refresh(args)
            path == "/auth/v1/logout" -> HttpResponse(204, "")
            // pública (papel anon): não exige token de usuário
            path == "/rest/v1/rpc/latest_release" -> ok(
                releases.maxByOrNull { it.code }?.let { r ->
                    buildJsonObject {
                        put("version_code", r.code); put("version_name", r.name); put("apk_url", r.url); put("notes", r.notes)
                        put("min_version_code", r.minCode); put("published_at", clock.instant().toString())
                    }
                } ?: JsonNull,
            )
            path.startsWith("/rest/v1/rpc/") -> {
                val token = headers["Authorization"]?.removePrefix("Bearer ")
                val (uid, exp) = accessTokens[token] ?: return HttpResponse(401, """{"code":"PGRST301","message":"JWT invalid"}""")
                if (exp <= clock.millis() / 1000) return HttpResponse(401, """{"code":"PGRST303","message":"JWT expired"}""")
                rpc(path.removePrefix("/rest/v1/rpc/"), uid, args)
            }
            else -> HttpResponse(404, "{}")
        }
    }

    private fun otp(args: JsonObject): HttpResponse {
        val email = args.str("email") ?: return HttpResponse(400, """{"error_code":"validation_failed"}""")
        emailsSent += email
        users.getOrPut(email) { UUID.randomUUID().toString() }
        return HttpResponse(200, "{}")
    }

    private fun verify(args: JsonObject): HttpResponse {
        val email = args.str("email").orEmpty()
        val id = users[email]
        if (id == null || args.str("token") != otp) return HttpResponse(403, """{"code":403,"error_code":"otp_expired","msg":"Token has expired or is invalid"}""")
        return HttpResponse(200, issue(id, email))
    }

    private fun refresh(args: JsonObject): HttpResponse {
        val id = refreshTokens.remove(args.str("refresh_token")) ?: return HttpResponse(400, """{"error_code":"refresh_token_not_found"}""")
        return HttpResponse(200, issue(id, users.entries.first { it.value == id }.key))
    }

    private fun issue(id: String, email: String): String {
        val access = "at-" + UUID.randomUUID()
        val refresh = "rt-" + UUID.randomUUID()
        accessTokens[access] = id to (clock.millis() / 1000 + 3600)
        refreshTokens[refresh] = id
        return """{"access_token":"$access","refresh_token":"$refresh","expires_in":3600,"user":{"id":"$id","email":"$email"}}"""
    }

    // ------------------------------------------------------------------------------------------------ funções

    private fun rpc(fn: String, uid: String, a: JsonObject): HttpResponse {
        val needsProfile = fn !in setOf("my_profile", "upsert_my_profile", "delete_my_account")
        if (needsProfile && uid !in profiles) return HttpResponse(400, """{"code":"P0002","message":"perfil não criado"}""")
        return when (fn) {
            "my_profile" -> ok(profiles[uid]?.let(::profileJson) ?: JsonNull)
            "upsert_my_profile" -> {
                val p = profiles.getOrPut(uid) { Profile(uid, "", "Guaiamum", "TRI-" + (1..5).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("")) }
                p.name = a.str("p_display_name")!!.trim(); p.mascot = a.str("p_mascot")!!
                ok(profileJson(p))
            }
            "set_show_in_global" -> { profiles.getValue(uid).showInGlobal = a.bool("p_show"); ok(profileJson(profiles.getValue(uid))) }
            "update_my_streak" -> { profiles.getValue(uid).streak = a.getValue("p_streak").jsonPrimitive.int; ok(JsonObject(emptyMap())) }
            "submit_session" -> submit(uid, a)
            "social_overview" -> ok(overview(uid))
            "add_friend_by_code" -> {
                val other = profiles.values.firstOrNull { it.code == a.str("p_code") }
                ok(
                    when {
                        other == null -> buildJsonObject { put("result", "not_found") }
                        other.id == uid -> buildJsonObject { put("result", "self") }
                        setOf(uid, other.id) in friendships -> buildJsonObject { put("result", "already_friends"); put("display_name", other.name) }
                        else -> { friendships += setOf(uid, other.id); buildJsonObject { put("result", "added"); put("display_name", other.name) } }
                    },
                )
            }
            "request_friend" -> {
                val to = a.str("p_user")!!
                if (requests.any { it.from == to && it.to == uid && it.status == "pending" }) {
                    friendships += setOf(uid, to); requests.filter { it.from == to && it.to == uid }.forEach { it.status = "accepted" }
                    ok(JsonPrimitive("accepted"))
                } else {
                    requests += Request(UUID.randomUUID().toString(), uid, to); ok(JsonPrimitive("requested"))
                }
            }
            "respond_friend_request" -> {
                val r = requests.firstOrNull { it.id == a.str("p_request") && it.to == uid && it.status == "pending" } ?: return ok(JsonPrimitive("not_found"))
                if (a.bool("p_accept")) { r.status = "accepted"; friendships += setOf(r.from, r.to); ok(JsonPrimitive("accepted")) } else { r.status = "declined"; ok(JsonPrimitive("declined")) }
            }
            "remove_friend" -> { friendships -= setOf(uid, a.str("p_user")!!); ok(JsonPrimitive("removed")) }
            "push_snapshot" -> push(uid, a)
            "pull_snapshot" -> ok(
                snapshots[uid]?.let { s ->
                    buildJsonObject { put("version", s.version); put("device_id", s.device); put("updated_at", s.updatedAt); put("payload", s.payload) }
                } ?: JsonNull,
            )
            "delete_my_account" -> {
                profiles.remove(uid); snapshots.remove(uid); sessions.removeAll { it.user == uid }
                users.entries.removeAll { it.value == uid }
                ok(JsonPrimitive("deleted"))
            }
            else -> HttpResponse(404, """{"code":"PGRST202","message":"Could not find the function public.$fn"}""")
        }
    }

    private fun submit(uid: String, a: JsonObject): HttpResponse {
        val id = a.str("p_id")!!
        if (sessions.any { it.id == id }) return ok(buildJsonObject { put("ok", true); put("duplicate", true) })
        val kind = a.str("p_kind")!!
        val xp = a.getValue("p_xp").jsonPrimitive.int
        val answers = a.getValue("p_answers").jsonPrimitive.int
        val duration = a.getValue("p_duration_ms").jsonPrimitive.long
        val day = LocalDate.parse(a.str("p_day"))
        val max = if (kind == "lesson") 210 else 120 // iguais ao submit_session do 0001_social.sql
        val reason = when {
            xp !in 0..max -> "xp"
            duration < answers * 2_000L -> "too_fast"
            day.isAfter(today()) || day.isBefore(today().minusDays(7)) -> "day"
            else -> null
        }
        if (reason != null) return ok(buildJsonObject { put("ok", false); put("reason", reason) })
        sessions += Session(id, uid, day, kind, xp)
        profiles.getValue(uid).streak = a.getValue("p_streak").jsonPrimitive.int
        return ok(buildJsonObject { put("ok", true); put("duplicate", false) })
    }

    private fun push(uid: String, a: JsonObject): HttpResponse {
        val expected = a.getValue("p_expected_version").jsonPrimitive.long
        val current = snapshots[uid]
        val currentVersion = current?.version ?: 0
        if (expected >= 0 && expected != currentVersion) {
            return ok(
                buildJsonObject {
                    put("ok", false); put("reason", "conflict"); put("server_version", currentVersion)
                    put("server_device", current?.device); put("server_updated_at", current?.updatedAt)
                },
            )
        }
        pushes++
        snapshots[uid] = Snap(currentVersion + 1, a.str("p_device_id")!!, a.getValue("p_payload"), clock.instant().toString())
        return ok(buildJsonObject { put("ok", true); put("version", currentVersion + 1) })
    }

    /** Outro aparelho gravou um retrato (para provocar conflito). */
    fun pushFromOtherDevice(uid: String, payload: JsonElement) {
        val v = (snapshots[uid]?.version ?: 0) + 1
        snapshots[uid] = Snap(v, "android-outro", payload, clock.instant().toString())
    }

    private fun weekXp(uid: String): Int {
        val ws = today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return sessions.filter { it.user == uid && !it.day.isBefore(ws) && !it.day.isAfter(ws.plusDays(6)) }.sumOf { it.xp }
    }

    private fun overview(uid: String): JsonObject {
        val friends = profiles.values.filter { setOf(uid, it.id) in friendships }
        fun state(p: Profile) = when {
            p.id == uid -> "me"
            setOf(uid, p.id) in friendships -> "friend"
            requests.any { it.from == uid && it.to == p.id && it.status == "pending" } -> "outgoing"
            requests.any { it.from == p.id && it.to == uid && it.status == "pending" } -> "incoming"
            else -> "none"
        }
        fun ranking(people: List<Profile>) = buildJsonArray {
            val rows = people.map { it to weekXp(it.id) }.sortedByDescending { it.second }
            rows.forEach { (p, xp) ->
                add(
                    buildJsonObject {
                        put("user_id", p.id); put("display_name", p.name); put("mascot", p.mascot); put("xp", xp); put("streak", p.streak)
                        put("pos", rows.count { it.second > xp } + 1); put("friend_state", state(p))
                    },
                )
            }
        }
        val me = profiles.getValue(uid)
        val lessonToday = { id: String -> sessions.any { it.user == id && it.day == today() && it.kind == "lesson" } }
        return buildJsonObject {
            put("profile", profileJson(me))
            put("week_start", today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString())
            put("today", today().toString())
            put("global", ranking(profiles.values.filter { it.id == uid || it.showInGlobal || it in friends }))
            put("friends_ranking", ranking(friends + me))
            put(
                "friends",
                buildJsonArray {
                    friends.forEach { f ->
                        add(
                            buildJsonObject {
                                put("user_id", f.id); put("display_name", f.name); put("mascot", f.mascot); put("streak", f.streak)
                                put("week_xp", weekXp(f.id)); put("pair_streak", 0); put("me_today", lessonToday(uid)); put("friend_today", lessonToday(f.id))
                            },
                        )
                    }
                },
            )
            put(
                "requests",
                buildJsonArray {
                    requests.filter { it.to == uid && it.status == "pending" }.forEach { r ->
                        val from = profiles.getValue(r.from)
                        add(
                            buildJsonObject {
                                put("request_id", r.id); put("from_id", r.from); put("display_name", from.name); put("mascot", from.mascot)
                                put("created_at", clock.instant().toString())
                            },
                        )
                    }
                },
            )
            put("last_week", JsonNull)
        }
    }

    private fun profileJson(p: Profile) = buildJsonObject {
        put("id", p.id); put("display_name", p.name); put("mascot", p.mascot); put("friend_code", p.code)
        put("show_in_global", p.showInGlobal); put("streak", p.streak); put("streak_longest", p.streak); put("last_study_day", JsonNull)
    }

    private fun ok(body: JsonElement) = HttpResponse(200, body.toString())
    private fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
    private fun JsonObject.bool(key: String): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull == true
}
