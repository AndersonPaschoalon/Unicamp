# Lab 3: Padrões de Projeto, Polimorfismo e Resiliência na Camada de Interação

## 1. Sumário da Atividade

A evolução do **Nexus** continua. Após a reestruturação dos dados e a quebra do monólito inicial, a arquitetura agora enfrenta o desafio da escalabilidade de funcionalidades e manutenibilidade da interface. A atual estrutura de decisão baseada em extensos blocos `switch-case` fere princípios fundamentais de design de software, tornando a adição de novos comandos um processo frágil e propício a regressões.

Neste laboratório, o foco desloca-se para a camada de interação e despacho (I/O) e para a integração de serviços periféricos. A aplicação sistemática dos princípios **Aberto/Fechado (OCP)**, **Substituição de Liskov (LSP)** e **Inversão de Dependência (DIP)** será avaliada criticamente.

As seções deste laboratório estão divididas em:

1. **Reformando o CommandDispatcher** – transição de lógica procedural para Polimorfismo de Interface e Herança Estrutural.
2. **CLI Polimórfica** – encapsulamento do JLine e criação de um motor de I/O expansível.
3. **Novos Requisitos da CLI** – cobertura total do ciclo de vida das tarefas e workflows.
4. **Sistema de Notificações Multicanal** – nova funcionalidade transversal utilizando herança e contratos.
5. **Critérios de Avaliação** – rigor arquitetural e penalidades.

OBS: Na pasta readmes há explicações para a reorganização de pastas e para o novo sistema de persistêncuia usado (vamos abandonar o log aos poucos).

---

## 2. Reformando o CommandDispatcher

O roteamento de comandos centralizado e procedural deve ser desmantelado.

### A. O Fim do `Switch-Case` Gigante (Polimorfismo de Interface)
O `CommandDispatcher` viola o princípio Aberto/Fechado (OCP).
1. Crie uma interface `CommandHandler` (o Contrato puro) que defina o método `void handle(Command command)`.
2. Crie implementações separadas para cada comando do sistema (ex: `CreateUserHandler`, `AssignUserHandler`), garantindo que implementem a interface.
3. Substitua o `switch-case` no `CommandDispatcher` por um mapa de registros (`Map<String, CommandHandler>`).
4. O `CommandDispatcher` deve operar via **Polimorfismo de Interface**, delegando a execução através de `map.get(comando).handle(args)`, garantindo a substitutibilidade das lógicas sem alterar o despachante.
5. O enunciado intencionalmente não especifica *onde* o `Map<String, CommandHandler>` deve ser instanciado e populado. Esta é uma decisão de design sua. Avalie criticamente o local apropriado para construir este registro (ex: na classe `Main`, em uma classe `Registry` dedicada, ou via fábrica) e injete-o no `CommandDispatcher`. O objetivo é garantir baixo acoplamento e respeitar o princípio da Inversão de Dependência (DIP), evitando que o Dispatcher precise instanciar suas próprias dependências.

### B. Compartilhamento de Comportamento sem Estado (Default Methods)
A conversão de tipos dos argumentos (como `Long.parseLong()`, `LocalDate.parse()`) não deve ser repetida exaustivamente nas classes concretas. Como essas conversões não exigem armazenamento de estado (atributos), você **não deve** usar uma classe abstrata. Utilize os recursos modernos do Java:

```java
public interface CommandHandler {
    
    // O Contrato principal
    void handle(String[] args);

    // Comportamento utilitário injetado via Default Method (Java 8+)
    default Long extractLong(String[] args, int index, String fieldName) {
        try {
            return Long.parseLong(args[index]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new CommandParsingException("Argumento inválido para o campo: " + fieldName);
        }
    }
    
    default LocalDate extractDate(String[] args, int index) {
        // Implementação similar com tratamento de DateTimeParseException
    }
}
```
As classes de comando concretas (ex: `CreateUserHandler`) implementarão esta interface, fornecendo a lógica do `handle` e utilizando os métodos `default` herdados para extrair argumentos de forma segura e limpa.

