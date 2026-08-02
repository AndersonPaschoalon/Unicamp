[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/IF7nXRNj)
[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=24101313)
# Laboratório 4: Evoluindo a Arquitetura do Nexus para Sistemas Corporativos Reativos

## Introdução e Motivação

Até o momento, o desenvolvimento do **Nexus** permitiu consolidar os fundamentos de Domain-Driven Design (DDD), separação de responsabilidades e tratamento de regras de negócio. No entanto, sistemas de gestão de tarefas modernos e de alta concorrência (como *Jira*, *Linear* ou *Trello*) demandam garantias arquiteturais que vão muito além de um CRUD (*Create, Read, Update, Delete*) tradicional baseado em memória mutável.

Modificações diretas em objetos (`setters`) geram efeitos colaterais perigosos em sistemas complexos, dificultando a auditoria de alterações, quebrando o isolamento de concorrência e corrompendo facilmente o estado salvo em disco se a aplicação cair no meio de um ciclo de escrita. Além disso, as interfaces de usuário modernas precisam refletir essas mudanças de forma fluida, blindada e desacoplada.

Neste laboratório, você transformará o Nexus em uma aplicação corporativa madura através de três grandes evoluções de engenharia:

### O que você vai aprender (Objetivos de Aprendizado)

* **Máquinas de Estados na Prática (State Pattern):** Substituir o antigo fluxo imperativo e linear de menus textuais por uma infraestrutura robusta de navegação desacoplada baseada em estados (`ViewState`).
* **Imutabilidade Estrita e Paradigma Funcional no Domínio:** Eliminar completamente o uso de *setters* nas suas Entidades de Domínio. Você aplicará o padrão **Wither** para garantir que qualquer mutação de negócio gere uma nova instância imutável do objeto, erradicando alterações silenciosas na memória.
* **State Sourcing e Persistência Append-Only (Logs Históricos):** Substituir a estratégia destrutiva de sobrescrever arquivos JSON por um mecanismo de banco de dados baseado em logs textuais (`JSONL`), onde registros novos são apenas anexados ao final do arquivo. Você aprenderá a técnica de **Reidratação de Estado**, reconstruindo a memória cache da aplicação sequencialmente a partir do histórico de disco.
* **Arquitetura de Dados Unidirecional na Web (Vaadin Flow):** Integrar componentes visuais ricos que lidam nativamente com databinding em telas Web, forçando a interface gráfica a respeitar a barreira da imutabilidade do ecossistema backend e reagir a dados desacoplados por IDs.

### O Resultado Final

Ao concluir este laboratório, o Nexus deixará de ser um protótipo acadêmico mutável e passará a ser um sistema de gerenciamento de fluxos resiliente. Se um usuário alterar o status de um card no painel Kanban da CLI por estados ou na interface Web com Vaadin, o sistema gerará um comando de intenção imutável, validará regras de negócio sem efeitos colaterais locais, fará o *append* seguro do novo estado completo no log histórico em disco e reidratará a interface gráfica de forma transparente.

---

## Parte 1: Finalizando a Portabilidade para a Máquina de Estados (CLI)

**Objetivo:**
Substituir definitivamente o antigo fluxo linear e imperativo contido em `com.nexus.view.interactive` pela infraestrutura orientada a objetos baseada no padrão de projeto **State** (`com.nexus.view.statescli`). Você integrará as funcionalidades legadas de relatórios, workflows analíticos, e adicionará as fundações de controle de sessão e temporalidade exigidas por sistemas de gestão reais.

Os componentes centrais que guiarão essa transição são a abstração `ViewState` e o contexto de navegação da CLI.

---

### 1. Geração de Relatórios e Consultas Avançadas (`ReportMenuState`)

A infraestrutura atual do pacote `statescli` prioriza operações CRUD e a renderização visual do quadro Kanban. No entanto, a inteligência de negócios contida em `WorkspaceQueryService` precisa ser exposta de forma estruturada.

