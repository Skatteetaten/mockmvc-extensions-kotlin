package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

fun HttpHeaders.authorization(value: String): HttpHeaders {
    this.set(HttpHeaders.AUTHORIZATION, value)
    return this
}

fun HttpHeaders.contentType(contentType: String = MediaType.APPLICATION_JSON_VALUE): HttpHeaders {
    this.set(HttpHeaders.CONTENT_TYPE, contentType)
    return this
}

fun HttpHeaders.header(key: String, value: String): HttpHeaders {
    this.set(key, value)
    return this
}

fun MockMvc.get(
    headers: HttpHeaders? = null,
    docsIdentifier: String? = null,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.GET, headers, null, path, fn, docsIdentifier)

fun MockMvc.post(
    headers: HttpHeaders? = null,
    body: Any? = null,
    docsIdentifier: String? = null,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.POST, headers, body, path, fn, docsIdentifier)

fun MockMvc.put(
    headers: HttpHeaders? = null,
    body: Any? = null,
    docsIdentifier: String? = null,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.PUT, headers, body, path, fn, docsIdentifier)

fun MockMvc.patch(
    headers: HttpHeaders? = null,
    body: Any? = null,
    docsIdentifier: String? = null,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.PATCH, headers, body, path, fn, docsIdentifier)

fun MockMvc.delete(
    headers: HttpHeaders? = null,
    body: Any? = null,
    docsIdentifier: String? = null,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.DELETE, headers, body, path, fn, docsIdentifier)

private fun MockMvc.execute(
    method: HttpMethod,
    headers: HttpHeaders?,
    body: Any?,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit,
    docsIdentifier: String?
) {
    val builder = MockMvcRequestBuilders.request(method, path.expandedUrl(), *path.vars)
    headers?.let { builder.headers(it) }
    body?.let {
        val jsonString = if (it is String) {
            it
        } else {
            jacksonObjectMapper().writeValueAsString(it)
        }

        builder.content(jsonString)
    }

    val resultActions = this.perform(builder)
    val mock = MockMvcData(path, resultActions)
    fn(mock)

    val mappingBuilder = mock.request(method)
    headers?.keys?.forEach {
        mappingBuilder.withHeader(it, matching(".+"))
    }
    mock.andDo(
        WireMockRestDocs.verify().wiremock(
            mappingBuilder.atPriority(path.priority)
        )
    )

    docsIdentifier?.let {
        mock.andDo(document(it))
    }
}
