package br.com.trilhati.domain.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Fuso fixo do app: a virada do dia acontece à meia-noite de Brasília (CLAUDE.md, regra 7). */
val APP_ZONE: ZoneId = ZoneId.of("America/Sao_Paulo")

/** Relógio de produção. Toda lógica de datas recebe um [Clock]; ninguém chama `System.currentTimeMillis()`. */
fun systemAppClock(): Clock = Clock.system(APP_ZONE)

/** Dia de estudo "de hoje" segundo o [Clock] informado, no fuso do app. */
fun Clock.today(): LocalDate = LocalDate.now(withZone(APP_ZONE))

/** Instante atual segundo o [Clock] informado. */
fun Clock.nowInstant(): Instant = instant()
