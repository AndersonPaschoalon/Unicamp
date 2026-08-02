# Nexus — Plano de Ataque: Parte 4 (Web Vaadin + Sincronização de Estados)

> Este documento assume que as Partes 1, 2 e 3 do Lab 4 estão **concluídas e
> validadas**, e que a Parte 1 (CLI) também está funcional (login, RBAC
> corporativo e por projeto, relatórios, workflows, sprints). Para contexto
> arquitetural geral, ver `tutorial-arquitetura.md`.
>
> **Versão 2 deste documento:** todos os fontes reais de `view.statesgui` e
> `MainGui.java` já foram revisados. O plano abaixo é preciso quanto ao estado
> atual de cada arquivo — não há mais suposições sobre conteúdo não visto.

---

## 0. Pré-requisito: reconectar o que foi excluído da build

`MainGui.java` e o pacote `view/statesgui/**` foram excluídos da compilação
(via `pom.xml` e `.vscode/settings.json`) desde a Fase 0. Antes de tudo:

- [ ] Remover as exclusões do `maven-compiler-plugin` no `pom.xml`.
- [ ] Remover as exclusões equivalentes em `.vscode/settings.json`.
- [ ] Rodar `mvn clean compile` — os erros esperados agora já são conhecidos
      (ver seção 1), não é mais uma sondagem às cegas.

## 1. Diagnóstico dos fontes Vaadin existentes

| Arquivo | Situação | Ação |
|---|---|---|
| `MainGui.java` | **Quebrado.** Usa `PersistenceService`/`NexusData` (removidos na Fase 3); `Workspace.getTasks()` (API antiga — hoje é `findAllTasks()`); `ProjectService(userService, workspace)` (faltando `workflowService`); `WorkspaceQueryServiceImpl.build(projectService, taskService, workflowService, userService)` (assinatura antiga — hoje é `build(workspace)`); shutdown hook chamando `persistence.save(...)` (não existe mais — persistência é automática a cada `save`). | **Reescrever por completo** (seção 3). |
| `NexusGUI.java` | OK. `@Route("nexus")`, mantém `currentState`, `navigateTo` limpa e re-renderiza. Modelo request/render simples — **não tem o problema de loop infinito da CLI** (não existe um `while(true)`; cada clique dispara um novo ciclo de render do Vaadin). | Nenhuma mudança necessária. |
| `RootRedirect.java` | OK. Redireciona `""` → `NexusGUI`. | Nenhuma mudança necessária. |
| `VaadinViewState.java` | OK. Contrato mínimo (`render(HasComponents)`). Sem necessidade de `isTerminal()` — não há estado de saída no Vaadin (fechar a aba já resolve). | Nenhuma mudança necessária. |
| `LoginState.java` (GUI) | Login não valida usuário (`TODO` explícito); grava `"loggedInUser"` como `String` solta na sessão. | Corrigir (seção 4). |
| `MainMenuGuiState.java` | Usa a mesma checagem crua de `"loggedInUser"`; só tem botões "Projetos" e "Sair"; falta "Relatórios" e "Workflows" (este último com gate MANAGER). | Corrigir (seção 5). |
| `ProjectMenuState.java` | `TODO` explícito: não lista projetos existentes, só tem os botões fixos. | Corrigir (seção 6). |
| `KanbanBoardState.java` | `TODO` explícito: colunas não são populadas com tarefas; usa `"loggedInUser"` cru. | Corrigir (seção 7). |
| `AddProjectState.java` | **Funcional**, mas com 3 desvios: (1) `catch (Exception ex)` genérico em vez de `NexusDomainException`; (2) monta a linha do comando por concatenação de String (`"CREATE_PROJECT;" + nome + ";" + orcamento`) — funciona, mas é frágil se `nome` contiver `;`; (3) **sem gate de Role** — `CREATE_PROJECT` é ação estrutural (nível MANAGER na tabela da CLI), e aqui qualquer usuário logado pode criar projeto. | Corrigir (seção 8). |
| `AddTaskState.java` (GUI) | Formulário existe, mas o botão "Criar Tarefa" está **vazio** (`TODO` explícito); falta campo de `dueDate` opcional. | Corrigir (seção 9). |

