package br.com.trilhati.notify

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "Silenciar notificações durante a prova" (pedido do dono, 0.10.0): liga o Não perturbe do Android, no modo
 * prioridade, ao começar uma prova anterior e desliga quando ela acaba. Precisa da permissão
 * `ACCESS_NOTIFICATION_POLICY`, que o aluno libera uma vez nas configurações do Android.
 *
 * - Só liga se estava tudo liberado: quem já estava em Não perturbe continua como estava.
 * - Só desliga o que ele mesmo ligou, e só se o aluno não mudou nada no meio. A marca fica em SharedPreferences (a
 *   leitura é síncrona, porque também acontece quando a tela da prova é destruída).
 * - Se o app for fechado no meio da prova, o Não perturbe é desligado quando o app abrir de novo ([TrilhaApp]).
 */
@Singleton
class ExamFocus @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager: NotificationManager? get() = context.getSystemService(NotificationManager::class.java)
    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** O aluno já liberou o acesso ao Não perturbe para o BanQuest? */
    fun hasAccess(): Boolean = manager?.isNotificationPolicyAccessGranted == true

    /** Tela do Android onde se libera o acesso ao Não perturbe. */
    fun accessIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** A prova ligou o Não perturbe e ainda não desligou. */
    val active: Boolean get() = prefs.getBoolean(KEY_ACTIVE, false)

    /** Liga o Não perturbe para a prova. Devolve `true` se ligou agora. */
    fun silence(): Boolean {
        val nm = manager ?: return false
        if (!hasAccess() || nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) return false
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        prefs.edit { putBoolean(KEY_ACTIVE, true) }
        return true
    }

    /** Desliga o Não perturbe, se foi a prova que ligou. Pode ser chamado mais de uma vez. */
    fun restore() {
        if (!active) return
        prefs.edit { putBoolean(KEY_ACTIVE, false) }
        val nm = manager ?: return
        if (hasAccess() && nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    private companion object {
        const val PREFS = "exam_focus"
        const val KEY_ACTIVE = "dnd_on_by_exam"
    }
}
