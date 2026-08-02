# Nexus — Lab 4: Panorama e Plano de Ataque

> Documento-guia de desenvolvimento. Consolida os enunciados dos Labs 1–4, o
> estado atual do código-base do Lab 4 e um plano de execução faseado.
>
> **Regra de desempate (acordada):** o código-base do Lab 4 e o enunciado do
> Lab 4 são a **verdade absoluta**. Onde o Lab 4 contradiz labs anteriores, o
> Lab 4 vence. Ambiguidades remanescentes são resolvidas por razoabilidade e
> ficam registradas na seção "Decisões Arquiteturais".

---

## 1. A tese do Lab 4

O Lab 4 transforma o Nexus de um protótipo mutável em um sistema resiliente,
apoiado em três pilares de engenharia:

1. **Imutabilidade estrita (padrão Wither).** Nada de `setters`. Toda mutação de
   negócio gera uma *nova instância* da entidade. Entidades não se referenciam
   por objeto, apenas por **ID textual**.
2. **State Sourcing append-only (JSONL).** O disco vira um *ledger* cronológico:
   cada mutação anexa uma linha nova em `*.jsonl`; nunca há UPDATE/DELETE. Na
   inicialização, o estado é *reidratado* varrendo o histórico (último registro
   por ID vence).
3. **Duas interfaces, um só motor.** CLI (máquina de estados) e Web (Vaadin)
   despacham os **mesmos `Command`** e acionam os **mesmos handlers**. Prova de
   desacoplamento total da apresentação (é o critério do bônus de +1,5).

Resultado final desejado: alterar um card (na CLI ou na Web) → gera um comando de
intenção imutável → valida regra sem efeito colateral local → faz *append* do
novo estado no JSONL → reidrata/atualiza a UI de forma transparente.

---

## 2. Evolução resumida dos labs (contexto)

| Lab | Tema | O que deixou de herança |
|-----|------|-------------------------|
| 1 | Núcleo fail-fast | Domínio auto-validável; `Task` como máquina de estados; imutabilidade de `id`/`deadline`; Streams para relatórios; `LogProcessor`. |
| 2 | Governança data-driven | `Status` vira classe (com `isTerminal`); `Workflow` com `Map<Status,Set<Status>>`; RBAC (`Role`, `teamMembers`); `Map`/`Set` + `equals`/`hashCode`; fim do `static` (TelemetryRegistry); quebra do monólito (`FileLoader`/`CommandParser`/`Dispatcher`); Lei de Demeter (Project como autoridade). |
| 3 | Padrões na interação | `CommandHandler` + `Map<String,CommandHandler>` (OCP/DIP); `default methods` de parsing; taxonomia de exceções (`NexusException` → System/Domain → especializadas); `PromptService` (JLine); `InteractiveCLIHandler`; `WorkspaceQueryService` read-only; navegação Kanban com State Pattern (`ViewState`). |
| 4 | **(alvo)** Imutabilidade + State Sourcing + CLI/Web por estados | *este documento* |

---

## 3. As 4 partes do Lab 4

### Parte 1 — Finalizar a máquina de estados da CLI (20%)
Migrar tudo de `com.nexus.view.interactive` para `com.nexus.view.statescli` e
**aposentar o pacote antigo** (nenhuma classe de `interactive` pode ser usada na
execução final; `Main.java` inicia direto em `LoginState`). Itens novos:

- **`ReportMenuState`** — acessível a partir do `MainMenuState`; expõe os 4
  relatórios (`ProjectHealth`, `GlobalBottleneck`, `TopPerformers`,
  `OverloadedUsers`) via `WorkspaceQueryService`, com saída tabular no terminal.
- **`WorkflowManagementState`** — criar/vincular `Workflow` a projeto; estado de
  captura de transições ("de → para"); mover para estado não mapeado dispara
  `InvalidTransitionException`.
- **`LoginState`** — ponto de entrada obrigatório da CLI; sessão retém referência
  **imutável** ao `User` autenticado; validação de `Role` em cascata antes de
  despachar comandos críticos → `PermissionDeniedException` local.
- **Temporalidade** — `dueDate` (`LocalDate`) no `AddTaskState`; tag `[ATRASADA]`
  em `TaskDetailView`/`KanbanMenuState`; Sprints/iterações via VO `DateRange`
  (início/fim) associando subconjuntos de tarefas.

