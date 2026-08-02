# Nexus — Lab 4: Panorama e Plano de Ataque

> Documento-guia de desenvolvimento. Consolida os enunciados dos Labs 1–4, o
> estado atual do código-base do Lab 4 e um plano de execução faseado.
>
> **Regra de desempate:** o código-base do Lab 4 e o enunciado do Lab 4 são a
> **verdade absoluta**. Onde o Lab 4 contradiz labs anteriores, o Lab 4 vence.
> Ambiguidades remanescentes são resolvidas por razoabilidade e ficam
> registradas na seção "Decisões Arquiteturais".

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
| 2 | Governança data-driven | `Status` vira classe (com `isTerminal`); `Workflow` com transições data-driven; RBAC (`Role`, `teamMembers`); `Map`/`Set` + `equals`/`hashCode`; fim do `static` (TelemetryRegistry); quebra do monólito (`FileLoader`/`CommandParser`/`Dispatcher`); Lei de Demeter (Project como autoridade). |
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
- **`Workspace` é Fachada (Facade)**: repositórios privados; expõe
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

## 4. Estado atual do código-base

### 4.1 Domínio (`com.nexus.model`)

**Modelo já imutável (concluído):**

| Classe | Local | Estado |
|--------|-------|--------|
| `Entity` | `model/Entity.java` | Interface marcadora com `getId()`. |
| `IdStrategy` | `model/id/` | Contrato trocável para cunhar ids. |
| `SlugIdStrategy` | `model/id/` | Impl. default (slug simples do texto). |
| `Status` | `model/entity/` | VO imutável (`name`, `terminal`), `equals`/`hashCode` por valor. |
| `Role` | `model/valueobject/` | Enum (`VIEWER`, `CONTRIBUTOR`, `MANAGER`). |
| `Command` | `model/` | VO imutável (`line`, `action`, `parameters[]`), com cópia defensiva. |
| `Transition` | `model/valueobject/` | VO novo, par `(fromStatusId, toStatusId)`. |
| `User` | `model/entity/` | Entity imutável, `id=username`, `Role` corporativa, `withRole`, `equals`/`hashCode` por `id`. |
| `Task` | `model/entity/` | Entity imutável, `id` gerado uma vez e preservado, `projectId`/`ownerId`/`statusId` como String, `withStatusId`/`withOwnerId`. |
| `Workflow` | `model/` | Entity imutável, `id=name`, transições via `Set<Transition>`, `initialStatusId` String, `canTransition(String,String)`, `withTransition`/`withInitialStatusId`. |
| `Project` | `model/entity/` | Entity imutável, `id=name`, `workflowId` String, `memberRoles Map<String,Role>` (userId→papel), `withWorkflowId`/`withMember`. Sem lógica de negócio. |

**Ainda faltam VOs novos:**
- `DateRange` — VO (`LocalDate start`, `LocalDate end`), validação `start ≤ end`,
  helper `isExpired(hoje)`. Usado só na Parte 1 (Sprints), mas contrato já
  estabelecido.

### 4.2 Camada de serviço (`com.nexus.service`)

Todos os componentes abaixo **ainda operam sobre o modelo antigo** (referências
de objeto, setters, `Set<Task>` no `Project`). Precisam ser reescritos para o
modelo imutável por IDs. Detalhamento por arquivo:

