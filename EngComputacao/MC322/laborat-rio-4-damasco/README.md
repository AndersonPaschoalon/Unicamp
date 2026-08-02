# Arquitetura

- docs/tutorial-arquitetura.md

# TODO: Interface Web Vaadin

- docs/plano-de-ataque-lab4-vaadin.md


# Smoke Tests - CLI

## Execução 1 — login como `alice` (Role corporativa `CONTRIBUTOR`)

Rode a CLI sem argumentos (config "Nexus - CLI Interativa" ou `mvn compile exec:java -Dexec.mainClass="com.nexus.Main"`).

| # | Digite | Esperado |
|---|---|---|
| 1 | `bob` (no prompt de login) | Aviso "Usuário 'bob' não encontrado." e o prompt de login aparece de novo (sem crash). |
| 2 | `alice` | "Login realizado com sucesso. Bem-vindo, alice!" e cai no menu principal. |
| 3 | `asdkjh` (lixo, comando inexistente) | "Comando inválido. Tente novamente." e **permanece no menu principal, sem crash** — este é o bug original que corrigimos no item 1. |
| 4 | `usuarios` | Lista de usuários (deve incluir `alice` e `admin` — o `admin` é semeado automaticamente na primeira execução). |
| 5 | `add-user` | Deve ser **barrado**: aviso de `PermissionDeniedException` ("Apenas MANAGER pode criar novos usuários."), permanece na tela de usuários — é a cascata de Role em ação (alice é só CONTRIBUTOR corporativo). |
| 6 | `voltar` | Volta ao menu principal. |
| 7 | `workflows` | Também deve ser **barrado** pelo mesmo motivo (gate MANAGER), aviso e permanece no menu principal. |
| 8 | `exit` | Encerra a aplicação. |

---

## Execução 2 — login como `admin` (Role corporativa `MANAGER`)

Reinicie o processo do zero.

| # | Digite | Esperado |
|---|---|---|
| 1 | `admin` | Login bem-sucedido. |
| 2 | `usuarios` → `add-user` | Desta vez **deve funcionar** (MANAGER passa no gate). Preencha `Username: carol`, `Email: carol@nexus.com`, confirme `s`. Esperado: "Usuário criado com sucesso!". |
| 3 | `voltar` (duas vezes até o menu principal) → `workflows` | Deve abrir normalmente (`WorkflowManagementState`), listando `FLOW1` e os status já existentes. Digite `criar` → nome `FLOW2` → esperado "Workflow criado com sucesso!". **Não** faça `vincular` aqui (evita quebrar o `Proj1`, que já está linkado ao `FLOW1`, e vamos precisar dele funcional no próximo passo). Digite `voltar`. |
| 4 | `voltar` → `projetos` → `Proj1` | Abre o Kanban de `Proj1`, deve mostrar `TASK1...` em `DONE` (do teste da Fase 3). |
| 5 | `add task` | Preencha: `Título: Fix bug`, `Prazo final: 2026-01-01`, `Horas estimadas: 5`, `Data de vencimento (opcional): 2020-01-01` (propositalmente no passado), confirme `s`. Esperado: "Tarefa criada com sucesso!". |
| 6 | (de volta ao board) | Procure a nova task na coluna `TODO` — **deve aparecer com a tag `[ATRASADA]`** ao lado, já que o `dueDate` está no passado e o status não é terminal. Anote o ID exibido. |
| 7 | `sprints` | Abre `SprintManagementState`. Digite `criar` → `Nome da sprint: Sprint1`, `Data início: 2026-01-01`, `Data fim: 2026-01-15` → esperado "Sprint criada com sucesso!". Depois `add-task` → `Nome da sprint: Sprint1`, `ID da tarefa: <cole o id da task "Fix bug" anotado no passo 6>` → esperado "Tarefa adicionada à sprint com sucesso!". Digite `voltar`. |
| 8 | Digite o ID da task "Fix bug" diretamente (mostrado no board) | Deve abrir uma tela de detalhe da tarefa (`TaskDetailView`) — **não tenho o fonte dela**, então não sei exatamente o que vai aparecer. Aqui preciso que você **me relate o que a tela mostra e quais comandos ela aceita** — principalmente se há alguma opção pra mudar status, já que é onde o `ChangeTaskStatusState` (que corrigimos) deveria ser alcançado. |
| 9 | `exit` (ou `Ctrl+C`) | Encerra. |

---

**Depois das duas execuções**, também dá uma olhada nos arquivos `data/users.jsonl` e `data/projects.jsonl` — confirme que cresceram (novas linhas para `carol`, para o `workflow` novo, para a sprint) sem nenhuma linha antiga ter sido removida/sobrescrita.

Roda esse roteiro e me manda a saída (pode ser só colar o transcript do terminal, como você já vem fazendo)?