### C. Separação de Responsabilidades de I/O (O Contrato Restrito)
O `CommandDispatcher` não é um formatador de texto.
1. Remova qualquer menção a `System.out` ou `System.err` do núcleo de despacho e regras de negócio.
2. Em caso de dados inconsistentes, falhas de parsing ou violações de domínio, o método deve lançar uma exceção pertinente da taxonomia do sistema. O tratamento e a exibição do erro são responsabilidade exclusiva da camada visual (a CLI).

## 3. Tratamento de Erros: A Hierarquia de Exceções Polimórficas

No Lab anterior, utilizávamos uma exceção genérica (`NexusValidationException`). Isso impede que o sistema reaja de forma diferente a erros estruturais versus erros provocados pelo usuário. Com base nos conceitos de **Fail-Fast** e **Encapsulamento de Erro**, você deve construir uma taxonomia polimórfica de exceções.

### A. A Taxonomia Base
Crie a seguinte hierarquia estendendo `RuntimeException`:

1. **`NexusException`**: A raiz de todos os erros customizados do sistema.
2. **`NexusSystemException`** (Herda de `NexusException`): Representa falhas da máquina ou de infraestrutura (ex: falha de I/O em arquivos, injeção de dependência falha). O sistema **não** deve tentar recuperar-se silenciosamente destas; elas indicam um problema estrutural.
3. **`NexusDomainException`** (Herda de `NexusException`): Representa quebras de contrato de negócio provocadas pelo usuário (ex: projeto inexistente, falta de permissão). O sistema deve capturar essas exceções e informar o usuário para que ele corrija a ação.

### B. Especialização de Domínio e "Fail-Fast"
O princípio do *Fail-Fast* dita que os erros devem ser identificados o mais cedo possível. Crie exceções filhas de `NexusDomainException` com nomes que representem o domínio do problema:

* **`EntityNotFoundException`**: Lançada quando buscas no `Workspace` ou repositórios falham.
* **`PermissionDeniedException`**: Lançada quando um usuário tenta executar uma ação (como `MOVE_TASK`) sem possuir o *Role* adequado.
* **`InvalidStateException` / `InvalidTransitionException`**: Lançada quando as regras do `Workflow` são violadas (ex: mover uma tarefa finalizada).
* **`CommandFormatException`**: Lançada logo no início do processamento se argumentos numéricos contiverem letras ou se a sintaxe interativa for violada.
* **`UnknownCommandException`**: Lançada quando o sistema recebe uma instrução inexistente.

### C. Encapsulamento de Erro (Exception Translation)
Nunca permita que exceções internas da linguagem (como `NullPointerException`, `IndexOutOfBoundsException` ou `NumberFormatException`) vazem para a interface do usuário. Você deve interceptar ou prevenir essas falhas técnicas e "reembalá-las" em exceções de domínio.

**Exemplo Prático no `CommandDispatcher`:**
Se o usuário digitar um comando inválido, buscar no mapa (`map.get(comando)`) retornará `null`. Chamar `.handle()` logo em seguida causará um `NullPointerException` (vazamento estrutural). Previna isso traduzindo o estado para a nossa semântica:

```java
CommandHandler handler = handlers.get(commandName);
if (handler == null) {
    // Encapsulamento de erro: Protegemos a CLI de um NPE
    throw new UnknownCommandException("O comando '" + commandName + "' não é reconhecido pelo Nexus.");
}
handler.handle(args);
```

Da mesma forma, ao converter uma `String` para `Long` nos utilitários do Handler, capture a `NumberFormatException` silenciosa e lance uma `CommandFormatException`.

### D. Resiliência na Camada de Interação
O polimorfismo de exceções permitirá que o seu ponto de entrada na CLI atue como uma barreira final limpa, reagindo ao "tipo" do erro sem conhecer a implementação específica de cada handler:

