package br.com.trilhati.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.trilhati.MainActivity
import br.com.trilhati.R
import br.com.trilhati.domain.notify.ReminderKind
import br.com.trilhati.domain.notify.ReminderMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Canais e envio das notificações locais (PLAN.md §8). Nada sai do aparelho. */
@Singleton
class Notifications @Inject constructor(@ApplicationContext private val context: Context) {

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_DAILY, "Lembrete diário", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Lembra de estudar no horário escolhido, se você ainda não fez uma lição."
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_RISK, "Ofensiva em risco", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Aviso às 21:30 quando a ofensiva pode ser perdida."
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SOCIAL, "Amigos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Pedidos de amizade e conquistas do ranking."
            },
        )
    }

    /** Aviso social (pedido de amizade, pódio). Chega quando o app sincroniza (sem Firebase). */
    fun postSocial(title: String, text: String) {
        if (!canPost()) return
        val open = PendingIntent.getActivity(
            context, 1,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL)
            .setSmallIcon(R.drawable.ic_stat_trilha)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(SOCIAL_BASE_ID + (text.hashCode() and 0xFFFF), notification)
        } catch (_: SecurityException) {
            // Permissão revogada: sem aviso.
        }
    }

    /** `true` se o app pode mostrar notificações (permissão concedida e não bloqueadas pelo usuário). */
    fun canPost(): Boolean {
        val permissionOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return permissionOk && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun post(message: ReminderMessage) {
        if (!canPost()) return
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val channel = if (message.kind == ReminderKind.StreakRisk) CHANNEL_RISK else CHANNEL_DAILY
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_trilha)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .setContentTitle(message.title)
            .setContentText(message.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permissão revogada entre a checagem e o envio: não há o que fazer além de não avisar.
        }
    }

    /** Some com o lembrete pendente (por exemplo, depois que o aluno concluiu uma lição). */
    fun clear() = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    companion object {
        const val CHANNEL_DAILY = "lembrete_diario"
        const val CHANNEL_RISK = "ofensiva_em_risco"
        const val CHANNEL_SOCIAL = "amigos"
        private const val SOCIAL_BASE_ID = 2000
        private const val NOTIFICATION_ID = 1001
    }
}
