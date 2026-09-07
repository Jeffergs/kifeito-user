# Kifeito User

O **Kifeito User** é responsável pelo cadastro, autenticação e gerenciamento da própria conta do usuário.

O serviço mantém a identidade e os dados cadastrais do usuário, realiza a autenticação através de e-mail e senha e fornece a identidade autenticada aos demais serviços protegidos.

Na versão 1, o serviço utiliza **Spring Security**, **JWT**, **BCrypt** e **Stateless Authentication**.

---

<a id="indice"></a>

## 📋 Índice

1. [🎯 Responsabilidade](#-responsabilidade)
2. [👤 Entidade Usuário](#-entidade-usuário)
3. [📝 Criar conta](#-criar-conta)
4. [🔐 Login](#-login)
5. [🪪 Identidade do usuário](#-identidade-do-usuário)
6. [🔎 Consultar própria conta](#-consultar-própria-conta)
7. [✏️ Atualizar própria conta](#️-atualizar-própria-conta)
8. [📧 Alterar e-mail](#-alterar-e-mail)
9. [🗑️ Excluir própria conta](#️-excluir-própria-conta)
10. [🔒 Segurança](#-segurança)
11. [🛡️ Autorização](#️-autorização)
12. [📨 Integração com Tasks](#-integração-com-tasks)
13. [🛠️ Tecnologias](#️-tecnologias)
14. [📁 Estrutura](#-estrutura)
15. [⚙️ Configuração](#️-configuração)
16. [🐳 Docker](#-docker)
17. [🧪 Testes](#-testes)
18. [🚫 Fora do escopo da versão 1](#-fora-do-escopo-da-versão-1)
19. [📄 Licença](#-licença)

---

<a id="responsabilidade"></a>

# 🎯 Responsabilidade

O serviço possui uma responsabilidade específica:

> **Gerenciar a identidade, autenticação e os dados cadastrais do usuário.**

O **Kifeito User** é a fonte da identidade e dos dados cadastrais do usuário.

Os demais microsserviços podem manter apenas uma referência ao usuário através do seu `userId`.

| Responsabilidade | Serviço |
|---|---|
| Identidade do usuário | User |
| Dados cadastrais | User |
| E-mail da conta | User |
| Senha | User |
| Autenticação | User |
| JWT | User |
| Autorização baseada na identidade | Serviços protegidos |
| Tarefas | Tasks |
| Lembretes | Notification |

O User não deve permitir que um usuário consulte ou altere os dados de outra conta.

⬆️ [Voltar ao índice](#indice)

---

<a id="entidade-usuário"></a>

# 👤 Entidade Usuário

A entidade `User` representa a conta e a identidade de um usuário dentro do Kifeito.

| Campo | Descrição |
|---|---|
| `id` | Identificador único gerado pelo sistema. |
| `name` | Nome do usuário. |
| `email` | E-mail utilizado na conta e no login. |
| `password` | Senha armazenada de forma segura utilizando BCrypt. |

A senha nunca deve ser armazenada em texto puro.

O `id` representa a identidade do usuário dentro do sistema e não deve ser alterado durante a vida da conta.

⬆️ [Voltar ao índice](#indice)

---

<a id="criar-conta"></a>

# 📝 Criar conta

O usuário pode criar uma nova conta.

**POST /usuario**

### Fluxo

```text
Requisição
  ↓
validação dos campos
  ↓
validação da senha
  ↓
verifica e-mail único
  ↓
hash da senha com BCrypt
  ↓
persiste User
```

### Regras

- `name` é obrigatório;
- `email` é obrigatório;
- `email` deve possuir formato válido;
- `email` deve ser único;
- `password` é obrigatório;
- `password` deve possuir no mínimo 8 caracteres;
- `password` não pode ser nulo ou vazio;
- A senha nunca deve ser armazenada em texto puro;
- A senha deve ser validada antes de ser armazenada;
- A senha deve ser armazenada utilizando BCrypt;
- `id` é gerado pelo sistema.

⬆️ [Voltar ao índice](#indice)

---

<a id="login"></a>

# 🔐 Login

O usuário pode autenticar utilizando e-mail e senha.

**POST /usuario/login**

### Fluxo

```text
email + senha
  ↓
AuthenticationManager
  ↓
validação das credenciais
  ↓
geração do JWT
  ↓
JWT retornado ao cliente
```

O JWT será utilizado para identificar o usuário autenticado nas requisições aos serviços protegidos.

O e-mail é utilizado para autenticação, enquanto o `userId` representa a identidade do usuário dentro do sistema.

⬆️ [Voltar ao índice](#indice)

---

<a id="identidade-do-usuário"></a>

# 🪪 Identidade do usuário

O microsserviço User é a **fonte da identidade e dos dados cadastrais do usuário**.

Os demais microsserviços podem manter uma referência ao usuário através de `userId`.

### Identidade

O JWT deve identificar o usuário pelo `userId`, e não pelo e-mail.

```text
JWT
 └── subject = userId
```

Isso permite que o usuário altere o e-mail sem alterar sua identidade dentro do sistema.

O e-mail continua sendo utilizado para autenticação.

### Exemplo

```text
email + senha
      ↓
autenticação
      ↓
User.id = 42
      ↓
JWT
      ↓
subject = 42
```

⬆️ [Voltar ao índice](#indice)

---

<a id="consultar-própria-conta"></a>

# 🔎 Consultar própria conta

O usuário autenticado pode consultar os dados da própria conta.

**GET /usuario**

A identificação do usuário deve vir da autenticação.

### Fluxo

```text
JWT
  ↓
userId
  ↓
User
  ↓
dados da própria conta
```

### Regras

- A identidade do usuário é obtida através do JWT;
- O cliente não deve informar outro `userId` para consultar uma conta;
- O cliente não deve conseguir consultar outra conta através do e-mail;
- Somente os dados da própria conta devem ser retornados.

⬆️ [Voltar ao índice](#indice)

---

<a id="atualizar-própria-conta"></a>

# ✏️ Atualizar própria conta

O usuário autenticado pode atualizar os dados permitidos da própria conta.

Na versão 1, podem ser alterados:

```text
name
email
```

A alteração de senha será implementada futuramente como uma operação específica.

### Fluxo

```text
JWT
  ↓
userId
  ↓
User correspondente
  ↓
validação
  ↓
atualização
  ↓
persistência
```

### Regras

- O usuário só pode alterar a própria conta;
- A identidade do usuário é obtida através do JWT;
- O usuário não pode alterar o `id`;
- O usuário não pode alterar a identidade de outro usuário;
- O alteração de senha não faz parte desta operação.

⬆️ [Voltar ao índice](#indice)

---

<a id="alterar-e-mail"></a>

# 📧 Alterar e-mail

O usuário pode alterar o próprio e-mail somente quando estiver autenticado.

### Regras

- O usuário deve estar autenticado;
- O novo e-mail deve possuir formato válido;
- O novo e-mail não pode pertencer a outro usuário;
- Se o novo e-mail for igual ao atual, nenhuma alteração é necessária;
- O novo e-mail será utilizado nos próximos logins;
- A alteração do e-mail não altera a identidade do usuário;
- O `userId` permanece o mesmo após a alteração.

### Exemplo

```text
Antes:

userId = 42
email  = usuario@email.com


Depois:

userId = 42
email  = novo@email.com
```

A identidade continua sendo:

```text
userId = 42
```

⬆️ [Voltar ao índice](#indice)

---

<a id="excluir-própria-conta"></a>

# 🗑️ Excluir própria conta

O usuário pode excluir sua própria conta.

**DELETE /usuario**

### Fluxo

```text
JWT
  ↓
userId
  ↓
usuário autenticado
  ↓
exclusão da própria conta
  ↓
publica UserDeleted
```

A conta a ser excluída deve ser determinada pela identidade autenticada.

O cliente não deve informar outro usuário para determinar qual conta será excluída.

### Regras

- Apenas o próprio usuário pode excluir sua conta;
- A identidade é obtida através do JWT;
- A exclusão é definitiva na V1;
- Após a exclusão, a conta não poderá ser recuperada;
- As tarefas pertencentes ao usuário também deverão ser excluídas;
- O User não acessa diretamente o banco de dados do Tasks;
- A comunicação com Tasks ocorre através de evento.

⬆️ [Voltar ao índice](#indice)

---

<a id="segurança"></a>

# 🔒 Segurança

A versão 1 utiliza:

```text
Spring Security
JWT
BCrypt
Stateless Authentication
```

### Spring Security

Responsável pela autenticação e pelo controle de acesso aos recursos protegidos.

### BCrypt

Responsável pelo armazenamento seguro das senhas.

A senha fornecida pelo usuário não deve ser armazenada diretamente.

### JWT

Responsável por transportar a identidade autenticada entre as requisições.

O `userId` é utilizado como `subject` do token.

### Stateless Authentication

O servidor não mantém uma sessão HTTP tradicional.

A identidade do usuário é transportada através do JWT enviado nas requisições protegidas.

⬆️ [Voltar ao índice](#indice)

---

<a id="autorização"></a>

# 🛡️ Autorização

A regra principal de autorização do Kifeito é:

> **Um usuário só pode acessar e modificar a própria conta.**

O processo pode ser representado por:

```text
Autenticação
     ↓
Quem é o usuário?
     ↓
userId
     ↓
Autorização
     ↓
O recurso pertence a esse usuário?
     ↓
Acesso permitido ou negado
```

A identidade utilizada para autorização deve vir da autenticação e não de informações fornecidas pelo cliente.

⬆️ [Voltar ao índice](#indice)

---

<a id="integração-com-tasks"></a>

# 📨 Integração com Tasks

O User e o Tasks são microsserviços independentes.

O User não acessa diretamente o banco de dados do Tasks.

Quando uma conta é excluída, o User comunica o fato através de um evento.

### Fluxo

```text
User
  │
  │ UserDeleted
  ▼
RabbitMQ
  │
  ▼
Tasks
  │
  ▼
localiza tarefas pelo userId
  │
  ▼
exclui tarefas
```

### Evento

```text
UserDeleted
```

O evento deve conter a identidade do usuário excluído para que o Tasks consiga localizar as tarefas pertencentes a ele.

### Regra

Cada microsserviço permanece responsável pelo seu próprio banco de dados.

O User não deve executar operações diretamente no banco do Tasks.

⬆️ [Voltar ao índice](#indice)

---

<a id="tecnologias"></a>

# 🛠️ Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 17 | Linguagem |
| Spring Boot | Framework |
| Spring Web | Desenvolvimento da API REST |
| Spring Data JPA | Persistência |
| PostgreSQL | Banco de dados |
| Spring Security | Autenticação e autorização |
| JWT | Identificação do usuário autenticado |
| BCrypt | Hash das senhas |
| Spring AMQP | Integração com RabbitMQ |
| RabbitMQ | Mensageria assíncrona |
| Gradle | Build |
| Docker | Containerização |
| GitHub Actions | CI |

⬆️ [Voltar ao índice](#indice)

---

<a id="estrutura"></a>

# 📁 Estrutura

Estrutura inicial:

```text
kifeito-user
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.jefferson.user
│   │   │       │
│   │   │       ├── controller
│   │   │       │   └── UserController.java
│   │   │       │
│   │   │       ├── dto
│   │   │       │   ├── UserRequestDTO.java
│   │   │       │   └── UserResponseDTO.java
│   │   │       │
│   │   │       ├── entity
│   │   │       │   └── User.java
│   │   │       │
│   │   │       ├── enums
│   │   │       │   └── UserStatus.java
│   │   │       │
│   │   │       ├── exception
│   │   │       │   └── UserException.java
│   │   │       │
│   │   │       ├── repository
│   │   │       │   └── UserRepository.java
│   │   │       │
│   │   │       ├── service
│   │   │       │   └── UserService.java
│   │   │       │
│   │   │       ├── security
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── JwtService.java
│   │   │       │
│   │   │       ├── messaging
│   │   │       │   └── UserEventPublisher.java
│   │   │       │
│   │   │       └── UserApplication.java
│   │   │
│   │   └── resources
│   │       └── application.yaml
│
├── .github
│   └── workflows
│       └── pull-request.yml
│
├── .gitignore
├── Dockerfile
├── build.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

⬆️ [Voltar ao índice](#indice)

---

<a id="configuracao"></a>

# ⚙️ Configuração

As configurações do banco de dados, JWT e RabbitMQ são fornecidas por variáveis de ambiente.

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD

JWT_SECRET

RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

As informações sensíveis não devem ser armazenadas diretamente no código-fonte.

> O arquivo `.env` não deve ser versionado.

⬆️ [Voltar ao índice](#indice)

---

<a id="docker"></a>

# 🐳 Docker

O Kifeito User possui seu próprio `Dockerfile` e pode ser executado junto aos demais serviços através do Docker Compose.

O Docker garante um ambiente de execução padronizado, facilita a configuração e permite executar os serviços de forma isolada e reproduzível.

⬆️ [Voltar ao índice](#indice)

---

<a id="testes"></a>

# 🧪 Testes

O serviço terá testes unitários para as regras de negócio e testes de integração para suas principais integrações.

- **Testes unitários:** regras de negócio, autenticação, autorização e ciclo de vida da conta do usuário.
- **Testes de integração:** PostgreSQL, Spring Security, JWT e RabbitMQ.

⬆️ [Voltar ao índice](#indice)

---

<a id="fora-do-escopo-da-versão-1"></a>

# 🚫 Fora do escopo da versão 1

Para manter a complexidade proporcional à necessidade do sistema, a versão 1 não possui:

- Troca de senha;
- Recuperação de senha;
- Refresh Token;
- Refresh Token Rotation;
- Revogação de Refresh Tokens;
- Proteção avançada contra força bruta;
- Política avançada de senhas;
- Autenticação em dois fatores;
- Login com Google;
- OAuth 2.0;
- OpenID Connect;
- Foto de perfil;
- Múltiplos métodos de autenticação;
- Gerenciamento avançado de sessões.

Esses recursos poderão ser avaliados em versões futuras conforme a necessidade do produto.

⬆️ [Voltar ao índice](#indice)

---

<a id="licenca"></a>

# 📄 Licença

O Kifeito está sendo desenvolvido inicialmente para uso próprio e para um grupo limitado de usuários.

Apesar do uso inicial restrito, o projeto está sendo desenvolvido com arquitetura, práticas e estrutura voltadas para um produto comercial, podendo futuramente ser disponibilizado de forma mais ampla.

O código-fonte, a aplicação, a identidade visual, a documentação e demais componentes do projeto são de propriedade do próprio autor.

A utilização, cópia, modificação, distribuição ou comercialização de qualquer parte do projeto depende de autorização expressa do detentor dos direitos.

⬆️ [Voltar ao índice](#indice)
