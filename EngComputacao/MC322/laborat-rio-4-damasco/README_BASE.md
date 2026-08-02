# Lab 4:

## Parte 1: Finalizando Portabilidade para States

Avaliando as classes dentro de `com.nexus.view.interactive` (que representam a abordagem baseada em múltiplos handlers orientados a comandos ou menus lineares) e comparando com a nova máquina de estados em `com.nexus.view.statescli`, observamos o seguinte cenário:

Já foi migrado com sucesso a criação básica de entidades através dos estados de captura de dados (`AddProjectState`, `AddUserState`, `AddTaskState`, `AddStatusState`, `AddMemberState`) e o fluxo de alteração de estado (`ChangeTaskStatusState`).

No entanto, as seguintes funcionalidades ou lógicas presentes nos Handlers do pacote `interactive` **ainda não possuem um estado equivalente ou não foram totalmente integradas** ao fluxo do `statescli` além de algumas funcionalidades adicionais.

1. **Geração de Relatórios e Consultas Avançadas (`GenerateReportCLIHandler`)**:
* **O que falta:** No pacote `statescli`, os menus focam muito em operações CRUD e visualização do Kanban. Falta transicionar a lógica de consultas do `WorkspaceQueryService` para estados visíveis, tais como:
* `ProjectHealthHandler` (Saúde do projeto).
* `GlobalBottleneckHandler` (Gargalos globais).
* `TopPerformersHandler` (Membros mais produtivos).
* `OverloadedUsersHandler` (Usuários sobrecarregados).


* Seria ideal criar um `ReportMenuState` acessível a partir do `MainMenuState`.


2. **Configuração Detalhada de Workflows (`AddWorkflowCLIHandler`, `LinkWorkflowHandler`, `AddTransitionCLIHandler`)**:
* **O que falta:** Embora tenha feito `AddStatusState` para criar novos estados em um projeto, a criação explícita de um objeto `Workflow`, o vínculo dele a um projeto (`LinkWorkflowHandler`) e a definição de transições permitidas entre estados (`AddTransitionCLIHandler`) não estão mapeados de forma intuitiva como sub-menus ou estados de configuração no `statescli`.


3. **Autenticação, Sessão e Controle de Acesso Real (RBAC)**

* **Lacuna atual:** O sistema possui o Value Object `Role` (MEMBER, LEADER, ADMIN) e lança `PermissionDeniedException`, mas **não existe o conceito de usuário logado (sessão)**. Atualmente, os handlers/estados simulam ou passam parâmetros estáticos, ou apenas assumem a operação.
* **O que implementar:** Um estado inicial de `LoginState`. Uma vez autenticado, o `User` logado fica retido no contexto da CLI (ou na sessão do Vaadin), e todas as ações validam automaticamente se aquele usuário específico pode mover a tarefa, adicionar membros ou fechar o projeto.

4. Gestão de Prazos e Temporalidade (Deadlines & Sprints)

* **Lacuna atual:** A classe `Task` possui campos essenciais, mas gerenciamento de tarefas real depende criticamente do fator **tempo**.
* **O que implementar:** * Data de vencimento (`dueDate`) nas tarefas com validações (ex: alertar se a tarefa está atrasada).
* Criação de marcos (Milestones) ou iterações (Sprints) dentro de um `Project`, agrupando um subconjunto de tarefas com um intervalo de tempo definido.


## Parte 2: Refatoração para Arquitetura Imutável e State Sourcing

**Objetivo:**
Nesta fase do projeto Nexus, você abandonará a mutabilidade perigosa e o acoplamento de persistência. O objetivo é reconstruir o núcleo do sistema para que as Entidades sejam imutáveis, a memória reflita sempre a projeção mais recente dos dados e o disco atue como um log histórico (append-only) de todas as alterações.

**Requisitos Arquiteturais:**

**1. Entidades Imutáveis (Wither Pattern) e Value Objects**

O modelo de domínio deve ser refatorado para separar claramente o que possui identidade (Entidades) do que apenas descreve características (Value Objects).

**1A. Entidades (Raízes de Agregação)**
Estas são as peças centrais do sistema. Possuem um ID único e serão guardadas diretamente nos repositórios (e projetadas nos `HashMaps` do `Workspace`). Elas devem ser estritamente imutáveis (todos os atributos `final` e sem métodos `set`). Devem relacionar-se entre si apenas através de IDs, sem listas de objetos complexos:

