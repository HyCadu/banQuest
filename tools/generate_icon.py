#!/usr/bin/env python3
"""Gera o ícone adaptativo do app a partir da geometria do mascote Guaiamum (ui/mascot/MascotBodies.kt).

Saída (em app/src/main/res):
  drawable/ic_launcher_background.xml  fundo turquesa com brilho
  drawable/ic_launcher_foreground.xml  Guaiamum sorrindo (cores do app)
  drawable/ic_launcher_monochrome.xml  silhueta para ícones temáticos (Android 13+)
  drawable/ic_stat_trilha.xml          ícone pequeno das notificações
  mipmap-anydpi/ic_launcher.xml e ic_launcher_round.xml
Uso: python3 tools/generate_icon.py
"""
import math
from pathlib import Path

RES = Path(__file__).resolve().parent.parent / "app/src/main/res"
INK = "#FF2B1A5E"
BODY, DARK, BELLY = "#FF6F6BF0", "#FF4B45C9", "#FFAFAAFF"

# Mapeia a grade 200x200 do mascote para a grade 108x108 do ícone adaptativo (zona segura: círculo de 66 dp).
S = 0.31
def X(x): return 54 + (x - 100) * S
def Y(y): return 57 + (y - 118) * S
def P(x, y): return f"{X(x):.2f},{Y(y):.2f}"
def W(w): return f"{w * S:.2f}"

def ellipse(cx, cy, rx, ry):
    a, b = rx * S, ry * S
    x0 = X(cx) - a
    return f"M{x0:.2f},{Y(cy):.2f} a{a:.2f},{b:.2f} 0 1,0 {2*a:.2f},0 a{a:.2f},{b:.2f} 0 1,0 {-2*a:.2f},0 z"

def pacman(cx, cy, r, gap_center, gap):
    start = math.radians(gap_center + gap / 2)
    end = math.radians(gap_center - gap / 2 + 360)
    sx, sy = cx + r * math.cos(start), cy + r * math.sin(start)
    ex, ey = cx + r * math.cos(end), cy + r * math.sin(end)
    return f"M{P(cx, cy)} L{P(sx, sy)} A{r*S:.2f},{r*S:.2f} 0 1,1 {P(ex, ey)} Z"

def stroke(path, color, width, extra=""):
    return f'    <path android:pathData="{path}" android:strokeColor="{color}" android:strokeWidth="{width}" android:strokeLineCap="round" android:strokeLineJoin="round" android:fillColor="#00000000"{extra}/>\n'

def fill(path, color, outline=None, width="1.6", alpha=None):
    o = f' android:strokeColor="{outline}" android:strokeWidth="{width}" android:strokeLineJoin="round"' if outline else ""
    a = f' android:fillAlpha="{alpha}"' if alpha else ""
    return f'    <path android:pathData="{path}" android:fillColor="{color}"{o}{a}/>\n'

LEGS = [((52, 128), (30, 138), (22, 156)), ((58, 138), (38, 152), (34, 172)), ((70, 146), (56, 164), (56, 184))]

def mirror(p): return (200 - p[0], p[1])

def leg_paths():
    out = []
    for a, b, c in LEGS:
        for f in (lambda p: p, mirror):
            out.append(f"M{P(*f(a))} L{P(*f(b))} L{P(*f(c))}")
    return out

