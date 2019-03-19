package no.skatteetaten.aurora.mockmvc.extensions.wiremock

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Response
import org.junit.jupiter.api.Test

class MockMvcWireMockExtensionsTest {

    @Test
    fun `Add Connection header to WireMock response`() {
        val response = Response(
            200,
            "",
            "",
            HttpHeaders(HttpHeader.httpHeader("Authorization", "Bearer test")),
            false,
            null,
            0,
            null,
            false
        )

        val headerTransformer = MockMvcWireMockExtensions().extensions()[0]
        val returnedResponse = headerTransformer.transform(null, response, null, null)

        assertThat(returnedResponse.headers.size()).isEqualTo(2)
        assertThat(returnedResponse.headers.getHeader("Authorization").isPresent).isTrue()
        assertThat(returnedResponse.headers.getHeader("Connection").isPresent).isTrue()
    }
}