* **O que deve ser implementado:**
* Um novo estado de menu chamado `ReportMenuState`, estendendo `ViewState`, que deve ser diretamente acessível a partir do `MainMenuState`.
* Esse menu deve capturar a intenção do usuário e invocar as operações analíticas já mapeadas na camada de serviço. Cada relatório deve renderizar uma saída tabular ou formatada via terminal:
1. **Saúde do Projeto (`ProjectHealthHandler`):** Exibir métricas de conclusão, total de cards e taxas de atraso para um projeto selecionado.
2. **Gargalos Globais (`GlobalBottleneckHandler`):** Identificar quais colunas/status possuem um acúmulo desproporcional de tarefas no Workspace.
3. **Membros Mais Produtivos (`TopPerformersHandler`):** Listar o ranking de usuários com base na vazão (*throughput*) de tarefas concluídas.
4. **Usuários Sobrecarregados (`OverloadedUsersHandler`):** Emitir alertas listando membros que violaram o limite saudável de cards simultâneos em andamento (*WIP - Work in Progress*).





---

### 2. Configuração Avançada de Workflows e Transições

Embora o estado `AddStatusState` permita injetar novos identificadores de fase em um projeto, a modelagem rica de ciclo de vida do Nexus exige que o usuário configure explicitamente as restrições de fluxo de valor (as regras de transição "De $\to$ Para").

* **O que deve ser implementado:**
* Um sub-menu ou ramificação de estados dedicado à customização do ciclo de vida: `WorkflowManagementState`.
* **Criação e Vínculo:** Estados para instanciar um objeto `Workflow` e vinculá-lo dinamicamente a um projeto (`LinkWorkflowHandler`).
* **Barreira de Transição (`AddTransitionCLIHandler`):** Um estado de captura de dados que permita ao usuário selecionar um status de origem, um status de destino e registrar essa transição como válida dentro do Workflow do projeto. Se o usuário tentar mover um card entre estados não mapeados, o sistema deve disparar um `InvalidTransitionException`.



---

### 3. Autenticação, Sessão e Controle de Acesso Baseado em Papéis (RBAC)

Atualmente, o Value Object `Role` (MEMBER, LEADER, ADMIN) e a exceção `PermissionDeniedException` existem de forma isolada, pois os estados e handlers simulam parâmetros estáticos ou assumem privilégios máximos. Não há o conceito de estado de sessão ativa.

* **O que deve ser implementado:**
* **`LoginState`:** O novo ponto de entrada obrigatório da aplicação CLI. O sistema não deve permitir o acesso ao `MainMenuState` sem que uma autenticação válida (via e-mail ou username cadastrado no `UserService`) seja efetuada.
* **Contexto de Sessão:** A máquina de estados deve reter uma referência imutável ao `User` autenticado durante todo o ciclo de vida da CLI.
* **Validação em Cascance:** Todas as transições críticas executadas nos menus ou sub-estados subsequentes devem interceptar a sessão ativa. Ações como criar projetos, modificar workflows ou fechar tarefas devem validar o papel do usuário corrente antes de despachar o comando para o Core, lançando `PermissionDeniedException` localmente se violado.



---

### 4. Gestão de Prazos e Temporalidade (Deadlines & Sprints)

Para aproximar o Nexus de um ecossistema ágil real, o fator tempo deve ser integrado de forma nativa às tarefas e agrupamentos do projeto.

* **O que deve ser implementado:**
* **Prazos nas Tarefas:** Modificar a captura do `AddTaskState` para exigir uma data de vencimento (`dueDate`, utilizando `LocalDate`). Os estados de visualização (`TaskDetailView` e `KanbanMenuState`) devem destacar visualmente (ex: tags `[ATRASADA]`) os cards cujo prazo expirou em relação à data corrente do sistema.
* **Conceito de Iterações (Sprints/Milestones):** Implementar um modelo ou agrupador temporal dentro da entidade `Project`. O usuário deve ser capaz, via novos estados de configuração, de abrir uma iteração delimitada por um objeto de valor `DateRange` (data de início e fim) e associar um subconjunto de tarefas a ela.



---

### Critérios de Aceitação e Avaliação da Parte 1:

