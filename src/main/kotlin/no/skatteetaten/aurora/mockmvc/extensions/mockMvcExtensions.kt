package no.skatteetaten.aurora.mockmvc.extensions

import com.github.tomakehurst.wiremock.client.WireMock.matching
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
    pathBuilder: StubPathBuilder,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.GET, headers, null, pathBuilder, fn, docsIdentifier)

fun MockMvc.post(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    pathBuilder: StubPathBuilder,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.POST, headers, body, pathBuilder, fn, docsIdentifier)

fun MockMvc.put(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    pathBuilder: StubPathBuilder,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.PUT, headers, body, pathBuilder, fn, docsIdentifier)

fun MockMvc.patch(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    pathBuilder: StubPathBuilder,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.PATCH, headers, body, pathBuilder, fn, docsIdentifier)

fun MockMvc.delete(
    headers: HttpHeaders? = null,
    body: String? = null,
    docsIdentifier: String? = null,
    pathBuilder: StubPathBuilder,
    fn: (mockMvcData: MockMvcData) -> Unit
) = this.execute(HttpMethod.DELETE, headers, body, pathBuilder, fn, docsIdentifier)

private fun MockMvc.execute(
    method: HttpMethod,
    headers: HttpHeaders?,
    body: String?,
    pathBuilder: StubPathBuilder,
    fn: (mockMvcData: MockMvcData) -> Unit,
    docsIdentifier: String?
) {
    val builder = MockMvcRequestBuilders.request(method, pathBuilder.expandedUrl(), *pathBuilder.vars)
    headers?.let { builder.headers(it) }
    body?.let { builder.content(it) }

    val resultActions = this.perform(builder)
    val mock = MockMvcData(pathBuilder, resultActions)
    fn(mock)

    headers?.keys?.forEach {
        mock.andDo(
            WireMockRestDocs.verify().wiremock(
                mock.request(method).withHeader(it, matching(".+")).atPriority(pathBuilder.priority)
            )
        )
    }

    docsIdentifier?.let {
        mock.andDo(document(it))
    }
}
