| Comando              | Sintaxe                                                    | Descrição                                      |
| -------------------- | ---------------------------------------------------------- | ---------------------------------------------- |
| `CREATE_USER`        | `CREATE_USER;username;email`                               | Cria um usuário                                |
| `CREATE_PROJECT`     | `CREATE_PROJECT;projectName`                               | Cria um projeto                                |
| `CREATE_STATUS`      | `CREATE_STATUS;statusName;isTerminal`                      | Cria um status dinâmico                        |
| `CREATE_WORKFLOW`    | `CREATE_WORKFLOW;workflowName`                             | Cria um workflow vazio                         |
| `SET_INITIAL_STATUS` | `SET_INITIAL_STATUS;workflowName;statusName`               | Define o status inicial automático do workflow |
| `ALLOW_TRANSITION`   | `ALLOW_TRANSITION;workflowName;statusOrigem;statusDestino` | Adiciona transição válida entre estados        |
| `LINK_WORKFLOW`      | `LINK_WORKFLOW;projectName;workflowName`                   | Associa workflow a um projeto                  |
| `CREATE_TASK`        | `CREATE_TASK;taskName;deadline;effort;projectName`         | Cria tarefa vinculada ao projeto               |
| `ASSIGN_USER`        | `ASSIGN_USER;taskId;username`                              | Define owner da tarefa                         |
| `ADD_MEMBER`         | `ADD_MEMBER;projectName;username;ROLE`                     | Adiciona usuário ao projeto com papel RBAC     |
| `MOVE_TASK`          | `MOVE_TASK;taskId;newStatusName;usernameAtuante`           | Solicita alteração de status da task           |
| `REPORT_STATUS`      | `REPORT_STATUS`                                            | Executa relatórios analíticos                  |


