package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import assertk.assertThat
import assertk.assertions.isEqualTo
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

class MockWebServerTest {

    private val server = MockWebServer()
    private val url = server.url("/")

    @Test
    fun `Test execute with MockResponse`() {
        val request = server.execute(MockResponse().setBody("test")) {
            val response = RestTemplate().getForEntity<String>(url.toString())
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isEqualTo("test")
        }
        assertThat(request.path).isEqualTo("/")
    }
}