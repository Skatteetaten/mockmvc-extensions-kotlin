package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.jayway.jsonpath.JsonPath
import no.skatteetaten.aurora.mockmvc.extensions.TestObjectMapperConfigurer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

private fun MockWebServer.enqueueJson(status: Int = 200, body: Any, objectMapper: ObjectMapper) {
    val json = body as? String ?: objectMapper.writeValueAsString(body)
    val response = MockResponse()
        .setResponseCode(status)
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
        .setBody(json)
    this.enqueue(response)
}

fun MockWebServer.execute(vararg responses: MockResponse, fn: () -> Unit): List<RecordedRequest> {
    fun takeRequests() = (1..responses.size).toList().map { this.takeRequest() }

    try {
        responses.forEach { this.enqueue(it) }
        fn()
        return takeRequests()
    } catch (t: Throwable) {
        takeRequests()
        throw t
    }
}

fun MockWebServer.execute(
    vararg responses: Pair<Int, Any>,
    objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper,
    fn: () -> Unit
): List<RecordedRequest> {
    fun takeRequests() = (1..responses.size).toList().map { this.takeRequest() }

    try {
        responses.forEach { this.enqueueJson(status = it.first, body = it.second, objectMapper = objectMapper) }
        fn()
        return takeRequests()
    } catch (t: Throwable) {
        takeRequests()
        throw t
    }
}

fun MockWebServer.execute(
    vararg responses: Any,
    objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper,
    fn: () -> Unit
): List<RecordedRequest> {
    fun takeRequests() = (1..responses.size).toList().map { this.takeRequest() }

    try {
        responses.forEach { this.enqueueJson(body = it, objectMapper = objectMapper) }
        fn()
        return takeRequests()
    } catch (t: Throwable) {
        takeRequests()
        throw t
    }
}

fun MockResponse.setJsonFileAsBody(fileName: String): MockResponse {
    val classPath = ClassPathResource("/$fileName")
    val json = classPath.file.readText()
    this.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
    return this.setBody(json)
}

inline fun <reified T> RecordedRequest.bodyAsObject(
    path: String = "$",
    objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper
): T {
    val content: Any = JsonPath.parse(String(body.readByteArray())).read(path)
    return objectMapper.convertValue(content)
}

fun RecordedRequest.bodyAsString(): String = this.body.readUtf8()