1. **Desacoplamento Total:** Nenhuma classe do pacote `com.nexus.view.interactive` poderá ser utilizada na execução final da CLI. O arquivo `Main.java` deve inicializar a aplicação apontando diretamente para o estado inicial (`LoginState`) da nova máquina de estados.
2. **Tratamento de Exceções na UI:** Os novos estados de captura de dados e menus devem ser resilientes. Exceções de domínio (`NexusValidationException`, `PermissionDeniedException`, etc.) capturadas após a execução de comandos não devem derrubar a aplicação; a CLI deve exibir a mensagem de erro amigável ao usuário e retornar de forma segura para o estado de menu anterior.
3. **Navegação Fluida:** O fluxo de navegação deve ser estritamente controlado via retornos do método `handleInput()`. O uso de loops aninhados globais ou desvios condicionais imperativos (`if/else` controlando telas dentro da `Main`) resultará em penalização na nota.

---

## Parte 2: Refatoração para Arquitetura Imutável e State Sourcing

**Objetivo:**
Nesta fase, você abandonará definitivamente a mutabilidade baseada em *setters* e o acoplamento direto de persistência do Nexus. O objetivo é reconstruir o núcleo do sistema para que as Entidades sejam estritamente imutáveis, a memória da aplicação funcione como uma projeção otimizada cacheada e o disco atue como um log histórico de auditoria (*append-only*).

---

### 1. Entidades Imutáveis (Padrão Wither) e Value Objects

O modelo de domínio deve ser refatorado para separar com clareza o que possui identidade contínua (Entidades) do que apenas descreve características intercambiáveis (Value Objects).

#### 1A. Entidades (Raízes de Agregação)

Estas são as peças centrais do sistema, que possuem um identificador único estável. Elas devem ser estritamente imutáveis (todos os atributos declarados como `private final` e sem nenhum método `set`). **O acoplamento por referência de memória direta entre entidades deve ser eliminado.** A navegação e associação entre domínios deve ocorrer exclusivamente através de IDs textuais:

* **`Task`:** Deve conter o `id`, atributos nativos (título, descrição, prazos) e identificadores externos (`projectId`, `assignedUserId`, `statusId`).
* **`Project`:** Deve conter o `id`, nome, descrições, métricas acumuladas e coleções de identificadores (ex: `List<String> memberIds`).
* **`User`:** Deve conter o `id`, nome, email e o cargo corporativo (`Role`).
* **`Workflow`:** Deve conter o `id`, nome e as regras transicionais de ciclo de vida associadas.

#### 1B. Value Objects Verdadeiros (Imutáveis e Descartáveis)

Estas classes não possuem identidade própria ou ciclo de vida isolado. Seu valor é sua própria definição jurídica dentro do domínio. Servem para compor o estado interno das Entidades ou transportar intenções:

* **`Status`:** Representa o rótulo de uma fase ativa (ex: "TODO", "DOING").
* **`Role`:** Enumeração ou objeto de valor que encapsula as permissões de acesso.
* **`Command`:** Estrutura de dados imutável que chega das interfaces. Atua como um DTO (*Data Transfer Object*) de intenção pura: transporta dados e é descartada imediatamente após a execução local.
* **Abstrações Internas:** Estruturas como `Transition` (regras "De $\to$ Para" dentro do Workflow) e `DateRange` (para modelar janelas temporais de iterações).

#### 1C. A Barreira Arquitetural: O Padrão Wither

Para realizar mutações de negócio em dados imutáveis, você implementará métodos com o prefixo `with` (Padrão *Wither*). Toda alteração de estado (ex: mudar o status de uma tarefa com `task.withStatusId(...)`) deve retornar uma **nova instância** completa da entidade, copiando os dados anteriores e aplicando apenas o atributo modificado.

Esse padrão atua como uma barreira de segurança: ele **elimina a possibilidade de alterações silenciosas ou concorrentes na memória**. Como os objetos não possuem mutação interna, é impossível alterar uma entidade e esquecer de registrá-la. Se a nova instância gerada pelo *Wither* não cruzar explicitamente a fronteira dos serviços de persistência, o estado global do sistema permanece perfeitamente intacto.

---

## Parte 3: Repositório Genérico, Persistência Append-Only (State Sourcing) e Reidratação

A infraestrutura de dados passará a ser gerida por Repositórios isolados. Rompendo com o modelo relacional ou de sobrescrita destrutiva de arquivos JSON, a persistência adotará a estratégia de **State Sourcing com arquivos JSONL (*JSON Lines*, formato baseado em append-only)**.

