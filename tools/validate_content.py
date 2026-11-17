#!/usr/bin/env python3
"""Valida os pacotes de conteúdo do Trilha TI (PLAN.md §5.1 e §6, CLAUDE.md).

Uso:  python3 tools/validate_content.py app/src/main/assets/content/

Erros (saem com código 1): JSON inválido, schema, ids, quantidade de alternativas, resposta fora do intervalo,
explicação vazia, "todas as anteriores", duplicatas, questão de IA marcada como revisada, menção a "FCC" em
questão que não é fcc_real, posições da resposta correta muito desbalanceadas.
Avisos: mistura de estilos fora da meta, alternativa correta sempre a mais longa, alternativas de tamanhos muito
diferentes, ausência de relatório de revisão em content/review/.
"""
import difflib
import json
import re
import sys
from collections import Counter
from pathlib import Path

TYPES = {"mcq5", "truefalse", "match", "order_code", "fill_blank", "code_output", "case", "quick"}
FIVE_OPTION_TYPES = {"mcq5", "case", "code_output"}
STYLES = {"conceitual", "literal", "caso_pratico", "excecao"}
SOURCES = {"autoral", "ia", "fcc_real"}
LEVELS = {"aprender", "praticar", "prova"}
QUESTIONS_PER_LESSON = 10          # lição no formato antigo (só questões de prova)
CONCEPT_LESSON_RANGE = (24, 80)    # lição por conceitos: banco maior, a sessão sorteia 12
BANNED = ("todas as anteriores", "nenhuma das anteriores", "todas as alternativas", "nenhuma das alternativas")

errors, warnings = [], []
ROOT = Path(__file__).resolve().parent.parent


def load_official():
    """Questões oficiais curadas (content/provas), para conferir as adaptadas e os conceitos das oficiais."""
    found = {}
    for f in sorted((ROOT / "content" / "provas").glob("*.json")):
        for q in json.loads(f.read_text(encoding="utf-8"))["questions"]:
            found[q["id"]] = {**q["ref"], "lesson": q.get("lesson"), "concept": q.get("concept"), "id": q["id"]}
    return found


OFFICIAL = load_official()


def err(where, msg):
    errors.append(f"ERRO   {where}: {msg}")


def warn(where, msg):
    warnings.append(f"AVISO  {where}: {msg}")


def norm(text):
    return re.sub(r"\W+", " ", text.lower()).strip()


def check_options(where, q, expected):
    opts = q.get("options")
    if not isinstance(opts, list) or not all(isinstance(o, str) and o.strip() for o in opts):
        err(where, "options deve ser lista de textos não vazios")
        return
    if isinstance(expected, int) and len(opts) != expected:
        err(where, f"esperava {expected} alternativas, achei {len(opts)}")
    if isinstance(expected, tuple) and not (expected[0] <= len(opts) <= expected[1]):
        err(where, f"esperava de {expected[0]} a {expected[1]} alternativas, achei {len(opts)}")
    # Só minúsculas e espaços: símbolos contam ("x = NULL" e "x <> NULL" são alternativas diferentes).
    if len({re.sub(r"\s+", " ", o.lower()).strip() for o in opts}) != len(opts):
        err(where, "alternativas repetidas")
    ans = q.get("answer")
    if not isinstance(ans, int) or isinstance(ans, bool) or not (0 <= ans < len(opts)):
        err(where, f"answer deve ser índice entre 0 e {len(opts) - 1}")
    for o in opts:
        if any(b in o.lower() for b in BANNED):
            err(where, f'alternativa proibida: "{o}"')
    if len(opts) >= 4:
        lengths = [len(o) for o in opts]
        if max(lengths) > 25 and max(lengths) > 3.5 * max(1, min(lengths)):
            warn(where, f"alternativas com tamanhos muito diferentes ({min(lengths)} a {max(lengths)} caracteres)")


