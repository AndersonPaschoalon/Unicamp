# A Evolução da Persistência: Introdução ao JSON e Serialização de Objetos

Até agora, nosso sistema dependia de arquivos de texto plano com formatos customizados (como valores separados por ponto e vírgula `;`). Embora funcional para cenários simples, ler e escrever dados dessa forma gera um acoplamento perigoso e um código procedural frágil. 

A extração manual de dados com `split(";")` força o sistema a conhecer a ordem exata das colunas, misturando regras de negócio com lógica de formatação. Para evoluir a maturidade do nosso design, introduzimos a persistência baseada em **JSON**.

## 1. O que é JSON?

**JSON** (JavaScript Object Notation) é um formato de intercâmbio de dados leve, de fácil leitura para humanos e simples de ser processado por máquinas. Ele organiza a informação em pares de `chave-valor` e estruturas de listas, mapeando perfeitamente os conceitos que usamos em Orientação a Objetos (atributos e coleções).

Um objeto `Task` em Java:
```java
public class Task {
    private String title = "Implementar Login";
    private boolean isTerminal = false;
}
```

O mesmo objeto representado em JSON:
```json
{
  "title": "Implementar Login",
  "isTerminal": false
}
```

## 2. Serialização e Desserialização

A transição entre os objetos vivos na memória do nosso programa e o texto estático no arquivo é governada por dois processos fundamentais:

* **Serialização (Objeto $\rightarrow$ JSON):** É o processo de traduzir o estado atual de um objeto em um formato que possa ser armazenado (em um arquivo) ou transmitido (por uma rede). A estrutura complexa da memória é "achatada" em uma string JSON.
* **Desserialização (JSON $\rightarrow$ Objeto):** É o processo inverso. O sistema lê a string JSON estruturada e a utiliza para instanciar e reconstruir perfeitamente o objeto e suas dependências na memória da aplicação.

## 3. Vantagens Arquiteturais (Clean Code e SRP)

Adotar JSON resolve diversos problemas de design de software:

1.  **Preservação de Hierarquia:** Se um `Project` contém uma `List<Task>`, o JSON aninha nativamente essas estruturas usando arrays `[ ]` e objetos `{ }`. Não precisamos mais de lógicas complexas para "amarrar" linhas de um arquivo de texto ao seu projeto pai.
2.  **Separação de Responsabilidades (SRP):** Delegamos a responsabilidade de converter dados para bibliotecas maduras (como Gson ou Jackson). Nossas classes de domínio (`User`, `Task`) não precisam mais ter métodos que formatam a si mesmas em strings exóticas para serem salvas.
3.  **Resiliência a Mudanças:** Adicionar um novo atributo a uma classe não quebra o carregamento dos dados antigos (o atributo simplesmente iniciará nulo ou com valor padrão).

## 4. O Desafio dos Tipos Complexos (Adaptadores)

Embora bibliotecas de JSON saibam converter automaticamente tipos primitivos (`int`, `boolean`) e `Strings`, elas não conhecem a estrutura interna de tipos complexos ou específicos do Java, como o `LocalDate`.

Como o JSON não possui um tipo nativo de "Data" (tudo é string ou número), tentar serializar um `LocalDate` diretamente pode gerar um erro ou um formato ilegível. É por isso que em projetos reais utilizamos o padrão **Adapter** (como o `LocalDateAdapter` presente na nossa arquitetura). Ele ensina o serializador a converter objetos complexos para uma string padronizada (ex: `"2026-04-15"`) ao salvar, e a fazer o parse reverso ao carregar.