```java
try {
    dispatcher.dispatch(command);
} catch (NexusDomainException e) {
    // Erros do utilizador (Formatação, Permissão, Não Encontrado)
    prompt.printWarning("Aviso de Negócio: " + e.getMessage());
} catch (NexusSystemException e) {
    // Erros estruturais
    prompt.printError("Falha Crítica do Sistema: " + e.getMessage());
    // Aqui seria o local ideal para gravar num arquivo de log antes de encerrar
}
```

## 4. CLI Polimórfica e Roteamento Inteligente (View)

A interface de linha de comando (View) deve ser totalmente desacoplada das regras de negócio (Controller/Service). A arquitetura exigirá um roteamento inteligente para otimizar a experiência do usuário e evitar repetição de código.

O fluxo unidirecional de I/O deve operar sob as seguintes regras:
1. O usuário digita uma linha no terminal.
2. A construção do `Command` e o despacho direto para o `CommandDispatcher` (sem interatividade) ocorrerão imediatamente se a entrada atender a **pelo menos um** dos seguintes critérios:
   * O usuário forneceu múltiplos argumentos na linha de comando (ex: separados por `;`, conforme a regra de parsing do projeto).
   * O comando acionado **não** possui suporte ao modo interativo (lembre-se: nem todos os comandos do sistema precisarão de um assistente passo a passo).
3. Caso o usuário digite **apenas a palavra-chave** (ou argumentos insuficientes) **e** exista um **`InteractiveCLIHandler`** registrado para este comando, o sistema suspende o parser direto e delega o controle para a interface interativa, que guiará o usuário passo a passo.

### A. O `PromptService` Concreto (Encapsulamento de I/O)
Para a fase interativa, os alunos devem utilizar esta classe, que centraliza a interação com o terminal e isola a complexidade da biblioteca `JLine`:

```java
package com.nexus.view;

import org.jline.reader.LineReader;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import java.util.List;

public class PromptService {
    private final LineReader reader;
    private final Terminal terminal;
    private Completer activeCompleter;

    public PromptService(LineReader reader, Terminal terminal) {
        this.reader = reader;
        this.terminal = terminal;
    }

    public String readString(String promptText) {
        return reader.readLine(promptText).trim();
    }

    public String readWithOptions(String promptText, List<String> options) {
        Completer previous = this.activeCompleter;
        try {
            this.activeCompleter = new StringsCompleter(options);
            return reader.readLine(promptText).trim();
        } finally {
            this.activeCompleter = previous;
        }
    }

    public void printSuccess(String message) {
        terminal.writer().println("\u001B[32m✔ " + message + "\u001B[0m");
    }

    public void printError(String message) {
        terminal.writer().println("\u001B[31m❌ " + message + "\u001B[0m");
    }
}
```

### B. O Contrato `InteractiveCLIHandler`
O contrato agora é enxuto. Ele é acionado *exclusivamente* quando o parser identifica que a entrada requer assistência interativa.

```java
public interface InteractiveCLIHandler {
    String getTriggerWord();
    
    /**
     * Assume o controle do terminal para coletar os dados faltantes
     * e, ao final, despacha o comando para o Controller.
     */
    void processInteractive(PromptService prompt);
}
```

### C. Implementação Interativa e Projeção de Dados (Read-Only)
É **terminantemente proibido** que um `InteractiveCLIHandler` aplique regras de negócio ou altere o estado do sistema. Ele apenas coleta dados (utilizando serviços de leitura para alimentar o autocompletar em **todas** as etapas possíveis), empacota tudo e delega a execução.

Exemplo de implementação com interatividade plena:

