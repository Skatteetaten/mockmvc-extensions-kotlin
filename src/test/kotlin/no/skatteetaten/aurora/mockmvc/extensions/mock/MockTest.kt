package no.skatteetaten.aurora.mockmvc.extensions.mock

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestObject
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock

open class MockMe {
    open fun getTestObject() = TestObject()
    open fun getNullObject(): TestObject? = null
}

class MockTest {
    private val mock = mock(MockMe::class.java)

    @Test
    fun `Return object from json file in mock`() {
        val testObject = given(mock.getTestObject())
            .withContractResponse("test-response") {
                willReturn(content)
            }.mockResponse

        val mockedTestObject = mock.getTestObject()

        assertThat(mockedTestObject).isEqualTo(testObject)
    }

    @Test
    fun `Return object from json file in mock when optional return type`() {
        val testObject = given(mock.getNullObject())
            .withContractResponse("test-response") {
                willReturn(content)
            }.mockResponse

        val mockedTestObject = mock.getNullObject()

        assertThat(mockedTestObject).isEqualTo(testObject)
    }
}