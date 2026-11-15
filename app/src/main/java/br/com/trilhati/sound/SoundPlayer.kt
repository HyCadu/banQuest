package br.com.trilhati.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import br.com.trilhati.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Efeitos sonoros curtos (PLAN.md §3). Os arquivos são gerados por `tools/generate_sounds.py`. */
enum class Sfx(val res: Int) {
    Correct(R.raw.snd_correct),
    Wrong(R.raw.snd_wrong),
    Combo(R.raw.snd_combo),
    Complete(R.raw.snd_complete),
    Achievement(R.raw.snd_achievement),
    Click(R.raw.snd_click),
}

@Singleton
class SoundPlayer @Inject constructor(@ApplicationContext context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids: Map<Sfx, Int> = Sfx.entries.associateWith { pool.load(context, it.res, 1) }

    /** Toca o efeito se [enabled]. Respeita o volume de mídia do aparelho (modo silencioso não toca). */
    fun play(sfx: Sfx, enabled: Boolean) {
        if (!enabled) return
        ids[sfx]?.let { pool.play(it, 0.8f, 0.8f, 1, 0, 1f) }
    }
}