```java
public class AssignUserCLIHandler implements InteractiveCLIHandler {
    private final CommandDispatcher dispatcher; // O Controller
    private final WorkspaceQueryService queryService; // Serviço puramente de leitura para a View

    public AssignUserCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryService queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() { return "ASSIGN_USER"; }

    @Override
    public void processInteractive(PromptService prompt) {
        // 1. Coleta guiada com autocompletar para TODOS os campos
        List<String> projectNames = queryService.listProjectNames();
        if (projectNames.isEmpty()) {
            prompt.printError("Não há projetos disponíveis para esta operação.");
            return;
        }
        String projectName = prompt.readWithOptions("Selecione o Projeto (Tab): ", projectNames);
        
        List<String> userNames = queryService.listUserNames();
        if (userNames.isEmpty()) {
            prompt.printError("Não há usuários cadastrados.");
            return;
        }
        String userName = prompt.readWithOptions("Selecione o Usuário (Tab): ", userNames);
        
        List<String> roles = queryService.listRoles();
        String role = prompt.readWithOptions("Selecione o Papel (Tab): ", roles);

        // 2. Empacotamento
        String[] commandArgs = {projectName, userName, role};
        Command command = new Command(getTriggerWord(), commandArgs);

        // 3. Delegação para o Controller com Polimorfismo de Exceções
        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Usuário atribuído com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
```

### D. A Fronteira de Leitura: O `WorkspaceQueryService`

Nos exemplos anteriores, notará a injeção de um `WorkspaceQueryService` na classe da View em vez do `Workspace` tradicional. Isto não é acidental. A interface interativa precisa de consultar dados constantemente para alimentar os menus e o autocompletar (Tab). No entanto, fornecer o agregador principal (`Workspace`) ou os repositórios à camada de View quebra o encapsulamento, abrindo portas para manipulações indevidas do estado do sistema.

Para resolver este dilema e respeitar o princípio da Segregação de Interfaces (ISP), deve implementar um serviço dedicado exclusivamente a projeções de leitura para a interface.

**Regras para o Query Service:**
1. **Apenas Leitura (Read-Only):** A classe ou interface deve conter apenas métodos de busca.
2. **Projeções Seguras:** Não deve retornar entidades de domínio completas (como `Project` ou `User`). Deve retornar dados formatados, tipos primitivos ou listas de *Strings* (ex: nomes, IDs) que a interface precisa estritamente para apresentar.
3. **Isolamento:** A View usa o `QueryService` para apresentar opções. O Controller usa o `Workspace` ou `CommandDispatcher` para aplicar alterações.

**Exemplo de Contrato:**
```java
public interface WorkspaceQueryService {
    List<String> listProjectNames();
    List<String> listUserNames();
    List<String> getRolesForDisplay();
    // Qualquer outro dado necessário para alimentar a I/O, sem expor as entidades.
}
```
A implementação concreta desta interface pode consultar os mesmos repositórios ou agregadores internos, mas como a CLI interage apenas através do contrato do `WorkspaceQueryService`, o compilador garante que a View não tentará executar ações de escrita disfarçadas.

### E. Delegação Estrita: O Caso do Comando de Atribuição (Camada de Negócio)
**Aviso Arquitetural Crítico:** Quando o Controller (`CommandDispatcher`) despachar o comando construído pela CLI para o respectivo `CommandHandler` (na camada de negócio), a responsabilidade de buscar entidades ou validar integridade pertence estritamente às coleções detentoras (os repositórios ou agregadores, como `Workspace`). 

**Incorreto (Quebra de Delegação e Vazamento de Lógica):**
```java
// O Handler investigando o estado interno do Workspace indevidamente
Project project = workspace.getProjects().stream()
    .filter(p -> p.getName().equalsIgnoreCase(projectName))
    .findFirst()
    .orElseThrow(...);
```

**Correto:**
```java
// Delegação plena para a coleção detentora
Project project = workspace.getProjectByName(projectName); 
```
A busca iterativa para extrair nomes e validar presenças deve ser isolada no `Workspace`, `Project` ou Repositórios. O `CommandHandler` apenas orquestra o fluxo de dados entre os serviços.

