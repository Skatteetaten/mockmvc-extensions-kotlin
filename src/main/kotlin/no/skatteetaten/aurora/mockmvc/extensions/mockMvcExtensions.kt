package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.jayway.jsonpath.JsonPath
import org.springframework.cloud.contract.wiremock.restdocs.ContractResultHandler
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.web.util.UriComponentsBuilder
import org.hamcrest.Matchers.equalTo
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

fun HttpHeaders.authorization(value: String): HttpHeaders {
    this.set(HttpHeaders.AUTHORIZATION, value)
    return this
}

data class MockMvcData(val requestUrl: String, val results: ResultActions) : ResultActions by results {
    fun get() = WireMock.get(requestUrl)!!
}

fun MockMvc.get(
    headers: HttpHeaders? = null,
    docsIdentifier: String,
    urlTemplate: String,
    vararg uriVars: String,
    fn: (mockMvcData: MockMvcData) -> Unit
) {
    val url = UriComponentsBuilder.fromUriString(urlTemplate).buildAndExpand(*uriVars).encode().toUri()
    val builder = MockMvcRequestBuilders.get(urlTemplate, *uriVars)
    headers?.let { builder.headers(it) }

    val resultActions = this.perform(builder)
    val mock = MockMvcData(url.toString(), resultActions)
    fn(mock)

    headers?.keys?.forEach {
        mock.andDo(WireMockRestDocs.verify().wiremock(mock.get().withHeader(it, matching(".+"))))
    }
    mock.andDo(document(docsIdentifier))
}

fun ContractResultHandler.get(mockMvcData: MockMvcData): MappingBuilder? {
    val get = WireMock.get(mockMvcData.requestUrl)
    this.wiremock(get)
    return get
}

fun ResultActions.status(expected: HttpStatus) = this.andExpect(status().`is`(expected.value()))

data class JsonPathEquals(val expression: String, val resultActions: ResultActions) {
    fun equalsValue(value: Any): ResultActions {
        resultActions.andExpect(jsonPath(expression, equalTo(value)))
        return resultActions
    }

    fun equalsObject(expected: Any): ResultActions {
        val expectedValue = jacksonObjectMapper().convertValue<LinkedHashMap<String, *>>(expected)
        resultActions.andExpect {
            val response = JsonPath.read<LinkedHashMap<String, *>>(it.response.contentAsString, expression)
            // assertThat(response).isEqualTo(expectedValue)
        }
        return resultActions
    }
}

fun ResultActions.responseJsonPath(jsonPath: String) = JsonPathEquals(jsonPath, this)