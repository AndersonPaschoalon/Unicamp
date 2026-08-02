## Organização da Aplicação

A aplicação segue o Design Pattern MVC, na qual a camada de visualização (view) é totalmente desacoplada das camadas de dados (model) e lógica de negócio (services), sendo o Controller, o componente responsável pela troca de mensagens.

Os packages desenvolvidos na aplicação são os seguintes:
- `app`;
- `controller`;
- `exeption`;
- `model`;
- `service`;
- `view`;


#### `app`

Este componente funciona como o container do estado aplicação, e é responsável por armazenar os objetos persistentes que definem o a maquina de estados da aplicação.

A classe `AppContext` funciona como este container, enquanto através de `AppBootstrap.create()` instanciamos um `AppContext`. Os seguintes objetos são armazenados atualmente:
-     `PersistenceService persistenceService`: objeto responsável por armazenar e recarregar o estado da aplicação no arquivo `nexus_state.json`, permitindo que a aplicação seja encerrada e reiniciada com o mesmo estado.
-     `Workspace workspace`: container dos objetos de gestão de projeto do Nexus.
-     `UserService userService`: regras de negócio de usuário;
-     `WorkflowService workflowService`: regras de negócio do fluxo de trabalho;
-     `TaskService taskService`: regras de negócio de tarefas;
-     `ProjectService projectService`: regras de negócio de projeto;
-     `ReportService reportService`: serviços de relatório;
-     `TelemetryRegistry telemetryRegistry`: registro de telimetria;
-     `CommandDispatcher dispatcher`: o CommandDispatcher funciona como um roteador. Ele é responsável por tomar um objeto do tipo `Command`, ou seja a representação de um comando em forma de dados, converter esse objeto para um `CommandHandler` e rotear ele para o controlador.
-     `WorkspaceQueryServiceInterface queryService`: trata-se de um objeto responsável por consumir de forma ready-only os dados da camada de regra de negócio.


#### `view`

Camada de visualização da aplicação. Ela possui as seguintes responsabilidades:
- Requisitar os dados a serem exibidos na interface de usuário. Isso é feito através do objeto `WorkspaceQueryServiceInterface queryService` na classe `CLIHandlerRegistry` que provê uma interface soment-leitura dos dados da camada de regra de negócio.
- Interagir com o usuário, ou seja ser capaz de exibir os dados de forma consistente, e capturar os dados de entrada do usuário. A principal classe que exerce essa função é a `PromptService`, que funciona como uma *facade* para a biblioteca *Jline*, abstraindo a complexidade e detalhes de operação.
- Enviar de volta os dados capturados do usuário ao `Controller` para serem processados. Isto é feito também na classe `CLIHandlerRegistry` que mantém um registro de cada objeto de transição pertencente ao sub-pacote `view.handler`. Para cada comendo que a aplicação possa receber, existe um  objeto do tipo CLIHandler responsável por receber os dados de entrada desse comando e envia-los ao controlador. 
É sempre importante que a camada de view não contenha em si regra de negócio algum. Isso é especialmente útil atualmente, uma vez que uma mesma aplicação pode ter diversas interfaces diferentes, dependendo do contexto (Ex: interface de linha de comando, app desktop, mobile, web). 


#### `model`

Representa a camada de dominio do projeto. Cada entidade de regra de negócio deve possuir uma classe correspondente como `model`. A camada de regra de negócio possui acesso direto ás classes de model, mas elas não devem ser expostas diretamente a camada de `view`. Em uma aplicação de produção, a interface com um banco de dados poderia ficar dentro desse escopo. Sem alterações significativas do lab anterior.


#### `exeption`

Camadas de excessões da aplicação, importantes para a aplicação do princípio *fail-fast*.


#### `service`

A camada de serviços trata-se da regra de negócio central da aplicação. A camada de *model* pode ter que sofrer ajustes depentendo do tipo de tecnologia de base de dados usada dependendo da arquitetura, a *view* pode ser frequentemente trocada (ou extendida via polimorfismo) devido o tipo de tecnologia utilizado para a intereção com o usuário (ou com outro serviço). Idealmente a camada de serviço nunca é modificada a menos que regras de negócio sejam modificadas. É consumida pela restante da aplicação. Há pessoas que defendem que o *service* deveria ser considerada parte da camada de model, e outras que deveria ser uma camada independente. 
Possui os seguintes sub-pacotes:
- `service.core`: camada central da regra de negócio, não foi muito alterada dos ultimos labs ;
- `service.infra`: parseador de comandos e manipulador de arquivos (não foi necessário manipular);
- `service.persistence`: serviços de persistencia de dados (não foi necessário manipular);
- `service.query`: sub-componente responsável por requisitar de forma read-only dados da camada de serviço.