## 5. Nova Implementação: Navegação Interativa do Kanban (Padrão State)

O Nexus agora precisará exibir o estado dos projetos de forma visual e navegável. O usuário deve conseguir entrar no modo Kanban, listar projetos, visualizar o quadro de tarefas (agrupadas por status) e aprofundar-se nos detalhes de uma tarefa específica. 

Para evitar a criação de um monólito de navegação com múltiplos `while` e `switch-cases` aninhados (o que tornaria o código impossível de manter), você construirá uma interface de usuário baseada em uma **Máquina de Estados (State Pattern)**. Cada "Tela" (View) do sistema será um objeto responsável por renderizar a si mesmo e ditar qual é a próxima tela com base na entrada do usuário.

### A. O Contrato de Navegação (`interface ViewState`)
Crie a interface que define o comportamento de qualquer tela navegável do Nexus:
```java
public interface ViewState {
    /**
     * Renderiza o conteúdo da tela no terminal.
     */
    void render(PromptService prompt);

    /**
     * Processa a escolha do usuário e retorna o próximo estado (Tela).
     * Retorna 'this' para se manter na mesma tela, ou 'null' para sair do modo de navegação.
     */
    ViewState handleInput(String input);
}
```

### B. A Estrutura Visual Base (`abstract class AbstractNexusView`)
Para garantir uma identidade visual consistente e não repetir código de cabeçalhos e tratamento de comandos de saída em todas as telas, utilize herança estrutural e o padrão *Template Method*:
1. Crie a classe `AbstractNexusView implements ViewState`.
2. Ela deve possuir a instância do `WorkspaceQueryService` para buscar os dados de forma read-only.
3. Implemente a lógica comum: um método protegido `printHeader(String title)` e o tratamento do comando global de escape (ex: digitar `VOLTAR` ou `0` para retornar à tela anterior). 
4. Esta classe abstrata pode conter uma referência à tela `parent` (a tela anterior) para permitir o retorno dinâmico na árvore de navegação.

### C. As Especializações (A Árvore de Navegação)
Você deverá implementar, no mínimo, três estados concretos que herdam de `AbstractNexusView`. O fluxo de transição entre eles é unidirecional:

1. **`ProjectSelectorView` (Estado Inicial):**
   * **Renderização:** Exibe a lista de projetos ativos com um índice numérico.
   * **Input:** Se o usuário digitar um número válido, instancia e retorna a `KanbanBoardView` passando o ID do projeto selecionado no construtor.

2. **`KanbanBoardView` (O Quadro):**
   * **Renderização:** Agrupa e exibe as tarefas pelo seu `Status` atual. Você deve iterar sobre os status do Workflow do projeto e listar as tarefas pertencentes a cada coluna (ex: `[TODO]`, `[IN PROGRESS]`, `[DONE]`). Cada tarefa deve exibir seu ID interno.
   * **Input:** Recebe o ID de uma tarefa. Se o ID existir neste projeto, instancia e retorna a `TaskDetailView`. Se o usuário digitar `VOLTAR`, retorna o estado `parent` (`ProjectSelectorView`).

3. **`TaskDetailView` (Inspeção Profunda):**
   * **Renderização:** Exibe todos os dados da tarefa (Título, Descrição, Responsável, Data de Criação, Histórico de Transições/Logs).
   * **Input:** Aguarda o comando `VOLTAR` para retornar ao `KanbanBoardView`.

4. Ponto aditional (1 ponto): Implementar todas as visualizações dentro deste mesmo padrão.

### D. O Motor de Execução
No seu `CommandDispatcher` ou na classe principal da CLI, a ativação do comando interativo (ex: `VIEW_KANBAN`) iniciará o seguinte motor agnóstico de I/O, que confia plenamente no polimorfismo das telas:

