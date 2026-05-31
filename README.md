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