O sistema nunca executará operações de `UPDATE` ou `DELETE` no disco. Cada vez que uma entidade sofrer uma mutação através do padrão *Wither*, o novo estado completo será serializado em uma linha JSON única e anexado ao final do arquivo correspondente (`tasks.jsonl`, `projects.jsonl`, etc.).

### 1. A Interface e o Repositório Genérico

Crie uma interface parametrizada `Repository<T, ID>` para padronizar o ciclo de vida de acesso aos dados. A implementação concreta lidará simultaneamente com o log em disco e o cache em memória:

```java
package com.nexus.service.persistence;

import java.util.List;

public interface Repository<T, ID> {
    T findById(ID id);
    List<T> findAll();
    void save(T entity);
}

```

A implementação baseada em arquivos JSON Lines (`JsonlRepository`) deve processar o histórico acumulado cronologicamente para reidratar a memória cache local da aplicação:

```java
package com.nexus.service.persistence;

import com.google.gson.Gson;
import java.io.*;
import java.util.*;

public class JsonlRepository<T extends Entity> implements Repository<T, String> {
    private final Map<String, T> memoryCache = new HashMap<>();
    private final String filePath;
    private final Class<T> type;
    private final Gson gson;

    public JsonlRepository(String filePath, Class<T> type, Gson gson) {
        this.filePath = filePath;
        this.type = type;
        this.gson = gson;
        this.rehydrate(); // Triggers state rehydration upon initialization
    }

    // 1. Reidratação: Reconstrói o estado atual projetando o histórico sequencialmente
    private void rehydrate() {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                T entity = gson.fromJson(line, type);
                // The state sourcing mechanism: newer states (lines lower in the file)
                // overwrite older entries for the same ID in the in-memory cache.
                memoryCache.put(entity.getId(), entity);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error rehydrating repository: " + filePath, e);
        }
    }

    @Override
    public T findById(String id) {
        return memoryCache.get(id); // Direct O(1) in-memory resolution
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(memoryCache.values());
    }

    // 2. Persistência Append-Only
    @Override
    public void save(T entity) {
        String json = gson.toJson(entity);
        try (FileWriter fw = new FileWriter(filePath, true); // true enables append-only mode
             PrintWriter out = new PrintWriter(new BufferedWriter(fw))) {
            
            out.println(json); // Commits state snapshot to disk log
            memoryCache.put(entity.getId(), entity); // Projects state to memory cache
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist entity: " + entity.getId(), e);
        }
    }
}

```

### 2. Mecanismo de State Log no Disco

Se você criar uma tarefa e alterar o status dela duas vezes subsequentes, o arquivo `tasks.jsonl` conterá exatamente **três registros estruturados independentes** com o mesmo identificador estável, atuando como um ledger cronológico:

```json
{"id":"T1","title":"Implementar Repositorios","statusId":"TODO","projectId":"P1"}
{"id":"T1","title":"Implementar Repositorios","statusId":"DOING","projectId":"P1"}
{"id":"T1","title":"Implementar Repositorios","statusId":"DONE","projectId":"P1"}

```

Ao invocar a reidratação (`rehydrate()`), o loop lê a linha 1 e armazena no mapa. Em seguida, processa a linha 2 e sobrescreve a chave "T1" com o estado intermediário. Por fim, avalia a linha 3, garantindo que o mapa retenha apenas o estado final `DONE` para consultas em tempo de execução, mantendo intacto o histórico completo no disco.

### 3. Isolamento via Fachada do Workspace

O `Workspace` passa a centralizar o controle e inicialização da persistência, porém mantendo os repositórios privados. Ele atua como uma **Fachada (Facade)**, fornecendo assinaturas de métodos limpas para os Command Handlers, ocultando detalhes de infraestrutura.

