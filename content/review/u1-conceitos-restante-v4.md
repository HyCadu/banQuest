# Revisão técnica — Banco de dados, lições 2–5, pacote v4

Passada separada da geração, pela mesma IA; não equivale à revisão do dono. Todos os exercícios novos permanecem source ia e reviewed false.

## Escopo

Cada lição recebeu 5 conceitos, 10 exercícios de aprender e 10 de praticar. As 10 questões anteriores foram vinculadas a conceitos, preservando IDs. Total da unidade: 170 exercícios (50 da lição 1 e 30 em cada lição restante). Pacote incrementado de v3 para v4.

## Revisão de conteúdo

- Conferidos gabaritos, correspondências, sequência 1–2–3 e exemplos resolvidos com cenários próprios: biblioteca, oficina, clínica e mercado.
- Ajustado o passo 2 para aplicações em contexto; evitada a mera repetição de definições.
- Conferidos NULL/NOT IN/NOT EXISTS, COUNT sem GROUP BY, empate em RANK/DENSE_RANK, frame explícito de acumulado, rollback até savepoint e distinção entre VACUUM e ANALYZE.
- Mantida a questão de isolamento vinculada ao padrão SQL; o PostgreSQL oferece garantias adicionais em REPEATABLE READ.
- Os exemplos de ensino usam cenários diferentes das questões antigas de prova. Nenhuma questão oficial foi copiada ou adaptada.
- As lições mantêm a seleção já implementada: três conceitos, blocos de três exercícios e três questões de prova. Não foi alterado o critério de conclusão/desbloqueio da trilha.

## Fontes técnicas consultadas

- https://www.postgresql.org/docs/15/transaction-iso.html
- https://www.postgresql.org/docs/15/mvcc-intro.html
- https://www.postgresql.org/docs/15/routine-vacuuming.html
- https://www.postgresql.org/docs/15/sql-merge.html

## Validação e limites

Verificação estrutural em JavaScript: 170 IDs únicos, referências a conceitos, ao menos 2 itens de aprender e 2 de praticar por conceito, prova, passos 1/2/3, tamanho de enunciados, alternativas, respostas e pares; sem erros. Python não está utilizável neste ambiente, portanto tools/validate_content.py não foi executado. Build e teste no aparelho pendentes.

A cobertura didática inicial segue os conceitos agrupados das questões existentes; não representa esgotamento de todos os tópicos do edital. O progresso por conclusão continua liberando a próxima lição após uma sessão, mesmo havendo conceitos ainda não vistos.
