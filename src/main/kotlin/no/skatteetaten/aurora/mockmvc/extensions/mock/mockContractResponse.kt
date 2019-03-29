package no.skatteetaten.aurora.mockmvc.extensions.mock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.skatteetaten.aurora.mockmvc.extensions.TestObjectMapperConfigurer
import org.mockito.BDDMockito

inline fun <reified T : Any> ObjectMapper.getClassPathFileContent(
    folder: String,
    name: String,
    extension: String
): T {
    val fileName = "/$folder/$name.$extension"
    return this.readValue(this::class.java.getResource(fileName))
}

inline fun <reified T : Any> BDDMockito.BDDMyOngoingStubbing<T>.withContractResponse(
    name: String,
    folder: String = "contracts",
    extension: String = "json",
    objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper,
    fn: ExtendedBDDMyOngoingStubbing<T>.() -> BDDMockito.BDDMyOngoingStubbing<T>
): ExtendedBDDMyOngoingStubbing<T> {
    val content = objectMapper.getClassPathFileContent<T>(folder, name, extension)
    val onGoingStubbing = fn(ExtendedBDDMyOngoingStubbing(this, content))
    return ExtendedBDDMyOngoingStubbing(onGoingStubbing, content)
}

class ExtendedBDDMyOngoingStubbing<T>(ongoingStubbing: BDDMockito.BDDMyOngoingStubbing<T>, val content: T) :
    BDDMockito.BDDMyOngoingStubbing<T> by ongoingStubbing {
    val mockResponse = content
}

inline fun <reified T : Any> BDDMockito.BDDMyOngoingStubbing<T?>.withNullableContractResponse(
    name: String,
    folder: String = "contracts",
    extension: String = "json",
    objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper,
    fn: ExtendedNullableBDDMyOngoingStubbing<T?>.() -> BDDMockito.BDDMyOngoingStubbing<T?>
): ExtendedNullableBDDMyOngoingStubbing<T> {
    val content = objectMapper.getClassPathFileContent<T>(folder, name, extension)
    val onGoingStubbing = fn(ExtendedNullableBDDMyOngoingStubbing(this, content))
    return ExtendedNullableBDDMyOngoingStubbing(onGoingStubbing, content)
}

class ExtendedNullableBDDMyOngoingStubbing<T>(ongoingStubbing: BDDMockito.BDDMyOngoingStubbing<T?>, val content: T) :
    BDDMockito.BDDMyOngoingStubbing<T?> by ongoingStubbing {
    val mockResponse = content
}

