[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/twJEQcmK)
[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=23300958)
# Lab 2: A Evolução do Nexus — Workflows Dinâmicos e Governança

## 1. Sumário da Atividade

Neste segundo laboratório, você deverá elevar a maturidade estrutural do **Nexus**. O código construído no Lab 1, embora funcional, acumulou dívidas técnicas: classes sobrecarregadas, regras de transição engessadas e falta de controle de acesso. O foco principal agora será refatorar o sistema aplicando os princípios de **Coesão (SRP)**, **Delegação (Lei de Demeter)** e o uso avançado de **Coleções (`Map` e `Set`)** para transformar o Nexus em um motor *Data-Driven*.

**Atenção à Dinâmica:** Conforme acordado, os pares foram trocados. Você e sua nova dupla deverão escolher o repositório do Lab 1 de um dos membros para ser a base deste laboratório. Ler e refatorar código alheio faz parte do desafio.

As seções estão divididas em:

1. **A Nova Fase do Nexus** – o contexto de negócios para workflows dinâmicos e controle de acesso.
2. **Refatoração Estrutural (Pagando a Dívida)** – quebrando o monólito e eliminando o estado global.
3. **Mecânicas de Governança** – implementação de grafos de Status, Workflows customizados e RBAC.
4. **Evolução do Ecossistema** – os novos comandos de log.
5. **Avaliação** – novos critérios de qualidade, com foco em coleções e design orientado a objetos.

## 2. A Nova Fase do Nexus

O mercado rejeitou a ideia de que todas as tarefas devem seguir o mesmo fluxo (`TO_DO` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `DONE`). Equipes de Design precisam de um status de "Revisão", enquanto equipes de TI precisam de "Testes". O sistema não pode ser recompilado toda vez que um cliente quiser um status novo.

Além disso, a auditoria de segurança apontou uma falha crítica: atualmente, qualquer usuário logado no sistema pode alterar o status de qualquer tarefa. O Nexus precisa agora gerenciar escopos de permissão.

O sistema deve lidar com:
* **Workflows Dinâmicos:** As rotas que uma tarefa pode seguir devem ser configuráveis em tempo de execução.
* **Segurança por Projeto (RBAC):** Apenas membros autorizados de um projeto podem interagir com as tarefas dele.
* **Eficiência de Busca:** Com o aumento exponencial de dados, varrer listas sequenciais ($O(n)$) se tornou um gargalo. Estruturas de mapeamento ($O(1)$) são necessárias.

## 3. Refatoração Estrutural (Pagando a Dívida)

Antes de adicionar novas funcionalidades, você deve "limpar a casa". Aplique os seguintes princípios de design no código base recebido:

### A. Quebrando o Monólito (SRP - Single Responsibility Principle)
O seu `LogProcessor` original provavelmente faz tudo: lê arquivos, converte strings e processa lógica de negócio. Você deve dividi-lo em três classes distintas:
1. **`FileLoader`**: Responsável estritamente por ler o arquivo de texto e retornar uma coleção de linhas (Strings).
2. **`CommandParser`**: Traduz uma linha de texto em um objeto ou instrução compreensível.
3. **`NexusEngine` (ou `CommandDispatcher`)**: Recebe a instrução e orquestra as chamadas aos serviços corretos.

### B. Erradicação do Estado Global (`static`)
Variáveis estáticas criam acoplamento invisível e dificultam testes.
* **Remova** todos os contadores `static` da classe `Task` ou `Workspace`.
* **Crie** uma classe `TelemetryRegistry` que encapsule essas contagens.
* Passe a instância de `TelemetryRegistry` via parâmetro (Injeção de Dependência) para as classes que precisam registrar métricas, substituindo o acoplamento global por acoplamento paramétrico.

### C. Otimização com Coleções (`Map` e `Set`)
* **No `Workspace`**: Substitua a `List<Project>` por um `Map<String, Project>` para busca instantânea de projetos pelo nome.
* **No `Project`**: A coleção de tarefas deve passar a ser um `Set<Task>`. O sistema deve impedir nativamente tarefas duplicadas. **Atenção:** Para que o `Set` e o `Map` funcionem corretamente, você é **obrigado** a sobrescrever os métodos `equals()` e `hashCode()` nas entidades principais (`Task`, `User`, `Project`), garantindo a identidade dos objetos.

### D. Erradicação de Classes Utilitárias (`Utils` e afins)
Classes com sufixos como `Utils`, `Helper` ou `Manager` frequentemente se tornam "gavetas de bagunça" de código procedural, ferindo os princípios de coesão e encapsulamento da Orientação a Objetos. Objetos autênticos devem possuir tanto os dados quanto os comportamentos que operam sobre eles.
* **O que deve ser feito:** Elimine completamente do projeto qualquer classe genérica de utilitários. Não deve sobrar nenhum método estático solto que não pertença ao contexto exato do objeto.
* **A Decisão Arquitetural:** Será sua responsabilidade (e de sua dupla) analisar cada método atualmente presente no utilitário (como validações de domínio, formatação de datas ou conversões de string) e decidir a qual classe ele realmente pertence. Aplique o princípio do *Information Expert*: a responsabilidade por um comportamento deve pertencer estritamente à classe que detém a informação necessária para executá-lo.