```java
package com.nexus.service;

import com.google.gson.Gson;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.service.persistence.JsonlRepository;
import com.nexus.service.persistence.Repository;

public class Workspace {
    private final Repository<Task, String> taskRepository;
    private final Repository<Project, String> projectRepository;

    public Workspace(Gson gson) {
        // Initializes domain repositories, triggering historical disc rehydration automatically
        this.taskRepository = new JsonlRepository<>("data/tasks.jsonl", Task.class, gson);
        this.projectRepository = new JsonlRepository<>("data/projects.jsonl", Project.class, gson);
    }

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

### 4. Fluxo de Execução nos Command Handlers

Sem o uso de métodos modificadores e interagindo unicamente com a fachada do `Workspace`, os fluxos operacionais tornam-se transacionalmente protegidos, divididos em três etapas atômicas: consultar, mutar (gerando instância nova) e salvar.

```java
package com.nexus.service.core.command;

import com.nexus.exception.EntityNotFoundException;
import com.nexus.model.entity.Task;
import com.nexus.service.Workspace;

public class ChangeTaskStatusHandler {

    public void execute(ChangeTaskStatusCommand cmd, Workspace workspace) {
        // 1. Fetch: Resolves current aggregate snapshot from in-memory cache
        Task currentTask = workspace.getTask(cmd.taskId());
        if (currentTask == null) {
            throw new EntityNotFoundException("Requested task not found.");
        }

        // 2. Pure Mutation: Wither pattern returns a new immutable instance with updated status
        Task updatedTask = currentTask.withStatusId(cmd.newStatusId());

        // 3. Persist: Dispatches modified state back to the workspace barrier
        // The repository appends the JSONL snapshot to disk and updates the memory projection
        workspace.saveTask(updatedTask);
    }
}