* **Task**: Deve conter apenas o `id`, atributos nativos (título, descrição, prazo) e identificadores externos (`projectId`, `assignedUserId`, `statusId`).
* **Project**: Deve conter apenas o `id`, nome, descrições, métricas e listas de identificadores (ex: `memberIds`).
* **User**: Deve conter apenas o `id`, nome, email e permissões/cargos (`Role`).
* **Workflow**: Deve conter o `id`, nome e regras de como os estados se relacionam.

**1B. Value Objects Verdadeiros (Imutáveis e Descartáveis)**
Estas classes não têm um ID próprio (ou o seu valor é o próprio ID) e não possuem ciclo de vida isolado. Servem apenas para compor o estado das Entidades ou transportar dados:

* **Status**: Representa a fase (ex: "TODO", "DOING").
* **Role**: Enumeração ou objeto estrito que define permissões.
* **Command**: Estrutura de dados que chega do interface (CLI ou Parser). Atua como um DTO de intenção: transporta dados e é descartado imediatamente após gerar as novas instâncias do modelo no handler.
* **Abstrações Internas**: Estruturas de valor como `Transition` (regras "De -> Para" dentro do Workflow) e `DateRange` (para modelar prazos).

**1C. A Barreira Arquitetural (Padrão Wither)**
Para realizar qualquer mutação de negócio nas Entidades, implemente o padrão *Wither*. Toda alteração (ex: mudar o status de uma tarefa com `task.withStatus(...)`) deve retornar uma **nova instância** do objeto, copiando os dados anteriores e aplicando a alteração.
Este padrão atua como uma barreira de segurança: ele **elimina a possibilidade de alterações silenciosas na memória**. Como os objetos não têm métodos de mutação interna, é impossível alterar uma entidade e esquecer de a gravar. Se a nova instância gerada pelo *Wither* não for explicitamente enviada para a persistência, o estado do sistema permanece intacto.

---

### 2. Repositório Genérico, Persistência Append-Only (State Sourcing) e Reidratação

A infraestrutura de dados passará a ser gerida por Repositórios, que atuarão como a única ponte entre a memória da aplicação e o disco. As entidades deixarão de guardar referências diretas para outras entidades (ex: um `Project` não terá uma `List<Task>`). **A navegação entre domínios será feita exclusivamente através de IDs**, consultando os respetivos repositórios.

A persistência adotará o modelo **State Sourcing com ficheiros Append-Only**. O sistema nunca atualizará (overwrite) ou apagará registos no disco. Os ficheiros (`tasks.jsonl`, `users.jsonl`, etc.) receberão apenas inserções no final.  Cada vez que uma entidade sofrer uma mutação através do padrão *Wither*, o novo estado completo será serializado em JSON e anexado ao ficheiro. O disco conterá a história imutável de todas as alterações.

#### A. A Interface e o Repositório Genérico

Crie uma interface `Repository<T, ID>` para padronizar o acesso aos dados. A implementação concreta lidará simultaneamente com a escrita no ficheiro *append-only* e com o cache em memória.

```java
public interface Repository<T, ID> {
    T findById(ID id);
    List<T> findAll();
    void save(T entity);
}

```

A implementação para ficheiros JSONL (`JsonlRepository`) deve ler o histórico e manter a memória atualizada:

```java
public class JsonlRepository<T extends Entity> implements Repository<T, String> {
    private final Map<String, T> memoryCache = new HashMap<>();
    private final String filePath;
    private final Class<T> type;
    private final Gson gson;

    public JsonlRepository(String filePath, Class<T> type, Gson gson) {
        this.filePath = filePath;
        this.type = type;
        this.gson = gson;
        rehydrate(); // Carrega o estado ao instanciar
    }

    // 1. Reidratação: Reconstrói o estado atual a partir do histórico
    private void rehydrate() {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                T entity = gson.fromJson(line, type);
                // A magia acontece aqui: se o ID já existir no mapa, a versão 
                // mais recente (linha mais abaixo no ficheiro) sobrescreve a antiga.
                memoryCache.put(entity.getId(), entity);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao reidratar " + filePath, e);
        }
    }

    @Override
    public T findById(String id) {
        return memoryCache.get(id); // Consulta O(1) na memória
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(memoryCache.values());
    }

    // 2. Persistência Append-Only
    @Override
    public void save(T entity) {
        String json = gson.toJson(entity);
        try (FileWriter fw = new FileWriter(filePath, true); // 'true' = modo append
             PrintWriter out = new PrintWriter(new BufferedWriter(fw))) {
            
            out.println(json); // Grava no disco
            memoryCache.put(entity.getId(), entity); // Atualiza a memória
            
        } catch (IOException e) {
            throw new RuntimeException("Falha ao persistir entidade", e);
        }
    }
}

```

