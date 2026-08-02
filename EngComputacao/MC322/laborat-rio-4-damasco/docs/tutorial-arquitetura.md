# Nexus — Tutorial de Arquitetura (visão geral)

> Documento de onboarding. Explica os componentes principais do sistema e como
> os dados fluem entre eles, no estado atual do projeto (Partes 1, 2 e 3 do
> Lab 4 concluídas; CLI funcional).

---

## 1. A ideia central: imutabilidade + comandos + histórico em disco

O Nexus é um motor de gestão de tarefas construído sobre três pilares:

1. **Domínio imutável.** Nenhuma entidade tem setters. Mudar algo significa criar
   uma *nova instância* via métodos `withXxx(...)` (padrão Wither).
2. **Comunicação por comandos.** Toda ação (criar usuário, mudar status, criar
   sprint...) é representada por um objeto `Command` (ação + parâmetros),
   despachado a um `CommandHandler` que sabe executá-la.
3. **Persistência como histórico (State Sourcing).** Nada é sobrescrito em
   disco. Cada mudança de estado é *anexada* como uma nova linha num arquivo
   `.jsonl`. Ao iniciar, o sistema relê todo o histórico e reconstrói o estado
   atual em memória (a última linha de cada entidade "vence").

Esses três pilares aparecem em praticamente todo canto do código, então vale
entendê-los antes de mexer em qualquer arquivo.

---

## 2. O caminho de uma ação, de ponta a ponta

Pegue como exemplo o comando `CHANGE_STATUS;TASK1-XYZ;DONE;alice` (mudar o
status de uma tarefa). O caminho é sempre o mesmo, não importa a ação:

```
Texto do comando (ou construção direta de um objeto Command)
        ↓
CommandParser.parseCommand(line)         → gera um Command (ação + params)
        ↓
CommandDispatcher.dispatchCommand(cmd)   → localiza o handler certo
        ↓
CommandFactory (Map<String, CommandHandler>) → resolve "CHANGE_STATUS" → ChangeStatusHandler
        ↓
ChangeStatusHandler.handle(cmd)          → extrai os parâmetros, chama o serviço
        ↓
TaskService.changeTaskStatus(taskId, newStatus, username)
        ↓
   1. busca a Task, o User, o Project no Workspace
   2. valida RBAC (o usuário tem papel suficiente NESTE projeto?)
   3. valida a transição (o Workflow permite ir do status atual para o novo?)
   4. gera uma NOVA Task via task.withStatusId(newStatus)
   5. workspace.saveTask(novaTask)
        ↓
Workspace → delega para o Repository certo (JsonlRepository<Task>)
        ↓
JsonlRepository.save(task)
   - atualiza o cache em memória (Map<id, Task>)
   - faz APPEND de uma linha JSON no arquivo data/tasks.jsonl
```

Se qualquer validação falhar (usuário sem permissão, transição não permitida,
tarefa inexistente...), uma exceção de domínio (`NexusDomainException` e suas
subclasses) é lançada. Ela nunca deve derrubar a aplicação — sempre é
capturada em algum ponto próximo da interface (CLI ou, futuramente, Web) e
convertida numa mensagem amigável.

---

## 3. Os pacotes principais e o que cada um faz

```
com.nexus
├── model/            → o domínio: entidades, value objects, o contrato Entity
├── controller/        → CommandDispatcher, LogProcessor (execução em lote)
├── exception/         → taxonomia de exceções de domínio e de sistema
├── service/
│   ├── core/           → regras de negócio (UserService, TaskService, ...)
│   ├── core/command/   → os CommandHandler, um por ação, + CommandFactory
│   ├── persistence/     → Repository, JsonlRepository, GsonFactory
│   ├── infra/           → CommandParser, FileLoader
│   ├── Workspace.java    → fachada única de acesso ao estado (ver seção 5)
│   └── WorkspaceQueryService(Impl) → projeção read-only para a UI
└── view/
    ├── statescli/     → a CLI, como máquina de estados (ver seção 6)
    └── statesgui/     → a interface Web em Vaadin (ainda incompleta — é o
                          alvo da próxima fase do projeto)
```

## 4. O domínio (`model/`)

### Entidades (têm identidade estável — implementam `Entity { String getId(); }`)

- **`User`** — `id` = username. Carrega uma `Role` corporativa
  (`VIEWER`/`CONTRIBUTOR`/`MANAGER`), usada para gate de funcionalidades
  estruturais (criar usuário, gerenciar workflow...).
- **`Task`** — `id` gerado uma vez na criação e preservado para sempre.
  Referencia outras entidades **só por ID em String**: `projectId`,
  `ownerId`, `statusId`. Tem `deadline` (prazo formal) e `dueDate` (usado
  para a tag `[ATRASADA]`).
- **`Project`** — `id` = nome. Guarda `workflowId` (String), `teamMembers`
  (`Map<String,Role>`, RBAC **por projeto**, distinto da Role corporativa do
  User) e `sprints` (`Map<String,Sprint>`).
- **`Workflow`** — `id` = nome. Guarda as transições permitidas como
  `Set<Transition>` e o `initialStatusId`.

### Value Objects (sem identidade própria — comparados por valor)

- **`Status`** — nome + flag `isTerminal`. **Nunca** compare status por string
  (`status.equals("DONE")`); use `isTerminal()`.
