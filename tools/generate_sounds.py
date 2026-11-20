#!/usr/bin/env python3
"""Gera os efeitos sonoros originais do app (ondas simples sintetizadas) em app/src/main/res/raw.

acerto, erro, combo, lição concluída, conquista, clique e a ignição da chama da ofensiva. WAV PCM 16 bits mono, 22050 Hz.
Uso: python3 tools/generate_sounds.py
"""
import math
import random
import struct
import wave
from pathlib import Path

RATE = 22050
OUT = Path(__file__).resolve().parent.parent / "app/src/main/res/raw"


def note(freq, dur, vol=0.5, shape="sine", attack=0.008, release=0.08, vibrato=0.0):
    n = int(RATE * dur)
    out = []
    for i in range(n):
        t = i / RATE
        f = freq * (1 + vibrato * math.sin(2 * math.pi * 6 * t))
        ph = 2 * math.pi * f * t
        if shape == "sine":
            v = math.sin(ph) + 0.25 * math.sin(2 * ph)  # um harmônico para soar "de jogo"
        elif shape == "triangle":
            v = 2 / math.pi * math.asin(math.sin(ph))
        else:
            v = 0.6 * math.copysign(1, math.sin(ph)) + 0.4 * math.sin(ph)
        env = min(1.0, t / attack) * min(1.0, (dur - t) / release) if dur > attack + release else min(1.0, t / attack)
        out.append(v * vol * max(0.0, env))
    return out


def silence(dur):
    return [0.0] * int(RATE * dur)


def glide(f0, f1, dur, vol=0.45, shape="triangle"):
    n = int(RATE * dur)
    out, ph = [], 0.0
    for i in range(n):
        t = i / RATE
        f = f0 + (f1 - f0) * (i / n)
        ph += 2 * math.pi * f / RATE
        v = 2 / math.pi * math.asin(math.sin(ph)) if shape == "triangle" else math.sin(ph)
        env = min(1.0, t / 0.01) * min(1.0, (dur - t) / 0.12)
        out.append(v * vol * env)
    return out


def mix(*tracks):
    n = max(len(t) for t in tracks)
    return [sum(t[i] for t in tracks if i < len(t)) for i in range(n)]


def write(name, samples):
    OUT.mkdir(parents=True, exist_ok=True)
    peak = max(1e-9, max(abs(s) for s in samples))
    scale = 0.85 / peak if peak > 0.85 else 1.0
    with wave.open(str(OUT / f"{name}.wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s * scale)) * 32767)) for s in samples))


C5, E5, G5, A5, C6, D6, E6, G6, C7 = 523.25, 659.25, 783.99, 880.0, 1046.5, 1174.7, 1318.5, 1568.0, 2093.0

write("snd_correct", note(C6, 0.09, 0.45) + note(G6, 0.22, 0.45, release=0.15))
write("snd_wrong", glide(330, 196, 0.32, 0.5))
write("snd_combo", note(E6, 0.07, 0.4) + note(G6, 0.07, 0.4) + note(C7, 0.2, 0.4, release=0.14))
write("snd_complete", note(C5, 0.11, 0.4) + note(E5, 0.11, 0.4) + note(G5, 0.11, 0.4) + mix(note(C6, 0.45, 0.4, release=0.3), note(E6, 0.45, 0.25, release=0.3)))
write("snd_achievement", mix(
    note(G5, 0.1, 0.35) + note(C6, 0.1, 0.35) + note(E6, 0.1, 0.35) + note(G6, 0.4, 0.35, release=0.3, vibrato=0.004),
    silence(0.3) + note(C7, 0.3, 0.12, release=0.25),
))
write("snd_click", note(1800, 0.018, 0.25, attack=0.001, release=0.012))


def whoosh(dur, vol=0.35, seed=7):
    """Sopro de ar subindo (ruído filtrado que abre e fecha), para a chama pegando fogo."""
    rnd = random.Random(seed)
    n = int(RATE * dur)
    out, low = [], 0.0
    for i in range(n):
        t = i / RATE
        k = 0.04 + 0.5 * (t / dur)  # o filtro abre: o sopro fica mais brilhante
        low += k * (rnd.uniform(-1, 1) - low)
        env = min(1.0, t / 0.06) * min(1.0, (dur - t) / 0.18)
        out.append(low * vol * 2.2 * env)
    return out


# Ignição da chama da ofensiva (0.15.0): sopro subindo e um acorde que acende por cima.
write("snd_ignite", mix(
    whoosh(0.55),
    glide(220, 660, 0.42, 0.18, shape="sine"),
    silence(0.32) + mix(note(G5, 0.5, 0.28, release=0.4), note(C6, 0.5, 0.22, release=0.4), note(E6, 0.5, 0.16, release=0.4)),
))
print("sons gerados em", OUT)
