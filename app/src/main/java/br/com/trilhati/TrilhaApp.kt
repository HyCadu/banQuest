package br.com.trilhati

import android.app.Application
import br.com.trilhati.notify.Notifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TrilhaApp : Application() {
    @Inject lateinit var notifications: Notifications

    override fun onCreate() {
        super.onCreate()
        notifications.createChannels()
    }
}
