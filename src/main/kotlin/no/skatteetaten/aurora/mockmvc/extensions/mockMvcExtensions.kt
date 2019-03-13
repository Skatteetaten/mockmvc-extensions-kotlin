package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.cloud.contract.wiremock.restdocs.ContractResultHandler
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.util.UriComponentsBuilder

fun HttpHeaders.authorization(value: String): HttpHeaders {
    this.set(HttpHeaders.AUTHORIZATION, value)
    return this
}

data class MockMvcData(val requestUrl: String, val results: ResultActions) : ResultActions by results {
    private val containsPlaceholder = Regex(pattern = "\\{.+?}")

    fun get(): MappingBuilder = getWireMockUrl()?.let { WireMock.get(it) } ?: WireMock.get(requestUrl)

    fun getWireMockUrl(): UrlPattern? =
        if (requestUrl.contains(containsPlaceholder)) {
            UrlPattern(RegexPattern(requestUrl.replace(containsPlaceholder, ".+")), true)
        } else {
            null
        }
}

class UrlTemplate(val template: String, vararg val vars: String) {
    fun urlString() = UriComponentsBuilder.fromUriString(template).buildAndExpand(*vars).encode().toUri().toString()
}

fun MockMvc.get(
    headers: HttpHeaders? = null,
    docsIdentifier: String? = null,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit
) {
    val builder = MockMvcRequestBuilders.get(urlTemplate.urlString(), *urlTemplate.vars)
    headers?.let { builder.headers(it) }

    val resultActions = this.perform(builder)
    val mock = MockMvcData(urlTemplate.template, resultActions)
    fn(mock)

    headers?.keys?.forEach {
        mock.andDo(WireMockRestDocs.verify().wiremock(mock.get().withHeader(it, matching(".+"))))
    }

    docsIdentifier?.let {
        mock.andDo(document(it))
    }
}

fun ContractResultHandler.get(mockMvcData: MockMvcData): MappingBuilder? {
    val get = WireMock.get(mockMvcData.requestUrl)
    this.wiremock(get)
    return get
}

fun ResultActions.status(expected: HttpStatus): ResultActions = this.andExpect(status().`is`(expected.value()))
fun ResultActions.statusIsOk(): ResultActions = this.andExpect(status().`is`(HttpStatus.OK.value()))

data class JsonPathEquals(val expression: String, val resultActions: ResultActions) {
    fun equalsValue(value: Any): ResultActions {
        resultActions.andExpect(jsonPath(expression, equalTo(value)))
        return resultActions
    }

    fun equalsObject(expected: Any): ResultActions {
        val expectedValue = jacksonObjectMapper().convertValue<LinkedHashMap<String, *>>(expected)
        resultActions.andExpect {
            val response = JsonPath.read<LinkedHashMap<String, *>>(it.response.contentAsString, expression)
            assertEquals(expectedValue, response)
        }
        return resultActions
    }
}

fun ResultActions.responseJsonPath(jsonPath: String) = JsonPathEquals(jsonPath, this)
