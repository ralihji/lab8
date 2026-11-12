# SEG3502 · Laboratoire 8 · Services Web GraphQL

Serveur GraphQL (Spring Boot, Kotlin, MongoDB) complété pour que le client Angular
« employés » fourni fonctionne : liste des employés et ajout d'un employé par formulaire.
Le client n'a pas été modifié (sauf `client/.npmrc`, voir les remarques).

Rapport : [`docs/Rapport_Lab8_SEG3502_Adou_Elghali.pdf`](docs/Rapport_Lab8_SEG3502_Adou_Elghali.pdf)

## Équipe

| Nom | Numéro | Partie |
|---|---|---|
| Hjiyej Andaloussi Elghali | 300379897 | Lecture (commit 1) : dépôt, requête `employees`, page GraphiQL, tests, données d'exemple |
| Brayan Adou | 300433616 | Écriture (commit 2) : mutation `newEmployee`, validation, tests, README et rapport |

## Travail réalisé

Le client (`client/src/app/service/employee.service.ts`) envoie deux opérations au serveur
`http://localhost:9000/graphql`. Le schéma reprend exactement leurs noms, arguments et champs.

| Opération du client | Schéma (`employee.graphqls`) | Résolveur (`EmployeesResolver.kt`) |
|---|---|---|
| `GET_EMPLOYEES` (liste) | `Query.employees: [Employee]` | `@QueryMapping employees()` : `findAll()` |
| `ADD_EMPLOYEE` (formulaire) | `Mutation.newEmployee(createEmployeeInput: CreateEmployeeInput!): Employee!` | `@MutationMapping newEmployee()` : validation, id UUID, `save()` |

- `newEmployee` crée l'id avec `UUID.randomUUID()` (comme `newBook` dans l'exemple du laboratoire).
  Sans cela, l'id vide par défaut ferait remplacer l'employé précédent à chaque ajout.
- Les règles du formulaire sont aussi vérifiées par le serveur : nom, date de naissance, ville et
  salaire obligatoires, date au format `yyyy-MM-dd`, genre `M`, `F` ou `X`, courriel valide.
  Une entrée invalide donne une erreur GraphQL `BAD_REQUEST` (`@GraphQlExceptionHandler`) et rien
  n'est enregistré. Un genre ou un courriel laissé vide (`""`) est enregistré comme `null`.

## Structure

```text
client/                         Application Angular fournie (Apollo)
server/
  src/main/resources/graphql/employee.graphqls          Schéma GraphQL
  src/main/kotlin/seg3x02/employeeGql/resolvers/EmployeesResolver.kt
  src/main/resources/graphiql/index.html                Page GraphiQL (versions fixées)
  src/test/kotlin/...                                    17 tests (JUnit 5, GraphQlTester)
  src/test/resources/graphql-test/                      Requêtes copiées du client
scripts/seed-employees.sh       4 employés d'exemple dans MongoDB
docs/                           Rapport PDF et captures d'écran
```

## Prérequis

- Docker (MongoDB).
- Java 21 : `java -version` (le projet utilise une toolchain Java 21).
- Node.js 18.19+, 20.11+ ou 22+ : `node -v` (Angular 18).
- Git (Git Bash sous Windows pour le script de données).

## Installer et exécuter

1. Cloner le dépôt, puis ouvrir son dossier.

2. Démarrer MongoDB (port 27017) :

   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo
   ```

   Les fois suivantes : `docker start mongodb`.

3. Démarrer le serveur GraphQL (port 9000) :

   ```bash
   cd server
   ./gradlew bootRun        # Windows : gradlew.bat bootRun
   ```

   Point de terminaison : `http://localhost:9000/graphql` · GraphiQL : `http://localhost:9000/graphiql`

4. Facultatif, à la racine du dépôt : ajouter 4 employés d'exemple.

   ```bash
   bash scripts/seed-employees.sh
   ```

5. Démarrer le client Angular (port 4200), dans un autre terminal :

   ```bash
   cd client
   npm install
   npm start
   ```

6. Ouvrir `http://localhost:4200/`. Le bouton **Employee List** affiche la liste, **Add Employee**
   le formulaire ; après **Submit**, le nouvel employé apparaît dans la liste.
   Arrêter avec `Ctrl + C` dans chaque terminal, puis `docker stop mongodb`.

## Tests

```bash
cd server
./gradlew test           # 17 tests, MongoDB n'est pas nécessaire
```

Les tests utilisent le vrai schéma et le vrai résolveur (`@GraphQlTest`, `GraphQlTester`) avec les
requêtes du client ; le repository MongoDB est simulé (`@MockBean`). Rapport :
`server/build/reports/tests/test/index.html`.

## Exemples GraphiQL

```graphql
{
  employees { id name city salary }
}
```

```graphql
mutation {
  newEmployee(createEmployeeInput: {name: "Lucas Martin", dateOfBirth: "1991-09-04",
                                    city: "Kingston", salary: 59900.5, gender: "M", email: ""}) {
    id name dateOfBirth city salary gender email
  }
}
```

## Remarques

- Ouvrir le client avec `localhost`, pas `127.0.0.1` : le serveur n'accepte que l'origine
  `http://localhost:4200` (CORS, `WebConfiguration.kt`).
- `client/.npmrc` contient `legacy-peer-deps=true` : `apollo-angular` 5.0.2 (version du code de
  départ) déclare Angular 14 à 16, alors que le client utilise Angular 18, et `npm install`
  s'arrêtait sinon avec l'erreur `ERESOLVE`.
- La page GraphiQL de Spring for GraphQL 1.3.1 charge `graphiql` sans version depuis unpkg ; depuis
  GraphiQL 4 (mai 2025), ce fichier n'existe plus et la page restait sur « Loading... ».
  `server/src/main/resources/graphiql/index.html` charge GraphiQL 3.9.0.