```java
public void startKanbanNavigation(PromptService prompt) {
    ViewState currentState = new ProjectSelectorView(queryService, null); // null pois é a raiz
    
    while (currentState != null) {
        prompt.clearScreen(); // Limpa o terminal para sensação de transição de tela
        currentState.render(prompt);
        
        String input = prompt.readString("Selecione uma opção (ou VOLTAR): ");
        try {
            currentState = currentState.handleInput(input);
        } catch (NexusDomainException e) {
            prompt.printWarning(e.getMessage());
            prompt.readString("Pressione ENTER para continuar...");
        }
    }
}
```

### E. Exemplos de Renderização (Mockup do Terminal)

Abaixo estão exemplos textuais de como a CLI deve renderizar cada estado da máquina. A formatação exata (cores, bordas) é livre, mas a disposição da informação e os *prompts* de transição de estado devem seguir esta estrutura lógica.

**Estado Inicial: `ProjectSelectorView`**
```text
==================================================
           NEXUS: SELEÇÃO DE PROJETO
==================================================

Projetos Ativos:
[1] Sistema de RH (Workflow: Standard)
[2] Migração de Cloud (Workflow: Agile)
[3] App Mobile (Workflow: Agile)

--------------------------------------------------
> Selecione o ID do projeto (ou digite VOLTAR para sair): 2
```

**Transição para o Estado: `KanbanBoardView`**
*(Após o usuário digitar `2` na tela anterior)*
```text
==================================================
        NEXUS KANBAN: Migração de Cloud
==================================================

[ BACKLOG ]
- TSK-201: Mapear dependências locais
- TSK-204: Definir arquitetura de rede

[ EM ANDAMENTO ]
- TSK-202: Configurar instâncias EC2 (Resp: Alice)

[ CONCLUÍDO ]
- TSK-203: Criar repositório Git (Resp: Bob)

--------------------------------------------------
> Digite o ID da Tarefa para inspecionar (ou VOLTAR): TSK-202
```

**Transição para o Estado: `TaskDetailView`**
*(Após o usuário digitar `TSK-202` na tela anterior)*
```text
==================================================
        NEXUS INSPEÇÃO: TSK-202
==================================================

Título: Configurar instâncias EC2
Projeto: Migração de Cloud
Status Atual: EM ANDAMENTO
Responsável: Alice (Role: DEV)
Criada em: 2026-04-15

[ Descrição ]
Provisionar 3 instâncias t3.medium com Ubuntu 22.04 na sub-rede pública.

[ Histórico de Transições ]
1. BACKLOG -> EM ANDAMENTO (por Alice em 2026-04-16)

--------------------------------------------------
> Digite VOLTAR para retornar ao Kanban: VOLTAR
```

--- 

### Por que adicionar isso ao enunciado?
Muitos alunos perdem tempo tentando criar interfaces gráficas complexas no terminal usando bibliotecas pesadas quando o foco da disciplina é Arquitetura Orientada a Objetos. Mostrar mockups em texto puro (`ASCII/ANSI`) calibra o esforço deles para o que realmente importa: a estrutura das classes (`ViewState`, polimorfismo, padrão State) e não a "beleza" da tela.

---

### E. Exemplos de Renderização (Mockup do Terminal)

Abaixo estão exemplos textuais de como a CLI deve renderizar cada estado da máquina. A formatação exata (cores, bordas) é livre, mas a disposição da informação e os *prompts* de transição de estado devem seguir esta estrutura lógica.

**Estado Inicial: `ProjectSelectorView`**
```text
==================================================
           NEXUS: SELEÇÃO DE PROJETO
==================================================

Projetos Ativos:
[1] Sistema de RH (Workflow: Standard)
[2] Migração de Cloud (Workflow: Agile)
[3] App Mobile (Workflow: Agile)

--------------------------------------------------
> Selecione o ID do projeto (ou digite VOLTAR para sair): 2
```