## Como criar comandos novos:

O processo de criação de comandos novos envolve: 
1. A criação de uma classe executora de comandos em `controller.commands`, por exemplo: `AddMemberHandler implements CommandHandler`;
2. A instanciação desse comando na classe `CommandDispatcher`, e a vinculação a uma string de comando:
    ```
    h.put("ADD_MEMBER", new AddMemberHandler(projectService));
    ```
3. A implementação de um *view-handler* desse comando  em `view.handlers`, pelo exemplo seria `AddMemberCLIHandler implements InteractiveCLIHandler`.
4. O registro desse handler na classe `CLIHandlerRegistry`:
    ```
    new AddMemberCLIHandler(dispatcher, queryService),
    ```




## Comandos executados 

```
# Usuários

CREATE_USER;alice;alice@nexus.com
CREATE_USER;bob;bob@nexus.com
CREATE_USER;carol;carol@nexus.com
CREATE_USER;dave;dave@nexus.com
CREATE_USER;eve;eve@nexus.com

# Status

## Workflow Agile
CREATE_STATUS;BACKLOG;false
CREATE_STATUS;TODO;false
CREATE_STATUS;IN_PROGRESS;false
CREATE_STATUS;DONE;true

## Workflow Enterprise
CREATE_STATUS;OPEN;false
CREATE_STATUS;ANALYSIS;false
CREATE_STATUS;VALIDATION;false
CREATE_STATUS;CLOSED;true

#  Workflows
CREATE_WORKFLOW;AGILE_FLOW
CREATE_WORKFLOW;ENTERPRISE_FLOW

# Configuração Workflow Agile
SET_INITIAL_STATUS;AGILE_FLOW;BACKLOG
ALLOW_TRANSITION;AGILE_FLOW;BACKLOG;TODO
ALLOW_TRANSITION;AGILE_FLOW;TODO;IN_PROGRESS
ALLOW_TRANSITION;AGILE_FLOW;IN_PROGRESS;DONE
ALLOW_TRANSITION;AGILE_FLOW;TODO;BACKLOG
ALLOW_TRANSITION;AGILE_FLOW;IN_PROGRESS;TODO

# Configuração Workflow Enterprise
SET_INITIAL_STATUS;ENTERPRISE_FLOW;OPEN
ALLOW_TRANSITION;ENTERPRISE_FLOW;OPEN;ANALYSIS
ALLOW_TRANSITION;ENTERPRISE_FLOW;ANALYSIS;VALIDATION
ALLOW_TRANSITION;ENTERPRISE_FLOW;VALIDATION;CLOSED
ALLOW_TRANSITION;ENTERPRISE_FLOW;VALIDATION;ANALYSIS

# Projetos

## Projetos Agile
CREATE_PROJECT;MobileApp;100
CREATE_PROJECT;CloudMigration;200

## Projetos Enterprise
CREATE_PROJECT;ERPModernization;200
CREATE_PROJECT;SecurityAudit;300

# Vinculação Workflow → Projeto
LINK_WORKFLOW;MobileApp;AGILE_FLOW
LINK_WORKFLOW;CloudMigration;AGILE_FLOW
LINK_WORKFLOW;ERPModernization;ENTERPRISE_FLOW
LINK_WORKFLOW;SecurityAudit;ENTERPRISE_FLOW

# Membros dos Projetos

## MobileApp
ADD_MEMBER;MobileApp;alice;MANAGER
ADD_MEMBER;MobileApp;bob;CONTRIBUTOR
ADD_MEMBER;MobileApp;carol;VIEWER

## CloudMigration
ADD_MEMBER;CloudMigration;alice;MANAGER
ADD_MEMBER;CloudMigration;dave;CONTRIBUTOR
ADD_MEMBER;CloudMigration;eve;CONTRIBUTOR

## ERPModernization
ADD_MEMBER;ERPModernization;bob;MANAGER
ADD_MEMBER;ERPModernization;carol;CONTRIBUTOR
ADD_MEMBER;ERPModernization;dave;VIEWER

## SecurityAudit
ADD_MEMBER;SecurityAudit;eve;MANAGER
ADD_MEMBER;SecurityAudit;alice;CONTRIBUTOR
ADD_MEMBER;SecurityAudit;bob;VIEWER

# Tarefas — MobileApp
CREATE_TASK;Implement Login;2026-06-01;5;MobileApp
CREATE_TASK;Create Dashboard;2026-06-02;3;MobileApp
CREATE_TASK;Build API Client;2026-06-03;8;MobileApp
CREATE_TASK;Fix Session Bug;2026-06-04;2;MobileApp
CREATE_TASK;Dark Mode;2026-06-05;5;MobileApp
CREATE_TASK;Push Notifications;2026-06-06;8;MobileApp
CREATE_TASK;User Preferences;2026-06-07;3;MobileApp
CREATE_TASK;Accessibility Review;2026-06-08;2;MobileApp

# Tarefas — CloudMigration
CREATE_TASK;Provision EC2;2026-06-01;5;CloudMigration
CREATE_TASK;Setup VPC;2026-06-02;3;CloudMigration
CREATE_TASK;Configure DNS;2026-06-03;2;CloudMigration
CREATE_TASK;Deploy Kubernetes;2026-06-04;8;CloudMigration
CREATE_TASK;Create Terraform;2026-06-05;5;CloudMigration
CREATE_TASK;Configure Monitoring;2026-06-06;5;CloudMigration
CREATE_TASK;Database Migration;2026-06-07;8;CloudMigration
CREATE_TASK;Disaster Recovery;2026-06-08;13;CloudMigration

# Tarefas — ERPModernization

CREATE_TASK;Legacy Mapping;2026-06-01;5;ERPModernization
CREATE_TASK;Define APIs;2026-06-02;3;ERPModernization
CREATE_TASK;SAP Integration;2026-06-03;8;ERPModernization
CREATE_TASK;Accounting Rules;2026-06-04;5;ERPModernization
CREATE_TASK;Database Refactor;2026-06-05;8;ERPModernization
CREATE_TASK;Security Compliance;2026-06-06;3;ERPModernization
CREATE_TASK;Performance Tests;2026-06-07;5;ERPModernization
CREATE_TASK;Release Candidate;2026-06-08;2;ERPModernization

# Tarefas — SecurityAudit

CREATE_TASK;Review Firewall;2026-06-01;3;SecurityAudit
CREATE_TASK;Analyze Logs;2026-06-02;2;SecurityAudit
CREATE_TASK;Inspect IAM;2026-06-03;5;SecurityAudit
CREATE_TASK;Pentest API;2026-06-04;8;SecurityAudit
CREATE_TASK;Review Backups;2026-06-05;2;SecurityAudit
CREATE_TASK;Validate Encryption;2026-06-06;3;SecurityAudit
CREATE_TASK;Patch Servers;2026-06-07;5;SecurityAudit
CREATE_TASK;Final Security Report;2026-06-08;8;SecurityAudit


=======================



# ASSIGN_USER

## MobileApp
ASSIGN_USER;IMPLEMEN-485E3281;alice
ASSIGN_USER;CREATEDA-2380F3DB;bob
ASSIGN_USER;BUILDAPI-9DBA18D0;bob
ASSIGN_USER;FIXSESSI-6B0D87D6;alice
ASSIGN_USER;DARKMODE-35C3DA4A;bob
ASSIGN_USER;PUSHNOTI-C2840D09;alice
ASSIGN_USER;USERPREF-42F27D95;bob
ASSIGN_USER;ACCESSIB-F18B9E4B;alice


## CloudMigration
ASSIGN_USER;PROVISIO-DEF9B9A4;dave
ASSIGN_USER;SETUPVPC-BC85B896;eve
ASSIGN_USER;CONFIGUR-D82F1238;dave
ASSIGN_USER;DEPLOYKU-DD236E6E;eve
ASSIGN_USER;CREATETE-06E44467;dave
ASSIGN_USER;CONFIGUR-868E8DBA;eve
ASSIGN_USER;DATABASE-19BB1830;dave
ASSIGN_USER;DISASTER-6D81DE8F;eve

## ERPModernization
ASSIGN_USER;LEGACYMA-DB3B1D0B;carol
ASSIGN_USER;DEFINEAP-409C01F1;bob
ASSIGN_USER;SAPINTEG-3602FCAF;carol
ASSIGN_USER;ACCOUNTI-362CA9F4;bob
ASSIGN_USER;DATABASE-A9A92938;carol
ASSIGN_USER;SECURITY-41D31035;bob
ASSIGN_USER;PERFORMA-FDFD3F47;carol
ASSIGN_USER;RELEASEC-D12AC53C;bob

## SecurityAudit
ASSIGN_USER;REVIEWFI-A0738EE2;alice
ASSIGN_USER;ANALYZEL-457CF3CB;eve
ASSIGN_USER;INSPECTI-9D9C869E;alice
ASSIGN_USER;PENTESTA-519A6445;eve
ASSIGN_USER;REVIEWBA-C1F50253;alice
ASSIGN_USER;VALIDATE-E66BFCF8;eve
ASSIGN_USER;PATCHSER-851BFD20;alice
ASSIGN_USER;FINALSEC-8DE0406A;eve

## MobileApp

### TODO
MOVE_TASK;BUILDAPI-9DBA18D0;TODO;alice
MOVE_TASK;FIXSESSI-6B0D87D6;TODO;alice

### IN_PROGRESS

MOVE_TASK;DARKMODE-35C3DA4A;TODO;alice
MOVE_TASK;DARKMODE-35C3DA4A;IN_PROGRESS;alice
MOVE_TASK;PUSHNOTI-C2840D09;TODO;alice
MOVE_TASK;PUSHNOTI-C2840D09;IN_PROGRESS;alice

### DONE CHANGE_STATUS
MOVE_TASK;USERPREF-42F27D95;TODO;alice
MOVE_TASK;USERPREF-42F27D95;IN_PROGRESS;alice
MOVE_TASK;USERPREF-42F27D95;DONE;alice
MOVE_TASK;ACCESSIB-F18B9E4B;TODO;alice
MOVE_TASK;ACCESSIB-F18B9E4B;IN_PROGRESS;alice
MOVE_TASK;ACCESSIB-F18B9E4B;DONE;alice

## CloudMigration

### TODO
MOVE_TASK;CONFIGUR-D82F1238;TODO;alice
MOVE_TASK;DEPLOYKU-DD236E6E;TODO;alice

### IN_PROGRESS
MOVE_TASK;CREATETE-06E44467;TODO;alice
MOVE_TASK;CREATETE-06E44467;IN_PROGRESS;alice
MOVE_TASK;CONFIGUR-868E8DBA;TODO;alice
MOVE_TASK;CONFIGUR-868E8DBA;IN_PROGRESS;alice

### DONE
MOVE_TASK;DATABASE-19BB1830;TODO;alice
MOVE_TASK;DATABASE-19BB1830;IN_PROGRESS;alice
MOVE_TASK;DATABASE-19BB1830;DONE;alice
MOVE_TASK;DISASTER-6D81DE8F;TODO;alice
MOVE_TASK;DISASTER-6D81DE8F;IN_PROGRESS;alice
MOVE_TASK;DISASTER-6D81DE8F;DONE;alice

# 15. MOVE_TASK — Workflow ENTERPRISE_FLOW

## ERPModernization
### ANALYSIS

MOVE_TASK;SAPINTEG-3602FCAF;ANALYSIS;bob
MOVE_TASK;ACCOUNTI-362CA9F4;ANALYSIS;bob

### VALIDATION
MOVE_TASK;DATABASE-A9A92938;ANALYSIS;bob
MOVE_TASK;DATABASE-A9A92938;VALIDATION;bob

MOVE_TASK;SECURITY-41D31035;ANALYSIS;bob
MOVE_TASK;SECURITY-41D31035;VALIDATION;bob

### CLOSED
MOVE_TASK;PERFORMA-FDFD3F47;ANALYSIS;bob
MOVE_TASK;PERFORMA-FDFD3F47;VALIDATION;bob
MOVE_TASK;PERFORMA-FDFD3F47;CLOSED;bob

MOVE_TASK;RELEASEC-D12AC53C;ANALYSIS;bob
MOVE_TASK;RELEASEC-D12AC53C;VALIDATION;bob
MOVE_TASK;RELEASEC-D12AC53C;CLOSED;bob

## SecurityAudit

### ANALYSIS
MOVE_TASK;INSPECTI-9D9C869E;ANALYSIS;eve
MOVE_TASK;PENTESTA-519A6445;ANALYSIS;eve

### VALIDATION
MOVE_TASK;REVIEWBA-C1F50253;ANALYSIS;eve
MOVE_TASK;REVIEWBA-C1F50253;VALIDATION;eve
MOVE_TASK;VALIDATE-E66BFCF8;ANALYSIS;eve
MOVE_TASK;VALIDATE-E66BFCF8;VALIDATION;eve

### CLOSED
MOVE_TASK;PATCHSER-851BFD20;ANALYSIS;eve
MOVE_TASK;PATCHSER-851BFD20;VALIDATION;eve
MOVE_TASK;PATCHSER-851BFD20;CLOSED;eve
MOVE_TASK;FINALSEC-8DE0406A;ANALYSIS;eve
MOVE_TASK;FINALSEC-8DE0406A;VALIDATION;eve
MOVE_TASK;FINALSEC-8DE0406A;CLOSED;eve

```