```

---

### Critérios de Aceitação e Avaliação da Parte 2:

1. **Estrita Imutabilidade do Domínio:** A presença de qualquer método com o prefixo `set`, ou atributos de entidade que não possuam o modificador `final`, invalidará a nota deste quesito.
2. **Abstração por Identificadores:** Nenhuma entidade poderá armazenar referências nativas de objetos de outra entidade (ex: `Project` contendo referências diretas de listas de `Task`). O acoplamento de tabelas ou agregação deve ser mapeado puramente por IDs de texto.
3. **Persistência Incremental e Não Destrutiva:** Os arquivos salvos na pasta `data/` devem crescer de forma estritamente incremental (*append-only*). O uso de lógicas que limpem ou sobrescrevam os arquivos `.jsonl` durante o ciclo de execução da aplicação será penalizado.

---

## Parte 4: Portabilidade para a Interface Web (Vaadin) e Sincronização de Estados

**Objetivo:**
Com a transição do núcleo do sistema para um modelo estritamente imutável (Parte 2) e a consolidação da máquina de estados no terminal (Parte 1), a camada de apresentação gráfica em navegador também deve ser reformulada. Nesta fase, você adaptará a interface Web desenvolvida em **Vaadin Flow** para consumir a nova fachada de dados do `Workspace`, integrando-a ao ecossistema de estados visuais (`VaadinViewState`) e garantindo que o frontend reaja de forma reativa e blindada à imutabilidade do domínio.

---

### 1. Desafios da Imutabilidade em Frameworks de UI (Vaadin)

A esmagadora maioria dos frameworks visuais corporativos baseia-se no princípio de *Two-way Data Binding* (vínculo bidirecional de dados), onde os componentes gráficos modificam diretamente os atributos de um objeto em memória via métodos mutadores (`setters`). Ferramentas nativas de captura do Vaadin, como o `Binder`, esperam ler e escrever propriedades de maneira incremental (ex: `binder.readBean(task)` e `binder.writeBean(task)`).

Como as nossas Entidades agora são **estritamente imutáveis** e utilizam o padrão *Wither*, a estratégia de atualização da interface com o usuário deve obrigatoriamente migrar para uma **Arquitetura de Dados Unidirecional**.

#### Conceitos e Modelagens Necessários:

* **Formulários Isolados por DTOs Mutáveis:** Os componentes de entrada de dados da tela (`TextField`, `ComboBox`, `DatePicker`) não devem ser amarrados diretamente às Entidades de Domínio. Eles devem interagir com estruturas temporárias mutáveis de transporte (como classes auxiliares de formulário ou os próprios objetos de comando `Command`).
* **Ciclo de Fluxo Unidirecional:** 1. **Leitura:** A UI consulta o estado atual da entidade imutável na fachada e renderiza os campos na tela.
2. **Interação:** O usuário altera os dados graficamente nos componentes de texto ou seleção.
3. **Intenção (Command):** Ao acionar um evento (como o clique de um botão), a UI captura o estado mutável desses campos e monta um objeto de intenção imutável (`Command`).
4. **Mutação Imutável:** O sistema invoca a fachada do `Workspace`, processando a regra de negócio via padrão *Wither* e gerando a nova instância.
5. **Atualização Visual:** A tela é inteiramente notificada, puxando a projeção mais recente da memória cache para atualizar os componentes visuais.

---

### 2. Mapeamento da UI para a Interface de Estados (`view.statesgui`)

No projeto base fornecido, você encontrará uma infraestrutura inicial de telas web dentro do pacote `com.nexus.view.statesgui`. **Esse código atua como um pontapé inicial.** Nele, a abstração `VaadinViewState` gerencia os componentes da árvore visual do navegador exatamente sob a mesma lógica da máquina de estados do terminal, trocando layouts em vez de linhas de texto.

Você deve estender essa estrutura inicial para cobrir os seguintes fluxos críticos da aplicação:

#### A. O Quadro Kanban Dinâmico (Projeção de Leitura por ID)

* Como o acoplamento por referência direta de objetos foi quebrado (ou seja, a entidade `Project` não possui mais uma coleção `List<Task>` interna, e sim o contrário), o componente visual do Kanban Web não pode mais ler dados aninhados.
* **Implementação:** Suas colunas visuais do Kanban devem ser montadas dinamicamente. O componente deve coletar todos os status válidos do projeto e disparar consultas filtradas à fachada do `Workspace` (ex: coletando todas as tarefas cujo `projectId` coincida com o selecionado), distribuindo os cards graficamente nas colunas com base no `statusId`.

#### B. Formulários e Interações Sem Efeitos Colaterais

* Ao arrastar um card entre colunas ou clicar em um botão de ação, você nunca modificará o objeto associado ao componente gráfico em tempo de execução.
* **Implementação:** Todo evento gerado pelo navegador deve disparar o mesmo fluxo transacional blindado de três passos empregado na CLI:

```java
// Exemplo de comportamento reativo dentro de um componente de evento da UI Vaadin
Button saveButton = new Button("Mover", event -> {
    // 1. Consulta o estado atual da entidade imutável na fachada
    Task currentTask = workspace.getTask(taskId);
    
    // 2. Cria uma nova instância imutável via padrão Wither
    Task updatedTask = currentTask.withStatusId(selectedStatusId);
    
    // 3. Despacha à barreira do Workspace (faz o append no JSONL e atualiza o cache)
    workspace.saveTask(updatedTask);
    
    // 4. Notifica os componentes gráficos do Vaadin a re-renderizarem com a nova projeção
    refreshKanbanGrid();
});

```

#### C. Autenticação e Sessão Visual (`LoginState`)

* Alinhado com o requisito de segurança e RBAC detalhado na Parte 1, o ponto de entrada da aplicação Web deve ser uma tela de login controlada por um estado visual.
* **Implementação:** Após o usuário inserir um e-mail ou username válido cadastrado no repositório de usuários, retenha a referência imutável desse `User` dentro da sessão nativa do framework (`VaadinSession.getCurrent().setAttribute(User.class, loggedInUser)`). Todos os componentes subsequentes devem extrair o usuário da sessão para ocultar, exibir ou bloquear ações em tela com base em seu papel corporativo (`Role`).

---

### 3. Mecanismo de Execução do Ecossistema Nexus

O projeto utiliza o Apache Maven como gerenciador de compilação e dependências. Como o ecossistema agora expõe duas aplicações cliente independentes compartilhando o mesmo motor de domínio imutável, utilize as diretrizes abaixo para executar cada ambiente através do terminal na raiz do projeto:

#### A. Executando a Interface de Terminal (CLI por Estados)

Para compilar o código e rodar a aplicação em linha de comando baseada na nova máquina de estados (`Main.java`), execute:

```bash
mvn clean compile exec:java -Dexec.mainClass="com.nexus.Main"

