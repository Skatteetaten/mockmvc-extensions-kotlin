package no.skatteetaten.aurora.mockmvc.extensions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.cloud.contract.wiremock.restdocs.ContractResultHandler
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

fun HttpHeaders.authorization(value: String): HttpHeaders {
    this.set(HttpHeaders.AUTHORIZATION, value)
    return this
}

fun HttpHeaders.header(key: String, value: String): HttpHeaders {
    this.set(key, value)
    return this
}

fun MockMvc.get(
    headers: HttpHeaders? = null,
    docsIdentifier: String? = null,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.GET, headers, null, urlTemplate, fn, docsIdentifier)

fun MockMvc.post(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.POST, headers, body, urlTemplate, fn, docsIdentifier)

fun MockMvc.put(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.PUT, headers, body, urlTemplate, fn, docsIdentifier)

fun MockMvc.patch(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.PATCH, headers, body, urlTemplate, fn, docsIdentifier)

fun MockMvc.delete(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.DELETE, headers, body, urlTemplate, fn, docsIdentifier)

private fun MockMvc.execute(
    method: HttpMethod,
    headers: HttpHeaders?,
    body: String?,
    urlTemplate: UrlTemplate,
    fn: (mockMvcData: MockMvcData) -> Unit,
    docsIdentifier: String?
) {
    val builder = MockMvcRequestBuilders.request(method, urlTemplate.urlString(), *urlTemplate.vars)
    headers?.let { builder.headers(it) }
    body?.let { builder.content(it) }

    val resultActions = this.perform(builder)
    val mock = MockMvcData(urlTemplate.template, resultActions)
    fn(mock)

    headers?.keys?.forEach {
        mock.andDo(WireMockRestDocs.verify().wiremock(mock.request(method).withHeader(it, matching(".+"))))
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
