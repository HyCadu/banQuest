package br.com.trilhati.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Perfil do usuário (PLAN.md §5.2). Linha única, `id = 1`. Demais entidades entram nas fases seguintes. */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ID,
    val name: String,
    val character: String,
    val dailyGoalXp: Int,
    val createdAtEpochMillis: Long,
) {
    companion object {
        const val SINGLE_ID = 1
    }
}