- **`Role`** — enum `VIEWER < CONTRIBUTOR < MANAGER` (a ordem de declaração
  importa: é usada para comparação hierárquica).
- **`Transition`** — par `(fromStatusId, toStatusId)`, uma aresta do grafo de
  workflow.
- **`DateRange`** — par `(start, end)` de datas, validado.
- **`Sprint`** — nome + `DateRange` + conjunto de `taskId`s. Vive aninhado
  dentro de `Project` (não tem repositório próprio).
- **`Command`** — ação (String) + parâmetros (`String[]`), o "envelope" de
  toda intenção de mutação no sistema.

## 5. `Workspace`: a única porta de entrada para o estado

Nenhum serviço acessa arquivo ou coleção diretamente — tudo passa pelo
`Workspace`, que por sua vez delega a um `JsonlRepository` por tipo de
entidade:

```java
Optional<Task> getTask(String id)
void saveTask(Task task)
List<Task> findAllTasks()
// idem para Project, User, Workflow, Status
```

Por trás dessa fachada, cada `JsonlRepository<T extends Entity>`:
- Mantém um cache em memória (`Map<id, T>`).
- No boot, **reidrata** o cache lendo o arquivo `.jsonl` linha a linha (a
  última linha de cada id vence — é assim que "edições" funcionam num
  histórico append-only).
- Em `save`, atualiza o cache **e** anexa uma nova linha ao arquivo — nunca
  reescreve o arquivo inteiro.

Isso significa: **reiniciar o processo não perde nada.** Os arquivos moram em
`data/*.jsonl` (um por tipo de entidade).

## 6. A CLI (`view/statescli/`) — máquina de estados

A CLI não tem um "menu gigante com switch/case". Cada tela é uma classe que
implementa `ViewState`:

```java
public interface ViewState {
    ViewState handleInput(PromptService prompt);   // lê input, retorna o PRÓXIMO estado
    Collection<String> getAutocompleteCommands();
    default void renderHeader(PromptService prompt) { ... }  // o que aparece antes do prompt
    default boolean isTerminal() { return false; }            // true só no ExitState
}
```

O loop principal (`NexusCLI.run()`) é deliberadamente burro:

```java
while (true) {
    currentState.renderHeader(prompt);
    if (currentState.isTerminal()) return;
    try {
        currentState = currentState.handleInput(prompt);  // navega
    } catch (NexusDomainException e) {
        prompt.printWarning(...);  // rede de segurança: erro nunca derruba a app
    }
}
```

**Fluxo de entrada:** `Main` → `NexusCLI` (começa em `LoginState`) → login
válido monta um `SessionContext` (singleton com `username`+`Role` imutáveis) →
`MainMenuState` → daí em diante, cada opção do menu é uma transição para outro
`ViewState` (`ProjectMenuState`, `UserMenuState`, `WorkflowManagementState`,
`ReportMenuState`, etc.).

**Telas de ação** (que criam/mudam algo) seguem sempre o mesmo roteiro
interno: leem os campos via `PromptService`, montam um `Command`, chamam
`CommandDispatcher.dispatchCommand(command)` diretamente (não via string —
evita parsing desnecessário), e capturam `NexusDomainException` localmente
para mostrar um aviso sem sair da tela.

**Gate de permissão (RBAC corporativo)** acontece no **construtor** da tela
protegida:
```java
if (!SessionContext.getInstance().hasAtLeastRole(Role.MANAGER)) {
    throw new PermissionDeniedException(...);
}
```
A exceção sobe até o loop do `NexusCLI`, que a captura e mostra o aviso — o
usuário nunca chega a ver a tela.

## 7. A camada de leitura para a UI: `WorkspaceQueryService`

É a única porta pela qual a `view` (CLI, e futuramente Web) enxerga o estado.
Propositalmente **nunca retorna entidades de domínio** — só `String`,
`List<String>`, `boolean` etc. Isso impede a UI de manipular o domínio
diretamente e mantém a fronteira MVC estrita. Toda vez que a UI precisa de um
dado novo (ex.: "essa tarefa está atrasada?"), a resposta é um método novo
nessa interface, implementado resolvendo IDs internamente no
`WorkspaceQueryServiceImpl` — nunca vazando o objeto `Task`/`User`/`Project`
para fora.

## 8. Relatórios e telemetria

- **`ReportService`** consome `Workspace.findAllX()` e produz tanto dados
  brutos (`getTopPerformers()`, `getProjectHealth(project)`...) quanto texto
  formatado para exibição (`generateStatusReport()`...).
- **`TelemetryRegistry`** é um singleton simples com contadores (`tasksCreated`,
  `activeWorkload`...). No boot, é "hidratado" a partir do que já existe no
  `Workspace` reidratado, para os contadores baterem com a realidade mesmo
  após reiniciar o processo.

## 9. O que ainda falta (próxima fase)

A interface **Web** (`view/statesgui/`, Vaadin) está esboçada mas incompleta
— há um `TODO` explícito no `KanbanBoardState` para popular as colunas, e o
`LoginState` da Web ainda não valida o usuário contra o `Workspace`. Essa é a
próxima fase do projeto, coberta em detalhe no documento
`plano-parte4-vaadin.md`.