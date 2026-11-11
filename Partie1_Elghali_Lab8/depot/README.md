# SEG3502 — Laboratoire 8 — Code de départ

Ce dossier contient les deux parties nécessaires au laboratoire 8 :

- `client/` : application Angular Employee déjà configurée pour communiquer avec GraphQL;
- `server/` : application Spring Boot/Kotlin + MongoDB à compléter.

Le code source fourni correspond au code de départ du laboratoire. La solution du livrable n'est pas incluse.

## 1. Démarrer MongoDB

Le serveur utilise MongoDB sur le port `27017`.

Exemple avec Docker :

```bash
docker run -d -p 27017:27017 --name mongodb mongo
```

## 2. Démarrer le serveur GraphQL

Le projet serveur utilise Java 21.

```bash
cd server
./gradlew bootRun
```

Sous Windows :

```text
gradlew.bat bootRun
```

Le serveur démarre sur le port `9000`.

GraphiQL :

```text
http://localhost:9000/graphiql
```

Point de terminaison GraphQL :

```text
http://localhost:9000/graphql
```

## 3. Démarrer le client Angular

Dans un deuxième terminal :

```bash
cd client
npm install
npm start
```

Puis ouvrez :

```text
http://localhost:4200
```

## Travail demandé

Le client Angular est déjà fourni et contient les opérations GraphQL dont il a besoin.

Dans le serveur, complétez uniquement l'implémentation nécessaire pour que ce client fonctionne,
notamment :

- `src/main/resources/graphql/employee.graphqls`
- `src/main/kotlin/seg3x02/employeeGql/resolvers/EmployeesResolver.kt`

Suivez les consignes du laboratoire 8 pour définir le schéma GraphQL et les résolveurs nécessaires.

Aucun accès à GitHub Classroom n'est requis pour utiliser ce code de départ.
