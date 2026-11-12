package seg3x02.employeeGql.resolvers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.graphql.execution.ErrorType
import org.springframework.graphql.test.tester.GraphQlTester
import seg3x02.employeeGql.entity.Employee
import seg3x02.employeeGql.repository.EmployeesRepository
import java.util.UUID

// Mutation newEmployee : schéma employee.graphqls + EmployeesResolver, avec la mutation
// exacte du client Angular (graphql-test/addEmployee.graphql). Le repository est simulé :
// les employés enregistrés sont gardés dans une liste, relue par la requête employees.
@GraphQlTest(EmployeesResolver::class)
class EmployeesMutationTests {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockBean
    private lateinit var employeesRepository: EmployeesRepository

    private val saved = mutableListOf<Employee>()

    // Variables envoyées par le client pour un formulaire valide
    private val validForm: Map<String, Any> = mapOf(
        "name" to "Amina Diallo",
        "dateOfBirth" to "1994-03-12",
        "city" to "Ottawa",
        "salary" to 72500.5,
        "gender" to "F",
        "email" to "amina.diallo@example.com"
    )

    @BeforeEach
    fun simulateMongo() {
        Mockito.doAnswer { call ->
            val employee = call.getArgument<Employee>(0)
            saved.add(employee)
            employee
        }.`when`(employeesRepository).save(Mockito.any(Employee::class.java))
        Mockito.`when`(employeesRepository.findAll()).thenAnswer { saved.toList() }
    }

    private fun addEmployee(vararg changes: Pair<String, Any>): GraphQlTester.Response {
        var request: GraphQlTester.Request<*> = graphQlTester.documentName("addEmployee")
        (validForm + changes).forEach { (name, value) -> request = request.variable(name, value) }
        return request.execute()
    }

    @Test
    fun `newEmployee saves the employee sent by the client form and returns it`() {
        val response = addEmployee()

        response.path("newEmployee").matchesJson("""
            {"name": "Amina Diallo", "dateOfBirth": "1994-03-12", "city": "Ottawa",
             "salary": 72500.5, "gender": "F", "email": "amina.diallo@example.com"}
        """)
        val id = response.path("newEmployee.id").entity(String::class.java).get()

        Mockito.verify(employeesRepository).save(Mockito.any(Employee::class.java))
        assertThat(saved).hasSize(1)
        assertThat(saved[0].id).isEqualTo(id)
        assertThat(saved[0].name).isEqualTo("Amina Diallo")
        assertThat(saved[0].salary).isEqualTo(72500.5f)
    }

    @Test
    fun `each new employee receives its own generated id`() {
        val first = addEmployee().path("newEmployee.id").entity(String::class.java).get()
        val second = addEmployee("name" to "Marc Tremblay")
            .path("newEmployee.id").entity(String::class.java).get()

        assertThat(UUID.fromString(first).toString()).isEqualTo(first)
        assertThat(UUID.fromString(second).toString()).isEqualTo(second)
        assertThat(first).isNotEqualTo(second)
        assertThat(saved.map { it.id }).containsExactly(first, second)
    }

    @Test
    fun `text is trimmed and empty optional fields are stored as null`() {
        // Gender et Email laissés vides dans le formulaire : le client envoie ""
        addEmployee("name" to "  Sofia Rossi  ", "city" to " Toronto ", "gender" to "", "email" to "")
            .path("newEmployee").matchesJson("""
                {"name": "Sofia Rossi", "city": "Toronto", "gender": null, "email": null}
            """)

        assertThat(saved[0].name).isEqualTo("Sofia Rossi")
        assertThat(saved[0].gender).isNull()
        assertThat(saved[0].email).isNull()
    }

    @ParameterizedTest(name = "{0} = \"{1}\" is rejected")
    @CsvSource(
        "name, '   '",
        "dateOfBirth, 12/03/1994",
        "dateOfBirth, 1994-02-30",
        "gender, Z",
        "email, amina.example.com"
    )
    fun `invalid values are rejected with BAD_REQUEST and nothing is saved`(field: String, value: String) {
        addEmployee(field to value)
            .errors()
            .satisfy { errors ->
                assertThat(errors).hasSize(1)
                assertThat(errors[0].errorType).isEqualTo(ErrorType.BAD_REQUEST)
                assertThat(errors[0].message).startsWith("Invalid employee: ").contains(field)
                assertThat(errors[0].path).isEqualTo("newEmployee")
            }

        Mockito.verify(employeesRepository, Mockito.never()).save(Mockito.any(Employee::class.java))
        assertThat(saved).isEmpty()
    }

    @Test
    fun `a missing required field sent outside the client is rejected`() {
        // Possible depuis GraphiQL, pas depuis le formulaire (salaire absent)
        graphQlTester.document("""
                mutation {
                  newEmployee(createEmployeeInput: {name: "Marc Tremblay", dateOfBirth: "1988-11-02", city: "Gatineau"}) {
                    id
                  }
                }
            """)
            .execute()
            .errors()
            .satisfy { errors ->
                assertThat(errors).hasSize(1)
                assertThat(errors[0].errorType).isEqualTo(ErrorType.BAD_REQUEST)
                assertThat(errors[0].message).isEqualTo("Invalid employee: salary is required.")
            }

        assertThat(saved).isEmpty()
    }

    @Test
    fun `an employee added with the mutation is then listed by the employees query`() {
        addEmployee().path("newEmployee.name").entity(String::class.java).isEqualTo("Amina Diallo")
        addEmployee("name" to "Marc Tremblay", "gender" to "M", "email" to "")
            .path("newEmployee.name").entity(String::class.java).isEqualTo("Marc Tremblay")

        graphQlTester.documentName("getEmployees")
            .execute()
            .path("employees").entityList(Any::class.java).hasSize(2)
            .path("employees[*].name").entityList(String::class.java)
            .containsExactly("Amina Diallo", "Marc Tremblay")
            .path("employees[1]").matchesJson("""{"gender": "M", "email": null}""")
    }
}