def crab(mono=False):
    ink = "#FF000000" if mono else INK
    body = "#FF000000" if mono else BODY
    dark = "#FF000000" if mono else DARK
    s = ""
    for leg in leg_paths():
        s += stroke(leg, ink, W(14))
        if not mono: s += stroke(leg, dark, W(6))
    for top, base in (((76, 62), (80, 90)), ((124, 62), (120, 90))):
        s += stroke(f"M{P(*base)} L{P(*top)}", ink, W(15))
        if not mono: s += stroke(f"M{P(*base)} L{P(*top)}", dark, W(7))
    for shoulder, claw in (((50, 118), (26, 100)), ((150, 118), (172, 104))):
        s += stroke(f"M{P(*shoulder)} L{P(*claw)}", ink, W(16))
        if not mono: s += stroke(f"M{P(*shoulder)} L{P(*claw)}", dark, W(8))
    s += fill(pacman(26, 100, 21, 225, 44), dark, None if mono else ink, W(4.5))
    s += fill(pacman(172, 104, 16, 315, 44), dark, None if mono else ink, W(4.5))
    s += fill(ellipse(100, 122, 64, 46), body, None if mono else ink, W(5))
    if mono:
        # olhos vazados na silhueta
        for cx in (76, 124):
            s += fill(ellipse(cx, 52, 9.5, 9.5), "#FF000000")
        return s
    s += fill(ellipse(100, 142, 42, 20), BELLY)
    for sx, sy in ((80, 104), (100, 98), (120, 104)):
        s += fill(ellipse(sx, sy, 4.5, 4.5), DARK, alpha="0.45")
    s += f'    <group android:rotation="-25" android:pivotX="{X(78):.2f}" android:pivotY="{Y(100):.2f}">\n  ' + fill(ellipse(78, 100, 18, 8), "#FFFFFFFF", alpha="0.32") + "    </group>\n"
    for cx in (76, 124):  # olhos-bolinha
        s += fill(ellipse(cx, 52, 8.7, 8.7), "#FFFFFFFF", ink, W(4))
        s += fill(ellipse(cx, 53.5, 4.2, 7.0), INK)
        s += fill(ellipse(cx - 1.2, 49.5, 1.7, 1.7), "#FFFFFFFF")
    for bx in (66, 134):  # bochechas
        s += fill(ellipse(bx, 128, 9, 5.5), "#FFFF6F91", alpha="0.55")
    # sorriso aberto
    m, w, d = (100, 126), 13, 7 + 20 * 0.45
    mouth = f"M{P(m[0]-w, m[1]-2)} C{P(m[0]-w*0.9, m[1]+d*1.35)} {P(m[0]+w*0.9, m[1]+d*1.35)} {P(m[0]+w, m[1]-2)} Z"
    s += fill(mouth, "#FF5A1740", ink, W(3.5))
    s += fill(ellipse(100, 126 + d * 0.9, 6.5, 3.2), "#FFFF7A9A")
    return s

def vector(body, size=108, tint=None):
    t = f' android:tint="{tint}"' if tint else ""
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Gerado por tools/generate_icon.py. Não edite à mão. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{size}dp" android:height="{size}dp"
    android:viewportWidth="108" android:viewportHeight="108"{t}>
{body}</vector>
'''

background = (
    '    <path android:pathData="M0,0h108v108h-108z" android:fillColor="#FF19D3C5"/>\n'
    '    <path android:pathData="M-10,0 h70 a70,70 0 0,1 -70,70 z" android:fillColor="#FF5EE7DC" android:fillAlpha="0.55"/>\n'
    '    <path android:pathData="M0,86 Q54,74 108,86 V108 H0 Z" android:fillColor="#FF0E9C92" android:fillAlpha="0.35"/>\n'
)

def stat_icon():
    # mesma silhueta, recentrada numa grade 108 e desenhada em branco (o sistema colore)
    return vector(crab(mono=True).replace("#FF000000", "#FFFFFFFF"), size=24)

(RES / "drawable").mkdir(parents=True, exist_ok=True)
(RES / "mipmap-anydpi").mkdir(parents=True, exist_ok=True)
(RES / "drawable/ic_launcher_background.xml").write_text(vector(background))
(RES / "drawable/ic_launcher_foreground.xml").write_text(vector(crab()))
(RES / "drawable/ic_launcher_monochrome.xml").write_text(vector(crab(mono=True)))
(RES / "drawable/ic_stat_trilha.xml").write_text(stat_icon())
adaptive = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
'''
(RES / "mipmap-anydpi/ic_launcher.xml").write_text(adaptive)
(RES / "mipmap-anydpi/ic_launcher_round.xml").write_text(adaptive)
old = RES / "drawable/ic_launcher.xml"
if old.exists(): old.unlink()
print("ícone gerado em", RES)
