package seg3x02.employeeGql.resolvers

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.execution.ErrorType
import org.springframework.stereotype.Controller
import seg3x02.employeeGql.entity.Employee
import seg3x02.employeeGql.repository.EmployeesRepository
import seg3x02.employeeGql.resolvers.types.CreateEmployeeInput
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

@Controller
class EmployeesResolver(private val employeesRepository: EmployeesRepository) {

    // Query.employees : tous les employés enregistrés dans MongoDB
    @QueryMapping
    fun employees(): List<Employee> {
        return employeesRepository.findAll()
    }

    // Mutation.newEmployee : enregistre l'employé saisi dans le formulaire du client
    // et le renvoie avec son id (le client l'ajoute alors à sa liste).
    @MutationMapping
    fun newEmployee(@Argument("createEmployeeInput") input: CreateEmployeeInput): Employee {
        val employee = Employee(
            requiredText(input.name, "name"),
            requiredDate(input.dateOfBirth),
            requiredText(input.city, "city"),
            input.salary ?: throw InvalidEmployeeInputException("salary is required."),
            optionalGender(input.gender),
            optionalEmail(input.email)
        )
        // Comme newBook dans l'exemple du laboratoire : l'id est créé ici.
        // Avec l'id vide par défaut, chaque ajout remplacerait le précédent dans MongoDB.
        employee.id = UUID.randomUUID().toString()
        employeesRepository.save(employee)
        return employee
    }

    // Une entrée invalide donne une erreur GraphQL BAD_REQUEST avec un message clair
    // (au lieu de l'erreur INTERNAL_ERROR par défaut), et rien n'est enregistré.
    @GraphQlExceptionHandler
    fun handleInvalidInput(ex: InvalidEmployeeInputException, env: DataFetchingEnvironment): GraphQLError {
        return GraphqlErrorBuilder.newError(env)
            .errorType(ErrorType.BAD_REQUEST)
            .message("Invalid employee: " + ex.message)
            .build()
    }

    // Les règles reprennent celles du formulaire Angular (employee.component.ts) :
    // nom, date de naissance, ville et salaire obligatoires, genre M, F ou X, courriel valide.
    private fun requiredText(value: String?, field: String): String {
        val text = value?.trim()
        if (text.isNullOrEmpty()) {
            throw InvalidEmployeeInputException("$field is required.")
        }
        return text
    }

    // <input type="date"> envoie la date au format yyyy-MM-dd ; la liste l'affiche avec DatePipe
    private fun requiredDate(value: String?): String {
        val date = requiredText(value, "dateOfBirth")
        try {
            LocalDate.parse(date)
        } catch (e: DateTimeParseException) {
            throw InvalidEmployeeInputException("dateOfBirth must be a valid date (yyyy-MM-dd).")
        }
        return date
    }

    // Champ facultatif laissé vide dans le formulaire ("") : enregistré comme null
    private fun optionalText(value: String?): String? {
        val text = value?.trim()
        return if (text.isNullOrEmpty()) null else text
    }

    private fun optionalGender(value: String?): String? {
        val gender = optionalText(value) ?: return null
        if (gender !in GENDERS) {
            throw InvalidEmployeeInputException("gender must be M, F or X.")
        }
        return gender
    }

    private fun optionalEmail(value: String?): String? {
        val email = optionalText(value) ?: return null
        if (!EMAIL.matches(email)) {
            throw InvalidEmployeeInputException("email is not a valid address.")
        }
        return email
    }

    companion object {
        private val GENDERS = setOf("M", "F", "X")
        private val EMAIL = Regex("""^[^\s@]+@[^\s@]+$""")
    }
}

// Entrée refusée par newEmployee : champ obligatoire absent ou format invalide
class InvalidEmployeeInputException(message: String) : RuntimeException(message)
