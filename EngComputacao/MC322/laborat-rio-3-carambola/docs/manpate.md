# Man-Page Consolidada dos Comandos

---

# `CREATE_USER`

## Sintaxe

```text id="o8l3xp"
CREATE_USER;username;email
```

## Descrição

Cria um novo usuário no sistema.

---

## Regras obrigatórias

### Username

Não pode:

* ser `null`
* ser vazio
* conter apenas espaços

Caso inválido:

* lançar `IllegalArgumentException`

---

### E-mail

Deve:

* conter `@`
* possuir domínio válido

Caso inválido:

* lançar `IllegalArgumentException`

---

## Regras arquiteturais

`User` deve implementar:

* `equals()`
* `hashCode()`

---

# `CREATE_PROJECT`

## Sintaxe

```text id="pnx58n"
CREATE_PROJECT;projectName
```

## Descrição

Cria um novo projeto.

---

## Estrutura obrigatória do projeto

O projeto deve possuir:

* `Set<Task>`
* `Map<User, Role>`
* `Workflow`

---

## Regras obrigatórias

### Tarefas duplicadas

O sistema deve impedir duplicidade usando:

```java id="twhkec"
Set<Task>
```

---

### Workflow

O projeto pode existir sem workflow.

Porém:

* não pode criar tarefas
* não pode alterar status

sem workflow associado.

Caso ocorra:

* lançar `NexusValidationException`

---

## Regras arquiteturais

`Project` deve implementar:

* `equals()`
* `hashCode()`

---

# `CREATE_STATUS`

## Sintaxe

```text id="l4x3hx"
CREATE_STATUS;statusName;isTerminal
```

## Descrição

Cria um status dinâmico do sistema.

---

## Estrutura obrigatória

A classe `Status` deve ser:

* imutável
* baseada em objeto
* não pode ser enum

---

## Campos obrigatórios

```java id="fws2ux"
name
isTerminal
```

---

## Regras obrigatórias

O sistema deve usar:

```java id="mn4k7n"
isTerminal
```

para identificar estados finais.

É proibido depender de:

```java id="b5g6ec"
status.equals("DONE")
```

---

## Regras arquiteturais

`Status` deve implementar:

* `equals()`
* `hashCode()`

---

# `CREATE_WORKFLOW`

## Sintaxe

```text id="x6ww9y"
CREATE_WORKFLOW;workflowName
```

## Descrição

Cria um workflow vazio.

---

## Estrutura obrigatória

```java id="5te4d9"
Map<Status, Set<Status>> allowedTransitions
```

---

## Responsabilidades

O workflow:

* conhece regras de transição
* valida mudanças de estado

---

## Método obrigatório

```java id="8k8a8g"
canTransition(Status from, Status to)
```

---

# `SET_INITIAL_STATUS`

## Sintaxe

```text id="74av9n"
SET_INITIAL_STATUS;workflowName;statusName
```

## Descrição

Define o status inicial automático do workflow.

---

## Regras obrigatórias

Toda tarefa criada em projeto associado ao workflow:

* nasce automaticamente nesse status

---

# `ALLOW_TRANSITION`

## Sintaxe

```text id="d7c6i8"
ALLOW_TRANSITION;workflowName;statusOrigem;statusDestino
```

## Descrição

Adiciona uma transição permitida entre dois estados.

---

## Modelo conceitual

Representa:

* uma aresta direcionada
* em um grafo de estados

---

## Regras obrigatórias

As transições devem ser armazenadas usando:

```java id="owv5kh"
Set<Status>
```

para impedir duplicidade.

---

# `LINK_WORKFLOW`

## Sintaxe

```text id="v3w6m5"
LINK_WORKFLOW;projectName;workflowName
```

## Descrição

Associa um workflow a um projeto.

---

## Regras obrigatórias

Sem workflow associado:

* `CREATE_TASK` deve falhar
* `CHANGE_STATUS` deve falhar

---

# `CREATE_TASK`

## Sintaxe

```text id="ghq2hz"
CREATE_TASK;taskName;deadline;effort;projectName
```