```

#### B. Executando a Interface Gráfica Web (GUI Vaadin Flow)

O framework Vaadin Flow integra códigos Java de backend com um servidor embutido que gerencia os componentes do navegador (gerando os arquivos TypeScript/JavaScript necessários de forma transparente na pasta `frontend/generated/`). Para levantar o servidor web e testar a interface gráfica via navegador (por padrão em `http://localhost:8080`), execute:

```bash
mvn clean vaadin:prepare-frontend jetty:run

```

---

### Critérios de Aceitação e Avaliação da Parte 3:

1. **Persistência Transparente e Reidratação:** Qualquer ação executada em tela (criar um projeto, associar um membro, mover um card no Kanban) deve obrigatoriamente gerar uma nova linha incremental (*append-only*) nos arquivos `.jsonl` em disco. Se o servidor do Vaadin for reiniciado, a interface gráfica deve reidratar o estado visual perfeitamente a partir dos logs históricos salvos.
2. **Isolamento de Infraestrutura:** Nenhum componente de tela ou classe visual do pacote `statesgui` poderá acessar arquivos em disco, instanciar repositórios concretos ou lidar com o GSON. Toda leitura e modificação de dados deve cruzar exclusivamente a barreira da fachada do `Workspace`.
3. **Resiliência e Tratamento de Erros Visuais:** Exceções lançadas pelas regras de negócio imutáveis do domínio (como `InvalidTransitionException` ou `PermissionDeniedException`) devem ser interceptadas pelos listeners de evento do Vaadin. A aplicação web não pode travar ou expor *stacktraces* brutos; o erro deve ser capturado e renderizado de forma elegante na tela através de componentes amigáveis de notificação (ex: `Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE)`).

Aqui está uma seção equivalente de **Critérios de Avaliação, Bônus e Penalidades** perfeitamente adaptada para o escopo e os novos requisitos arquiteturais do **Lab 4** (Imutabilidade, Máquina de Estados CLI/Web, State Sourcing e Vaadin Flow).

---

## 4. Critérios de Avaliação e Penalidades

### A. Portabilidade para a Máquina de Estados (CLI) (20%)

* **Relatórios e Consultas Avançadas (ReportMenuState):** Um novo estado `ReportMenuState` foi implementado com acesso direto do `MainMenuState`? Os quatro handlers (`ProjectHealthHandler`, `GlobalBottleneckHandler`, `TopPerformersHandler`, `OverloadedUsersHandler`) consultam o `WorkspaceQueryService` sem expor estruturas internas e retornam formatação tabular legível no terminal?

* **Configuração de Workflows e Transições:** Um `WorkflowManagementState` foi implementado permitindo criar e vincular `Workflow` a um projeto? Um estado de captura de transições (`AddTransitionCLIHandler`) permite selecionar status de origem/destino e registrar a regra? O sistema valida transições e dispara `InvalidTransitionException` ao mover cards entre estados não mapeados?

* **Autenticação, Sessão e RBAC:** Um `LoginState` é o ponto de entrada obrigatório da CLI? O sistema retém uma referência imutável do `User` autenticado e valida o papel (`Role`) antes de despachar comandos críticos, lançando `PermissionDeniedException` quando violado? As exceções são capturadas e exibidas amigavelmente sem derrubar a aplicação?

* **Gestão de Prazos e Temporalidade:** O `AddTaskState` foi modificado para exigir `dueDate` (LocalDate)? Os estados de visualização (`TaskDetailView`, `KanbanMenuState`) destacam tarefas atrasadas com tags `[ATRASADA]`? Um modelo de iteração (Sprint) com `DateRange` foi implementado permitindo associar subconjuntos de tarefas a períodos definidos?

### B. Estrita Imutabilidade e Padrão Wither (25%)

* **Pureza do Modelo de Domínio:** Todas as Entidades Raízes de Agregação (`Task`, `Project`, `User`, `Workflow`) e Value Objects possuem seus atributos declarados estritamente como `private final`? A presença de qualquer método modificador (`setter`) ou mutação local destrutiva foi completamente erradicada?
* **Barreira Arquitetural (Wither Pattern):** As alterações de estado de negócio geram novas instâncias imutáveis copiando os dados anteriores? O fluxo operacional dos handlers segue rigorosamente as três etapas atômicas: *consultar a projeção*, *mutar via Wither* e *salvar na fachada do Workspace*?
* **Abstração e Desacoplamento por IDs:** As entidades relacionam-se entre si exclusivamente através de identificadores textuais (`String id`)? Foi eliminado qualquer acoplamento por referência direta de memória (como um `Project` conter uma `List<Task>` em vez de apenas IDs)?

