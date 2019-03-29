package no.skatteetaten.aurora.mockmvc.extensions.mock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.skatteetaten.aurora.mockmvc.extensions.TestObjectMapperConfigurer
import org.mockito.BDDMockito

inline fun <reified T : Any> BDDMockito.BDDMyOngoingStubbing<T?>.withContractResponse(
    name: String,
    folder: String = "contracts",
    extension: String = "json",
    objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper,
    fn: ExtendedBDDMyOngoingStubbing<T?>.() -> BDDMockito.BDDMyOngoingStubbing<T?>
): ExtendedBDDMyOngoingStubbing<T> {
    val fileName = "/$folder/$name.$extension"
    val content = objectMapper.readValue<T>(this::class.java.getResource(fileName))

    val onGoingStubbing = fn(ExtendedBDDMyOngoingStubbing(this, content))

    return ExtendedBDDMyOngoingStubbing(onGoingStubbing, content)
}

class ExtendedBDDMyOngoingStubbing<T>(ongoingStubbing: BDDMockito.BDDMyOngoingStubbing<T?>, val content: T) :
    BDDMockito.BDDMyOngoingStubbing<T?> by ongoingStubbing {
    val mockResponse = content
}

