#!/usr/bin/env python3
"""
Gera o pacote de provas anteriores do app (app/src/main/assets/provas/provas-anteriores.json) a partir das questões
curadas em content/provas/*.json, que são a fonte da verdade.

Ficam de fora as questões que não valem para o edital atual (flags versao_antiga e fora_do_edital). Cada questão
leva a lição da trilha onde entra ("lesson") e a referência da prova ("ref"). O app junta essas questões às lições
que já têm pacote de conteúdo.

Uso:
  python3 tools/build_provas.py            # regrava o pacote
  python3 tools/build_provas.py --check    # só confere se o pacote está atualizado (usado no ./gradlew check)
"""
import json, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "content" / "provas"
OUT = ROOT / "app" / "src" / "main" / "assets" / "provas" / "provas-anteriores.json"
HIDDEN = {"versao_antiga", "fora_do_edital"}
KEEP = ["id", "lesson", "concept", "type", "topic", "subtopic", "difficulty", "style", "stem", "code", "options", "answer",
        "explanation", "source", "reviewed"]
REF = ["banca", "orgao", "ano", "cargo", "prova", "numero"]


def build() -> str:
    questions = []
    for path in sorted(SRC.glob("*.json")):
        for q in json.loads(path.read_text(encoding="utf-8"))["questions"]:
            if HIDDEN & set(q.get("flags", [])):
                continue
            item = {k: q[k] for k in KEEP if q.get(k) is not None}
            # A FGV fecha cada alternativa com ";" na prova impressa; no app isso sobra em "Resposta correta: 5;".
            item["options"] = [o[:-1].rstrip() if o.endswith(";") else o for o in q["options"]]
            item["ref"] = {k: q["ref"][k] for k in REF if q["ref"].get(k)} | {"gabarito": q["gabarito"]}
            questions.append(item)
    questions.sort(key=lambda q: (int(q["lesson"][1:].split("l")[0]), int(q["lesson"].split("l")[1]), q["id"]))
    pack = {"schema": 1, "pack": "provas-anteriores", "version": 1, "questions": questions}
    return json.dumps(pack, ensure_ascii=False, indent=1) + "\n"


def main() -> int:
    text = build()
    count = text.count('"lesson"')
    if "--check" in sys.argv:
        if not OUT.exists() or OUT.read_text(encoding="utf-8") != text:
            print(f"ERRO {OUT.relative_to(ROOT)} desatualizado: rode python3 tools/build_provas.py")
            return 1
        print(f"{OUT.relative_to(ROOT)}: atualizado ({count} questões)")
        return 0
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(text, encoding="utf-8")
    print(f"{OUT.relative_to(ROOT)}: {count} questões")
    return 0


if __name__ == "__main__":
    sys.exit(main())