### C. State Sourcing, Reidratação e Isolamento (25%)

* **Persistência Incremental Append-Only:** O repositório concreto (`JsonlRepository`) grava os snapshots completos em disco utilizando exclusivamente o modo anexo (*append*) em arquivos `.jsonl`? A aplicação evita de forma absoluta reescrever, limpar ou sobrescrever linhas antigas durante a execução?
* **Reidratação de Estado:** O mecanismo de inicialização reconstrói perfeitamente o cache em memória (`Map`) varrendo o histórico cronológico do disco, garantindo que o estado mais recente se sobreponha às linhas passadas de forma transparente?
* **Isolamento de Infraestrutura (Facade):** A classe `Workspace` atua como uma fachada opaca, mantendo os repositórios privados? As camadas visuais (tanto a CLI por estados quanto os componentes Vaadin na Web) estão proibidas de instanciar repositórios, acessar arquivos diretamente ou manipular o GSON?

### D. Consistência de UI e Arquitetura Unidirecional (30%)

* **Arquitetura de Dados Unidirecional na UI:** Os formulários e componentes de entrada do Vaadin Flow interagem com estruturas temporárias mutáveis (DTOs/Commands) em vez de vincularem componentes gráficos diretamente a entidades imutáveis via *Two-way Data Binding* tradicional?
* **Controle de Sessão e RBAC Interceptável:** O estado de sessão (`LoginState`) retém o usuário autenticado de forma imutável tanto no contexto da CLI quanto na `VaadinSession`? As ações críticas nos menus interceptam a sessão ativa e validam os papéis (`Role`) antes de despachar comandos, lançando `PermissionDeniedException` quando violados?
* **Resiliência e Tratamento de Exceções Visuais:** As exceções lançadas pelo domínio (ex: `InvalidTransitionException`) são capturadas adequadamente pelas interfaces? O sistema reage exibindo mensagens amigáveis no terminal ou através de componentes gráficos do Vaadin (`Notification`), sem travar a execução e sem expor *stacktraces* brutos para o usuário?

---

### Bônus de Coesão Visual e Polimorfismo Transversal

| Critério | Descrição | Impacto |
| --- | --- | --- |
| **Simetria CLI-Web (Unified Engine)** | Concedido se as duas interfaces de usuário (`statescli` e `statesgui`) consumirem exatamente os mesmos objetos de intenção (`Command`) e dispararem rigorosamente os mesmos Handlers de negócio da camada de serviço, provando o desacoplamento total da lógica de apresentação. | **+1.5 Pontos** |

---

### Penalidades

| Violação Arquitetural | Descrição / Evidência | Penalidade |
| --- | --- | --- |
| **Mutabilidade ou Vazamento de Setters** | Presença de métodos `set` ou falta do modificador `final` em atributos de Entidades de Domínio, permitindo alterações silenciosas em memória. | **-1.5 Pontos** |
| **Sobrescrita Destrutiva de Arquivos** | Uso de lógicas de persistência que limpem, deletem ou reescrevam arquivos `.jsonl` (falha no requisito *append-only* do State Sourcing). | **-1.0 Ponto** |
| **Vazamento de I/O ou Infraestrutura** | Componentes visuais do Vaadin Flow ou da CLI instanciando repositórios, manipulando arquivos diretamente ou contornando a fachada do `Workspace`. | **-1.0 Ponto** |
| **Vazamento de Referências Aninhadas** | Entidades armazenando referências diretas de instâncias de outras entidades (ex: `Project` contendo coleções de objetos `Task` ou `User`). | **-1.0 Ponto** |
| **Exposição de Stacktraces** | Falha em capturar as exceções de domínio (`NexusException` e derivadas) na camada visual, resultando no travamento da CLI ou em telas brancas de erro no Vaadin. | **-0.5 Pontos** |