**Pendência de verificação cruzada (não é bug da Web, é lacuna nossa):**
nunca auditamos o `AddProjectState` **da CLI** (`view.statescli.info`, se
existir) para confirmar se ele tem o gate `MANAGER`. Como estamos corrigindo
esse gate agora do lado Web, vale conferir/igualar do lado CLI também, para
não haver assimetria de regras entre as duas interfaces.

## 2. Unificação do bootstrap: `NexusBootstrap`

Como o `MainGui.java` precisa ser reescrito do zero de qualquer forma, é o
momento certo para **extrair o wiring comum** entre CLI e Web para uma classe
única. Isso não é só limpeza — reduz a superfície onde CLI e Web podem
divergir silenciosamente, reforçando o espírito do bônus de simetria já no
nível de inicialização, não só nos comandos.

**Criar** `com.nexus.NexusBootstrap`:

```java
package com.nexus;

import com.nexus.controller.CommandDispatcher;
import com.nexus.model.valueobject.Role;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.service.core.ProjectService;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;
import com.nexus.service.core.WorkflowService;
import com.nexus.service.core.command.CommandFactory;

public final class NexusBootstrap {
    private NexusBootstrap() {}

    public static Workspace initialize() {
        Workspace workspace = new Workspace(); // reidrata sozinho no construtor

        int tasksCreated = workspace.findAllTasks().size();
        int workloadActive = (int) workspace.findAllTasks().stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatusId()))
                .count();
        TelemetryRegistry.getInstance().hydrate(tasksCreated, workloadActive);

        UserService userService = new UserService(workspace);
        WorkflowService workflowService = new WorkflowService(workspace);
        ProjectService projectService = new ProjectService(userService, workflowService, workspace);
        TaskService taskService = new TaskService(userService, projectService, workflowService, workspace);
        ReportService reportService = new ReportService(workspace);

        if (workspace.getUser("admin").isEmpty()) {
            userService.createUser("admin", "admin@nexus.com", Role.MANAGER);
        }

        CommandFactory commandFactory = new CommandFactory(
                userService, projectService, taskService, workflowService, reportService);
        CommandDispatcher.build(commandFactory);
        WorkspaceQueryServiceImpl.build(workspace);

        return workspace;
    }
}
```

- [ ] **Refatorar `Main.java` (CLI)** para chamar `NexusBootstrap.initialize()`
      no lugar do bloco de wiring manual que ele tem hoje (mantém só a parte
      específica da CLI: `LogProcessor`, `Terminal`, `NexusCLI`).
- [ ] **`MainGui.java`** chama a mesma `NexusBootstrap.initialize()` (seção 3).

`LogProcessor` continua fora do bootstrap comum — só a CLI usa (processamento
de logs em lote não é um caso de uso da Web).

## 3. `MainGui.java` — reescrita completa

```java
package com.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainGui {
    public static void main(String[] args) {
        NexusBootstrap.initialize();
        SpringApplication.run(MainGui.class, args);
    }
}
```

Sem `PersistenceService`, sem `NexusData`, sem shutdown hook manual — a
persistência já é automática a cada `save()` do `Workspace` (append no
`.jsonl`), então não há "estado em memória a salvar no encerramento".

## 4. `WebSessionContext` — o análogo do `SessionContext` da CLI

A CLI tem `SessionContext` (singleton de processo). Na Web, a sessão é **por
usuário conectado** (múltiplas abas/pessoas simultâneas), então o objeto vive
dentro da própria `VaadinSession` — inclusive sob Spring Boot, o acesso via
`VaadinSession.getCurrent()` funciona igual (a integração Spring+Vaadin não
muda esse contrato).

**Criar** `com.nexus.view.WebSessionContext`:

```java
package com.nexus.view;

import com.nexus.model.valueobject.Role;
import com.vaadin.flow.server.VaadinSession;

public final class WebSessionContext {
    private static final String ATTR = WebSessionContext.class.getName();

    private final String username;
    private final Role role;

    public WebSessionContext(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }

    public boolean hasAtLeastRole(Role minimum) {
        return role.ordinal() >= minimum.ordinal();
    }

    public static WebSessionContext current() {
        return (WebSessionContext) VaadinSession.getCurrent().getAttribute(ATTR);
    }

    public static void set(WebSessionContext ctx) {
        VaadinSession.getCurrent().setAttribute(ATTR, ctx);
    }

    public static void clear() {
        VaadinSession.getCurrent().setAttribute(ATTR, null);
    }
}
```

