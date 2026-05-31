# TTScore API — Exemplos de Uso (curl)

Execute os comandos em sequência no mesmo terminal. As variáveis `TOKEN_JOAO`, `TOKEN_MARIA`, `ID_JOAO`, `ID_MARIA`, `MATCH_ID` e `FRIENDSHIP_ID` são reutilizadas entre os passos.

---

## 1. Auth

### Registrar dois usuários

```bash
REGISTER_JOAO=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"joao999","email":"joao999@email.com","password":"senha123"}')
TOKEN_JOAO=$(echo $REGISTER_JOAO | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
ID_JOAO=$(echo $REGISTER_JOAO | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['id'])")
echo "TOKEN_JOAO=$TOKEN_JOAO"
echo "ID_JOAO=$ID_JOAO"
```

```bash
REGISTER_MARIA=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"maria999","email":"maria999@email.com","password":"senha123"}')
TOKEN_MARIA=$(echo $REGISTER_MARIA | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
ID_MARIA=$(echo $REGISTER_MARIA | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['id'])")
echo "TOKEN_MARIA=$TOKEN_MARIA"
echo "ID_MARIA=$ID_MARIA"
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"joao999","password":"senha123"}' | python3 -m json.tool
```

---

## 2. Usuários

### Meu perfil

```bash
curl -s http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Buscar usuário por ID

```bash
curl -s http://localhost:8080/api/users/$ID_JOAO \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Pesquisar usuários por nome

```bash
curl -s "http://localhost:8080/api/users/search?q=mar" \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Atualizar perfil

```bash
curl -s -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN_JOAO" \
  -H "Content-Type: application/json" \
  -d '{"avatarUrl":"https://exemplo.com/avatar.png"}' | python3 -m json.tool
```

---

## 3. Ranking

### Listar ranking global (ordenado por vitórias)

```bash
curl -s http://localhost:8080/api/users/ranking \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

---

## 4. Partidas

### Registrar vitória de joao contra maria (11x8)

```bash
curl -s -X POST http://localhost:8080/api/matches \
  -H "Authorization: Bearer $TOKEN_JOAO" \
  -H "Content-Type: application/json" \
  -d '{"opponentUsername":"maria999","player1Score":11,"player2Score":8}' | python3 -m json.tool
```

### Registrar derrota de joao para maria (5x11)

```bash
curl -s -X POST http://localhost:8080/api/matches \
  -H "Authorization: Bearer $TOKEN_JOAO" \
  -H "Content-Type: application/json" \
  -d '{"opponentUsername":"maria999","player1Score":5,"player2Score":11}' | python3 -m json.tool
```

### Minhas partidas

```bash
curl -s http://localhost:8080/api/matches/me \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Salvar ID da primeira partida

```bash
MATCH_RESPONSE=$(curl -s http://localhost:8080/api/matches/me \
  -H "Authorization: Bearer $TOKEN_JOAO")
MATCH_ID=$(echo $MATCH_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")
echo "MATCH_ID=$MATCH_ID"
```

### Buscar partida por ID

```bash
curl -s http://localhost:8080/api/matches/$MATCH_ID \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Partidas de um usuário por username

```bash
curl -s http://localhost:8080/api/matches/user/maria999 \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Head-to-head joao vs maria

```bash
curl -s http://localhost:8080/api/matches/versus/maria999 \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```

### Ranking após as partidas (wins/losses atualizados)

```bash
curl -s http://localhost:8080/api/users/ranking \
  -H "Authorization: Bearer $TOKEN_JOAO" | python3 -m json.tool
```