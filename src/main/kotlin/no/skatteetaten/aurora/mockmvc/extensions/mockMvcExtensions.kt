package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
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
    val builder = MockMvcRequestBuilders
        .request(method, path.url, *path.vars)
        .addHeaders(headers)
        .addBody(body)

    val resultActions = this.perform(builder)
    val mock = MockMvcData(path, resultActions)
    fn(mock)

    mock.setupWireMock(headers, method)
        .addDocumentation(docsIdentifier)
}

private fun MockHttpServletRequestBuilder.addHeaders(headers: HttpHeaders?): MockHttpServletRequestBuilder {
    headers?.let { this.headers(it) }
    return this
}

private fun MockHttpServletRequestBuilder.addBody(body: Any?): MockHttpServletRequestBuilder {
    body?.let {
        val jsonString = if (it is String) {
            it
        } else {
            jacksonObjectMapper().writeValueAsString(it)
        }

        this.content(jsonString)
    }
    return this
}