## 4. Mecânicas de Governança: Workflows e Permissões

Nesta etapa, você abandonará condicionais fixas e usará a composição de objetos para ditar as regras do negócio.

### A. Workflows Dinâmicos (Data-Driven)

1.  **A Classe `Status`**: Deixa de ser um `enum` e passa a ser uma classe imutável que possui um `name` (ex: "REVIEW") e um booleano `isTerminal`.
2.  **O Conceito de Status Terminal**: O booleano `isTerminal` indica se a tarefa atingiu um ponto de conclusão. **Atenção:** Um workflow pode ter múltiplos estados terminais (ex: "CONCLUÍDO", "CANCELADO", "REJEITADO"). O sistema deve usar essa flag para cálculos de progresso, evitando comparar nomes de status via String (ex: `if(name.equals("DONE"))` é proibido).
3.  **A Classe `Workflow`**: Representa a máquina de estados. Ela deve possuir um mapa de transições permitidas:
    ```java
    private final Map<Status, Set<Status>> allowedTransitions;
    ```
    * **Método `canTransition(Status from, Status to)`**: Consulta o mapa para autorizar ou não a mudança de estado.
    * **Estado Inicial**: Adicione um atributo `Status initialStatus`. Toda tarefa criada em um projeto que utiliza esse workflow deve nascer automaticamente com esse status.
4.  **Integração e Delegação**: Cada `Project` é configurado com um `Workflow`. A `Task` não deve conter lógica de transição; ela é um objeto passivo que recebe ordens do `Project`. O `Project`, por sua vez, consulta o `Workflow` para validar se a ordem é permitida.
5.  **Migração de Regras**: As condicionais do Lab 1 (ex: "não pode ir de TO_DO para DONE direto") devem ser extintas do código Java e recriadas via comandos `ALLOW_TRANSITION` no arquivo de log.
6.  **Atenção à Consistência**: Um Projeto pode ser criado antes da definição de seu Workflow. No entanto, se o comando `CREATE_TASK` ou `CHANGE_STATUS` for executado em um projeto sem workflow vinculado, o sistema **deve** lançar uma `NexusValidationException`.

#### Exemplo de Configuração via Log:

```text
# === CONFIGURAÇÃO DO ECOSSISTEMA ===

# 1. Criação dos Status (Nome;éTerminal)
CREATE_STATUS;TO_DO;false
CREATE_STATUS;IN_PROGRESS;false
CREATE_STATUS;DONE;true
CREATE_STATUS;CANCELLED;true

# 2. Configuração do Workflow
CREATE_WORKFLOW;W_NORMAL
SET_INITIAL_STATUS;W_NORMAL;TO_DO
ALLOW_TRANSITION;W_NORMAL;TO_DO;IN_PROGRESS
ALLOW_TRANSITION;W_NORMAL;IN_PROGRESS;DONE
ALLOW_TRANSITION;W_NORMAL;IN_PROGRESS;CANCELLED

# 3. Vínculo com o Domínio
CREATE_PROJECT;Apollo11
VINCULAR_WORKFLOW;Apollo11;W_NORMAL
```


### B. Controle de Acesso (RBAC Simplificado)

1. **Enum `Role`**: Crie um enum simples com papéis como `VIEWER`, `CONTRIBUTOR` e `MANAGER`.
2. **Mapeamento de Equipe**: Dentro da classe `Project`, adicione um mapa para gerenciar os membros da equipe e seus papéis:
   ```java
   private final Map<User, Role> teamMembers;
   ```
3. **Validação de Permissão**: Toda tentativa de `CHANGE_STATUS` agora exige identificar o usuário que está fazendo a ação. O `Project` deve verificar no mapa `teamMembers` se o usuário possui pelo menos o nível `CONTRIBUTOR` antes de repassar a ordem para a tarefa. Caso contrário, lance a `NexusValidationException`.

### C. A Lei de Demeter e o Papel do Orquestrador

Para evitar o "desastre de trem" (chamadas encadeadas como `getProject().getWorkflow().check()`), aplique o princípio de que **quem detém a informação toma a decisão**.

