# API Security Gateway Minimal - Cars API

Proiectul implementeaza un **API Security Gateway Minimal** pentru un API REST de vanzare masini. Gateway-ul functioneaza ca **reverse proxy** intre client si backend si aplica:

- validare JWT pentru rutele private;
- rate limiting pentru rute publice si private;
- sanitizare input pentru request body;
- separare intre endpointuri publice si private.

## 1. Structura proiectului

```text
api-security-gateway-cars/
├── backend-service/
│   ├── src/
│   ├── build.gradle
│   └── settings.gradle
├── gateway-service/
│   ├── src/
│   ├── build.gradle
│   └── settings.gradle
├── docker-compose.yml
└── README.md
```

Roluri:

- `backend-service` ruleaza pe portul `8085` si contine logica aplicatiei: useri, masini, mesaje, autentificare.
- `gateway-service` ruleaza pe portul `8086` si este singurul serviciu accesat din Postman.
- `PostgreSQL` ruleaza in Docker pe portul `5432`.
- `pgAdmin` ruleaza in Docker pe portul `5050`.

Clientul nu ar trebui sa foloseasca direct backend-ul. Toate requesturile se fac prin gateway:

```text
http://localhost:8086/gateway/...
```

---

## 2. Cerinte instalare

Ai nevoie de:

- Java 25;
- Gradle wrapper inclus in proiect;
- Docker Desktop;
- Postman;
- IntelliJ IDEA sau terminal.

---

## 3. Pornire baza de date cu Docker

Din folderul unde se afla `docker-compose.yml`, ruleaza:

```bash
docker compose up -d
```

Acest lucru porneste:

- PostgreSQL container: `cars_postgres`
- pgAdmin container: `cars_pgadmin`

Verifica daca ruleaza:

```bash
docker ps
```

Trebuie sa vezi containerele `cars_postgres` si `cars_pgadmin`.

---

## 4. Configurare pgAdmin optional

Deschide in browser:

```text
http://localhost:5050
```

Login pgAdmin:

```text
Email: admin@admin.com
Password: admin
```

Adauga server nou:

```text
General -> Name: cars-postgres
Connection -> Host name/address: postgres
Connection -> Port: 5432
Connection -> Maintenance database: carsdb
Connection -> Username: postgres
Connection -> Password: postgres
```

Apasa `Save`.

---

## 5. Configurare JWT secret

Pentru varianta simpla de rulare locala, proiectul poate avea in `application.properties`:

```properties
security.jwt.secret=12345678901234567890123456789012
```

Varianta mai buna pentru securitate este sa folosesti environment variable.

In `backend-service/src/main/resources/application.properties` si `gateway-service/src/main/resources/application.properties`, poti pune:

```properties
security.jwt.secret=${JWT_SECRET}
```

Apoi setezi variabila de mediu.

### Windows PowerShell

```powershell
$env:JWT_SECRET="12345678901234567890123456789012"
```

### CMD

```cmd
set JWT_SECRET=12345678901234567890123456789012
```

### IntelliJ IDEA

Run -> Edit Configurations -> Environment variables:

```text
JWT_SECRET=12345678901234567890123456789012
```

Important: acelasi secret trebuie folosit si in backend, si in gateway. Backend-ul genereaza tokenul, gateway-ul il valideaza.

---

## 6. Pornire backend-service

### 1. Deschide proiectul în IntelliJ

- File → Open → selectează folderul proiectului
- Așteaptă să se încarce Gradle

---

### 2. Pornește backend-service

- Mergi în:

```text
backend-service/src/main/java/.../BackendServiceApplication.java
```

- Click dreapta → **Run**

Backend-ul va porni pe:

```text
http://localhost:8085
```

## 7. Pornire gateway-service

- Mergi în:
```text
gateway-service/src/main/java/.../GatewayServiceApplication.java
```

- Click dreapta → **Run**

Gateway-ul va porni pe:

```text
http://localhost:8086
```

## 8. Date demo

La prima pornire, aplicatia creeaza automat useri si masini demo.

Useri disponibili:

```text
username: andrei
password: 1234

username: mihai
password: 1234

username: alex
password: 1234

username: ion
password: 1234
```

---

## 9. Configurare Postman Environment

Creeaza un environment in Postman, de exemplu `Cars Gateway Env`, cu variabilele:

```text
baseGateway = http://localhost:8086
jwt = gol initial
```

Toate endpointurile vor folosi:

```text
{{baseGateway}}
```

Pentru endpointurile private, la Authorization foloseste:

```text
Type: Bearer Token
Token: {{jwt}}
```

---

## 10. Login si salvare automata JWT in Postman

