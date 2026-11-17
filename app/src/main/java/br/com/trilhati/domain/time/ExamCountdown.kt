package br.com.trilhati.domain.time

import br.com.trilhati.domain.exam.TargetExam
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Dias que faltam até [date], contados no fuso do app. Negativo se já passou. */
fun Clock.daysUntil(date: LocalDate): Long = ChronoUnit.DAYS.between(today(), date)

/** Dias para a prova [exam], nunca abaixo de zero (para exibir na interface). */
fun Clock.daysToExam(exam: TargetExam = TargetExam.DEFAULT): Int = exam.daysLeft(today())
