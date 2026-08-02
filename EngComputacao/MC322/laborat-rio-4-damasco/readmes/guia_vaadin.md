# Guia de Implementação da Interface Web com Vaadin Flow

Este guia explica como construir a interface gráfica web do Nexus utilizando **Vaadin Flow**, integrando-a ao motor de domínio existente através dos mesmos padrões arquiteturais da CLI por estados.

---

## 1. Visão Geral: A Máquina de Estados Visual

A interface web do Nexus espelha a arquitetura da CLI. Enquanto a CLI usa `ViewState` para controlar quais textos e menus são impressos no terminal, a interface web usa `VaadinViewState` para controlar quais componentes HTML são renderizados no navegador.

### A Interface `VaadinViewState`

```java
public interface VaadinViewState {
    void render(HasComponents container);
}
```

Cada "tela" da aplicação web é uma classe que implementa essa interface. O método `render()` recebe o container pai (o layout da tela) e é responsável por montar todos os componentes visuais (botões, campos, tabelas, etc.) daquele estado.

### O Controlador de Navegação: `NexusGUI`

A classe `NexusGUI` é o ponto central da aplicação web. Ela atua como o **contexto** da máquina de estados, análogo ao `NexusCLI` na interface de terminal:

```java
@Route("nexus") // Acessível em http://localhost:8080/nexus
public class NexusGUI extends VerticalLayout {

    private VaadinViewState currentState;

    public NexusGUI() {
        setSizeFull();
        navigateTo(new LoginState(this)); // Ponto de entrada obrigatório
    }

    public void navigateTo(VaadinViewState nextState) {
        this.currentState = nextState;
        this.removeAll();              // Limpa todos os componentes do estado anterior
        this.currentState.render(this); // Renderiza o novo estado polimorficamente
    }
}
```

**Conceito central:** Ao chamar `navigateTo()`, a tela inteira é reconstruída pelo novo estado. Não há mutação parcial de componentes — o estado anterior é descartado e o novo toma conta do container.

---

## 2. Criando Sua Primeira Tela

Qualquer nova tela segue esta estrutura básica:

```java
package com.nexus.view.statesgui.menus;

import com.nexus.view.NexusGUI;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MinhaTela implements VaadinViewState {

    private final NexusGUI mainView;     // Referência ao controlador de navegação
    private final VaadinViewState parent; // Estado "pai" para o botão Voltar

    public MinhaTela(NexusGUI mainView, VaadinViewState parent) {
        this.mainView = mainView;
        this.parent = parent;
    }

    @Override
    public void render(HasComponents container) {
        VerticalLayout layout = new VerticalLayout();

        layout.add(new H2("Minha Tela"));

        Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));
        layout.add(voltarBtn);

        container.add(layout);
    }
}
```

**Padrão de navegação:** Sempre passe `this` como `parent` ao instanciar um estado filho. Isso forma uma cadeia de navegação análoga à pilha de chamadas da CLI.

---

## 3. Lendo Dados do Domínio com `WorkspaceQueryServiceImpl`

A camada visual **nunca acessa entidades de domínio diretamente**. Toda leitura de dados deve passar pela interface `WorkspaceQueryService`, que expõe apenas tipos primitivos (`String`, `List<String>`).

### Obtendo a Instância do Serviço de Consulta

```java
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;

// Dentro do render() ou no construtor do estado:
WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();
```

### Métodos Disponíveis para Leitura

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `listProjectNames()` | `List<String>` | Nomes de todos os projetos |
| `listUserNames()` | `List<String>` | Usernames de todos os usuários |
| `listStatusNames()` | `List<String>` | Todos os status registrados no workspace |
| `listStatusNamesByProject(projectName)` | `List<String>` | Status do workflow de um projeto específico |
| `listTaskIdsByProject(projectName)` | `List<String>` | IDs das tarefas de um projeto |
| `listTaskIdsByProjectAndStatus(projectName, statusName)` | `List<String>` | IDs das tarefas de um projeto filtradas por status |
| `listAllowedTransitions(taskId)` | `List<String>` | Transições permitidas para uma tarefa |
| `getTaskTitle(taskId)` | `String` | Título de uma tarefa |
| `getTaskStatusName(taskId)` | `String` | Status atual de uma tarefa |
| `getTaskOwnerName(taskId)` | `String` | Username do responsável pela tarefa |
| `getTaskDeadline(taskId)` | `String` | Prazo da tarefa (formato `yyyy-MM-dd`) |
| `getProjectWorkflowName(projectName)` | `String` | Nome do workflow do projeto |

### Exemplo: Exibindo uma Lista Dinâmica

