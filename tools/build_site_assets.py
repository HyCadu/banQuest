#!/usr/bin/env python3
"""Gera as imagens do site de divulgação (site-banquest/dist/assets/) a partir das telas reais do app.

Entrada: tools/preview-harness/shot/build/shots/site_*.png (SiteShots.kt na harness, tema Claro).
Saídas em site-banquest/dist/assets:
  tela-trilha.webp, tela-licao.webp, tela-revisao.webp  telas do celular do passeio (Trilha, Lição, Revisão)
  banquest-social.png                                   imagem de compartilhamento (og:image), 1200x630

Uso:
  ./gradlew -p tools/preview-harness :shot:testDebugUnitTest --tests 'shot.Site*'
  python3 tools/build_site_assets.py
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
SHOTS = ROOT / "tools/preview-harness/shot/build/shots"
OUT = ROOT / "site-banquest/dist/assets"
FONT = ROOT / "app/src/main/res/font/nunito.ttf"

# Cores do site (as do tema Claro do app).
PAPER, INK, MUTED, PURPLE, LILAC, MINT_SOFT, BEZEL = "#F6F2FF", "#1E1240", "#5A4C80", "#6B3FD6", "#E9DFFF", "#D4F8F4", "#392959"


def shot(name: str) -> Image.Image:
    path = SHOTS / f"site_{name}.png"
    if not path.exists():
        sys.exit(f"Falta {path}. Rode antes: ./gradlew -p tools/preview-harness :shot:testDebugUnitTest --tests 'shot.Site*'")
    return Image.open(path).convert("RGB")


def font(size: int, weight: int) -> ImageFont.FreeTypeFont:
    f = ImageFont.truetype(str(FONT), size)
    try:
        f.set_variation_by_axes([weight])
    except (OSError, AttributeError):
        pass
    return f


def screen(name: str, dest: str, width: int = 600) -> None:
    img = shot(name)
    img.resize((width, round(img.height * width / img.width)), Image.LANCZOS).save(OUT / dest, "WEBP", quality=84, method=6)


def social(trail: Image.Image, icon: Image.Image) -> Image.Image:
    W, H = 1200, 630
    card = Image.new("RGB", (W, H), PAPER)
    d = ImageDraw.Draw(card)
    d.ellipse((690, -150, 1330, 760), fill=LILAC)
    d.ellipse((800, -40, 1260, 680), fill=MINT_SOFT)
    # Celular com a trilha, como o do site.
    ph = 560
    pw = round(trail.width * ph / trail.height)
    x0, y0 = 840, 35
    d.rounded_rectangle((x0 - 12, y0 - 12, x0 + pw + 12, y0 + ph + 12), 40, fill=BEZEL)
    mask = Image.new("L", (pw, ph), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, pw, ph), 30, fill=255)
    card.paste(trail.resize((pw, ph), Image.LANCZOS), (x0, y0), mask)
    # Marca e frase do site ("Seu estudo, em modo jogo.").
    card.paste(icon.resize((72, 72), Image.LANCZOS), (80, 70))
    d.text((168, 78), "BanQuest", font=font(54, 900), fill=INK)
    title = font(92, 900)
    d.text((80, 200), "Seu estudo,", font=title, fill=INK)
    d.text((80, 300), "em modo jogo.", font=title, fill=PURPLE)
    sub = font(32, 700)
    d.text((80, 450), "Concurso Banestes · Analista de TI", font=sub, fill=MUTED)
    d.text((80, 494), "App para Android", font=sub, fill=MUTED)
    return card


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    screen("trilha", "tela-trilha.webp")
    screen("licao", "tela-licao.webp")
    screen("revisao", "tela-revisao.webp")
    social(shot("trilha"), shot("marca")).save(OUT / "banquest-social.png", optimize=True)
    for f in ["tela-trilha.webp", "tela-licao.webp", "tela-revisao.webp", "banquest-social.png"]:
        print(f"{f}: {(OUT / f).stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
