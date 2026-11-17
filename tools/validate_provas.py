#!/usr/bin/env python3
"""
Valida as questões de provas anteriores (content/provas/*.json): estrutura, gabarito, lixo de extração de PDF e metadados.
Uso: python3 tools/validate_provas.py [content/provas]
"""
import json, re, sys
from pathlib import Path

# Lições por unidade: Específicos u1–u10 (Curriculum.kt, PLAN.md §6) e Gerais u11–u14 (G1–G4, docs/CONTENT_PLAN.md).
LESSONS_PER_UNIT = [5, 5, 5, 5, 4, 6, 5, 4, 5, 4, 5, 8, 4, 4]
UNITS = {f"u{i}" for i in range(1, len(LESSONS_PER_UNIT) + 1)}
LESSONS = {f"u{u}l{l}" for u, n in enumerate(LESSONS_PER_UNIT, 1) for l in range(1, n + 1)}
TYPES = {"mcq5", "case", "code_output"}          # questões reais entram só nesses tipos (transcrição fiel)
STYLES = {"conceitual", "literal", "caso_pratico", "excecao"}
FLAGS = {"versao_antiga", "versao_citada_anterior", "produto_especifico", "fora_do_edital", "depende_de_figura"}
BANCAS = {"FCC", "FGV", "IDECAN", "Instituto Access", "CESPE", "CEBRASPE", "VUNESP", "CESGRANRIO"}
JUNK = re.compile(r"pcimark|pciconcursos|www\.|Tipo 1 – Cor|Página \d+|[-]|\s__\s*$|\w-\n\w")

def main(folder: Path) -> int:
    errors, total = [], 0
    for path in sorted(folder.glob("*.json")):
        pack = json.loads(path.read_text(encoding="utf-8"))
        ids = set()
        for q in pack["questions"]:
            total += 1
            where = f"{path.name}:{q.get('id')}"
            def err(msg): errors.append(f"{where}: {msg}")
            if q["id"] in ids: err("id repetido")
            ids.add(q["id"])
            ref = q.get("ref", {})
            for k in ("banca", "orgao", "ano", "cargo", "numero"):
                if not ref.get(k): err(f"ref.{k} faltando")
            if ref.get("banca") not in BANCAS: err(f"banca desconhecida: {ref.get('banca')}")
            if q.get("source") != "prova_real": err('source deve ser "prova_real"')
            if q.get("gabarito") not in ("definitivo", "preliminar", "nao_confirmado"): err("gabarito deve ser definitivo, preliminar ou nao_confirmado")
            if q.get("unit") not in UNITS: err(f"unidade inválida: {q.get('unit')}")
            if q.get("lesson") not in LESSONS: err(f"lição inválida: {q.get('lesson')}")
            elif not q["lesson"].startswith(f"{q.get('unit')}l"): err(f"lição {q['lesson']} não é da unidade {q.get('unit')}")
            elif "concept" in q and not re.fullmatch(re.escape(q["lesson"]) + r"c\d+", str(q["concept"])):
                err(f"conceito {q['concept']} não é da lição {q['lesson']} (esperado {q['lesson']}cN)")
            if q.get("type") not in TYPES: err(f"tipo inválido para questão real: {q.get('type')}")
            if q.get("style") not in STYLES: err(f"style inválido: {q.get('style')}")
            if q.get("difficulty") not in (1, 2, 3): err("difficulty deve ser 1, 2 ou 3")
            opts = q.get("options", [])
            # 5 alternativas (FCC, FGV, IDECAN) ou 4 (Banestes 2024): a questão fica como foi aplicada.
            if len(opts) not in (4, 5) or any(not str(o).strip() for o in opts): err("precisa de 4 ou 5 alternativas não vazias")
            if len({o.strip().lower() for o in opts}) != len(opts): err("alternativas repetidas")
            if any(len(o) > 350 for o in opts): err("alternativa longa demais (engoliu texto vizinho do PDF?)")
            letters = "ABCDE"[:len(opts)]
            if q.get("answer_letter") not in list(letters) or letters.index(q["answer_letter"]) != q.get("answer"):
                err("answer e answer_letter não batem")
            if len(q.get("stem", "").strip()) < 25: err("enunciado curto demais (faltou contexto?)")
            if q.get("type") == "code_output" and not q.get("code"): err('code_output exige "code"')
            for field in [q.get("stem", ""), q.get("code") or ""] + opts:
                if JUNK.search(field): err(f"lixo de extração do PDF: {JUNK.search(field).group(0)!r}")
            if len(q.get("explanation", "")) < 60 or "explicação pendente" in q.get("explanation", "").lower(): err("explicação faltando")
            if re.search(r"\bFCC\b", q.get("explanation", "") + q.get("stem", "")) and ref.get("banca") != "FCC":
                err('menciona "FCC" mas a questão não é da FCC (CLAUDE.md, regra 4)')
            for f in q.get("flags", []):
                if f not in FLAGS: err(f"flag desconhecida: {f}")
    for e in errors:
        print("ERRO", e)
    print(f"{total} questões em {folder}: {'OK' if not errors else f'{len(errors)} erro(s)'}")
    return 1 if errors else 0

if __name__ == "__main__":
    sys.exit(main(Path(sys.argv[1] if len(sys.argv) > 1 else "content/provas")))
