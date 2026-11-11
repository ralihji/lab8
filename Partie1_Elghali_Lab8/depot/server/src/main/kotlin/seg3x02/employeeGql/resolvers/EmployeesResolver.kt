package seg3x02.employeeGql.resolvers

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import seg3x02.employeeGql.entity.Employee
import seg3x02.employeeGql.repository.EmployeesRepository

@Controller
class EmployeesResolver(private val employeesRepository: EmployeesRepository) {

    // Query.employees : tous les employés enregistrés dans MongoDB
    @QueryMapping
    fun employees(): List<Employee> {
        return employeesRepository.findAll()
    }
}
