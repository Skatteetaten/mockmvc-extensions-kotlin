package no.skatteetaten.aurora.mockmvc.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.matches
import assertk.catch
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpMethod

class MockMvcDataTest {

    @Test
    fun `Get WireMock url containing url placeholders`() {
        val path = Path("/test/{test1}/testing/{test2}?key=value", "a", "b")
        val mockMvcData = MockMvcData(path, mockk())
        val urlPattern = mockMvcData.getWireMockUrl()

        val regex = urlPattern?.pattern?.value ?: ""
        assertThat("/test/abc123/testing/abc-123?key=value").matches(regex.toRegex())
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod::class, mode = EnumSource.Mode.EXCLUDE, names = ["HEAD", "OPTIONS", "TRACE"])
    fun `Request WireMock builder for HTTP method containing placeholder`(method: HttpMethod) {
        val path = Path("/test/{test1}?key=value", "a")
        val mockMvcData = MockMvcData(path, mockk())
        val mappingBuilder = mockMvcData.request(method)

        assertThat(mappingBuilder.build().request.urlMatcher.isRegex).isTrue()
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod::class, mode = EnumSource.Mode.EXCLUDE, names = ["HEAD", "OPTIONS", "TRACE"])
    fun `Request WireMock builder for HTTP method`(method: HttpMethod) {
        val mockMvcData = MockMvcData(Path("/test?key=value"), mockk())
        val mappingBuilder = mockMvcData.request(method)

        assertThat(mappingBuilder.build().request.urlMatcher.isRegex).isFalse()
    }

    @Test
    fun `Request WireMock builder for unsupported HTTP method`() {
        val mockMvcData = MockMvcData(Path("/test?key=value"), mockk())
        val exception = catch { mockMvcData.request(HttpMethod.TRACE) }

        assertThat(exception).isNotNull().isInstanceOf(IllegalArgumentException::class)
    }

    @Test
    fun `Get WireMock url not containing url placeholders`() {
        val mockMvcData = MockMvcData(Path("/test/testing"), mockk())
        val urlPattern = mockMvcData.getWireMockUrl()

        assertThat(urlPattern).isNull()
    }

    @Test
    fun `Get snippet name for exact path`() {
        val snippetName = MockMvcData(Path("/test"), mockk()).getSnippetName(HttpMethod.GET)
        assertThat(snippetName).isEqualTo("get-test")
    }

    @Test
    fun `Get snippet name for template path`() {
        val snippetName = MockMvcData(Path("/test/{name}/{id}"), mockk()).getSnippetName(HttpMethod.POST)
        assertThat(snippetName).isEqualTo("post-test-name-id")
    }

    @Test
    fun `Get snippet name for path with query params`() {
        val snippetName = MockMvcData(Path("/test/test123?testing=123&test=abc"), mockk()).getSnippetName(HttpMethod.PUT)
        assertThat(snippetName).isEqualTo("put-test-test123_testing=123&test=abc")
    }

}