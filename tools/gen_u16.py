#!/usr/bin/env python3
"""
Gera app/src/main/assets/content/u16-conhecimentos-bancarios.json a partir de content/source/u16_*.py.

Cada arquivo de origem define LESSONS, uma lista de lições; cada lição tem conceitos, e cada conceito, os exercícios
na ordem: aprender (passos 1 e 2), praticar (2 e 3) e prova. Os exercícios são escritos com a alternativa certa
PRIMEIRO; o gerador espalha a posição da resposta (A a E nas de 5 alternativas, A a C na escolha rápida) em rodízio,
para a posição não ser previsível. Uso: python3 tools/gen_u16.py
"""
import json
import runpy
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = sorted((ROOT / "content" / "source").glob("u16_*.py"))
OUT = ROOT / "app" / "src" / "main" / "assets" / "content" / "u16-conhecimentos-bancarios.json"

UNIT = {
    "id": "u16", "order": 16, "title": "Conhecimentos bancários e securitários",
    "editalTopics": [
        "Sistema Financeiro Nacional: estrutura, órgãos normativos, supervisores e operadores",
        "Instituições financeiras e de pagamento; produtos e serviços bancários; captação e aplicação de recursos",
        "Mercado financeiro e de capitais; política monetária, Copom, juros, inflação e indicadores",
        "Open Finance, Pix, meios eletrônicos de pagamento e inovação",
        "Prevenção à lavagem de dinheiro e ao financiamento do terrorismo; COAF",
        "Seguros, previdência complementar aberta, capitalização, bancassurance e consórcio",
        "Regulação e supervisão; adequação ao perfil do cliente; portabilidade; Resolução CMN nº 4.966/2021",
    ],
}


def rotate(correct, wrongs, pos):
    """Põe a alternativa certa na posição pos, mantendo a ordem das erradas."""
    opts = list(wrongs)
    opts.insert(pos, correct)
    return opts


def build():
    lessons = []
    five_pos = 0  # rodízio das de 5 alternativas no pacote inteiro
    quick_pos = 0
    for path in SRC:
        for lesson in runpy.run_path(str(path))["LESSONS"]:
            lessons.append(lesson)
    lessons.sort(key=lambda l: l["order"])
    out_lessons = []
    for lesson in lessons:
        lid = f"u16l{lesson['order']}"
        concepts, questions = [], []
        n = 0
        for ci, c in enumerate(lesson["concepts"], 1):
            cid = f"{lid}c{ci}"
            concepts.append({"id": cid, "title": c["title"], "summary": c["summary"]})
            for item in c["items"]:
                n += 1
                q = {
                    "id": f"{lid}q{n:02d}", "type": item["type"], "topic": "Conhecimentos bancários",
                    "subtopic": c.get("subtopic", c["title"].lower()), "difficulty": item.get("diff", 2),
                    "style": item.get("style", "conceitual"), "stem": item["stem"],
                }
                t = item["type"]
                if t in ("mcq5", "case"):
                    pos = five_pos % 5
                    five_pos += 1
                    q["options"] = rotate(item["correct"], item["wrong"], pos)
                    q["answer"] = pos
                elif t == "quick":
                    pos = quick_pos % 3
                    quick_pos += 1
                    q["options"] = rotate(item["correct"], item["wrong"], pos)
                    q["answer"] = pos
                elif t == "fill_blank":
                    pos = item.get("pos", quick_pos % 4)
                    quick_pos += 1
                    q["code"] = item["code"]
                    q["options"] = rotate(item["correct"], item["wrong"], pos)
                    q["answer"] = pos
                elif t == "truefalse":
                    q["options"] = ["Certo", "Errado"]
                    q["answer"] = 0 if item["true"] else 1
                elif t == "match":
                    q["pairs"] = [{"left": l, "right": r} for l, r in item["pairs"]]
                elif t == "order_code":
                    q["lines"] = item["lines"]
                q.update({"explanation": item["exp"], "source": "ia", "reviewed": False, "concept": cid,
                          "level": item["level"]})
                if item["level"] != "prova":
                    q["step"] = item["step"]
                questions.append(q)
        out_lessons.append({"id": lid, "order": lesson["order"], "title": lesson["title"], "concepts": concepts,
                            "questions": questions})
    pack = {"schema": 1, "pack": "u16-conhecimentos-bancarios", "version": 1, "unit": UNIT, "lessons": out_lessons}
    OUT.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{OUT.relative_to(ROOT)}: {len(out_lessons)} lições, {sum(len(l['questions']) for l in out_lessons)} exercícios")


if __name__ == "__main__":
    build()