| Componente | Estado atual | Precisa se tornar |
|------------|--------------|-------------------|
| `Workspace` | Agregador com `List<Task>`, `Map<String,Project>`, `List<Status>`, `List<Workflow>`; expõe métodos de analytics (`getTopPerformers`, `getProjectHealth`, `getGlobalBottlenecks`, `getOverloadedUsers`) que operam sobre grafo de objetos; getters retornam listas. | Fachada indexada por ID: `Map<String,Task>`, `Map<String,Project>`, `Map<String,User>`, `Map<String,Workflow>`, `Map<String,Status>`. API `getTask(id)/saveTask/findAllTasks`, idem para as outras entidades. Sem analytics — migra para `ReportService`. |
| `UserService` | Guarda `List<User>` interna, `createUser` só instancia; `getUserByUsername` filtra por stream. | Delegar ao `Workspace` (`workspace.getUser(username)`, `workspace.findAllUsers()`). `createUser` monta `User` imutável e chama `workspace.saveUser`. |
| `TaskService` | `createTask` monta `Task` mutável + chama `project.addTask` + `workspace.addTask`; `assignUserToTask` chama `task.setOwner`; `changeTaskStatus` chama `project.processStatusChange`. | *fetch → wither → save* em todos os métodos. `createTask` valida orçamento somando esforços de `workspace.findAllTasks()` filtradas por `projectId`; `assignUserToTask` = `task.withAssignedUserId(user.getId())`; `changeTaskStatus` valida RBAC via `project.getMemberRoles()`, valida `workflow.canTransition(from, to)` e retorna `task.withStatusId(newStatusId)`. |
| `ProjectService` | `createProject` monta `Project` mutável; `linkWorkflow` chama `project.setWorkflow`; `addTeamMemberToProject` chama `project.addTeamMember`; `hasChangeStatusPermission` lê `Map<User,Role>`. | *fetch → wither → save*. `linkWorkflow` = `project.withWorkflowId(workflow.getId())`; `addTeamMemberToProject` = `project.withMember(user.getId(), role)`; `hasChangeStatusPermission` lê `project.getMemberRoles().get(userId)`. Perde `getProjectByTask` (Task agora tem `projectId`, lookup direto no `Workspace`). |
| `WorkflowService` | `createWorkflow` monta `Workflow` mutável; `setInitialStatus` chama `workflow.setInitialStatus`; `allowTransition` chama `workflow.addTransition`. | `setInitialStatus` = `workflow.withInitialStatusId(status.getName())`; `allowTransition` = `workflow.withTransition(fromName, toName)`; ambos re-salvam via `workspace.saveWorkflow`. |
| `ReportService` | Delega para `workspace.getTopPerformers/...`. | Reimplementar analytics diretamente aqui, consumindo `workspace.findAllTasks/findAllProjects/findAllUsers`, agrupando por `assignedUserId`, usando `isTerminal` no lugar de `equals("DONE")`. |
| `WorkspaceQueryServiceImpl` | Navega por grafos de objetos (`task.getOwner().consultUsername()`, `project.getTasks()`). | Reescrever: `getTaskOwnerName` → resolve `task.getAssignedUserId()` → `workspace.getUser(id).consultUsername()`; `listTaskIdsByProject` → filtra `workspace.findAllTasks()` por `projectId`; `listAllowedTransitions` → consulta `workflow.getAllowedTransitions()` (agora `Set<Transition>`). |

### 4.3 Handlers (`com.nexus.service.core.command`)

Confirmado: todos os handlers são finos (uma linha de delegação ao serviço).
Após refatorar os serviços, os handlers **não requerem alteração** — a única
exceção potencial é ajustar mensagens de erro se as assinaturas dos serviços
mudarem os nomes dos parâmetros.

Mapa de comandos registrado no `CommandFactory`: `CREATE_USER`,
`CREATE_PROJECT`, `CREATE_TASK`, `ASSIGN_USER`, `CREATE_STATUS`,
`CHANGE_STATUS`, `SET_INITIAL_STATUS`, `CREATE_WORKFLOW`, `ALLOW_TRANSITION`,
`LINK_WORKFLOW`, `ADD_MEMBER`, `REPORT_STATUS`, `TOP_PERFORMERS`,
`OVERLOADED_USERS`, `PROJECT_HEALTH`, `GLOBAL_BOTTLENECK`, `LIST_TASKS`.

### 4.4 Persistência (`com.nexus.service.persistence`)

- `PersistenceService` — grava `NexusData` inteiro em `nexus_state.json` via
  `FileWriter(FILE_PATH)` (sobrescrita). **Incompatível com State Sourcing.**
- `NexusData` — DTO que agrega `Workspace` + `UserService`.
- `LocalDateAdapter` — reutilizável (o Gson do repositório novo precisa dele
  para `dueDate`/`DateRange`).

**Estratégia:** neutralizar (`save/load` viram no-op) na finalização da Parte 2,
e descartar `PersistenceService`/`NexusData` na Parte 3, criando
`Repository<T,ID>` + `JsonlRepository<T extends Entity>`.

### 4.5 Telemetria (`TelemetryRegistry`)