**Transição para o Estado: `KanbanBoardView`**
*(Após o usuário digitar `2` na tela anterior)*
```text
==================================================
        NEXUS KANBAN: Migração de Cloud
==================================================

[ BACKLOG ]
- TSK-201: Mapear dependências locais
- TSK-204: Definir arquitetura de rede

[ EM ANDAMENTO ]
- TSK-202: Configurar instâncias EC2 (Resp: Alice)

[ CONCLUÍDO ]
- TSK-203: Criar repositório Git (Resp: Bob)

--------------------------------------------------
> Digite o ID da Tarefa para inspecionar (ou VOLTAR): TSK-202
```

**Transição para o Estado: `TaskDetailView`**
*(Após o usuário digitar `TSK-202` na tela anterior)*
```text
==================================================
        NEXUS INSPEÇÃO: TSK-202
==================================================

Título: Configurar instâncias EC2
Projeto: Migração de Cloud
Status Atual: EM ANDAMENTO
Responsável: Alice (Role: DEV)
Criada em: 2026-04-15

[ Descrição ]
Provisionar 3 instâncias t3.medium com Ubuntu 22.04 na sub-rede pública.

[ Histórico de Transições ]
1. BACKLOG -> EM ANDAMENTO (por Alice em 2026-04-16)

--------------------------------------------------
> Digite VOLTAR para retornar ao Kanban: VOLTAR
```

## 6. Requisitos de Expansão da CLI e Comandos Interativos

A CLI deve fornecer cobertura interativa completa para o ecossistema do Nexus. Com a nova arquitetura, a implementação destes comandos avaliará a sua capacidade de separar a coleta de dados (View) da execução de regras de negócio (Controller/Handlers).

Você deve implementar e registrar os seguintes comandos. Para os fluxos interativos (via `InteractiveCLIHandler`), é obrigatório o uso do `WorkspaceQueryService` para alimentar o autocompletar (Tab) com projeções de dados seguras, orientando o usuário e prevenindo erros de digitação.

1. **Gestão de Projetos e Equipes:**
   * `ADD_PROJECT`: Solicita nome e duração/esforço esperado.
   * `ADD_TEAM_MEMBER`: Solicita o projeto (via autocompletar), lista os usuários disponíveis (via autocompletar) e define o papel (Role).

2. **Governança e Workflows:**
   * `ADD_WORKFLOW`: Solicita o nome do novo workflow.
   * `ADD_STATUS`: Solicita o workflow alvo (via autocompletar), o nome do status e um identificador booleano (S/N) determinando se é um estado terminal.
   * `ADD_TRANSITION`: Solicita a seleção guiada e restrita: Workflow $\rightarrow$ Status de Origem $\rightarrow$ Status de Destino.
   * `LINK_WORKFLOW`: Vincula um workflow existente a um projeto específico *(Corrigir inconsistências de nomenclatura antigas, padronizando estritamente como LINK_WORKFLOW)*.

3. **Operações de Ciclo de Vida:**
   * `MOVE_TASK`: Este é um teste crítico de delegação. O assistente interativo deve solicitar o ID da tarefa, validar o usuário atuante e consultar o serviço de leitura para apresentar no autocompletar **exclusivamente** os status de destino permitidos, com base no estado atual da tarefa e no grafo do workflow.

4. **Módulo de Relatórios (Dashboard Unificado):**
   * Em vez de múltiplos comandos soltos, consolide a entrada em um único comando interativo `GENERATE_REPORT`. Ele deve oferecer um menu (via `PromptService`) listando as opções de relatórios existentes (`REPORT_STATUS`, `TOP_PERFORMERS`, `OVERLOADED_USERS`, `PROJECT_HEALTH` e `GLOBAL_BOTTLENECK`). A View coleta a escolha e despacha o comando específico correspondente para o `CommandDispatcher`.

