package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import assertk.assertThat
import assertk.assertions.isEqualTo
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

data class TestObject(val value: String)

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MockWebServerTest {

    private val server = MockWebServer()
    private val url = server.url("/")

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `Test execute with MockResponse`() {
        val request = server.execute(MockResponse().setBody("test")) {
            val response = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isEqualTo("test")
        }
        assertThat(request.path).isEqualTo("/")
    }

    @Test
    fun `Test execute with status and and response object`() {
        val request = server.execute(201, TestObject("test")) {
            val response = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(response.body).isEqualTo("""{"value":"test"}""")
        }
        assertThat(request.path).isEqualTo("/")
    }

    @Test
    fun `Test execute with response object`() {
        val request = server.execute(TestObject("test")) {
            val response = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isEqualTo("""{"value":"test"}""")
        }
        assertThat(request.path).isEqualTo("/")
    }

    @Test
    fun `Test execute with vararg response objects`() {
        val requests = server.execute(TestObject("test"), TestObject("test")) {
            val response1 = RestTemplate().getForEntity<String>(url.toString())
            val response2 = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response1.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response2.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response1.body).isEqualTo("""{"value":"test"}""")
            assertThat(response2.body).isEqualTo("""{"value":"test"}""")
        }
        assertThat(requests.size).isEqualTo(2)
        assertThat(requests[0].path).isEqualTo("/")
        assertThat(requests[1].path).isEqualTo("/")
    }
}