Singleton com estado mutável (`totalTasksCreated`, `totalValidationErrors`,
`activeWorkload`). Método `hydrate(tasksCreated, workloadActive)` já existe.
Na Parte 3, após reidratar entidades, o bootstrap conta `findAllTasks().size()`
e as `IN_PROGRESS` para chamar `hydrate(...)`.

### 4.6 Infra (`com.nexus.controller`, `com.nexus.service.infra`)

- `LogProcessor` — **hoje não trata exceções**: se um comando falhar,
  `dispatchCommand` propaga e derruba o processamento. Contradiz o critério
  "resiliente a falhas de log" dos Labs anteriores. Precisa envolver o
  `dispatchCommand` em `try/catch(NexusDomainException)` e incrementar
  `telemetryRegistry.incrementValidationErrors()`.
- `CommandParser` — simples e correto; permanece.
- `CommandDispatcher` — singleton via `build()/getInstance()`; permanece.
- `FileLoader` — não requer alteração.

### 4.7 Bootstrap (`Main.java`)

Carrega `PersistenceService.load()`, instancia serviços, chama `hydrate`,
constrói `CommandFactory` + `CommandDispatcher`, monta CLI (via
`InteractiveCLIHandlerFactory`, do pacote `interactive` que será aposentado),
inicia `NexusCLI.run()`, e ao encerrar chama `persistence.save`.

Mudanças previstas:
- **P2 (final):** neutralizar `persistence.save/load` (no-op).
- **P3:** substituir bootstrap de persistência por construção dos
  `JsonlRepository`; hidratar `TelemetryRegistry` a partir dos repositórios.
- **P1:** substituir `InteractiveCLIHandlerFactory` por inicialização direta em
  `LoginState` (do pacote `statescli`).

### 4.8 View CLI (`com.nexus.view.statescli`) — visão macro

Já existe com `MainMenuState`, `KanbanMenuState`, `ProjectMenuState`,
`UserMenuState`, `StatusManagementState`, `TaskDetailView`, `ViewState`,
`info/{Add*State, ChangeTaskStatusState}`, `ExitState`. Loop principal em
`NexusCLI.run()` (`while(true)` sobre `handleInput → nextState`). Consulta
dados via `WorkspaceQueryService` (fica implicitamente correto assim que o
impl. for reescrito). **Detalhamento fino desta camada será feito ao iniciar a
Parte 1.**

### 4.9 View Web (`com.nexus.view.statesgui`) — visão macro

Vaadin já configurado (`pom.xml` com Vaadin Flow), `frontend/generated`
presente. Estrutura `VaadinViewState`, `LoginState`, `MainMenuGuiState`,
`ProjectMenuState`, `KanbanBoardState`, `info/{AddProjectState, AddTaskState}`.
**Detalhamento será feito na Parte 4.**

---

## 5. Decisões Arquiteturais (registro)