Request:

```text
POST {{baseGateway}}/gateway/auth/login
```

Body -> raw -> JSON:

```json
{
  "username": "andrei",
  "password": "1234"
}
```

Daca login-ul este corect, primesti un token JWT.

In tab-ul `Scripts` / `Tests` din Postman, pune scriptul:

```javascript
const response = pm.response.json();

if (response.token) {
    pm.environment.set("jwt", response.token);
}
```

Dupa ce dai `Send`, variabila `jwt` se completeaza automat.

---

## 11. Endpointuri publice

### 11.1 Lista masini

```text
GET {{baseGateway}}/gateway/public/cars
```

Nu necesita JWT.

Raspuns asteptat:

```text
200 OK
```

### 11.2 Masina dupa id

```text
GET {{baseGateway}}/gateway/public/cars/1
```

Nu necesita JWT.

Raspuns asteptat:

```text
200 OK
```

Daca id-ul nu exista:

```text
404 Not Found
```

---

## 12. Endpointuri private

Toate endpointurile private necesita JWT valid.

In Postman, la fiecare request privat:

```text
Authorization -> Bearer Token -> {{jwt}}
```

### 12.1 Profil utilizator

```text
GET {{baseGateway}}/gateway/private/profile
```

Raspuns asteptat:

```json
{
  "username": "andrei",
  "role": "USER"
}
```

### 12.2 Adaugare masina

```text
POST {{baseGateway}}/gateway/private/cars
```

Body -> raw -> JSON:

```json
{
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2020,
  "price": 13500,
  "description": "Masina intretinuta, revizii la zi"
}
```

Raspuns asteptat:

```text
200 OK
```

### 12.3 Masinile mele

```text
GET {{baseGateway}}/gateway/private/my-cars
```

Raspuns asteptat:

```text
200 OK
```

### 12.4 Trimitere mesaj

```text
POST {{baseGateway}}/gateway/private/messages
```

Body -> raw -> JSON:

```json
{
  "carId": 1,
  "content": "Salut, masina mai este disponibila?"
}
```

Raspuns asteptat:

```text
200 OK
```

### 12.5 Mesaje primite

```text
GET {{baseGateway}}/gateway/private/messages/received
```

Raspuns asteptat:

```text
200 OK
```

---

## 13. Testare JWT invalid sau lipsa

### 13.1 Request privat fara token

```text
GET {{baseGateway}}/gateway/private/profile
```

Fara Authorization header.

Raspuns asteptat:

```text
401 Unauthorized
```

sau, daca verificarea este facuta direct de Spring Security:

```text
403 Forbidden
```

### 13.2 Request privat cu token gresit

Authorization:

```text
Bearer token_invalid
```

Raspuns asteptat:

```text
401 Unauthorized
```

Scop: demonstreaza ca gateway-ul nu permite accesul la rute private fara JWT valid.

---

## 14. Testare atac 1: XSS / input malicios la adaugare masina

Acest proiect trateaza input malicios prin `InputSanitizer`. Gateway-ul verifica request body-ul inainte sa trimita requestul catre backend.

Request:

```text
POST {{baseGateway}}/gateway/private/cars
```

Authorization:

```text
Bearer {{jwt}}
```

Body -> raw -> JSON:

```json
{
  "brand": "<script>alert(1)</script>",
  "model": "X5",
  "year": 2020,
  "price": 35000,
  "sellerName": "Alex",
  "description": "hack"
}
```

Raspuns asteptat:

```text
400 Bad Request
```

Body raspuns:

```json
{
  "error": "Unsafe input detected"
}
```

Explicatie: gateway-ul detecteaza tag-ul `<script>` si blocheaza requestul. Requestul nu mai ajunge la backend.

---

## 15. Testare atac 2: XSS / input malicios la trimitere mesaj

Request:

```text
POST {{baseGateway}}/gateway/private/messages
```

Authorization:

```text
Bearer {{jwt}}
```

Body -> raw -> JSON:

```json
{
  "carId": 1,
  "content": "<script>alert('xss')</script>"
}
```

Raspuns asteptat:

```text
400 Bad Request
```

Body raspuns:

```json
{
  "error": "Unsafe input detected"
}
```

Explicatie: mesajul contine cod JavaScript malicios. Gateway-ul il respinge prin sanitizer.

---

## 16. Testare atac 3: SQL Injection simplu

Sanitizer-ul blocheaza si pattern-uri simple de SQL injection, cum ar fi:

- `' or '1'='1`
- `--`
- `;`

Request:

```text
POST {{baseGateway}}/gateway/auth/login
```

Body -> raw -> JSON:

