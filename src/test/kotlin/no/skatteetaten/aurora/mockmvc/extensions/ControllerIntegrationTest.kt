package no.skatteetaten.aurora.mockmvc.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestController
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestObject
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
import java.io.File

@EnableWebMvc
@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(classes = [TestController::class])
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class ControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Get request with json response`() {
        mockMvc.get(path = Path("/{test-path}", "test")) {
            it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
        }
    }

    @Test
    fun `Post request with json response`() {
        mockMvc.post(path = Path("/test"), body = "test123") {
            it.statusIsOk().responseJsonPath("$.key").equalsValue("test123")
        }
    }

    @Test
    fun `Post request with request object`() {
        mockMvc.post(
            path = Path("/test-with-request-object"),
            headers = HttpHeaders().contentType(),
            body = TestObject()
        ) {
            it.statusIsOk().responseJsonPath("$.key").equalsValue("123")
        }
    }

    @Test
    fun `Put request with json response`() {
        mockMvc.put(path = Path("/test"), body = "test123") {
            it.statusIsOk().responseJsonPath("$.key").equalsValue("test123")
        }
    }

    @Test
    fun `Patch request with json response`() {
        mockMvc.patch(path = Path("/test"), body = "test123") {
            it.statusIsOk().responseJsonPath("$.key").equalsValue("test123")
        }
    }

    @Test
    fun `Delete request with json response`() {
        mockMvc.delete(path = Path("/test"), body = "test123") {
            it.statusIsOk()
        }
    }

    @Test
    fun `Get request with headers`() {
        mockMvc.get(
            headers = HttpHeaders().authorization("test").header("x-my-custom-header", "abc123"),
            path = Path("/{test-path}", "test-with-header")
        ) {
            it.statusIsOk().responseJsonPath("$.header").equalsValue("test")
        }
    }

    @Test
    fun `Get request with object response`() {
        mockMvc.get(path = Path("/test-with-object")) {
            it.statusIsOk().responseJsonPath("$").equalsObject(TestObject())
        }
    }

    @Test
    fun `Get request with rest docs`() {
        val restDocsIdentifier = "get-with-restdocs"
        mockMvc.get(
            docsIdentifier = restDocsIdentifier,
            path = Path("/test")
        ) {
            it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
        }

        val stubFileName = File("target/generated-snippets/stubs").listFiles().first().name
        assertThat("$restDocsIdentifier.json").isEqualTo(stubFileName)
    }
}