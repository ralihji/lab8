# SEG3502 — Lab 8 — Serveur GraphQL

Application Spring Boot/Kotlin de départ pour le laboratoire 8.

Le projet contient déjà :

- l'entité `Employee`;
- `EmployeesRepository`;
- `CreateEmployeeInput`;
- la configuration MongoDB;
- la configuration Web/CORS/Security.

Les parties à compléter pour le livrable sont notamment :

```text
src/main/resources/graphql/employee.graphqls
src/main/kotlin/seg3x02/employeeGql/resolvers/EmployeesResolver.kt
```

Démarrage :

```bash
./gradlew bootRun
```

Sous Windows :

```text
gradlew.bat bootRun
```