Aceitação: desacoplamento total do `interactive`; exceções de domínio não
derrubam a app (voltam ao menu anterior); navegação só via retorno de
`handleInput()` (nada de `while`/`if` de tela na `Main`).

### Parte 2 — Domínio imutável (padrão Wither) (25%)
- Entidades (`Task`, `Project`, `User`, `Workflow`): todos os campos
  `private final`, **zero setters**. Mutação → `withXxx(...)` retorna nova
  instância copiando o resto.
- **Acoplamento só por ID textual.** Elimina referência direta de objeto entre
  entidades. Ex.: `Project` não tem mais `Set<Task>`; `Task` ganha `projectId`,
  `assignedUserId`, `statusId`.
- Separação clara Entidade × Value Object:
  - Entidades (têm `id` estável): `Task`, `Project`, `User`, `Workflow`.
  - Value Objects (sem identidade): `Status`, `Role`, `Command`, `Transition`
    (regra de→para do Workflow), `DateRange`.

Aceitação: qualquer `set`/campo não-`final` em entidade zera o quesito; nenhuma
entidade guarda referência nativa de outra; associação puramente por ID.

### Parte 3 — Repositório genérico + JSONL + reidratação (25%)
- Interface `Repository<T, ID>` (`findById`, `findAll`, `save`) e implementação
  `JsonlRepository<T extends Entity>` (o enunciado fornece o código quase
  pronto). Interface `Entity` com `String getId()`.
- `save` = serializa entidade completa e faz **append** de 1 linha no `.jsonl` +
  atualiza cache em memória. Nunca reescreve.
- `rehydrate()` no construtor: lê linha a linha, `cache.put(id, entity)` — o
  último por ID prevalece.
- **`Workspace` vira Fachada (Facade)**: repositórios privados; expõe
  `getTask/saveTask/getProject/saveProject/...`. Handlers seguem o ritmo
  *consultar → mutar via Wither → salvar*.
- Arquivos em `data/*.jsonl`. **Aposentar** `PersistenceService`/`NexusData`
  (sobrescrita destrutiva de `nexus_state.json`).

Aceitação: crescimento estritamente incremental dos `.jsonl`; reidratação
reconstrói o estado; nenhuma UI toca disco/Gson/repositório direto.

### Parte 4 — Web Vaadin, dados unidirecionais (30%)
- Sem *two-way binding*: formulários usam DTOs/`Command` mutáveis, nunca amarram
  componente direto à entidade imutável.
- Fluxo: ler projeção → editar campos → montar `Command` → fachada aplica via
  Wither → tela re-renderiza puxando o cache novo.
- Kanban web montado por **consulta filtrada por `projectId`/`statusId`** (não
  por lista aninhada, que não existe mais).
- Login em `VaadinSession.getCurrent().setAttribute(User.class, user)`; RBAC por
  `Role` da sessão.
- Exceções de domínio → `Notification.show(...)`, nunca stacktrace/tela branca.

Aceitação: toda ação em tela gera linha `.jsonl`; reinício do servidor reidrata a
UI; `statesgui` não instancia repositório nem toca disco; erros viram
notificação amigável.

**Bônus (+1,5):** simetria CLI↔Web — mesmas classes `Command` e mesmos handlers
de negócio nas duas interfaces.

---

## 4. Estado atual do código-base (o que já vimos)

Fontes analisados: `Task`, `Project`, `User`, `Workflow`, `Workspace`, `Status`,
`Role`, `Command`, `LocalDateAdapter`, `NexusData`, `PersistenceService`, e a
árvore de arquivos completa.

### 4.1 Diagnóstico do modelo (tudo precisa refatorar na Parte 2)

