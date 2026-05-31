# TTScore API

REST API para o app de ping pong TTScore, construída com **Java 21 + Spring Boot 3.5 + Cloud Firestore**.

---

## Sumário

- [Stack](#stack)
- [Configuração](#configuração)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Autenticação](#autenticação)
- [Endpoints](#endpoints)
  - [Auth](#auth)
  - [Usuários](#usuários)
  - [Partidas](#partidas)
  - [Amizades](#amizades)
  - [Ranking](#ranking)
- [Rodando Localmente com Android Studio](#rodando-localmente-com-android-studio)
- [Respostas de Erro](#respostas-de-erro)

---

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem |
| Spring Boot 3.5 | Framework |
| Spring Security | Autenticação |
| JWT (jjwt 0.12.6) | Token de acesso |
| Firebase Admin SDK 9.3 | Acesso ao Firestore |
| Cloud Firestore | Banco de dados |
| Lombok | Redução de boilerplate |

---

## Configuração

### 1. Firebase Service Account

1. Acesse o [Console do Firebase](https://console.firebase.google.com)
2. Projeto → Configurações → Contas de Serviço → Gerar nova chave privada
3. Renomeie o arquivo para `firebase-service-account.json`
4. Coloque-o em `src/main/resources/`

### 2. application.properties

```properties
# Firebase
app.firebase.service-account=classpath:firebase-service-account.json

# JWT - troque o secret em produção
app.jwt.secret=SEU_SECRET_BASE64_AQUI
app.jwt.expiration=86400000   # 24h em milissegundos

# Porta
server.port=8080
```

### 3. Rodar o projeto

```bash
./run.sh
```
OU
```bash
./mvnw spring-boot:run
```

---

## Estrutura do Projeto

```
src/main/java/com/ttscore/
├── auth/                   # Login e registro
│   ├── dto/
│   │   ├── AuthResponse.java
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   ├── AuthController.java
│   └── AuthService.java
├── config/
│   ├── FirebaseConfig.java  # Inicialização do Firestore
│   └── SecurityConfig.java  # Filtros e regras de segurança
├── exception/
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── friendship/             # Amizades entre usuários
│   ├── dto/
│   │   └── FriendshipResponse.java
│   ├── Friendship.java
│   ├── FriendshipController.java
│   ├── FriendshipRepository.java
│   ├── FriendshipService.java
│   ├── FriendshipStatus.java
│   └── FirestoreFriendshipRepository.java
├── match/                  # Partidas
│   ├── dto/
│   │   ├── MatchRequest.java
│   │   └── MatchResponse.java
│   ├── Match.java
│   ├── MatchController.java
│   ├── MatchRepository.java
│   ├── MatchService.java
│   └── FirestoreMatchRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   ├── JwtService.java
│   └── UserDetailsServiceImpl.java
└── user/                   # Usuários
    ├── dto/
    │   ├── UpdateProfileRequest.java
    │   └── UserResponse.java
    ├── User.java
    ├── UserController.java
    ├── UserRepository.java
    ├── UserService.java
    └── FirestoreUserRepository.java
```

---

## Autenticação

Todas as rotas (exceto `/api/auth/**`) requerem o header:

```
Authorization: Bearer <token>
```

O token é obtido nas rotas de **login** ou **registro**.

---

## Endpoints

### Auth

#### Registrar usuário

```
POST /api/auth/register
```

**Body:**
```json
{
  "username": "joao123",
  "email": "joao@email.com",
  "password": "minhasenha"
}
```

**Validações:**
- `username`: obrigatório, 3–50 caracteres
- `email`: obrigatório, formato válido
- `password`: obrigatório, mínimo 6 caracteres

**Resposta 201:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "a1b2c3d4-...",
    "username": "joao123",
    "email": "joao@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-15T10:30:00.000+00:00"
  }
}
```

---

#### Login

```
POST /api/auth/login
```

**Body:**
```json
{
  "username": "joao123",
  "password": "minhasenha"
}
```

**Resposta 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "a1b2c3d4-...",
    "username": "joao123",
    "email": "joao@email.com",
    "avatarUrl": "https://...",
    "createdAt": "2024-01-15T10:30:00.000+00:00"
  }
}
```

---

### Usuários

#### Meu perfil

```
GET /api/users/me
Authorization: Bearer <token>
```

**Resposta 200:**
```json
{
  "id": "a1b2c3d4-...",
  "username": "joao123",
  "email": "joao@email.com",
  "avatarUrl": "https://...",
  "createdAt": "2024-01-15T10:30:00.000+00:00",
  "wins": 10,
  "losses": 4
}
```

---

#### Buscar usuário por ID

```
GET /api/users/{id}
Authorization: Bearer <token>
```

**Path param:** `id` — ID do usuário

**Resposta 200:**
```json
{
  "id": "a1b2c3d4-...",
  "username": "joao123",
  "email": "joao@email.com",
  "avatarUrl": null,
  "createdAt": "2024-01-15T10:30:00.000+00:00",
  "wins": 10,
  "losses": 4
}
```

---

#### Pesquisar usuários por nome

```
GET /api/users/search?q={query}
Authorization: Bearer <token>
```

**Query param:** `q` — texto para busca (case-insensitive)

**Resposta 200:**
```json
[
  {
    "id": "a1b2c3d4-...",
    "username": "joao123",
    "email": "joao@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-15T10:30:00.000+00:00",
    "wins": 10,
    "losses": 4
  }
]
```

---

#### Atualizar perfil

```
PUT /api/users/me
Authorization: Bearer <token>
```

**Body (todos os campos opcionais):**
```json
{
  "username": "novo_nome",
  "avatarUrl": "https://exemplo.com/foto.png"
}
```

**Validações:**
- `username`: 3–50 caracteres (se enviado)

**Resposta 200:**
```json
{
  "id": "a1b2c3d4-...",
  "username": "novo_nome",
  "email": "joao@email.com",
  "avatarUrl": "https://exemplo.com/foto.png",
  "createdAt": "2024-01-15T10:30:00.000+00:00",
  "wins": 10,
  "losses": 4
}
```

---

### Ranking

#### Listar ranking global

Retorna todos os usuários ordenados por número de vitórias (decrescente).

```
GET /api/users/ranking
Authorization: Bearer <token>
```

**Resposta 200:**
```json
[
  {
    "id": "a1b2c3d4-...",
    "username": "joao123",
    "email": "joao@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-15T10:30:00.000+00:00",
    "wins": 15,
    "losses": 3
  },
  {
    "id": "b2c3d4e5-...",
    "username": "maria456",
    "email": "maria@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-10T08:00:00.000+00:00",
    "wins": 10,
    "losses": 5
  }
]
```

---

### Partidas

#### Registrar partida

O usuário autenticado é sempre o **player1**. O placar determina o vencedor automaticamente.

```
POST /api/matches
Authorization: Bearer <token>
```

**Body:**
```json
{
  "opponentUsername": "maria456",
  "player1Score": 11,
  "player2Score": 8
}
```

**Validações:**
- `opponentUsername`: obrigatório, deve ser um username cadastrado
- `player1Score`: obrigatório, >= 0
- `player2Score`: obrigatório, >= 0

**Resposta 201:**
```json
{
  "id": "m1n2o3p4-...",
  "player1": {
    "id": "a1b2c3d4-...",
    "username": "joao123",
    "email": "joao@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-15T10:30:00.000+00:00",
    "wins": 6,
    "losses": 2
  },
  "player2": {
    "id": "b2c3d4e5-...",
    "username": "maria456",
    "email": "maria@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-10T08:00:00.000+00:00",
    "wins": 10,
    "losses": 5
  },
  "player1Score": 11,
  "player2Score": 8,
  "winnerId": "a1b2c3d4-...",
  "playedAt": "2024-01-15T14:00:00.000+00:00"
}
```

---

#### Minhas partidas

```
GET /api/matches/me
Authorization: Bearer <token>
```

**Resposta 200:** array de `MatchResponse` (mesmo formato acima), ordenado por data decrescente.

---

#### Partidas de um usuário

```
GET /api/matches/user/{username}
Authorization: Bearer <token>
```

**Path param:** `username` — username do usuário

**Resposta 200:** array de `MatchResponse`.

---

#### Buscar partida por ID

```
GET /api/matches/{id}
Authorization: Bearer <token>
```

**Resposta 200:** `MatchResponse`.

---

#### Histórico frente a frente (head-to-head)

```
GET /api/matches/versus/{opponentUsername}
Authorization: Bearer <token>
```

Retorna todas as partidas entre o usuário autenticado e o oponente informado.

**Resposta 200:** array de `MatchResponse`.

---

### Amizades

#### Enviar pedido de amizade

```
POST /api/friendships/request/{addresseeId}?forceAccept=false
Authorization: Bearer <token>
```

**Path param:** `addresseeId` — ID do usuário a ser adicionado

**Query param (opcional):**

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `forceAccept` | boolean | `false` | **Apenas para testes.** Quando `true`, a amizade é criada diretamente com status `ACCEPTED`, sem precisar da confirmação do outro usuário. |

**Exemplo com forceAccept:**
```
POST /api/friendships/request/b2c3d4e5-...?forceAccept=true
```

**Resposta 201 (sem forceAccept / forceAccept=false):**
```json
{
  "id": "f1g2h3i4-...",
  "requester": {
    "id": "a1b2c3d4-...",
    "username": "joao123",
    "email": "joao@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-15T10:30:00.000+00:00"
  },
  "addressee": {
    "id": "b2c3d4e5-...",
    "username": "maria456",
    "email": "maria@email.com",
    "avatarUrl": null,
    "createdAt": "2024-01-10T08:00:00.000+00:00"
  },
  "status": "PENDING",
  "createdAt": "2024-01-15T15:00:00.000+00:00"
}
```

**Resposta 201 (forceAccept=true):**
```json
{
  "id": "f1g2h3i4-...",
  "requester": { "..." },
  "addressee": { "..." },
  "status": "ACCEPTED",
  "createdAt": "2024-01-15T15:00:00.000+00:00"
}
```

**Status possíveis:** `PENDING` | `ACCEPTED` | `BLOCKED`

---

#### Aceitar pedido de amizade

Somente o `addressee` (quem recebeu o pedido) pode aceitar.

```
PATCH /api/friendships/{id}/accept
Authorization: Bearer <token>
```

**Path param:** `id` — ID da amizade

**Resposta 200:** `FriendshipResponse` com `status: "ACCEPTED"`.

---

#### Remover / bloquear amizade

Qualquer um dos dois usuários pode remover. O status muda para `BLOCKED`.

```
DELETE /api/friendships/{id}
Authorization: Bearer <token>
```

**Resposta 204:** sem corpo.

---

#### Listar amigos

```
GET /api/friendships
Authorization: Bearer <token>
```

Retorna todas as amizades com `status: "ACCEPTED"` do usuário autenticado.

**Resposta 200:** array de `FriendshipResponse`.

---

#### Listar pedidos pendentes

```
GET /api/friendships/pending
Authorization: Bearer <token>
```

Retorna todos os pedidos com `status: "PENDING"` (enviados e recebidos).

**Resposta 200:** array de `FriendshipResponse`.

---

## Rodando Localmente com Android Studio

### Pré-requisitos

```bash
java -version   # Java 21+
./mvnw -version # já incluído no projeto
```

### 1. Adicionar o arquivo do Firebase

Baixe a chave privada (Console Firebase → Configurações do projeto → Contas de Serviço → Gerar nova chave privada) e salve em:

```
src/main/resources/firebase-service-account.json
```


### 2. Subir a API

```bash
./run.sh
```
OU
```bash
./mvnw spring-boot:run
```

Quando aparecer a mensagem abaixo, a API está no ar:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Started TtscoreApiApplication in 2.96 seconds (process running for 3.121)
```

Teste rápido no terminal:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teste","email":"teste@email.com","password":"123456"}'
```

### 3. Configurar o Android para acessar a API local

O emulador do Android Studio não enxerga `localhost` do computador. O IP correto para acessar o host a partir do emulador é `10.0.2.2`.

Configure a base URL no seu app Android:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

### 4. Liberar tráfego HTTP no Android

O Android 9+ bloqueia requisições HTTP por padrão. Crie o arquivo abaixo no projeto Android:

**`res/xml/network_security_config.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

Referencie no `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### 5. Fluxo completo

```
1. ./mvnw spring-boot:run              → API rodando em localhost:8080
2. Abrir Android Studio
3. Iniciar o emulador
4. BASE_URL = "http://10.0.2.2:8080/"
5. Rodar o app → login/registro funcionando
```

### Dica: separar URL de debug e release

```kotlin
// build.gradle (módulo app)
buildConfigField("String", "BASE_URL", '"http://10.0.2.2:8080/"')        // debug
buildConfigField("String", "BASE_URL", '"https://sua-api.railway.app/"') // release

// Uso no código
private const val BASE_URL = BuildConfig.BASE_URL
```

---

## Respostas de Erro

Todas as respostas de erro seguem o mesmo formato:

```json
{
  "status": 404,
  "message": "User not found with id: abc123",
  "timestamp": "2024-01-15T10:30:00.123"
}
```

| Código | Situação |
|---|---|
| 400 | Bad Request — dados inválidos (ex: username já em uso) |
| 401 | Unauthorized — token ausente ou inválido |
| 404 | Not Found — recurso não encontrado |
| 409 | Conflict — regra de negócio violada (ex: amizade já existe) |
| 422 | Unprocessable Entity — falha de validação dos campos |
| 500 | Internal Server Error — erro inesperado |

---

## Coleções no Firestore

| Coleção | Campos |
|---|---|
| `users` | `username`, `email`, `password`, `avatarUrl`, `createdAt`, `wins`, `losses` |
| `matches` | `player1Id`, `player2Id`, `player1Score`, `player2Score`, `winnerId`, `playedAt` |
| `friendships` | `requesterId`, `addresseeId`, `status`, `createdAt` |