def check_question(path, lesson, q, seen_ids):
    qid = q.get("id", "?")
    where = f"{path.name}:{qid}"
    for key in ("id", "type", "topic", "style", "stem", "explanation", "source", "reviewed"):
        if key not in q:
            err(where, f"campo obrigatório ausente: {key}")
            return
    if qid in seen_ids:
        err(where, "id duplicado")
    seen_ids.add(qid)
    if not re.fullmatch(re.escape(lesson["id"]) + r"q\d\d", qid):
        err(where, f"id deve seguir o padrão {lesson['id']}qNN")
    t = q["type"]
    if t not in TYPES:
        err(where, f"type inválido: {t}")
        return
    if q["style"] not in STYLES:
        err(where, f"style inválido: {q['style']}")
    if q["source"] not in SOURCES:
        err(where, f"source inválido: {q['source']}")
    if not isinstance(q["reviewed"], bool):
        err(where, "reviewed deve ser booleano")
    if q["source"] == "ia" and q.get("reviewed") is True:
        err(where, "questão de IA não pode vir com reviewed=true (só o dono revisa)")
    if q.get("difficulty") not in (1, 2, 3):
        err(where, "difficulty deve ser 1, 2 ou 3")
    stem = q["stem"]
    if not (15 <= len(stem) <= 700):
        err(where, f"enunciado com {len(stem)} caracteres (esperado de 15 a 700)")
    if len(q["explanation"].strip()) < 20:
        err(where, "explicação vazia ou curta demais")
    if q["source"] != "fcc_real":
        blob = " ".join([stem, q["explanation"]] + [str(o) for o in q.get("options", [])])
        if re.search(r"\bFCC\b", blob):
            err(where, 'menciona "FCC" mas a questão não é fcc_real (CLAUDE.md, regra 4)')

    if t in FIVE_OPTION_TYPES:
        check_options(where, q, 5)
    elif t == "fill_blank":
        check_options(where, q, (4, 5))
        if "___" not in (q.get("code") or ""):
            err(where, 'fill_blank precisa de "code" com a lacuna "___"')
    elif t == "truefalse":
        if q.get("options") != ["Certo", "Errado"]:
            err(where, 'truefalse deve ter options ["Certo", "Errado"]')
        if q.get("answer") not in (0, 1):
            err(where, "truefalse: answer deve ser 0 ou 1")
    elif t == "match":
        pairs = q.get("pairs")
        if not isinstance(pairs, list) or not (3 <= len(pairs) <= 5):
            err(where, "match deve ter de 3 a 5 pares")
        else:
            lefts = [norm(p.get("left", "")) for p in pairs]
            rights = [norm(p.get("right", "")) for p in pairs]
            if "" in lefts or "" in rights or len(set(lefts)) != len(lefts) or len(set(rights)) != len(rights):
                err(where, "pares vazios ou repetidos")
    elif t == "order_code":
        lines = q.get("lines")
        if not isinstance(lines, list) or not (3 <= len(lines) <= 8) or not all(isinstance(x, str) and x.strip() for x in lines):
            err(where, "order_code deve ter de 3 a 8 linhas não vazias")
    elif t == "quick":
        check_options(where, q, 3)
    if t == "code_output" and not q.get("code"):
        err(where, 'code_output exige o campo "code"')
    level = q.get("level", "prova")
    if level not in LEVELS:
        err(where, f"level inválido: {level}")
    concepts = {c.get("id") for c in lesson.get("concepts", [])}
    if concepts and q.get("concept") not in concepts:
        err(where, f"conceito inexistente na lição: {q.get('concept')}")
    if not concepts and "concept" in q:
        err(where, "conceito numa lição sem conceitos")
    if "step" in q and level == "prova":
        err(where, "step só vale em aprender e praticar")
    if concepts and level == "prova" and t in ("truefalse", "quick"):
        warn(where, f"{t} no nível prova (o formato da prova é de 5 alternativas)")
    adapted = q.get("adaptedFrom")
    if adapted is not None:
        if q["source"] not in ("ia", "autoral"):
            err(where, "só exercício de IA ou autoral pode ser adaptado de prova")
        missing = [k for k in ("id", "banca", "orgao", "ano", "numero") if not adapted.get(k)]
        if missing:
            err(where, f"adaptedFrom sem {', '.join(missing)}")
        elif adapted["id"] not in OFFICIAL:
            err(where, f"adaptedFrom aponta para questão que não existe em content/provas: {adapted['id']}")
        else:
            ref = OFFICIAL[adapted["id"]]
            if (ref["banca"], ref["orgao"], ref["ano"], ref["numero"]) != (adapted["banca"], adapted["orgao"], adapted["ano"], adapted["numero"]):
                err(where, f"adaptedFrom não bate com a referência de {adapted['id']} em content/provas")