| # | Questão | Decisão | Justificativa |
|---|---------|---------|---------------|
| D1 | Nomes de `Role` | `VIEWER / CONTRIBUTOR / MANAGER` | `Role.java` do base do Lab 4 = verdade absoluta. |
| D2 | Comando de mudança de status | **`CHANGE_STATUS`** é a action string real | Confirmado no `CommandFactory` (`"CHANGE_STATUS" → ChangeStatusHandler`). `MOVE_TASK` vinha do pacote `interactive`, que será aposentado. As telas montam `new Command("CHANGE_STATUS", ...)`. |
| D3 | Persistência | JSONL append-only via `JsonlRepository`; aposentar `PersistenceService`/`nexus_state.json` | Parte 3 é explícita e há penalidade por sobrescrever. |
| D4 | `statusId`/`assignedUserId`/`projectId`/`workflowId` | `Task`/`Project` guardam **String** (nome/id textual), nunca objeto | Parte 2: acoplamento só por ID. |
| D5 | `Role` no `User` vs por projeto | **Ambos:** `User` carrega uma `Role` corporativa global (default `CONTRIBUTOR`, `CREATE_USER` mantém assinatura `username;email`); `Project` guarda `Map<String,Role> memberRoles` (userId→papel) para o RBAC por projeto. Checagem de permissão de `CHANGE_STATUS` usa `memberRoles`. | Atende à letra da P2 ("User contém Role") sem quebrar o comando nem o RBAC por projeto. |
| D6 | `equals`/`hashCode` das entidades | Basear em `id` estável | com IDs estáveis, identidade por id é o correto. |
| D7 | `Transition` | VO com `fromStatusId`/`toStatusId` (nomes de status como String) | Parte 2 lista `Transition` como VO. |
| D8 | Telemetria/`static` | manter `TelemetryRegistry` como está no base | base = verdade; evitar escopo desnecessário. |
| D9 | Estratégia da fachada | Na **P2** o `Workspace` já vira fachada **indexada por ID**, com backing em `Map` em memória. Na **P3** troca-se só o `Map` por `JsonlRepository`, sem mexer nas assinaturas. | Evita reescrever o `Workspace` duas vezes; a P3 vira troca cirúrgica. |
| D10 | Geração de IDs | IDs a partir de chaves naturais: `User.id`←username, `Project.id`←nome, `Workflow.id`←nome, `statusId`=nome do Status. `Task.id` = **gerado uma vez** e **preservado** nos `withXxx` (o modelo `Task` já cunha seu próprio id; `IdStrategy` fica reservado para futuras necessidades). | IDs legíveis; contrato trocável; estabilidade do `Task.id` é requisito do State Sourcing. |
| D11 | Persistência na P2 | `PersistenceService`/`NexusData` neutralizados (no-op) durante a P2; app roda 100% em memória. Descartados na P3. | Permite validar a imutabilidade isoladamente antes de introduzir JSONL. |
| D12 | Onde vive a mutação | O ritmo *fetch → wither → save* fica **nos serviços** (`TaskService`, `ProjectService`, `WorkflowService`), não nos handlers. Handlers permanecem finos (`handler → service → workspace`). | Respeita a arquitetura do base; o exemplo `handler → workspace` do enunciado é ilustrativo. |
| D13 | `Status` vs pacote | `Status` mantido em `model/entity/` apesar de ser VO | evitar churn de imports agora; `git mv` para `valueobject/` no encerramento da P2 se sobrar tempo. |

---

## 6. Ordem de ataque (faseada, granular)

### Fase 0 — Fundação (concluída)

- [x] Rodar o base como veio.
- [x] Mapear os fontes de domínio, serviço, persistência, handlers e loop de CLI.

### Fase 1 — Parte 2: modelo imutável + IDs

- [x] Criar interface `Entity { String getId(); }`.
- [x] Criar `IdStrategy` + `SlugIdStrategy`.
- [x] Refatorar `Status` (final, `equals`/`hashCode` por valor).
- [x] Refatorar `Command` (final, cópia defensiva do array).
- [x] Criar VO `Transition`.
- [x] Refatorar `User` (`id=username`, `Role`, `withRole`, `implements Entity`).
- [x] Refatorar `Task` (ids externos, `description`, `dueDate`, `withXxx`, `implements Entity`).
- [x] Refatorar `Workflow` (`id=name`, `Set<Transition>`, `initialStatusId`, `withXxx`, `implements Entity`).
- [x] Refatorar `Project` (`id=name`, `workflowId`, `memberRoles`, `withXxx`, `implements Entity`, sem lógica de negócio).
- [ ] Criar VO `DateRange` (deixado para o início da Parte 1 — só é usado por Sprints).

### Fase 2 — Parte 2: camada de serviço + Workspace fachada (concluída, validada por smoke test)

