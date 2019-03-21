package no.skatteetaten.aurora.mockmvc.extensions.mock

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestObject
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock

open class MockMe {
    open fun getTestObject() = TestObject()
}

class MockTest {

    @Test
    fun `Return object from json file in mock`() {
        val mock = mock(MockMe::class.java)
        val testObject = given(mock.getTestObject())
            .withContractResponse("test-response") {
                willReturn(content)
            }.mockResponse

        val mockedTestObject = mock.getTestObject()

        assertThat(mockedTestObject).isEqualTo(testObject)
    }
}