| Classe | Estado hoje | Precisa virar |
|--------|-------------|---------------|
| `Task` | mutável (`setOwner`, `setStatus`); `status`/`owner`/`title`/`deadline`/`effort` não-final; referencia `User` e `Status` por objeto; **sem `description`, sem `dueDate` separado, sem `projectId`** | tudo `final`; `withXxx`; campos `projectId`, `assignedUserId`, `statusId` (String); `id` já ok; add `description` e `dueDate` |
| `Project` | mutável; `Set<Task>` + `Map<User,Role>` + `Workflow` por objeto; `setWorkflow`; lógica de negócio (`addTask`, `processStatusChange`) dentro | `final` + `withXxx`; `workflowId` (String); `memberIds` (List<String>); sem `Set<Task>`; sprints via `DateRange`; **mover `processStatusChange` para handler** |
| `User` | `username`/`email` final (bom), mas **sem `id`** e **sem `Role`**; `equals` usa `username OR email` (viola contrato equals/hashCode) | add `id` e `Role`; `equals`/`hashCode` por `id`; consertar o `||` |
| `Workflow` | mutável (`setInitialStatus`, `addTransition`); `Map<Status,Set<Status>>` por objeto; sem `id` | `final` + `withXxx`; `id`; transições por `Transition` (VO com ids/nomes de status); `initialStatusId` |
| `Status` | mutável (`name`/`isTerminal` não-final) | campos `final` (VO imutável) |
| `Role` | enum `VIEWER/CONTRIBUTOR/MANAGER` — **já imutável e é a fonte de verdade** | manter |
| `Command` | mutável (`line/action/parameters` não-final) | tornar VO imutável (`final`); é o DTO de intenção das duas UIs |

### 4.2 Diagnóstico da persistência

`PersistenceService` grava `NexusData` inteiro em `nexus_state.json` com
`FileWriter(FILE_PATH)` (**sobrescrita** — proibida no Lab 4) e serializa objetos
mutáveis com referências aninhadas. `NexusData` serializa `Workspace` +
`UserService` inteiros. Isso é incompatível com State Sourcing.

Alvo: `Repository<T,ID>` + `JsonlRepository` por entidade em `data/`. O
`LocalDateAdapter` é reaproveitável (o Gson do repositório precisa dele para
`dueDate`/`DateRange`).

### 4.3 Diagnóstico do `Workspace`

Hoje é um agregador com listas/mapa em memória e a lógica dos relatórios
(Streams). No Lab 4 vira **fachada de repositórios**. Os relatórios (Streams)
provavelmente migram para `ReportService`/`WorkspaceQueryService` consumindo
`repository.findAll()`.

### 4.4 O que a árvore já traz pronto (aproveitável)

- `statescli/` com `MainMenuState`, `KanbanMenuState`, `ProjectMenuState`,
  `UserMenuState`, `StatusManagementState`, `TaskDetailView`, `ViewState`,
  `info/*State`.
- `statesgui/` com `VaadinViewState`, `LoginState`, `KanbanBoardState`,
  `MainMenuGuiState`, `ProjectMenuState`, `AddProjectState`, `AddTaskState`.
- Taxonomia de exceções completa em `exception/`.
- ~19 `CommandHandler` em `service/core/command/`.
- `frontend/generated/` do Vaadin já gerado; `pom.xml` com Vaadin/JLine/Gson.

---

## 5. Decisões Arquiteturais (registro)