def validate_pack(path):
    try:
        pack = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        err(path.name, f"JSON inválido: {e}")
        return None
    if pack.get("schema") != 1:
        err(path.name, "schema deve ser 1")
    for key in ("pack", "version", "unit", "lessons"):
        if key not in pack:
            err(path.name, f"campo obrigatório ausente: {key}")
            return None
    unit = pack["unit"]
    for key in ("id", "order", "title"):
        if key not in unit:
            err(path.name, f"unit sem {key}")
            return None
    if not pack["lessons"]:
        err(path.name, "pacote sem lições")
    seen_ids = set()
    questions = []
    for i, lesson in enumerate(pack["lessons"], 1):
        lid = lesson.get("id", "?")
        if lid != f"{unit['id']}l{i}" or lesson.get("order") != i:
            err(path.name, f"lição {lid}: id/order fora do padrão (esperado {unit['id']}l{i}, order {i})")
        qs = lesson.get("questions", [])
        concepts = lesson.get("concepts", [])
        if concepts:
            lo, hi = CONCEPT_LESSON_RANGE
            if not (lo <= len(qs) <= hi):
                err(f"{path.name}:{lid}", f"lição por conceitos deve ter de {lo} a {hi} exercícios, achei {len(qs)}")
            check_concepts(path, lesson)
        elif len(qs) != QUESTIONS_PER_LESSON:
            err(f"{path.name}:{lid}", f"deve ter {QUESTIONS_PER_LESSON} questões, achei {len(qs)}")
        for q in qs:
            check_question(path, lesson, q, seen_ids)
            questions.append(q)
    return pack, questions


def check_concepts(path, lesson):
    """Formato por conceitos: ids, títulos, e cada conceito com exercícios de aprender, praticar e prova."""
    where = f"{path.name}:{lesson['id']}"
    ids = [c.get("id") for c in lesson["concepts"]]
    if len(set(ids)) != len(ids):
        err(where, "conceito repetido")
    for c in lesson["concepts"]:
        if not re.fullmatch(re.escape(lesson["id"]) + r"c\d+", c.get("id", "")):
            err(where, f"conceito {c.get('id')} fora do padrão {lesson['id']}cN")
        if not c.get("title", "").strip() or len(c.get("summary", "").strip()) < 20:
            err(where, f"conceito {c.get('id')} sem título ou com resumo curto demais")
    exam_ids = {q["id"] for q in OFFICIAL.values() if q.get("lesson") == lesson["id"]}
    for c in ids:
        qs = [q for q in lesson["questions"] if q.get("concept") == c]
        levels = Counter(q.get("level", "prova") for q in qs)
        official = sum(1 for q in OFFICIAL.values() if q.get("concept") == c)
        if levels["aprender"] < 2:
            err(where, f"conceito {c}: precisa de ao menos 2 exercícios de aprender (achei {levels['aprender']})")
        if levels["praticar"] < 2:
            err(where, f"conceito {c}: precisa de ao menos 2 exercícios de praticar (achei {levels['praticar']})")
        if levels["prova"] + official < 1:
            err(where, f"conceito {c}: sem nenhuma questão de prova (do pacote ou oficial)")
        teach = [q for q in qs if q.get("level", "prova") != "prova"]
        steps = {q.get("step") for q in teach}
        if any(not isinstance(q.get("step"), int) or q["step"] < 1 for q in teach):
            err(where, f"conceito {c}: todo exercício de aprender/praticar precisa de step (1, 2, 3…)")
        elif not {1, 2, 3} <= steps:
            err(where, f"conceito {c}: precisa dos passos 1, 2 e 3 (achei {sorted(steps)})")
        for level in ("aprender", "praticar"):
            if not any(q.get("level") == level and "adaptedFrom" not in q for q in qs):
                err(where, f"conceito {c}: precisa de ao menos um exercício de {level} que não seja adaptado")
    for qid in sorted(exam_ids):
        if OFFICIAL[qid].get("concept") not in ids:
            err(where, f"questão oficial {qid} está nesta lição mas sem conceito válido")
    quick = [q for q in lesson["questions"] if q.get("type") == "quick" and isinstance(q.get("answer"), int)]
    if len(quick) >= 9:
        counts = Counter(q["answer"] for q in quick)
        for pos in range(3):
            share = counts.get(pos, 0) / len(quick)
            if not (0.15 <= share <= 0.55):
                err(where, f"escolha rápida: posição {'ABC'[pos]} da resposta correta tem {share:.0%} (esperado entre 15% e 55%)")


