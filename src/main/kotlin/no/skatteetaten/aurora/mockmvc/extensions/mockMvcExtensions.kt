package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import javax.servlet.http.Cookie

fun HttpHeaders.authorization(value: String): HttpHeaders {
    this.set(HttpHeaders.AUTHORIZATION, value)
    return this
}

fun HttpHeaders.contentType(contentType: String = MediaType.APPLICATION_JSON_VALUE): HttpHeaders {
    this.set(HttpHeaders.CONTENT_TYPE, contentType)
    return this
}

fun HttpHeaders.contentTypeJson() = contentType()

fun HttpHeaders.header(key: String, value: String): HttpHeaders {
    this.set(key, value)
    return this
}

fun MockMvc.get(
    path: Path,
    headers: HttpHeaders? = null,
    cookies: Array<Cookie>? = null,
    docsIdentifier: String? = null,
    fn: MockMvcData.() -> Unit
) = this.execute(HttpMethod.GET, headers, cookies, null, path, fn, docsIdentifier)

fun MockMvc.post(
    path: Path,
    body: Any? = null,
    headers: HttpHeaders? = null,
    cookies: Array<Cookie>? = null,
    docsIdentifier: String? = null,
    fn: MockMvcData.() -> Unit
) = this.execute(HttpMethod.POST, headers, cookies, body, path, fn, docsIdentifier)

fun MockMvc.put(
    path: Path,
    body: Any? = null,
    headers: HttpHeaders? = null,
    cookies: Array<Cookie>? = null,
    docsIdentifier: String? = null,
    fn: MockMvcData.() -> Unit
) = this.execute(HttpMethod.PUT, headers, cookies, body, path, fn, docsIdentifier)

fun MockMvc.patch(
    path: Path,
    body: Any? = null,
    headers: HttpHeaders? = null,
    cookies: Array<Cookie>? = null,
    docsIdentifier: String? = null,
    fn: MockMvcData.() -> Unit
) = this.execute(HttpMethod.PATCH, headers, cookies, body, path, fn, docsIdentifier)

fun MockMvc.delete(
    path: Path,
    body: Any? = null,
    headers: HttpHeaders? = null,
    cookies: Array<Cookie>? = null,
    docsIdentifier: String? = null,
    fn: MockMvcData.() -> Unit
) = this.execute(HttpMethod.DELETE, headers, cookies, body, path, fn, docsIdentifier)

private fun MockMvc.execute(
    method: HttpMethod,
    headers: HttpHeaders?,
    cookies: Array<Cookie>?,
    body: Any?,
    path: Path,
    fn: (mockMvcData: MockMvcData) -> Unit,
    docsIdentifier: String?
): MvcResult {
    val builder = MockMvcRequestBuilders
        .request(method, path.url, *path.vars)
        .addHeaders(headers)
        .addCookies(cookies)
        .addBody(body)

    val resultActions = this.perform(builder)
    val mock = MockMvcData(path, resultActions)
    fn(mock)

    return mock.setupWireMock(headers, method)
        .addDocumentation(method, docsIdentifier)
        .andReturn()
}

private fun MockHttpServletRequestBuilder.addHeaders(headers: HttpHeaders?) =
    headers?.let { this.headers(it) } ?: this

private fun MockHttpServletRequestBuilder.addCookies(cookies: Array<Cookie>?) =
    cookies?.let { this.cookie(*cookies) } ?: this

private fun MockHttpServletRequestBuilder.addBody(body: Any?) =
    body?.let {
        val jsonString = if (it is String) {
            it
        } else {
            TestObjectMapperConfigurer.objectMapper.writeValueAsString(it)
        }

        this.content(jsonString)
    } ?: this

object TestObjectMapperConfigurer {
    var objectMapper: ObjectMapper = jacksonObjectMapper()

    fun reset() {
        this.objectMapper = jacksonObjectMapper()
    }
}