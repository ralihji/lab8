package seg3x02.employeeGql

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// La page http://localhost:9000/graphiql doit être celle de src/main/resources/graphiql,
// qui charge GraphiQL 3 avec une version fixée (celle du jar ne s'affiche plus).
@SpringBootTest
@AutoConfigureMockMvc
class GraphiQlPageTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `graphiql page loads GraphiQL 3 with pinned versions`() {
        mockMvc.perform(get("/graphiql").param("path", "/graphql"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("https://unpkg.com/graphiql@3.9.0/graphiql.min.js")))
            .andExpect(content().string(not(containsString("https://unpkg.com/graphiql/graphiql.min.js"))))
    }
}