def pack_statistics(path, pack, questions):
    where = path.name
    # Metas de estilo FCC e de posição da resposta valem para as questões de prova; aprender e praticar são outra coisa.
    questions_all = questions
    questions = [q for q in questions if q.get("level", "prova") == "prova"]
    five = [q for q in questions if q.get("type") in FIVE_OPTION_TYPES | {"fill_blank"} and len(q.get("options", [])) == 5
            and isinstance(q.get("answer"), int)]
    if len(five) >= 20:
        counts = Counter(q["answer"] for q in five)
        for pos in range(5):
            share = counts.get(pos, 0) / len(five)
            if not (0.10 <= share <= 0.30):
                err(where, f"posição {'ABCDE'[pos]} da resposta correta tem {share:.0%} (esperado entre 10% e 30%)")
        eligible = [q for q in five if max(len(o) for o in q["options"]) > 20]
        longest = sum(1 for q in eligible
                      if sorted((len(o) for o in q["options"]), reverse=True)[0] == len(q["options"][q["answer"]])
                      and sorted((len(o) for o in q["options"]), reverse=True)[0] > sorted((len(o) for o in q["options"]), reverse=True)[1])
        if eligible and longest / len(eligible) > 0.35:
            warn(where, f"a alternativa correta é a mais longa em {longest / len(eligible):.0%} das questões (viés fácil de explorar; ideal perto de 20%)")
    total = len(questions)
    if total:
        case_n = sum(1 for q in questions if q.get("style") == "caso_pratico" or q.get("type") == "case")
        exc_n = sum(1 for q in questions if q.get("style") == "excecao")
        lit_n = sum(1 for q in questions if q.get("style") == "literal")
        if case_n / total < 0.30:
            warn(where, f"casos práticos em {case_n / total:.0%} (meta ~40%)")
        if exc_n / total < 0.08:
            warn(where, f"questões de exceção em {exc_n / total:.0%} (meta ao menos ~10%)")
        if lit_n / total < 0.10:
            warn(where, f"questões literais em {lit_n / total:.0%} (meta ao menos ~10%)")
    # Compara o conteúdo do item, não só o enunciado: no associar e no ordenar o enunciado é uma instrução padrão e o
    # que distingue um exercício do outro são os pares e as linhas.
    def body(q):
        pairs = " ".join(f"{p.get('left', '')} {p.get('right', '')}" for p in q.get("pairs") or [] if isinstance(p, dict))
        lines = " ".join(l for l in q.get("lines") or [] if isinstance(l, str))
        return norm(" ".join([q.get("stem", ""), q.get("code") or "", pairs, lines]))
    stems = [(q.get("id", "?"), body(q)) for q in questions_all]
    for i in range(len(stems)):
        for j in range(i + 1, len(stems)):
            r = difflib.SequenceMatcher(None, stems[i][1], stems[j][1]).ratio()
            if r >= 0.9:
                err(where, f"enunciados quase idênticos: {stems[i][0]} e {stems[j][0]} ({r:.0%})")
            elif r >= 0.8:
                warn(where, f"enunciados muito parecidos: {stems[i][0]} e {stems[j][0]} ({r:.0%})")


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    target = Path(sys.argv[1])
    files = sorted(target.glob("*.json")) if target.is_dir() else [target]
    if not files:
        print(f"Nenhum pacote .json em {target}")
        return 1
    root = Path(__file__).resolve().parent.parent
    all_ids = {}
    total_q = 0
    for f in files:
        res = validate_pack(f)
        if res is None:
            continue
        pack, questions = res
        pack_statistics(f, pack, questions)
        total_q += len(questions)
        for q in questions:
            if q.get("id") in all_ids and all_ids[q["id"]] != f.name:
                err(f.name, f"id {q['id']} também existe em {all_ids[q['id']]}")
            all_ids[q.get("id")] = f.name
        review = root / "content" / "review" / f"{pack.get('pack')}.md"
        if not review.exists():
            warn(f.name, f"sem relatório de revisão técnica em content/review/{pack.get('pack')}.md")
    for line in errors + warnings:
        print(line)
    print(f"\n{len(files)} pacote(s), {total_q} questão(ões): {len(errors)} erro(s), {len(warnings)} aviso(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
