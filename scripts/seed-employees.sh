#!/usr/bin/env bash
# Ajoute 4 employés d'exemple dans MongoDB (base books-mongo, collection employee),
# la base et la collection lues par le serveur GraphQL (application.properties).
#
# Utilisation, à la racine du dépôt, MongoDB démarré :
#   bash scripts/seed-employees.sh
#
# Le script passe par le conteneur Docker "mongodb" du laboratoire (docker exec + mongosh).
# Sans Docker, il utilise mongosh s'il est installé. Variables possibles :
#   MONGO_CONTAINER=nom_du_conteneur   MONGO_URI=mongodb://localhost:27017/books-mongo
# Il peut être relancé : les 4 employés d'exemple sont remplacés, les autres restent.

set -euo pipefail

CONTAINER="${MONGO_CONTAINER:-mongodb}"
URI="${MONGO_URI:-mongodb://localhost:27017/books-mongo}"

read -r -d '' SEED_JS <<'EOF' || true
const cls = "seg3x02.employeeGql.entity.Employee";
const employees = [
  { _id: "seed-employee-1", name: "Amina Diallo", dateOfBirth: "1994-03-12", city: "Ottawa",
    salary: Double(72500.5), gender: "F", email: "amina.diallo@example.com", _class: cls },
  { _id: "seed-employee-2", name: "Marc Tremblay", dateOfBirth: "1988-11-02", city: "Gatineau",
    salary: Double(64000), gender: "M", email: "marc.tremblay@example.com", _class: cls },
  { _id: "seed-employee-3", name: "Sofia Rossi", dateOfBirth: "2000-07-25", city: "Toronto",
    salary: Double(58250.25), gender: "X", email: null, _class: cls },
  { _id: "seed-employee-4", name: "Koredo Tablanga", dateOfBirth: "1979-01-30", city: "Montreal",
    salary: Double(91000), gender: null, email: "koredo.tablanga@example.com", _class: cls }
];
const ids = employees.map(e => e._id);
db.employee.deleteMany({ _id: { $in: ids } });
db.employee.insertMany(employees);
print("Employes d'exemple ajoutes : " + employees.map(e => e.name).join(", "));
print("Total dans la collection employee : " + db.employee.countDocuments());
EOF

if command -v docker >/dev/null 2>&1 \
   && [ "$(docker inspect -f '{{.State.Running}}' "$CONTAINER" 2>/dev/null)" = "true" ]; then
  echo "MongoDB : conteneur Docker \"$CONTAINER\""
  docker exec "$CONTAINER" mongosh --quiet books-mongo --eval "$SEED_JS"
elif command -v mongosh >/dev/null 2>&1; then
  echo "MongoDB : mongosh local ($URI)"
  mongosh --quiet "$URI" --eval "$SEED_JS"
else
  echo "Erreur : aucun conteneur Docker \"$CONTAINER\" démarré et mongosh introuvable." >&2
  echo "Démarrer MongoDB : docker run -d -p 27017:27017 --name mongodb mongo" >&2
  exit 1
fi
