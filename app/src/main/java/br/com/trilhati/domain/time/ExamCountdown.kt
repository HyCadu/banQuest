package br.com.trilhati.domain.time

import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Dia da prova do Banestes (edital nº 01/2027, cap. 9). */
val EXAM_DATE: LocalDate = LocalDate.of(2027, 2, 21)

/** Dias que faltam até [date], contados no fuso do app. Negativo se já passou. */
fun Clock.daysUntil(date: LocalDate): Long = ChronoUnit.DAYS.between(today(), date)

/** Dias para a prova, nunca abaixo de zero (para exibir na interface). */
fun Clock.daysToExam(): Int = daysUntil(EXAM_DATE).coerceAtLeast(0).toInt()