| # | Questão | Decisão | Justificativa |
|---|---------|---------|---------------|
| D1 | Nomes de `Role` | `VIEWER / CONTRIBUTOR / MANAGER` | `Role.java` do base do Lab 4 = verdade absoluta. |
| D2 | Comando de mudança de status | **`CHANGE_STATUS`** é a action string real | Confirmado no `CommandFactory` (`"CHANGE_STATUS" → ChangeStatusHandler`). `MOVE_TASK` vinha do pacote `interactive`, que será aposentado. As telas montam `new Command("CHANGE_STATUS", ...)`. |
| D3 | Persistência | JSONL append-only via `JsonlRepository`; aposentar `PersistenceService`/`nexus_state.json` | Parte 3 é explícita e há penalidade por sobrescrever. |
| D4 | `statusId`/`assignedUserId` | `Task` guarda **String** (nome do status / id do user), nunca objeto | Parte 2: acoplamento só por ID. |
| D5 | `Role` no `User` vs por projeto | **Ambos (confirmado):** `User` carrega uma `Role` corporativa global (default `CONTRIBUTOR`, `CREATE_USER` mantém assinatura `username;email`); `Project` guarda `Map<String,Role> memberRoles` (userId→papel) para o RBAC por projeto. Checagem de permissão de `CHANGE_STATUS` usa `memberRoles`. | Atende à letra da P2 ("User contém Role") sem quebrar o comando nem o RBAC por projeto existente. |
| D6 | `equals`/`hashCode` das entidades | Basear em `id` estável | com IDs estáveis, identidade por id é o correto; conserta o `||` bugado do `User`. |
| D7 | `Transition` | VO com `fromStatus`/`toStatus` (por nome/id) dentro do `Workflow` | Parte 2 lista `Transition` como VO. |
| D8 | Telemetria/`static` | manter `TelemetryRegistry` como está no base (não é foco do Lab 4) | base = verdade; evitar escopo desnecessário. |
| D9 | Estratégia da fachada (confirmado) | Na **P2** o `Workspace` já vira fachada **indexada por ID**, com backing em `Map` em memória (`getTask/saveTask/findAllTasks`, idem Project/Workflow). Na **P3** troca-se só o `Map` por `JsonlRepository`, sem mexer nas assinaturas. | Evita reescrever o `Workspace` duas vezes; a P3 vira troca cirúrgica. |
| D10 | Geração de IDs (confirmado) | IDs a partir de chaves naturais: `User.id`←username, `Project.id`←nome, `Workflow.id`←nome, `statusId`=nome do Status (VO sem id). `Task.id` gerado **uma vez** na criação e **preservado** por todos os `withXxx`. Cálculo isolado atrás de um contrato trocável (`IdStrategy`), não uma classe `Utils` procedural. | IDs legíveis facilitam debug; contrato trocável permite mudar a estratégia (ex.: voltar a hash) sem tocar as entidades; estabilidade do `Task.id` é requisito do State Sourcing. |
| D11 | Persistência na P2 (confirmado) | `PersistenceService`/`NexusData` neutralizados (no-op) durante a P2; app roda 100% em memória. Descartados de vez na P3. | Permite validar a imutabilidade isoladamente antes de introduzir JSONL. |
| D12 | Onde vive a mutação | O ritmo *fetch → wither → save* fica **nos serviços** (`TaskService`, `ProjectService`, `WorkflowService`), não nos handlers. Handlers permanecem finos (`handler → service → workspace`). | Respeita a arquitetura do base (handlers já delegam a serviços); o exemplo `handler → workspace` do enunciado é ilustrativo. |

---

## 6. Ordem de ataque (faseada)

A dependência técnica manda começar pelo núcleo. Sequência recomendada
(**não** a ordem do enunciado):

### Fase 0 — Fundação
- [ ] Rodar o base como veio (`mvn clean compile exec:java -Dexec.mainClass="com.nexus.Main"`) e a Web (`mvn ... jetty:run`) para ter um baseline funcionando.
- [ ] Ler os fontes ainda não vistos (ver seção 8) e mapear o wiring atual.

### Fase 1 — Parte 2: imutabilidade + IDs (o coração)
- [ ] Criar interface `Entity { String getId(); }`.
- [ ] Refatorar `Status` e `Command` para VOs imutáveis (`final`).
- [ ] Criar VOs `Transition` e `DateRange`.
- [ ] Refatorar `User` (add `id`, `Role`; `withXxx`; equals/hashCode por id).
- [ ] Refatorar `Task` (ids externos; `description`; `dueDate`; `withXxx`).
- [ ] Refatorar `Workflow` (id; `initialStatusId`; transições via `Transition`; `withXxx`).
- [ ] Refatorar `Project` (ids; `memberIds`; sprints; sem lógica mutadora; `withXxx`).
- [ ] Tirar `processStatusChange`/`addTask` das entidades (viram handler).

### Fase 2 — Parte 3: repositórios + State Sourcing
- [ ] `Repository<T,ID>` + `JsonlRepository<T extends Entity>` (com reidratação).
- [ ] `Workspace` como fachada de repositórios (`data/*.jsonl`).
- [ ] Adaptar todos os `CommandHandler` ao ritmo *fetch → wither → save*.
- [ ] Aposentar `PersistenceService`/`NexusData`.
- [ ] Migrar relatórios (Streams) para consumir `findAll()`.

### Fase 3 — Parte 1: CLI por estados
- [ ] `LoginState` + contexto de sessão imutável + RBAC em cascata.
- [ ] `ReportMenuState` (4 relatórios).
- [ ] `WorkflowManagementState` + estado de transição.
- [ ] `dueDate` no `AddTaskState`; tag `[ATRASADA]`; Sprints/`DateRange`.
- [ ] `Main.java` → inicia em `LoginState`; **remover uso de `view.interactive`**.