- [x] **`Workspace` (fachada de storage por ID)** — `Map<String,Task>`, `Map<String,Project>`, `Map<String,User>`, `Map<String,Workflow>`, `Map<String,Status>`; API `getX(id)/saveX/findAllX`. Analytics removidas.
- [x] **`UserService`** — delega armazenamento ao `Workspace`.
- [x] **`WorkflowService`** — `setInitialStatus`/`allowTransition` via *fetch → wither → save*.
- [x] **`ProjectService`** — `linkWorkflow`/`addTeamMemberToProject` via *fetch → wither → save*; `hasChangeStatusPermission` lê `memberRoles`.
- [x] **`TaskService`** — `createTask` valida workflow+orçamento; `assignUserToTask`/`changeTaskStatus` via *fetch → wither → save*; RBAC + `canTransition` + telemetria de workload consolidados aqui.
- [x] **`ReportService`** — analytics reimplementadas consumindo `Workspace.findAllX()`; `equals("DONE")` substituído por `isTerminal`.
- [x] **`WorkspaceQueryServiceImpl`** — reescrita para resolver por ID (`ownerId→username`, `statusId`, `Set<Transition>`); construtor simplificado para receber só `Workspace`.
- [x] **`LogProcessor`** — `try/catch(NexusDomainException)` em volta de `dispatchCommand`; imprime `output` dos handlers (antes descartado); incrementa telemetria de erro; log `[!] Erro ao processar linha` explícito.
- [x] **`PersistenceService`/`NexusData`** — removidos (não neutralizados); `LocalDateAdapter` mantido para reuso na P3.
- [x] **`Main.java`** — reconstruído sem persistência; `Workspace` vazio no boot; suporta modo smoke test via `args[0]` (nome de arquivo de log), imprimindo `ReportService.generateStatusReport()` ao final e pulando a CLI interativa.
- [x] **Excluído da compilação** (via `pom.xml` + `.vscode/settings.json`): `MainGui.java` e pacote `view/statesgui/**` (Vaadin/Parte 4), até serem retomados.
- [x] **Smoke test validado** (`log_test_p2_v2.txt` → renomeado `log_test_p3.txt` no ambiente do dev): ciclo completo `CREATE_USER → CREATE_STATUS → CREATE_WORKFLOW → ALLOW_TRANSITION → CREATE_PROJECT → LINK_WORKFLOW → ADD_MEMBER → CREATE_TASK → ASSIGN_USER → CHANGE_STATUS (válido e inválido) → REPORT_STATUS → LIST_TASKS` — confirmado: transições válidas aplicadas, transição inválida (`DONE→TODO`) rejeitada e capturada sem crash, Project Health chegou a 100% ao terminar a task, Top Performers resolveu `alice` corretamente.

**Bug identificado e corrigido durante a validação:** `LogProcessor` descartava o retorno de `dispatchCommand` — `REPORT_STATUS`/`LIST_TASKS` nunca apareciam no console. Corrigido imprimindo `output` quando não-nulo.

**Bug identificado, não-bloqueante, adiado para a Parte 1:** `AddUserState` (CLI por estados) monta `CREATE_USER;username;fullName;role` (4 parâmetros), mas o handler real espera 2 (`username;email`). A CLI interativa via `MainMenuState`/`ViewState` também apresenta `NullPointerException` ao receber comandos brutos digitados livremente. Ambos são bugs pré-existentes da base do Lab 4 na camada `view.statescli`, não introduzidos pela refatoração — serão auditados e corrigidos integralmente na Fase 4 (Parte 1).

### Fase 3 — Parte 3: State Sourcing (JSONL) (concluída)

- [x] **Interface `Repository<T, ID>`** — `Optional<T> findById(ID id)`, `List<T> findAll()`, `void save(T entity)`. Em `service/persistence/Repository.java`.
- [x] **`JsonlRepository<T extends Entity>`** — `service/persistence/JsonlRepository.java`. Cache em `LinkedHashMap` populado por `rehydrate()` no construtor (última linha por id vence); `save` faz `cache.put` + append (`CREATE`+`APPEND`, nunca sobrescreve); cria diretório pai se necessário; falhas de I/O lançam `UncheckedIOException`.
- [x] **`GsonFactory`** — `service/persistence/GsonFactory.java`; instância única de `Gson` com `LocalDateAdapter` registrado, compacta (sem pretty-print, incompatível com JSONL de uma linha por registro); compartilhada por todos os repositórios.
- [x] **`Status implements Entity`** — ganhou `getId()` delegando a `getName()`, para caber no contrato genérico do repositório (é VO, mas usa a identidade textual já decidida em D10).
- [x] **`Workspace` (Parte 3)** — os 5 `Map<String,X>` internos substituídos por `JsonlRepository<X>` apontando para `data/{tasks,projects,users,workflows,statuses}.jsonl`; API pública inalterada (nenhum outro arquivo precisou mudar).
- [x] **`PersistenceService` + `NexusData`** — já haviam sido removidos na Fase 2.
- [x] **Bootstrap (`Main.java`)** — após `new Workspace()` (que já reidrata), calcula `tasksCreated`/`workloadActive` a partir de `workspace.findAllTasks()` e chama `TelemetryRegistry.getInstance().hydrate(...)`.
- [x] **Compilar e rodar smoke test de reidratação** — validado com `log_test_p3.txt` (1ª execução, cria estado) + `log_test_p3_rehydrate.txt` (2ª execução, `data/` preservada). Confirmado: `LIST_TASKS`/`REPORT_STATUS` já retornam o estado completo (Task1 `DONE`, Project Health 100%, Top Performers com `alice`) **antes de qualquer comando de escrita rodar na 2ª execução** — prova de que `JsonlRepository.rehydrate()` reconstrói corretamente tanto as entidades quanto o grafo de referências por ID. `CREATE_USER` repetido (mesmo id) não corrompe o sistema — apenas sobrescreve no cache e faz append de nova linha.