Note que `current()` retorna `null` (não lança exceção) se não houver login —
isso casa com o padrão já usado em todas as telas (`if (loggedInUser == null)
{ mainView.navigateTo(new LoginState(mainView)); return; }`), só trocando o
tipo de `String` para `WebSessionContext`.

## 5. `LoginState.java` (Web) — patch

```java
private void realizarLogin(String username) {
    if (username.isEmpty()) {
        Notification.show("O username não pode estar vazio.", 3000, Notification.Position.MIDDLE);
        return;
    }

    WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();
    if (!queryService.listUserNames().contains(username)) {
        Notification.show("Usuário '" + username + "' não encontrado.", 3000, Notification.Position.MIDDLE);
        return;
    }

    Role role = Role.valueOf(queryService.getUserRoleName(username));
    WebSessionContext.set(new WebSessionContext(username, role));
    mainView.navigateTo(new MainMenuGuiState(mainView));
}
```

Imports a adicionar: `com.nexus.model.valueobject.Role`,
`com.nexus.service.WorkspaceQueryService`,
`com.nexus.service.WorkspaceQueryServiceImpl`, `com.nexus.view.WebSessionContext`.
Remover o import de `com.vaadin.flow.server.VaadinSession` (não é mais usado
diretamente aqui).

## 6. `MainMenuGuiState.java` — patch

```java
@Override
public void render(HasComponents container) {
    WebSessionContext session = WebSessionContext.current();
    if (session == null) {
        mainView.navigateTo(new LoginState(mainView));
        return;
    }

    VerticalLayout layout = new VerticalLayout();
    layout.setAlignItems(FlexComponent.Alignment.CENTER);

    layout.add(new H2("Nexus — Menu Principal"));
    layout.add(new Span("Logado como: " + session.getUsername() + " (" + session.getRole() + ")"));

    Button projetosBtn = new Button("Projetos", e -> mainView.navigateTo(new ProjectMenuState(mainView, this)));
    Button relatoriosBtn = new Button("Relatórios", e -> mainView.navigateTo(new ReportMenuState(mainView, this)));
    projetosBtn.setWidth("200px");
    relatoriosBtn.setWidth("200px");
    layout.add(projetosBtn, relatoriosBtn);

    if (session.hasAtLeastRole(Role.MANAGER)) {
        Button workflowsBtn = new Button("Workflows", e -> mainView.navigateTo(new WorkflowManagementState(mainView, this)));
        workflowsBtn.setWidth("200px");
        layout.add(workflowsBtn);
    }

    Button sairBtn = new Button("Sair", e -> {
        WebSessionContext.clear();
        mainView.navigateTo(new LoginState(mainView));
    });
    sairBtn.setWidth("200px");
    layout.add(sairBtn);

    container.add(layout);
}
```

Nota de design: diferente da CLI (onde o gate lança `PermissionDeniedException`
no construtor da tela protegida e o loop principal captura), na Web o padrão é
**simplesmente não mostrar o botão** quando o usuário não tem Role suficiente
— mais natural para uma UI gráfica que para um terminal. Se quiser reforçar
(defesa em profundidade contra alguém navegar direto por URL/estado), replicar
a mesma checagem no início do `render()` de `WorkflowManagementState`, como
descrito na seção 12.

## 7. `ProjectMenuState.java` — patch

```java
@Override
public void render(HasComponents container) {
    WebSessionContext session = WebSessionContext.current();
    if (session == null) {
        mainView.navigateTo(new LoginState(mainView));
        return;
    }

    WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();

    VerticalLayout layout = new VerticalLayout();
    layout.add(new H2("Projetos"));

    List<String> projectNames = queryService.listProjectNames();
    if (projectNames.isEmpty()) {
        layout.add(new Span("Nenhum projeto encontrado."));
    } else {
        for (String projectName : projectNames) {
            Button projectBtn = new Button(projectName, e ->
                mainView.navigateTo(new KanbanBoardState(mainView, this, projectName))
            );
            projectBtn.setWidth("300px");
            layout.add(projectBtn);
        }
    }

    Button addProjectBtn = new Button("+ Novo Projeto", e ->
        mainView.navigateTo(new AddProjectState(mainView, this))
    );
    Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));

    layout.add(addProjectBtn, voltarBtn);
    container.add(layout);
}
```