## Descrição

Cria uma nova tarefa vinculada automaticamente a um projeto.

---

## Regras obrigatórias

### Workflow obrigatório

O projeto deve possuir workflow associado.

Caso contrário:

* lançar `NexusValidationException`

---

### Status inicial automático

A tarefa deve nascer com:

```java id="1gckg9"
workflow.initialStatus
```

---

### Identidade imutável

O `id`:

* é gerado na criação
* nunca pode ser alterado

É proibida a existência de:

```java id="6ij91p"
setId()
```

---

### Deadline imutável

Após criação:

* não pode ser alterado

---

### Estimated Effort

A tarefa deve possuir:

```java id="j6ap2o"
estimatedEffort
```

---

## Regras arquiteturais

A `Task`:

* NÃO deve validar workflow
* NÃO deve validar RBAC
* NÃO deve conter regras de transição

Ela deve apenas:

* armazenar estado
* obedecer ordens do `Project`

---

## Regras arquiteturais obrigatórias

`Task` deve implementar:

* `equals()`
* `hashCode()`

---

# `ASSIGN_USER`

## Sintaxe

```text id="2r5jli"
ASSIGN_USER;taskId;username
```

## Descrição

Define o owner da tarefa.

---

## Regras obrigatórias

O sistema deve localizar:

* tarefa pelo ID
* usuário pelo username

---

## Casos de falha

Caso:

* tarefa não exista
* usuário não exista

o sistema deve:

* lançar exceção
* continuar processamento do log

---

# `ADD_MEMBER`

## Sintaxe

```text id="l0lg3v"
ADD_MEMBER;projectName;username;ROLE
```

## Descrição

Adiciona usuário ao projeto com um papel RBAC.

---

## Roles obrigatórias

```java id="cqjlwm"
VIEWER
CONTRIBUTOR
MANAGER
```

---

## Permissões

### VIEWER

* não pode alterar status

### CONTRIBUTOR

* pode alterar status

### MANAGER

* permissões elevadas

---

# `CHANGE_STATUS`

## Sintaxe

```text id="xw9y1y"
CHANGE_STATUS;taskId;newStatusName;usernameAtuante
```

## Descrição

Solicita alteração de status de uma tarefa.

---

# Fluxo obrigatório

```text id="pnh22p"
NexusEngine
  -> Project
      -> Workflow
      -> RBAC
      -> Task.setStatus()
```

---

## Regras obrigatórias

### RBAC

O usuário atuante:

* deve pertencer ao projeto
* deve possuir ao menos:

```text id="h3efdx"
CONTRIBUTOR
```

Caso contrário:

* lançar `NexusValidationException`

---

### Workflow

A mudança de estado só pode ocorrer se:

```java id="qom9oe"
workflow.canTransition(from, to)
```

retornar verdadeiro.

Caso contrário:

* lançar `NexusValidationException`

---

### Workflow obrigatório

O projeto deve possuir workflow associado.

Caso contrário:

* lançar `NexusValidationException`

---

### Regras Data-Driven

As regras de transição:

* NÃO podem ser hardcoded
* NÃO podem usar `if/else` fixos

As regras devem vir exclusivamente de:

```text id="hhl7t7"
ALLOW_TRANSITION
```

---

### Estados terminais

O sistema deve usar:

```java id="85mw2u"
isTerminal
```

para identificar estados finais.

---

## Telemetria

Falhas de validação:

* devem registrar métricas de erro

---

# `REPORT_STATUS`

## Sintaxe

```text id="7d0n9p"
REPORT_STATUS
```

## Descrição

Executa relatórios analíticos utilizando Stream API.

---

# Relatórios obrigatórios

## Top Performers

Retorna:

* os 3 usuários com maior número de tarefas concluídas/terminais

---

## Overloaded Users

Lista usuários com:

```text id="jlwm1v"
mais de 10 tarefas em progresso
```

---

## Project Health

Calcula:

```text id="b33m6f"
tarefasTerminais / totalTarefas
```

---

## Global Bottlenecks

Identifica:

* o status não terminal mais frequente