#### B. Como os Dados Ficam Salvos no Disco?

Se você criar uma Tarefa e depois mudar o status dela duas vezes, o ficheiro `tasks.jsonl` não terá uma, mas **três linhas** com o mesmo ID. O disco funciona como um log de mutações:

```json
{"id":"T1","title":"Estudar Java","statusId":"TODO","projectId":"P1"}
{"id":"T1","title":"Estudar Java","statusId":"DOING","projectId":"P1"}
{"id":"T1","title":"Estudar Java","statusId":"DONE","projectId":"P1"}

```

Ao rodar o `rehydrate()`, o sistema lê a linha 1 e guarda no mapa. Lê a linha 2, e sobrescreve o ID "T1" no mapa. Lê a linha 3, e sobrescreve novamente. No final, a memória tem apenas a versão `DONE`, mas o ficheiro guardou todo o histórico.

#### C. A Nova Estrutura do Workspace (Fachada de Dados)

O `Workspace` passa a ser o ponto central que inicializa a persistência, mas **mantém os repositórios privados**. Ele atua como uma fachada, oferecendo métodos diretos e limpos para os Handlers lerem e gravarem o estado.

```java
public class Workspace {
    private final Repository<Task, String> taskRepository;
    private final Repository<Project, String> projectRepository;

    public Workspace(Gson gson) {
        // Inicializa os repositórios (o que dispara a reidratação a partir do disco automaticamente)
        this.taskRepository = new JsonlRepository<>("data/tasks.jsonl", Task.class, gson);
        this.projectRepository = new JsonlRepository<>("data/projects.jsonl", Project.class, gson);
    }

    // Encapsulamento: O Workspace fornece e recebe a Entidade, ocultando o Repositório
    public Task getTask(String id) {
        return taskRepository.findById(id);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    public Project getProject(String id) {
        return projectRepository.findById(id);
    }

    public void saveProject(Project project) {
        projectRepository.save(project);
    }
}

```

#### D. O Novo Fluxo num Command Handler

Sem métodos `set` e comunicando apenas com a fachada do `Workspace`, a lógica de alteração de estado num Handler torna-se um fluxo direto e blindado de três passos: consultar, mutar (gerando cópia) e salvar.

```java
public void execute(ChangeTaskStatusCommand cmd, Workspace workspace) {
    // 1. Consulta: Busca a entidade atual na memória (via fachada)
    Task currentTask = workspace.getTask(cmd.taskId());
    if (currentTask == null) {
        throw new EntityNotFoundException("Tarefa não encontrada.");
    }

    // 2. Mutação Imutável: O padrão Wither cria e retorna uma nova instância com o status alterado
    Task updatedTask = currentTask.withStatusId(cmd.newStatusId());

    // 3. Persistência: Envia a nova instância de volta para o Workspace
    // O Workspace delega ao Repositório, que fará o append no JSONL e atualizará a memória
    workspace.saveTask(updatedTask);
}

```

**Por que este fluxo é seguro?** Se fizermos apenas `currentTask.withStatusId("DONE")` e esquecer de invocar o `saveTask()`, absolutamente nada muda no sistema. A instância antiga continua intacta na memória e o disco não sofre alterações. O estado do sistema só avança quando a nova instância cruza a fronteira do `saveTask()`.

Aqui está uma proposta de seção para o enunciado do **Lab 4**, mantendo a mesma estrutura, o tom direto e a profundidade técnica que você já adotou nas Partes 1 e 2.

Esta seção explica aos alunos o que deve ser feito na interface web (**Vaadin**) para acompanhar as profundas mudanças arquiteturais do backend imutável e a nova máquina de estados da CLI.

---

## Parte 3: Portabilidade para a Interface Web (Vaadin) e Sincronização de Estados

**Objetivo:**
Com a mudança do núcleo do sistema para um modelo estritamente imutável (Parte 2) e a consolidação da máquina de estados no terminal (Parte 1), a camada de apresentação gráfica também precisa ser reformulada. Nesta fase, você adaptará a interface Web desenvolvida em **Vaadin Flow** para consumir a nova fachada de dados do `Workspace`, integrando-a ao padrão de estados e garantindo que o frontend reaja corretamente à imutabilidade do domínio.

---

### 1. Desafios da Imutabilidade em Frameworks de UI (Vaadin)

