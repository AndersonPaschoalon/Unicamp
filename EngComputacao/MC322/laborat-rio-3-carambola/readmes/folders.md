A estrutura de pastas que discutimos não é apenas uma convenção estética; ela é a materialização do princípio de **Separação de Preocupações (Separation of Concerns)**. No desenvolvimento Java profissional, especialmente em sistemas que seguem o padrão **MVC (Model-View-Controller)** e a **Arquitetura em Camadas**, cada diretório funciona como uma "fronteira" que protege a integridade do código.

## 1. O Padrão de Camadas (The Layered Architecture)

A ideia central é que camadas superiores podem falar com as inferiores, mas as inferiores nunca devem saber quem as está chamando.

### 🧩 `model/` (O Coração)
É a camada mais estável. Nela residem as **Entidades** (`entity/`) e os **Objetos de Valor** (`valueobject/`).
* **Como funciona:** Representa os dados e as regras que nunca mudam, independentemente da interface. Se o Nexus é um sistema de tarefas, uma "Tarefa" sempre terá um título, não importa se você a vê num terminal preto e branco ou num aplicativo de celular.
* **Regra de Ouro:** Nunca coloque `System.out.println` ou lógica de banco de dados aqui.

### 🖥️ `view/` (A Pele)
É a camada externa que interage com o usuário. No seu caso, ela abriga a `cli/` (usando JLine 3) e futuramente a `gui/`.
* **Como funciona:** Ela "escuta" o que o usuário quer e repassa a ordem para o Controlador. Ela é responsável apenas pela **apresentação**.


### 🎮 `controller/` (O Cérebro)
Funciona como um intermediário ou "despachante".
* **Como funciona:** O controlador recebe o input da View (ex: uma String vinda do terminal), valida se o comando faz sentido e decide qual **Service** deve ser acionado. Ele impede que a interface gráfica (View) "bagunce" os dados diretamente no Workspace (Model).

### ⚙️ `service/` (Os Músculos)
Onde a lógica pesada e a persistência acontecem.
* **Como funciona:** Se o Controlador diz "Crie uma tarefa", o `TaskService` verifica se o budget do projeto permite, gera o ID e salva no JSON via `PersistenceService`. 
* **Divisão Interna:** Costumamos usar `core/` para lógica de negócio e `infra/` para ferramentas técnicas (como leitores de arquivos).

### ⚠️ `exception/` (O Alarme)
Centraliza os erros específicos do seu negócio.
* **Como funciona:** Em vez de usar erros genéricos do Java, você usa `NexusValidationException`. Isso permite que a View saiba exatamente o que dizer ao usuário (ex: "Status inválido") sem que o programa trave.

---

## 2. Por que usar isso?

1.  **Manutenibilidade:** Se você precisar trocar o JLine por outra biblioteca de terminal, você altera apenas a pasta `view/cli`. O resto do sistema continua funcionando.
2.  **Colaboração:** Em uma equipe, um aluno pode trabalhar na interface visual enquanto outro melhora o algoritmo de cálculo de produtividade no `ReportService`, sem um atropelar o código do outro.
3.  **Testabilidade:** Você consegue criar testes automáticos que verificam se uma tarefa é criada corretamente sem precisar abrir o terminal e digitar comandos manualmente.