### Fase 4 — Parte 4: Web Vaadin
- [ ] `LoginState` web + `VaadinSession`.
- [ ] Kanban web por consulta filtrada `projectId`/`statusId`.
- [ ] Fluxo unidirecional (DTO/`Command` → fachada → refresh).
- [ ] Erros → `Notification`.
- [ ] Perseguir o bônus: mesmos `Command`/handlers da CLI.

### Fase 5 — Fechamento
- [ ] Varredura anti-penalidade (ver seção 7).
- [ ] JavaDoc, commits equilibrados da dupla, limpeza de código morto.

---

## 7. Armadilhas e penalidades a evitar

| Penalidade | −pts | Como evitar |
|------------|------|-------------|
| Setter/atributo não-`final` em entidade | −1,5 | varrer `grep -r "void set" model/` e campos sem `final`. |
| Sobrescrita destrutiva de `.jsonl` | −1,0 | `FileWriter(path, true)` sempre; nunca truncar. |
| UI tocando repositório/disco/Gson | −1,0 | tudo via fachada `Workspace`. |
| Entidade com referência aninhada de outra | −1,0 | só `String id` entre entidades. |
| Stacktrace vazando pra UI | −0,5 | `try/catch` em `NexusDomainException`/`NexusSystemException` na borda (CLI e listeners Vaadin). |

Outras heranças de penalidade dos labs anteriores que continuam valendo:
comparar status por string (`equals("DONE")`) em vez de `isTerminal`; *train
wrecks* (3+ getters encadeados); `instanceof`/`switch` de tipo para fluxo de
negócio (usar polimorfismo).

> Atenção: `Project.processStatusChange` atual usa `"IN_PROGRESS".equals(...)` e
> `Workflow.canTransition` usa `from.isTerminal()` — ao migrar, manter a lógica
> baseada em `isTerminal` e eliminar comparações de string de status.

---

## 8. Próximos fontes a ler (para destravar a implementação)

Para começar a Fase 1/2 com segurança, preciso ver:

1. `controller/CommandDispatcher.java` e `service/core/command/CommandFactory.java`
   e `CommandHandler.java` — para saber o contrato exato e onde o `Map` é montado
   (confirma D2).
2. Um handler representativo de escrita, ex.: `ChangeStatusHandler.java`,
   `CreateTaskHandler.java`, `AddMemberHandler.java`.
3. `Main.java` e `MainGui.java` — pontos de entrada / wiring / `AppContext`/`AppBootstrap` (se existirem como no resumo do Lab 3).
4. `service/WorkspaceQueryService.java` + `WorkspaceQueryServiceImpl.java`.
5. `view/statescli/ViewState.java` + `MainMenuState.java` (contrato de navegação
   e wiring da sessão).
6. `view/statesgui/VaadinViewState.java` + `LoginState.java` + `KanbanBoardState.java`.
7. `service/core/UserService.java` (referenciado pela persistência) e
   `TelemetryRegistry.java`.
8. `pom.xml` (versões de Vaadin/JLine/Gson/Java).

---

## 9. Pontos em aberto (decidir por razoabilidade quando surgirem)

- ~~Role global × por projeto~~ **RESOLVIDO (D5):** ambos — Role corporativa no
  `User` + `memberRoles` no `Project`.
- ~~`statusId` = nome do Status?~~ **RESOLVIDO (D10):** sim, o `name` é o
  identificador textual do Status (VO sem id próprio).
- ~~Identidade estável em JSONL~~ **RESOLVIDO (D10):** `Task.id` gerado uma vez e
  preservado nos `withXxx`; nunca mais derivado de `hashCode()`.
- **Sprints** (ainda em aberto): modelar como VO `Sprint(name, DateRange,
  List<String> taskIds)` dentro de `Project` (por ids) — a confirmar contra o que
  o corretor espera. Só entra de fato na Parte 1.

## 10. Bugs latentes do base a corrigir durante a P2

- **`Status` não tem `equals`/`hashCode`** e é usado como chave de `Map`/`Set` no
  `Workflow` — hoje funciona só por identidade de referência; ao serializar/reidratar
  (P3) quebraria. Adicionar `equals`/`hashCode` por `name` (+`isTerminal`).
- **`User.equals` usa `||`** (username OU email) — viola o contrato equals/hashCode.
  Passa a ser por `id`.
- **`Task.id` deriva de `hashCode()`** — instável entre snapshots. Ver D10.