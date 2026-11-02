# sapins-nowel

Spring Boot 4.1.1 and Java 17 practice project: a REST API that sells decorated Christmas trees, backed by Postgres.

## Run

```bash
cp .env.example .env
docker compose up -d --wait
./mvnw spring-boot:run
```

In IntelliJ, run `SapinsNowelApplication` from the project root so `.env` is found.

The `dev` profile seeds three decorations (ids 1 to 3) and one client (id 1) when those tables are empty.

## Test

```bash
./mvnw test
```

These run on in-memory H2, so they don't need Docker.

Import `postman/sapins-nowel.postman_collection.json` into Postman and run the collection with the app running.

## Routes

| Verb | Route | Params | Returns |
| --- | --- | --- | --- |
| GET | `/sapin/get` | `id` | `Sapin`, or empty |
| POST | `/sapin/create` | | number of sapins in DB |
| POST | `/sapin/addDecoration` | `idSapin`, `idDecoration` | `boolean` |
| POST | `/sapin/vente` | `idSapin`, `idClient` | `BonDeCommande`, or empty |
| GET | `/sapin/commande/get` | `id` | `BonDeCommande`, or empty |
