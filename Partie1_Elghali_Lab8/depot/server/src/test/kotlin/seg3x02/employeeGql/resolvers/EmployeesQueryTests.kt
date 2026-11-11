package seg3x02.employeeGql.resolvers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.graphql.test.tester.GraphQlTester
import seg3x02.employeeGql.entity.Employee
import seg3x02.employeeGql.repository.EmployeesRepository

// Requête employees : schéma employee.graphqls + EmployeesResolver.
// MongoDB n'est pas nécessaire : le repository est simulé avec Mockito.
@GraphQlTest(EmployeesResolver::class)
class EmployeesQueryTests {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockBean
    private lateinit var employeesRepository: EmployeesRepository

    private fun employee(id: String, name: String, dateOfBirth: String, city: String,
                         salary: Float, gender: String?, email: String?): Employee {
        val employee = Employee(name, dateOfBirth, city, salary, gender, email)
        employee.id = id
        return employee
    }

    @Test
    fun `employees returns an empty list when no employee is stored`() {
        Mockito.`when`(employeesRepository.findAll()).thenReturn(emptyList<Employee>())

        graphQlTester.documentName("getEmployees")
            .execute()
            .path("employees").entityList(Any::class.java).hasSize(0)
    }

    @Test
    fun `employees answers the client query with every requested field`() {
        Mockito.`when`(employeesRepository.findAll()).thenReturn(listOf(
            employee("e1", "Amina Diallo", "1994-03-12", "Ottawa", 72500.5f, "F", "amina.diallo@example.com"),
            employee("e2", "Marc Tremblay", "1988-11-02", "Gatineau", 64000f, "M", "marc.tremblay@example.com")
        ))

        graphQlTester.documentName("getEmployees")
            .execute()
            .path("employees").entityList(Any::class.java).hasSize(2)
            .path("employees[0].name").entity(String::class.java).isEqualTo("Amina Diallo")
            .path("employees[1].name").entity(String::class.java).isEqualTo("Marc Tremblay")
            .path("employees").matchesJson("""
                [
                  {"id": "e1", "name": "Amina Diallo", "dateOfBirth": "1994-03-12", "city": "Ottawa",
                   "salary": 72500.5, "gender": "F", "email": "amina.diallo@example.com"},
                  {"id": "e2", "name": "Marc Tremblay", "dateOfBirth": "1988-11-02", "city": "Gatineau",
                   "salary": 64000.0, "gender": "M", "email": "marc.tremblay@example.com"}
                ]
            """)
        Mockito.verify(employeesRepository).findAll()
    }

    @Test
    fun `optional gender and email are returned as null when they are not set`() {
        Mockito.`when`(employeesRepository.findAll()).thenReturn(listOf(
            employee("e3", "Sofia Rossi", "2000-07-25", "Toronto", 58250.25f, null, null)
        ))

        graphQlTester.documentName("getEmployees")
            .execute()
            .path("employees[0]").matchesJson("""
                {"id": "e3", "name": "Sofia Rossi", "salary": 58250.25, "gender": null, "email": null}
            """)
    }

    @Test
    fun `schema exposes the Employee fields and types used by the client`() {
        graphQlTester.document("""
                {
                  __type(name: "Employee") {
                    fields { name type { kind name ofType { name } } }
                  }
                }
            """)
            .execute()
            .path("__type.fields").entityList(Any::class.java).hasSize(7)
            .path("__type.fields").matchesJson("""
                [
                  {"name": "id",          "type": {"kind": "NON_NULL", "ofType": {"name": "ID"}}},
                  {"name": "name",        "type": {"kind": "NON_NULL", "ofType": {"name": "String"}}},
                  {"name": "dateOfBirth", "type": {"kind": "NON_NULL", "ofType": {"name": "String"}}},
                  {"name": "city",        "type": {"kind": "NON_NULL", "ofType": {"name": "String"}}},
                  {"name": "salary",      "type": {"kind": "NON_NULL", "ofType": {"name": "Float"}}},
                  {"name": "gender",      "type": {"kind": "SCALAR", "name": "String"}},
                  {"name": "email",       "type": {"kind": "SCALAR", "name": "String"}}
                ]
            """)
    }

    @Test
    fun `a field that is not in the schema is rejected without reading MongoDB`() {
        graphQlTester.document("{ employees { id salaryy } }")
            .execute()
            .errors()
            .satisfy { errors ->
                assertThat(errors).hasSize(1)
                assertThat(errors[0].message).contains("salaryy")
            }
        Mockito.verifyNoInteractions(employeesRepository)
    }
}