**Refinamento aplicado durante a validação:** a geração de id do `Task` deixou de usar entropia (`nanoTime`) e hash, passando a ser **determinística**: slug do título + slug de `deadline-estimatedEffort-projectId` (ex.: `TASK1-2026-12-01-10-PROJ1`). Facilita testes repetíveis e depuração. **Nota: esta é uma escolha temporária para a fase de desenvolvimento/testes — reavaliar antes da entrega final se um formato mais compacto (hash) é preferível.**

### Fase 4 — Parte 1: CLI por estados

- [ ] Criar `VO DateRange` (se ainda não criado).
- [ ] `LoginState` (CLI) + contexto de sessão imutável carregando `User` autenticado.
- [ ] `ReportMenuState` acessível de `MainMenuState`.
- [ ] `WorkflowManagementState` + estado de captura de transições.
- [ ] `dueDate` no `AddTaskState`; tag `[ATRASADA]` em `TaskDetailView`/`KanbanMenuState`.
- [ ] Sprints/`DateRange` (a definir modelagem exata no início da fase).
- [ ] `Main.java` → inicia em `LoginState`.
- [ ] **Remover pacote `view.interactive`** e todas as suas referências (`InteractiveCLIHandlerFactory` etc.).

### Fase 5 — (Opcional) Parte 4: Web Vaadin

- [ ] `LoginState` web + `VaadinSession`.
- [ ] Kanban web por consulta filtrada `projectId`/`statusId`.
- [ ] Fluxo unidirecional (DTO/`Command` → fachada → refresh).
- [ ] Erros → `Notification`.
- [ ] Perseguir o bônus: mesmos `Command`/handlers da CLI.

### Fase 6 — Fechamento

- [ ] Varredura anti-penalidade (ver seção 7).
- [ ] JavaDoc apenas nos pontos de API pública crítica (por escolha do desenvolvedor).
- [ ] Commits equilibrados da dupla, limpeza de código morto (pacote `interactive`, `PersistenceService`).

---

## 7. Armadilhas e penalidades a evitar

| Penalidade | −pts | Como evitar |
|------------|------|-------------|
| Setter/atributo não-`final` em entidade | −1,5 | varrer `grep -r "void set" model/` e campos sem `final`. |
| Sobrescrita destrutiva de `.jsonl` | −1,0 | usar `StandardOpenOption.APPEND`; nunca truncar. |
| UI tocando repositório/disco/Gson | −1,0 | tudo via fachada `Workspace`. |
| Entidade com referência aninhada de outra | −1,0 | só `String id` entre entidades. |
| Stacktrace vazando pra UI | −0,5 | `try/catch` em `NexusDomainException`/`NexusSystemException` na borda (CLI e listeners Vaadin). |

Outras heranças de penalidade dos labs anteriores que continuam valendo:
comparar status por string (`equals("DONE")`) em vez de `isTerminal`; *train
wrecks* (3+ getters encadeados); `instanceof`/`switch` de tipo para fluxo de
negócio (usar polimorfismo).

---

## 8. Pontos em aberto

- **Sprints**: modelar como VO `Sprint(name, DateRange, List<String> taskIds)`
  dentro de `Project` (por ids) — a confirmar contra o que o corretor espera.
  Só entra de fato na Parte 1.
- **Login por senha ou só username?** O `LoginState` da GUI usa só username; a
  Parte 1 é omissa. Provável: só username (identidade). A confirmar.

## 9. Bugs latentes do base ainda pendentes

- **`LogProcessor` sem `try/catch`** — comandos que falham derrubam o
  processamento. Corrigir na Fase 2.
- **`Status` no pacote errado (D13)** — mover para `model/valueobject/` no
  encerramento da P2, se sobrar tempo.

  