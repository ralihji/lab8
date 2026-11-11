# Lab 8 SEG3502 · Partie de Hjiyej (commit 1 : lecture)

Ta partie crée le dépôt et fait fonctionner la **lecture** : la requête GraphQL `employees`,
qui affiche la liste des employés dans le client Angular. Brayan fera ensuite le commit 2
(mutation `newEmployee`, validation, README et rapport) **par dessus le tien**.

Le dossier `depot/` contient déjà tout : le code de départ du lab + ta partie.

## Ce que contient ta partie

| Fichier | Rôle |
|---|---|
| `server/src/main/resources/graphql/employee.graphqls` | Schéma : `Query.employees` et type `Employee` |
| `server/src/main/kotlin/seg3x02/employeeGql/resolvers/EmployeesResolver.kt` | `@QueryMapping employees()` : `findAll()` sur MongoDB |
| `server/src/main/resources/graphiql/index.html` | Page GraphiQL réparée (sans elle, `localhost:9000/graphiql` reste sur « Loading... ») |
| `server/src/test/kotlin/.../resolvers/EmployeesQueryTests.kt` | 5 tests de la requête du client |
| `server/src/test/kotlin/.../GraphiQlPageTests.kt` | 1 test de la page GraphiQL |
| `server/src/test/resources/graphql-test/getEmployees.graphql` | Copie de la requête du client, utilisée par les tests |
| `scripts/seed-employees.sh` | Ajoute 4 employés d'exemple dans MongoDB |
| `client/.npmrc` | Sans lui, `npm install` échoue (erreur ERESOLVE) |
| `.gitignore`, `.gitattributes` | Fichiers ignorés ; fins de ligne des scripts (pour Git Bash sous Windows) |

## Étapes

1. **Créer le dépôt sur GitHub**, **vide** : sans README, sans .gitignore, sans licence.
   Nom proposé : `lab8`.

2. **Ajouter Brayan comme collaborateur** pour qu'il puisse faire son commit :
   Settings > Collaborators > Add people > `DevPro1772005`.

3. **Faire le commit 1** : dans un terminal (Git Bash sous Windows), à partir du dossier extrait :

   ```bash
   cd depot
   git init
   git add -A
   git update-index --chmod=+x server/gradlew scripts/seed-employees.sh
   git commit -m "Lab 8 partie 1 (lecture) : requete employees, GraphiQL, tests et donnees d'exemple"
   git branch -M main
   git remote add origin https://github.com/<ton_compte>/lab8.git
   git push -u origin main
   ```

   La ligne `git update-index` garde `gradlew` et le script exécutables pour ceux qui clonent
   sous Linux ou macOS.

4. **Envoyer le lien du dépôt à Brayan.**

## Vérifier (facultatif)

```bash
cd server
./gradlew test        # Windows : gradlew.bat test  -> 7 tests réussis (MongoDB pas nécessaire)
```

Avec MongoDB (`docker run -d -p 27017:27017 --name mongodb mongo`), le serveur
(`./gradlew bootRun`) et le client (`cd client`, `npm install`, `npm start`) : la page
**Employee List** de `http://localhost:4200` affiche la liste (lancer `bash scripts/seed-employees.sh`
pour avoir 4 employés). Le bouton **Submit** du formulaire n'enregistre rien encore : c'est
normal, la mutation est dans la partie de Brayan.

## Important

- Après le commit, **ne modifie pas** `employee.graphqls` ni `EmployeesResolver.kt` :
  le commit de Brayan remplace ces deux fichiers par leur version complète.
- Si `npm install` modifie `client/package-lock.json`, ne le commit pas
  (`git checkout -- client/package-lock.json`). Les versions ne changent pas, seulement des métadonnées.
- Java 21 est nécessaire pour le serveur (`java -version`).