```java
@Override
public void render(HasComponents container) {
    VerticalLayout layout = new VerticalLayout();
    WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();

    List<String> itens = queryService.listProjectNames(); // ou qualquer outro método de listagem

    if (itens.isEmpty()) {
        layout.add(new Span("Nenhum item encontrado."));
    } else {
        for (String item : itens) {
            Button btn = new Button(item, e -> {
                // navega para o próximo estado passando o item selecionado
            });
            layout.add(btn);
        }
    }
    container.add(layout);
}
```

---

## 4. Executando Ações com `CommandDispatcher`

Para **modificar** o estado do domínio (criar projeto, mudar status de tarefa, etc.), use o `CommandDispatcher`. Ele recebe uma string no formato `"AÇÃO;param1;param2;..."` e delega ao handler correto.

### Obtendo a Instância

```java
import com.nexus.controller.CommandDispatcher;

CommandDispatcher dispatcher = CommandDispatcher.getInstance();
```

### Formato dos Comandos

| Ação | Formato da String | Parâmetros |
|------|------------------|------------|
| `CREATE_USER` | `CREATE_USER;username;email` | 2 params |
| `CREATE_PROJECT` | `CREATE_PROJECT;nome;orcamento` | 2 params |
| `CREATE_TASK` | `CREATE_TASK;titulo;prazo;esforco;nomeProjeto` | 4 params |
| `ASSIGN_USER` | `ASSIGN_USER;taskId;username` | 2 params |
| `CHANGE_STATUS` | `CHANGE_STATUS;taskId;novoStatus;username` | 3 params |
| `ADD_MEMBER` | `ADD_MEMBER;nomeProjeto;username;papel` | 3 params |
| `CREATE_WORKFLOW` | `CREATE_WORKFLOW;nomeWorkflow` | 1 param |
| `LINK_WORKFLOW` | `LINK_WORKFLOW;nomeProjeto;nomeWorkflow` | 2 params |
| `CREATE_STATUS` | `CREATE_STATUS;nomeStatus` | 1 param |
| `ALLOW_TRANSITION` | `ALLOW_TRANSITION;nomeWorkflow;statusOrigem;statusDestino` | 3 params |

> **Atenção:** O prazo de `CREATE_TASK` deve estar no formato `yyyy-MM-dd` (ex: `2025-12-31`).

### Exemplo Completo: Botão que Cria um Projeto

```java
Button salvarBtn = new Button("Salvar Projeto", e -> {
    String nome = nomeField.getValue().trim();
    String orcamento = orcamentoField.getValue().trim();

    if (nome.isEmpty() || orcamento.isEmpty()) {
        Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
        return;
    }

    try {
        String command = "CREATE_PROJECT;" + nome + ";" + orcamento;
        CommandDispatcher.getInstance().process(command);
        Notification.show("Projeto '" + nome + "' criado com sucesso!");
        mainView.navigateTo(parent); // Volta para o menu anterior
    } catch (Exception ex) {
        Notification.show("Erro: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
    }
});
```

**Regra de ouro:** Sempre envolva as chamadas ao `CommandDispatcher` em um bloco `try/catch`. As exceções de domínio (como `NexusDomainException`, `PermissionDeniedException`, `InvalidTransitionException`) devem ser capturadas e exibidas de forma amigável com `Notification.show()`.

---

## 5. Gerenciamento de Sessão com `VaadinSession`

Após o login, o username do usuário autenticado deve ser armazenado na sessão do Vaadin para que todos os componentes subsequentes possam acessá-lo.

### Salvando um Atributo na Sessão

```java
import com.vaadin.flow.server.VaadinSession;

// Armazena qualquer valor serializable na sessão do usuário:
VaadinSession.getCurrent().setAttribute("chave", valor);
mainView.navigateTo(/* próximo estado */);
```

### Lendo um Atributo da Sessão em Qualquer Tela

```java
String valor = (String) VaadinSession.getCurrent().getAttribute("chave");

if (valor == null) {
    // Atributo não definido — redireciona para o estado adequado
    mainView.navigateTo(/* estado de fallback */);
    return;
}
```

### Removendo um Atributo da Sessão (ex: logout)

```java
VaadinSession.getCurrent().setAttribute("chave", null);
mainView.navigateTo(/* estado inicial */);
```

---

## 6. Componentes Vaadin Essenciais

