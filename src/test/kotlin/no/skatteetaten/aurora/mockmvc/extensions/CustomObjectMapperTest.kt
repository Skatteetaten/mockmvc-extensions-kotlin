package no.skatteetaten.aurora.mockmvc.extensions

import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestController
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TimeObject
import no.skatteetaten.aurora.mockmvc.extensions.testutils.customObjectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@EnableWebMvc
@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(classes = [TestController::class])
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class CustomObjectMapperTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    init {
        TestObjectMapperConfigurer.objectMapper = customObjectMapper()
    }

    @AfterAll
    fun tearDown() {
        TestObjectMapperConfigurer.reset()
    }

    @Test
    fun `Post and receive data using custom ObjectMapper`() {
        val timeObject = TimeObject()
        mockMvc.post(
            path = Path("/custom-object-mapper"),
            headers = HttpHeaders().contentType(),
            body = timeObject
        ) {
            statusIsOk().responseJsonPath("$").equalsObject(timeObject)
        }
    }
}