5. **Navegação Visual de Dados:**
   * `VIEW_KANBAN`: Comando de entrada que inicializa a Máquina de Estados da interface visual. (A especificação detalhada desta mecânica de navegação encontra-se na Seção 6).

Aqui está a reescrita da seção de critérios de avaliação, ajustada para refletir as novas decisões arquiteturais (como o padrão State e o Information Expert) e a remoção dos requisitos antigos (como os canais de notificação). Incluí também a regra de pontuação extra conforme você solicitou.

## 7. Critérios de Avaliação e Penalidades

### A. Qualidade da Arquitetura Orientada a Objetos (40%)
* **Polimorfismo (OCP):** O sistema utiliza delegação polimórfica (interfaces ou abstrações) em vez de blocos condicionais (`if/else` ou `switch-case`) baseados em tipo?
* **LSP e Herança Estrutural:** As subclasses concretas (seja para os `CommandHandlers` ou para os estados da interface em `ViewState`) substituem as classes base perfeitamente, sem quebrar os contratos originais ou forçar comportamentos não previstos (como lançar `UnsupportedOperationException` sem justificativa de domínio)?

### B. Encapsulamento e Delegação (30%)
* **I/O, Negócios e Information Expert:** Existe vazamento de dependências de interface (ex: `JLine`, `System.out`) para as regras de negócio? O `PromptService` atua como fronteira correta? A classe `Command` atua corretamente como *Information Expert* para a extração tipada de seus próprios argumentos?
* **Segregação da Camada Visual:** A interface iterativa interage exclusivamente com serviços de projeção de leitura (como o `WorkspaceQueryService`) para apresentar opções, garantindo que a visualização não consiga alterar o estado do domínio?
* **Demeter e Tell-Don't-Ask:** Os comandos limitam-se a orquestrar os serviços, delegando as buscas e regras para as coleções detentoras, ou estão indevidamente inspecionando e iterando sobre dados internos expostos por outros objetos?

### C. Robustez Operacional e Navegação (30%)
* **Máquina de Estados Visual:** O sistema de navegação do Kanban foi implementado estritamente com o Padrão State, onde cada tela dita a próxima transição através do polimorfismo de `handleInput`, sem recair em métodos monolíticos de roteamento visual?
* **Gestão de Exceções:** A taxonomia de exceções (`NexusDomainException` e `NexusSystemException`) é propagada e capturada de maneira coesa pela CLI, reagindo de forma diferente ao tipo do erro sem exibir *stack traces* agressivas para o usuário final?

### Bônus de Coesão Visual e Estrutural

| Critério Extra | Bônus |
| --- | --- |
| **Padronização ViewState**: Concedido se **todas** as janelas/telas interativas do sistema (incluindo menus, listagens do Kanban e visualizações de detalhes) implementarem rigorosamente o contrato `ViewState`, herdarem de uma classe visual base (`Template Method`) para o reaproveitamento de layout, e exibirem um estilo visual coeso e idêntico em todo o terminal. | +1.5 Pontos |

### Penalidades

| Violação Arquitetural | Penalidade |
| --- | --- |
| **Type-checking ou Condicionais de Domínio**: Uso de `instanceof`, `.getClass()`, `switch` ou blocos if-else extensos para definir fluxo de negócio em vez de usar Polimorfismo Dinâmico. | -1.5 Pontos |
| **Vazamento de I/O**: Presença de `System.out.print`, `Scanner` ou equivalente dentro de entidades, serviços ou handlers que não pertencem à View. | -1.0 Pontos |
| **Delegation Leak (Train Wrecks)**: Classes de comando iterando sobre coleções internas expostas por outros objetos, violando a Lei de Demeter. | -1.0 Pontos |
| **Código Duplicado de Parsing**: Falha em abstrair o processamento e validação de tipos (ex: conversão de Datas e Longs) para o objeto especialista (`Command`), resultando em blocos `try-catch` idênticos espalhados pelos handlers. | -0.5 Pontos |