| Componente | Pacote | Uso |
|------------|--------|-----|
| `VerticalLayout` | `orderedlayout` | Container vertical (empilha componentes) |
| `HorizontalLayout` | `orderedlayout` | Container horizontal (colunas lado a lado) |
| `Button` | `button` | Botão clicável com `addClickListener` |
| `TextField` | `textfield` | Campo de texto livre |
| `PasswordField` | `textfield` | Campo de senha (oculta o texto) |
| `DatePicker` | `datepicker` | Seletor de data (`LocalDate`) |
| `Select<T>` | `select` | Dropdown de seleção simples |
| `ComboBox<T>` | `combobox` | Dropdown com busca por texto |
| `H2`, `H3`, `Span` | `html` | Elementos de texto HTML |
| `Notification` | `notification` | Mensagens de feedback (toast) |
| `Grid<T>` | `grid` | Tabela de dados |

### Exibindo Notificações de Erro

```java
// Notificação simples (auto-fecha em 3 segundos, posicionada no centro)
Notification.show("Operação realizada com sucesso!");

// Notificação de erro com duração e posição personalizadas
Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
```

### Usando `DatePicker`

```java
DatePicker prazoField = new DatePicker("Prazo da Tarefa");
prazoField.setMin(LocalDate.now()); // Não permite datas no passado

// Lendo o valor e convertendo para String (formato yyyy-MM-dd)
LocalDate prazo = prazoField.getValue();
if (prazo == null) {
    Notification.show("Selecione uma data de prazo.");
    return;
}
String prazoStr = prazo.toString(); // Já está no formato yyyy-MM-dd
```

### Usando `ComboBox` para Listas Dinâmicas

```java
// ComboBox populado com uma lista de strings
ComboBox<String> opcaoBox = new ComboBox<>("Opção");
opcaoBox.setItems(List.of("Item A", "Item B", "Item C"));
opcaoBox.setPlaceholder("Selecione...");

// Ou populado dinamicamente via QueryService:
opcaoBox.setItems(queryService.listProjectNames());
```

---

## 7. Tratamento de Erros Visuais

Todos os listeners de evento do Vaadin devem capturar exceções de domínio e exibi-las de forma amigável. **Nunca deixe uma exceção não capturada travar a interface.**

```java
saveButton.addClickListener(e -> {
    try {
        // ... monta e despacha o comando ...
        CommandDispatcher.getInstance().process(command);
        Notification.show("Sucesso!");
        mainView.navigateTo(parent);
    } catch (NexusDomainException | PermissionDeniedException ex) {
        // Exibe o erro de domínio sem travar a aplicação
        Notification.show("Regra de negócio: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
    } catch (Exception ex) {
        // Captura erros inesperados
        Notification.show("Erro inesperado: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
    }
});
```

---

## 8. Execução da Aplicação Web

Para iniciar o servidor web com a interface Vaadin, execute na raiz do projeto:

```bash
mvn spring-boot:run
```

A interface estará disponível em: **`http://localhost:8080/nexus`**

> Acessar `http://localhost:8080` redireciona automaticamente para `/nexus`.

> O primeiro boot pode demorar alguns minutos enquanto o Vaadin compila os assets do frontend. Execuções subsequentes são mais rápidas.

---

## 9. Exemplos de Referência no Projeto

Os arquivos de kickstart no pacote `com.nexus.view.statesgui` demonstram os padrões descritos neste guia:

| Arquivo | Conceito Demonstrado |
|---------|---------------------|
| `view/NexusGUI.java` | Controlador de navegação da máquina de estados |
| `statesgui/menus/LoginState.java` | Sessão com `VaadinSession`, validação de entrada |
| `statesgui/menus/MainMenuGuiState.java` | Hub de navegação principal, checagem de sessão |
| `statesgui/menus/ProjectMenuState.java` | Listagem dinâmica com `WorkspaceQueryService` |
| `statesgui/menus/KanbanBoardState.java` | Quadro Kanban com colunas e cards interativos |
| `statesgui/info/AddProjectState.java` | Formulário de criação com `CommandDispatcher` |
| `statesgui/info/AddTaskState.java` | Formulário com `DatePicker` e `ComboBox` |

---

## 10. Checklist de Implementação

Antes de finalizar sua implementação da interface web, verifique:

- [ ] Nenhum estado do `statesgui` instancia repositórios ou acessa arquivos diretamente
- [ ] Todas as leituras de dados passam por `WorkspaceQueryServiceImpl.getInstance()`
- [ ] Todas as ações de escrita passam por `CommandDispatcher.getInstance().process(...)`
- [ ] Todos os listeners de evento possuem `try/catch` com `Notification.show()` no catch
- [ ] Telas que exigem autenticação verificam a sessão no início do `render()` e redirecionam se necessária
- [ ] Após qualquer ação que modifica o estado, a tela é recriada via `navigateTo()` (sem mutação parcial de componentes)