```json
{
  "username": "' or '1'='1",
  "password": "anything"
}
```

Raspuns asteptat:

```text
400 Bad Request
```

Body raspuns:

```json
{
  "error": "Unsafe input detected"
}
```

Explicatie: gateway-ul respinge requestul inainte sa ajunga la backend.

---

## 17. Testare rate limiting

Configuratia din `gateway-service/src/main/resources/application.properties` este:

```properties
rate.limit.public.max-requests=5
rate.limit.public.window-seconds=60

rate.limit.private.max-requests=10
rate.limit.private.window-seconds=60
```

Asta inseamna:

- maximum 5 requesturi publice pe minut pentru acelasi client;
- maximum 10 requesturi private pe minut pentru acelasi client.

### 17.1 Test rate limiting pe ruta publica

Trimite de 6 ori rapid:

```text
GET {{baseGateway}}/gateway/public/cars
```

Primele 5 requesturi:

```text
200 OK
```

Al 6-lea request:

```text
429 Too Many Requests
```

Raspuns asteptat:

```json
{
  "error": "Too many requests"
}
```

### 17.2 Test rate limiting pe ruta privata

Trimite de 11 ori rapid:

```text
GET {{baseGateway}}/gateway/private/profile
```

Cu JWT valid.

Primele 10 requesturi:

```text
200 OK
```

Al 11-lea request:

```text
429 Too Many Requests
```

---

## 18. Tenta personala / scenariu de atac

Aplicatia simuleaza un API pentru o platforma de vanzare masini, asemanatoare cu un marketplace auto.

Un atacator ar putea incerca:

1. sa trimita multe requesturi catre rutele publice pentru a supraincarca API-ul sau pentru scraping de anunturi;
2. sa faca brute-force pe autentificare;
3. sa trimita payload-uri XSS in anunturi sau mesaje, de exemplu `<script>alert(1)</script>`;
4. sa incerce payload-uri simple de SQL injection, de exemplu `' or '1'='1`.

Din acest motiv, gateway-ul aplica:

- rate limiting diferit pentru rutele publice si private;
- validare JWT pentru accesul la resurse private;
- sanitizare input inainte ca datele sa ajunga la backend.

Parametrii de rate limiting sunt:

```text
Public: 5 requesturi / 60 secunde
Private: 10 requesturi / 60 secunde
```

Rutele publice au o limita mai mica deoarece pot fi accesate fara autentificare si sunt mai expuse la abuz. Rutele private au o limita mai mare deoarece necesita JWT valid, dar sunt in continuare protejate impotriva abuzului.

---

## 20. Oprire aplicatie

Opreste serviciile Spring Boot cu `CTRL + C` in terminalele in care ruleaza sau pe butonul stop All din InteliJ.

Opreste Docker:

```bash
docker compose down
```

Daca vrei sa stergi si datele din baza de date:

```bash
docker compose down -v
```

---

## 22. Rezumat endpointuri

| Tip | Metoda | Endpoint | JWT necesar |
|---|---:|---|---|
| Auth | POST | `/gateway/auth/login` | Nu |
| Public | GET | `/gateway/public/cars` | Nu |
| Public | GET | `/gateway/public/cars/{id}` | Nu |
| Private | GET | `/gateway/private/profile` | Da |
| Private | POST | `/gateway/private/cars` | Da |
| Private | GET | `/gateway/private/my-cars` | Da |
| Private | POST | `/gateway/private/messages` | Da |
| Private | GET | `/gateway/private/messages/received` | Da |

---

## 23. Flow complet de testare in Postman

Ordinea recomandata:

1. Porneste Docker:

```bash
docker compose up -d
```

2. Porneste `backend-service`.
3. Porneste `gateway-service`.
4. In Postman seteaza environment:

```text
baseGateway=http://localhost:8086
jwt=
```

5. Ruleaza login:

```text
POST {{baseGateway}}/gateway/auth/login
```

6. Salveaza JWT automat cu scriptul din Postman.
7. Testeaza:

```text
GET {{baseGateway}}/gateway/public/cars
GET {{baseGateway}}/gateway/private/profile
POST {{baseGateway}}/gateway/private/cars
POST {{baseGateway}}/gateway/private/messages
GET {{baseGateway}}/gateway/private/my-cars
GET {{baseGateway}}/gateway/private/messages/received
```

8. Testeaza atacurile:

```text
POST Car malicious
POST Message malicious
SQL injection login malicious
Rate limiting public/private
JWT lipsa/invalid
```

Daca primesti `400 Bad Request`, `401 Unauthorized` sau `429 Too Many Requests` in testele de securitate, inseamna ca protectiile functioneaza corect.
