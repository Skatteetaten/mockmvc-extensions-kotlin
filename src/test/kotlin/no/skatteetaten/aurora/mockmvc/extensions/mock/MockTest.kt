package no.skatteetaten.aurora.mockmvc.extensions.mock

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.skatteetaten.aurora.mockmvc.extensions.testutils.TestObject
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock

open class MockMe {
    open fun get() = TestObject()
}

class MockTest {

    @Test
    fun `Return object from json file in mock`() {
        val mock = mock(MockMe::class.java)
        given(mock.get()).willReturnContractResponse("test-response")

        val testObject = mock.get()

        assertThat(testObject.value1).isEqualTo("test123")
        assertThat(testObject.value2).isEqualTo("abc123")
    }
}