1.  **O Projeto como Autoridade:** O `Project` é a única classe que conhece tanto o `Workflow` (as regras) quanto os `teamMembers` (quem pode fazer o quê). Portanto, a responsabilidade de validar uma mudança de status **não é da Task**, mas do `Project`.
2.  **O Fluxo de Delegação:** * O motor do sistema (`NexusEngine`) identifica qual projeto é dono da tarefa.
    * O motor chama um método no `Project`, por exemplo: `project.processStatusChange(taskId, newStatus, user)`.
    * O `Project` verifica se o `user` tem permissão e se o `Workflow` permite a transição.
    * **Só então**, se tudo estiver correto, o `Project` chama o método `task.setStatus()` da tarefa alvo.
3.  **Vantagem:** A `Task` permanece uma classe simples e "pura". Ela não precisa saber que existe um `Project`, um `Workflow` ou um `User`. Ela apenas armazena seus dados e obedece ao comando de seu "dono" (o Projeto).

---

## 5. Evolução do Ecossistema: Novos Comandos

O processador de arquivos do sistema deve ser atualizado para interpretar a construção dinâmica das regras antes de instanciar as tarefas. A ordem de execução importa.

Novos comandos a serem suportados:

1. **`CREATE_STATUS;statusName;isTerminal`**: Instancia um novo objeto `Status`.
2. **`CREATE_WORKFLOW;workflowName`**: Inicia um grafo de transições vazio.
3. **`ALLOW_TRANSITION;workflowName;statusOrigem;statusDestino`**: Adiciona uma aresta direcionada no mapa interno do Workflow.
4. **`VINCULAR_WORKFLOW;projectName;workflowName`**: Associa o conjunto de regras ao projeto.
5. **`ADD_MEMBER;projectName;username;ROLE`**: Adiciona um usuário à equipe do projeto com uma permissão específica.
6. **`CHANGE_STATUS;taskId;newStatusName;usernameAtuante`**: Atualização do comando antigo. Agora exige o `username` de quem está operando a alteração para passar pela validação RBAC do Projeto.
7. **`SET_INITIAL_STATUS;workflowName;statusName`**: Define qual objeto Status será atribuído automaticamente às novas tarefas de projetos que usem este workflow.

---

## 6. Critérios de Avaliação (Checklist de Qualidade)

A nota final avaliará a proficiência na refatoração e a aplicação de estruturas de dados adequadas.

### A. Uso Estratégico de Coleções (25%)
* **Identidade de Objetos:** Os métodos `equals()` e `hashCode()` foram implementados corretamente em `Task`, `User` e `Status`?
* **Adequação:** O sistema faz uso correto de `Map` para buscas rápidas ($O(1)$) em `Workspace` e `Project`, abandonando listas ineficientes? O mapa de transições no `Workflow` usa `Set` para evitar duplicação de regras?

### B. Delegação e Encapsulamento (25%)
* **Lei de Demeter:** As classes "falam" apenas com seus vizinhos diretos? A navegação por coleções expostas de outras classes foi erradicada?
* **Responsabilidade:** A lógica de validar se uma transição é possível está encapsulada no `Workflow` em vez de estar espalhada em `if/else` na `Task` ou no processador principal?

### C. Refatoração e Coesão (20%)
* **SRP:** O `LogProcessor` original foi desmembrado em classes coesas (Leitura, Parse, Despacho)?
* **Erradicação do Static:** O sistema funciona sem depender de estado global? O `TelemetryRegistry` é passado por composição/parâmetro corretamente?

### D. Regras de Negócio e Robustez (20%)
* **RBAC:** O sistema bloqueia com `NexusValidationException` tentativas de alteração de status por usuários que não estão no `Map` de permissões do Projeto?
* **Tratamento de Exceções Base:** O sistema continua resiliente a falhas no arquivo de log, capturando exceções e continuando o processamento de forma limpa?

### E. Qualidade de Código e Colaboração (10%)
* **Clean Code & JavaDoc:** Variáveis descritivas, métodos curtos e documentação rigorosa.
* **Colaboração:** A dupla atual conseguiu absorver e refatorar o código da dupla anterior, utilizando commits estruturados no repositório.

---

## Tabela de Penalidades (Redutores de Nota)

| Violação | Penalidade |
| --- | --- |
| **Train Wreck / Demeter Violation**: Encadeamento de 3+ getters acessando objetos profundos. | -2.0 Pontos |
| **Falta de `equals`/`hashCode`**: Usar coleções baseadas em Hash sem implementar a identidade do objeto. | -2.5 Pontos |
| **Uso de Variáveis Estáticas Globais**: Manutenção de atributos `static` para regras de negócio ou telemetria. | -1.5 Pontos |
| **Lista Oculta (Fake Set)**: Usar `List` internamente e fazer `if(!list.contains())` no lugar de usar um `Set` nativamente. | -1.0 Ponto |
| **Resquícios Procedurais:**: Manutenção de classes Utils ou métodos de validação estáticos que deveriam ser comportamentos de objeto.|	-1.5 Pontos|