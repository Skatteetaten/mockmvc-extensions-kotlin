package no.skatteetaten.aurora.mockmvc.extensions

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.matches
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpMethod

class MockMvcDataTest {

    @Test
    fun `Get WireMock url containing url placeholders`() {
        val template = PathTemplate("/test/{test1}/testing/{test2}?key=value", "a", "b", priority = 2)
        val mockMvcData = MockMvcData(template, mockk())
        val urlPattern = mockMvcData.getWireMockUrl()

        val regex = urlPattern?.pattern?.value ?: ""
        assertThat("/test/abc123/testing/abc-123?key=value").matches(regex.toRegex())
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod::class, mode = EnumSource.Mode.EXCLUDE, names = ["HEAD", "OPTIONS", "TRACE"])
    fun `Request WireMock builder for HTTP method containing placeholder`(method: HttpMethod) {
        val template = PathTemplate("/test/{test1}?key=value", "a")
        val mockMvcData = MockMvcData(template, mockk())
        val mappingBuilder = mockMvcData.request(method)

        assertThat(mappingBuilder.build().request.urlMatcher.isRegex).isTrue()
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod::class, mode = EnumSource.Mode.EXCLUDE, names = ["HEAD", "OPTIONS", "TRACE"])
    fun `Request WireMock builder for HTTP method`(method: HttpMethod) {
        val mockMvcData = MockMvcData(ExactPath("/test?key=value"), mockk())
        val mappingBuilder = mockMvcData.request(method)

        assertThat(mappingBuilder.build().request.urlMatcher.isRegex).isFalse()
    }

    @Test
    fun `Get WireMock url not containing url placeholders`() {
        val mockMvcData = MockMvcData(ExactPath("/test/testing"), mockk())
        val urlPattern = mockMvcData.getWireMockUrl()

        assertThat(urlPattern).isNull()
    }
}