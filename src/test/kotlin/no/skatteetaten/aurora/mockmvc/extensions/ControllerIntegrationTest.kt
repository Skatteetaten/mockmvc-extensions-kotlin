package no.skatteetaten.aurora.mockmvc.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.fasterxml.jackson.module.kotlin.readValue
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestController
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestObject
import no.skatteetaten.aurora.mockmvc.extensions.testutils.customObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
            statusIsOk()
                .responseHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.APPLICATION_JSON_VALUE)
                .responseJsonPath("$.value").equalsValue("test")
                .printResponseBody()
        }
    }

    @Test
    fun `Post request with json response`() {
        mockMvc.post(path = Path("/test"), body = "test123") {
            statusIsOk()
                .responseHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.APPLICATION_JSON_VALUE)
                .responseJsonPath("$.key").equalsValue("test123")
        }
    }

    @Test
    fun `Post request with request object`() {
        val result = mockMvc.post(
            path = Path("/test-with-request-object"),
            headers = HttpHeaders().contentTypeJson(),
            body = TestObject(value1 = "123", value2 = "", success = false)
        ) {
            statusIsOk()
                .responseJsonPath("$.key1").equalsValue("123")
                .responseJsonPath("$.key1").equalsValue<Long>(123)
                .responseJsonPath("$.key2").isEmpty()
                .responseJsonPath("$.success").isFalse()
        }

        val resultMap = customObjectMapper().readValue<Map<String, String>>(result.response.contentAsByteArray)
        assertThat(resultMap["key1"]).isEqualTo("123")
    }

    @Test
    fun `Put request with json response`() {
        mockMvc.put(path = Path("/test"), body = "test123") {
            statusIsOk().responseJsonPath("$.key").equalsValue("test123")
        }
    }

    @Test
    fun `Patch request with json response`() {
        mockMvc.patch(path = Path("/test"), body = "test123") {
            statusIsOk().responseJsonPath("$.key").equalsValue("test123")
        }
    }

    @Test
    fun `Delete request with json response`() {
        mockMvc.delete(path = Path("/test"), body = "test123") {
            statusIsOk()
        }
    }

    @Test
    fun `Get request with headers`() {
        mockMvc.get(
            headers = HttpHeaders().authorization("test").header("x-my-custom-header", "abc123"),
            path = Path("/{test-path}", "test-with-header")
        ) {
            statusIsOk().responseJsonPath("$.header").equalsValue("test")
        }
    }

    @Test
    fun `Get request with object response`() {
        mockMvc.get(path = Path("/test-with-object")) {
            statusIsOk()
                .responseJsonPath("$").equalsObject(TestObject())
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.value1").isNotEmpty()
                .responseJsonPath("$.value1").contains("12")
        }
    }

    @Test
    fun `Get request with rest docs identifier`() {
        val restDocsIdentifier = "get-with-restdocs"
        mockMvc.get(
            docsIdentifier = restDocsIdentifier,
            path = Path("/test")
        ) {
            statusIsOk().responseJsonPath("$.value").equalsValue("test")
        }
        val stubFileName = File("target/generated-snippets/stubs/$restDocsIdentifier.json")
        assertThat(stubFileName.isFile).isTrue()
    }

    @Test
    fun `Get request with path containing filename`() {
        mockMvc.get(Path("/test-with-filename/{filename}", "latest.properties")) {
            statusIsOk().responseJsonPath("$.value").equalsValue("latest.properties")
        }
    }

    @Test
    fun `Get request with request params`() {
        mockMvc.get(Path("/test-with-request-params?test=test123")) {
            statusIsOk().responseJsonPath("$.params").equalsValue("test123")
        }
    }

    @Test
    fun `Get request with request params and placeholders`() {
        mockMvc.get(Path("/{test}?test=test123", "test-with-request-params")) {
            statusIsOk().responseJsonPath("$.params").equalsValue("test123")
        }
    }

    @Test
    fun `Get request with request params containing underscore and slash in value`() {
        mockMvc.get(Path("/test-with-request-params?test=test_123.234/345")) {
            statusIsOk().responseJsonPath("$.params").equalsValue("test_123.234/345")
        }
    }
}