A maioria dos frameworks visuais corporativos baseia-se em databinding bidirecional (Two-way Data Binding), onde o componente gráfico modifica diretamente os atributos de um objeto na memória via métodos `set` (ex: `binder.readBean(task)` e `binder.writeBean(task)`). Como as nossas entidades agora são **estritamente imutáveis** e utilizam o padrão *Wither*, a estratégia de atualização da UI deve mudar.

#### Conceitos e Modelagens Necessários:

* **Separação por Data Transfer Objects (DTOs) ou Forms**: Os componentes de entrada de dados do Vaadin (`TextField`, `ComboBox`, etc.) devem interagir com estruturas temporárias mutáveis (pode ser o próprio objeto `Command` ou uma classe auxiliar de formulário).
* **Fluxo Unidirecional de Dados**:
1. A UI lê o estado atual da entidade imutável e renderiza os campos.
2. O usuário altera os dados na tela.
3. Ao salvar, a UI captura os dados mutáveis da tela, monta um comando de intenção (`Command`) ou invoca o padrão *Wither* a partir da entidade original.
4. O novo objeto gerado é enviado para o `Workspace`.
5. A tela é inteiramente reidratada ou atualizada com a nova instância retornada.



---

### 2. Mapeamento da UI para a Arquitetura de Estados (`view.statesgui`)

Assim como a CLI foi dividida em estados de navegação (`ViewState`), a interface Vaadin utilizará a abstração `VaadinViewState` para coordenar quais componentes ou telas estão ativos no navegador para o usuário atual.

#### O que deve ser implementado no Vaadin:

#### A. O Quadro Kanban Dinâmico (Projeção de Leitura)

* Como o `Project` não possui mais uma `List<Task>` interna (apenas as tarefas possuem o `projectId` e o `statusId`), o seu componente de Kanban Web não pode mais ler dados aninhados.
* **Implementação:** O componente do Kanban deve realizar consultas direcionadas ao `Workspace` (ex: `workspace.getTasksByProject(projectId)`), agrupando os cards em colunas horizontais com base no `statusId` de forma dinâmica.

#### B. Formulários de Criação e Alteração Sem Setters

* Ao editar uma tarefa ou mover um card de coluna na tela (via clique ou simulação de drag-and-drop), você não alterará o objeto visualizado.
* **Implementação:** O evento da UI deve disparar o mesmo fluxo blindado de três passos usado na CLI:

```java
// Exemplo de comportamento dentro de um componente de botão na UI Vaadin
Button saveButton = new Button("Mover", e -> {
    Task currentTask = workspace.getTask(taskId);
    
    // Altera gerando uma cópia imutável
    Task updatedTask = currentTask.withStatusId(selectedStatusId);
    
    // Persiste no arquivo append-only e atualiza o cache em memória
    workspace.saveTask(updatedTask);
    
    // Notifica o Vaadin para renderizar o Kanban novamente com os novos dados
    refreshKanbanGrid();
});

```

#### C. Estado de Sessão e Login Visual (`LoginState`)

* Conectando com o requisito de segurança e RBAC (Parte 1), a aplicação Vaadin deve iniciar exibindo uma tela de login em vez do painel principal.
* **Implementação:** Armazene o usuário autenticado no escopo da sessão do Vaadin (`VaadinSession.getCurrent().setAttribute(User.class, loggedInUser)`). Todos os componentes subsequentes (botões de criação, mudança de status) devem verificar o `Role` do usuário contido na sessão antes de permitir a interação ou invocar o `Workspace`.

---

### 3. Requisitos de Entrega para a Interface Web

Para que a interface gráfica seja considerada razoavelmente completa e alinhada com o backend, os seguintes pontos serão avaliados:

1. **Persistência Transparente:** Qualquer ação disparada pela interface Web (criar projeto, adicionar membro, mover tarefa) deve gerar um novo registro append-only nos arquivos `.jsonl`. Se a aplicação Web for reiniciada, o estado visualizado na tela deve ser perfeitamente reidratado a partir do disco.
2. **Isolamento de Domínio:** Nenhum componente visual do Vaadin poderá acessar diretamente os arquivos JSONL ou instanciar repositórios. Toda e qualquer operação de leitura ou escrita deve cruzar obrigatoriamente a fachada do `Workspace`.
3. **Tratamento de Exceções de Domínio:** Se o domínio lançar uma exceção imutável (como `InvalidTransitionException` ou `PermissionDeniedException`), a interface Web deve capturar o erro e exibi-lo de forma amigável para o usuário (ex: usando um componente `Notification` do Vaadin), impedindo que a tela trave ou fique desalinhada com o estado real dos dados.