Trocar a checagem de `"loggedInUser"` cru pela de `WebSessionContext` (igual
seção anterior). Adicionar imports: `java.util.List`,
`com.vaadin.flow.component.html.Span`, `com.nexus.view.WebSessionContext`.

## 8. `AddProjectState.java` — patch

Três correções:

```java
Button saveBtn = new Button("Salvar", e -> {
    WebSessionContext session = WebSessionContext.current();
    if (session == null || !session.hasAtLeastRole(Role.MANAGER)) {
        Notification.show("Apenas MANAGER pode criar projetos.", 3000, Notification.Position.MIDDLE);
        return;
    }

    String nome = nameField.getValue().trim();
    String orcamento = orcamentoField.getValue().trim();

    if (nome.isEmpty() || orcamento.isEmpty()) {
        Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
        return;
    }

    try {
        Command command = new Command(null, "CREATE_PROJECT", new String[]{ nome, orcamento });
        CommandDispatcher.getInstance().dispatchCommand(command);
        Notification.show("Projeto '" + nome + "' criado com sucesso!");
        mainView.navigateTo(parent);
    } catch (NexusDomainException ex) {
        Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
    }
});
```

Mudanças: (1) gate `MANAGER` adicionado; (2) `Command` montado diretamente
(mesmo padrão adotado nas telas da CLI) em vez de concatenar string e usar
`process(...)` — mais robusto a caracteres especiais no nome do projeto; (3)
`catch (NexusDomainException)` no lugar de `catch (Exception)`. Adicionar
imports: `com.nexus.exception.NexusDomainException`, `com.nexus.model.Command`,
`com.nexus.model.valueobject.Role`, `com.nexus.view.WebSessionContext`.

## 9. `AddTaskState.java` (Web) — patch

Implementar o botão vazio, e adicionar campo de `dueDate` opcional:

```java
DatePicker dueDateField = new DatePicker("Data de Vencimento (opcional)");
dueDateField.setWidthFull();

Button salvarBtn = new Button("Criar Tarefa", e -> {
    String titulo = tituloField.getValue().trim();
    LocalDate prazo = prazoField.getValue();
    String esforco = esforcoField.getValue().trim();
    LocalDate dueDate = dueDateField.getValue();

    if (titulo.isEmpty() || prazo == null || esforco.isEmpty()) {
        Notification.show("Preencha os campos obrigatórios.", 3000, Notification.Position.MIDDLE);
        return;
    }

    try {
        String[] params = (dueDate != null)
            ? new String[]{ titulo, prazo.toString(), esforco, projectName, dueDate.toString() }
            : new String[]{ titulo, prazo.toString(), esforco, projectName };
        Command command = new Command(null, "CREATE_TASK", params);
        CommandDispatcher.getInstance().dispatchCommand(command);
        Notification.show("Tarefa criada com sucesso!");
        mainView.navigateTo(parent);
    } catch (NexusDomainException ex) {
        Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
    }
});

form.add(tituloField, prazoField, esforcoField, dueDateField, salvarBtn, cancelarBtn);
```

Adicionar imports: `com.nexus.controller.CommandDispatcher`,
`com.nexus.exception.NexusDomainException`, `com.nexus.model.Command`.
Também trocar a checagem de `"loggedInUser"` para `WebSessionContext` (mesma
mudança das outras telas — mas aqui é opcional já que criar task não exige
gate de Role específico, é nível CONTRIBUTOR/qualquer logado; manter a
checagem só para impedir acesso direto sem login).

## 10. `KanbanBoardState.java` — patch (preencher o TODO + sprints)

Trocar a checagem de sessão (igual às outras) e substituir o bloco TODO:

