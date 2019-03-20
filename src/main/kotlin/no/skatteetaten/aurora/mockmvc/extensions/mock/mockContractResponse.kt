package no.skatteetaten.aurora.mockmvc.extensions.mock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.mockito.BDDMockito

inline fun <reified T : Any> BDDMockito.BDDMyOngoingStubbing<T?>.willReturnContractResponse(
    name: String,
    folder: String = "contracts",
    extension: String = "json",
    objectMapper: ObjectMapper = jacksonObjectMapper()
): BDDMockito.BDDMyOngoingStubbing<T?>? {
    val fileName = "/$folder/$name.$extension"
    val content = objectMapper.readValue<T?>(this::class.java.getResource(fileName))
    return this.willReturn(content)
}
