package no.skatteetaten.aurora.mockmvc.extensions

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.matches
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MockMvcDataTest {

    @Test
    fun `Get WireMock url containing url placeholders`() {
        val path = "/test/{test1}/testing/{test2}"
        val mockMvcData = MockMvcData(path, mockk())
        val urlPattern = mockMvcData.getWireMockUrl()

        val regex = urlPattern?.pattern?.value ?: ""
        assertThat(regex).isEqualTo("/test/.+/testing/.+")
        assertThat(path).matches(Regex(regex))
    }

    @Test
    fun `Get WireMock url not containing url placeholders`() {
        val mockMvcData = MockMvcData("/test/testing", mockk())
        val urlPattern = mockMvcData.getWireMockUrl()

        assertThat(urlPattern).isNull()
    }
}