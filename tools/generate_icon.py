#!/usr/bin/env python3
"""Gera as camadas do ícone adaptativo Maré de menta do BanQuest."""

from pathlib import Path

RES = Path(__file__).resolve().parent.parent / "app/src/main/res"

# Traçado da proposta original em uma prancheta de 112 unidades.
BODY = (
    "M33,65 Q18,63 20,45 Q21,41 23,43 L30,49 L32,41 Q35,39 36,45 "
    "Q39,54 33,57 L39,60 Q48,52 60,55 Q69,52 78,59 L82,54 "
    "Q76,45 80,39 L85,46 L91,38 Q96,51 89,58 Q86,62 80,63 "
    "Q80,78 58,80 Q38,80 33,65 Z"
)
SMILE = "M48,66 Q57,71 67,65 Q63,76 53,73 Q49,71 48,66 Z"
EYES = (
    "M47,58 L43,48 Q42,43 46,43 Q50,42 51,47 L53,56 Z "
    "M63,55 L65,45 Q66,41 70,42 Q73,43 72,47 L69,57 Z"
)


def foreground(color):
    # Os 72 dp centrais dos 108 dp correspondem à arte visível no launcher.
    return f'''    <group android:scaleX="0.642857" android:scaleY="0.642857"
        android:translateX="18" android:translateY="18">
        <path android:fillColor="{color}" android:pathData="{BODY} {SMILE}"
            android:fillType="evenOdd"/>
        <path android:fillColor="{color}" android:pathData="{EYES}"/>
    </group>'''


def vector(content):
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Gerado por tools/generate_icon.py. Não edite à mão. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
{content}
</vector>
'''


background = '''    <path android:pathData="M0,0h108v108H0Z">
        <aapt:attr name="android:fillColor">
            <gradient android:type="linear"
                android:startX="18" android:startY="18" android:endX="90" android:endY="90"
                android:startColor="#B4F2E5" android:endColor="#70DCD2"/>
        </aapt:attr>
    </path>'''

for name, content in (
    ("background", background),
    ("foreground", foreground("#273F72")),
    ("monochrome", foreground("#000000")),
):
    path = RES / "drawable" / f"ic_launcher_{name}.xml"
    path.write_text(vector(content), encoding="utf-8")

adaptive = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
'''
(RES / "mipmap-anydpi/ic_launcher.xml").write_text(adaptive, encoding="utf-8")
(RES / "mipmap-anydpi/ic_launcher_round.xml").write_text(adaptive, encoding="utf-8")
