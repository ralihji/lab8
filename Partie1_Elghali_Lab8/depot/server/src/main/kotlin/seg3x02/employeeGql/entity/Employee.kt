package seg3x02.employeeGql.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "employee")
data class Employee(
        val name: String,
        val dateOfBirth: String,
        val city: String,
        val salary: Float,
        val gender: String?,
        val email: String?
) {
    @Id
    var id: String = ""
}