```java
for (String statusName : statusList) {
    VerticalLayout coluna = new VerticalLayout();
    coluna.setWidth("220px");
    coluna.add(new H3(statusName));

    for (String taskId : queryService.listTaskIdsByProjectAndStatus(projectName, statusName)) {
        String title = queryService.getTaskTitle(taskId);
        String owner = queryService.getTaskOwnerName(taskId);
        boolean overdue = queryService.isTaskOverdue(taskId);

        Div card = new Div();
        StringBuilder texto = new StringBuilder(title);
        if (!"Não atribuído".equals(owner)) texto.append(" (").append(owner).append(")");
        if (overdue) texto.append(" [ATRASADA]");
        card.setText(texto.toString());
        card.getStyle()
            .set("border", "1px solid #ccc")
            .set("padding", "6px")
            .set("margin", "4px")
            .set("cursor", "pointer");

        card.addClickListener(ev -> abrirTransicao(taskId));
        coluna.add(card);
    }

    colunas.add(coluna);
}
```

E o método privado que abre o diálogo de transição:

```java
private void abrirTransicao(String taskId) {
    WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();
    List<String> permitidas = queryService.listAllowedTransitions(taskId);

    Dialog dialog = new Dialog();
    dialog.add(new H3("Mover: " + queryService.getTaskTitle(taskId)));

    if (permitidas.isEmpty()) {
        dialog.add(new Span("Nenhuma transição disponível para esta tarefa."));
    }
    for (String novoStatus : permitidas) {
        Button opcao = new Button(novoStatus, e -> {
            WebSessionContext session = WebSessionContext.current();
            try {
                Command command = new Command(null, "CHANGE_STATUS",
                        new String[]{ taskId, novoStatus, session.getUsername() });
                CommandDispatcher.getInstance().dispatchCommand(command);
                dialog.close();
                mainView.navigateTo(new KanbanBoardState(mainView, parent, projectName));
            } catch (NexusDomainException ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        dialog.add(opcao);
    }

    Button fechar = new Button("Fechar", e -> dialog.close());
    dialog.add(fechar);
    dialog.open();
}
```

Ponto central do "fluxo unidirecional": ao mudar de status com sucesso, **não
se atualiza o card em memória** — a tela inteira é reconstruída
(`new KanbanBoardState(...)`), forçando nova leitura via
`WorkspaceQueryService`, que já reflete o `Command` recém-aplicado.

Adicionar botão de acesso às Sprints, junto de `acoes`:
```java
Button sprintsBtn = new Button("Sprints", e ->
    mainView.navigateTo(new SprintManagementState(mainView, this, projectName))
);
acoes.add(adicionarTarefaBtn, sprintsBtn, voltarBtn);
```

Imports a adicionar: `com.vaadin.flow.component.html.Div`,
`com.vaadin.flow.component.dialog.Dialog`, `com.nexus.controller.CommandDispatcher`,
`com.nexus.exception.NexusDomainException`, `com.nexus.model.Command`,
`com.nexus.view.WebSessionContext`.

## 11. `VaadinCommandGateway` (opcional, recomendado)

Todas as telas acima repetem o mesmo `try { dispatch } catch
(NexusDomainException) { Notification }`. Extrair um helper reduz repetição:

```java
package com.nexus.view.statesgui;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.vaadin.flow.component.notification.Notification;

public final class VaadinCommandGateway {
    private VaadinCommandGateway() {}

    public static void dispatch(Command command, Runnable onSuccess) {
        try {
            CommandDispatcher.getInstance().dispatchCommand(command);
            onSuccess.run();
        } catch (NexusDomainException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}
```

Uso: `VaadinCommandGateway.dispatch(command, () -> mainView.navigateTo(parent));`
— aplicável em todas as seções 8, 9, 10, 13 no lugar do `try/catch` manual.
**Não obrigatório para funcionar**, mas reduz código duplicado; aplicar se
houver tempo.

## 12. Telas novas a criar (ainda não existem)

### 12.1 `menus/ReportMenuState.java`
Espelha `view.statescli.menus.ReportMenuState`. Botões para os 5 comandos
(`TOP_PERFORMERS`, `OVERLOADED_USERS`, `PROJECT_HEALTH`, `GLOBAL_BOTTLENECK`,
`REPORT_STATUS`), cada um despachando o `Command` (sem parâmetros) e
exibindo o texto retornado (usar `Pre` ou `TextArea` somente-leitura para
preservar quebras de linha da formatação em tabela ASCII). Sem gate de Role
(nível VIEWER).

