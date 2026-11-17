package br.com.trilhati

import android.app.Application
import br.com.trilhati.notify.ExamFocus
import br.com.trilhati.notify.Notifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TrilhaApp : Application() {
    @Inject lateinit var notifications: Notifications
    @Inject lateinit var examFocus: ExamFocus

    override fun onCreate() {
        super.onCreate()
        notifications.createChannels()
        // O app foi fechado no meio de uma prova com o Não perturbe ligado: nenhuma prova está aberta agora, então desliga.
        examFocus.restore()
    }
}
