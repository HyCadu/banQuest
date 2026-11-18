#!/usr/bin/env python3
"""
Gera um pacote de conteúdo (u16-conhecimentos-bancarios.json, u12-raciocinio-logico.json) a partir de
content/source/<unidade>_*.py.

Cada arquivo de origem define LESSONS, uma lista de lições; cada lição tem conceitos, e cada conceito, os exercícios
na ordem: aprender (passos 1 e 2), praticar (2 e 3) e prova. Os exercícios são escritos com a alternativa certa
PRIMEIRO; o gerador espalha a posição da resposta (A a E nas de 5 alternativas, A a C na escolha rápida) em rodízio,
para a posição não ser previsível. Uso: python3 tools/gen_u16.py [u16|u12] (padrão: u16)
"""
import json
import runpy
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
UNIT_U16 = {
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

UNIT_U12 = {
    "id": "u12", "order": 12, "title": "Raciocínio lógico e estatística",
    "editalTopics": [
        "Estrutura lógica de relações arbitrárias; dedução de novas informações; raciocínio verbal, sequencial, espacial e temporal",
        "Processo lógico que, a partir de hipóteses, conduz de forma válida a conclusões",
        "Números inteiros e racionais; expressões numéricas; múltiplos e divisores; frações",
        "Razões e proporções; divisão proporcional; regra de três; porcentagem, acréscimos e descontos",
        "Noções de estatística: tendência central, dispersão, gráficos e tabelas; contagem",
    ],
}

# unidade: (pacote, unidade, tópico dos exercícios)
PACKS = {
    "u16": ("u16-conhecimentos-bancarios", UNIT_U16, "Conhecimentos bancários"),
    "u12": ("u12-raciocinio-logico", UNIT_U12, "Raciocínio lógico"),
}


def rotate(correct, wrongs, pos):
    """Põe a alternativa certa na posição pos, mantendo a ordem das erradas."""
    opts = list(wrongs)
    opts.insert(pos, correct)
    return opts


def build(unit_id="u16"):
    pack_name, unit, topic = PACKS[unit_id]
    src = sorted((ROOT / "content" / "source").glob(f"{unit_id}_*.py"))
    out = ROOT / "app" / "src" / "main" / "assets" / "content" / f"{pack_name}.json"
    lessons = []
    five_pos = 0  # rodízio das de 5 alternativas no pacote inteiro
    quick_pos = 0
    for path in src:
        for lesson in runpy.run_path(str(path))["LESSONS"]:
            lessons.append(lesson)
    lessons.sort(key=lambda l: l["order"])
    out_lessons = []
    for lesson in lessons:
        lid = f"{unit_id}l{lesson['order']}"
        concepts, questions = [], []
        n = 0
        for ci, c in enumerate(lesson["concepts"], 1):
            cid = f"{lid}c{ci}"
            concepts.append({"id": cid, "title": c["title"], "summary": c["summary"]})
            for item in c["items"]:
                n += 1
                q = {
                    "id": f"{lid}q{n:02d}", "type": item["type"], "topic": topic,
                    "subtopic": c.get("subtopic", c["title"].lower()), "difficulty": item.get("diff", 2),
                    "style": item.get("style", "conceitual"), "stem": item["stem"],
                }
                t = item["type"]
                if t in ("mcq5", "case"):
                    # "pos" fixa a posição quando as alternativas têm ordem natural (números, dias da semana).
                    pos = item.get("pos", five_pos % 5)
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
    pack = {"schema": 1, "pack": pack_name, "version": 1, "unit": unit, "lessons": out_lessons}
    out.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(f"{out.relative_to(ROOT)}: {len(out_lessons)} lições, {sum(len(l['questions']) for l in out_lessons)} exercícios")


if __name__ == "__main__":
    build(*sys.argv[1:2])
