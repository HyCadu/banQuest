package br.com.trilhati.domain.time

import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/** Emite o dia de hoje (fuso do app) e reemite quando a meia-noite passa. */
fun Clock.todayFlow(checkEveryMs: Long = 15_000): Flow<LocalDate> = flow {
    while (true) {
        emit(today())
        delay(checkEveryMs)
    }
}.distinctUntilChanged()

/** Emite o instante atual em milissegundos a cada [periodMs] (para contagens regressivas e cronômetros). */
fun Clock.millisFlow(periodMs: Long = 1_000): Flow<Long> = flow {
    while (true) {
        emit(millis())
        delay(periodMs)
    }
}