### 12.2 `menus/WorkflowManagementState.java`
Espelha `view.statescli.menus.WorkflowManagementState`. **Gate MANAGER no
início do `render()`** (não no construtor, diferente da CLI — ver nota da
seção 6): se `!session.hasAtLeastRole(Role.MANAGER)`, mostrar
`Notification` + `mainView.navigateTo(parent)` e `return` antes de montar
qualquer componente. Formulários para os 4 comandos: `CREATE_WORKFLOW`,
`LINK_WORKFLOW`, `SET_INITIAL_STATUS`, `ALLOW_TRANSITION`.

### 12.3 `menus/SprintManagementState.java`
Espelha `view.statescli.menus.SprintManagementState`. Mesmo gate MANAGER.
Formulário de `CREATE_SPRINT` (com dois `DatePicker` para início/fim) e de
`ADD_TASK_TO_SPRINT`. Acessível a partir de `KanbanBoardState` (seção 10).

### 12.4 `info/AddUserState.java` (Web)
Gate MANAGER. Campos: username, email. Despacha `CREATE_USER`.

### 12.5 `info/AddMemberState.java` (Web)
Campos: projeto (pode vir fixo se aberto a partir do Kanban, ou `ComboBox`
populado via `queryService.listProjectNames()`), username, role (`ComboBox`
via `queryService.listRoles()`). Despacha `ADD_MEMBER`.

## 13. Ordem de implementação sugerida

1. **Seção 0** — reconectar a build.
2. **Seção 2** — `NexusBootstrap` (e refatorar `Main.java` da CLI para usá-lo).
3. **Seção 3** — `MainGui.java` reescrito.
4. **Seção 4** — `WebSessionContext`.
5. **Seção 5** — `LoginState` (Web) corrigido — nada mais funciona sem login.
6. **Seção 6** — `MainMenuGuiState` corrigido.
7. **Seção 7** — `ProjectMenuState` corrigido.
8. **Seção 8** — `AddProjectState` corrigido — necessário para ter projetos para testar o Kanban.
9. **Seção 10** — `KanbanBoardState` completo (a tela mais visível/testável).
10. **Seção 9** — `AddTaskState` (Web) completo.
11. **Seção 11** — `VaadinCommandGateway` (se houver tempo, refatorar as telas já feitas para usá-lo).
12. **Seção 12.1** — `ReportMenuState` (baixo risco, sem gate).
13. **Seção 12.2, 12.3** — `WorkflowManagementState`, `SprintManagementState` (MANAGER-gated).
14. **Seção 12.4, 12.5** — `AddUserState`, `AddMemberState`.
15. **Verificação cruzada** — conferir/igualar o gate `MANAGER` no `AddProjectState` **da CLI**, se existir (pendência da seção 1).
16. **Smoke test final** — repetir o roteiro de smoke test da CLI, agora pela Web (login como `admin`, criar projeto, workflow, membro, task com `dueDate`, mover status via diálogo, criar sprint, ver relatórios) e comparar `data/*.jsonl` gerado pelos dois caminhos.

## 14. Critérios de aceitação da Parte 4

- Nenhuma tela Web instancia `Repository`/`Gson`/toca arquivo diretamente —
  tudo via `CommandDispatcher` (escrita) e `WorkspaceQueryService` (leitura).
- Nenhum `Binder` sobre entidade — sempre campos soltos → `Command` manual.
- Toda ação bem-sucedida gera uma nova linha em `data/*.jsonl` (mesmo
  `Workspace`/`JsonlRepository` da CLI).
- Reiniciar o servidor Web reidrata o estado automaticamente.
- Erros de domínio viram `Notification`, nunca stacktrace na tela.
- (Bônus) Um `CREATE_TASK` disparado pela CLI e outro pela Web produzem
  exatamente o mesmo formato de linha no `.jsonl`.
- (Bônus) CLI e Web compartilham `NexusBootstrap`, `CommandDispatcher`,
  `CommandFactory`, `WorkspaceQueryService` — zero lógica de negócio duplicada.