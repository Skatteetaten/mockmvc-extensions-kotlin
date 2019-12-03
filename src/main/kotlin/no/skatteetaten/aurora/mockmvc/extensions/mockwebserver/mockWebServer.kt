package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import assertk.Assert
import assertk.assertThat
import assertk.assertions.support.expected
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.JsonPath
import no.skatteetaten.aurora.mockmvc.extensions.TestObjectMapperConfigurer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import java.util.concurrent.TimeUnit

fun responseWithBody(body: String) = MockResponse().setBody(body)

fun MockWebServer.enqueueJson(vararg responses: MockResponse) {
    responses.forEach {
        it.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        this.enqueue(it)
    }
}

fun MockWebServer.assert(timeoutInMs: Long = 500): Assert<List<RecordedRequest>> {
    val requests = mutableListOf<RecordedRequest>()
    do {
        val request = this.takeRequest(timeoutInMs, TimeUnit.MILLISECONDS)?.let {
            requests.add(it)
        }
    } while (request != null)
    return assertThat(requests)
}

fun Assert<List<RecordedRequest>>.containsRequest(method: HttpMethod, path: String): Assert<List<RecordedRequest>> =
    transform { requests ->
        if (requests.any { it.method == method.name && it.path == path }) {
            requests
        } else {
            expected("${method.name} request with $path but was $requests")
        }
    }

private fun MockWebServer.enqueueJson(status: Int = 200, body: Any, objectMapper: ObjectMapper) {
    val json = body as? String ?: objectMapper.writeValueAsString(body)
    val response = MockResponse()
        .setResponseCode(status)
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(json)
    this.enqueue(response)
}

fun MockWebServer.execute(
    vararg responses: MockResponse,
    timeoutInMs: Long = 3000,
    fn: () -> Unit
): List<RecordedRequest?> {
    fun takeRequests() =
        (1..responses.size).toList().map { this.takeRequest(timeoutInMs, TimeUnit.MILLISECONDS) }

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
    timeoutInMs: Long = 3000,
    fn: () -> Unit
): List<RecordedRequest?> {
    fun takeRequests() =
        (1..responses.size).toList().map { this.takeRequest(timeoutInMs, TimeUnit.MILLISECONDS) }

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
    timeoutInMs: Long = 3000,
    fn: () -> Unit
): List<RecordedRequest?> {
    fun takeRequests() =
        (1..responses.size).toList().map { this.takeRequest(timeoutInMs, TimeUnit.MILLISECONDS) }

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
    this.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
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

fun RecordedRequest.replayRequestJsonWithModification(
    rootPath: String,
    key: String,
    newValue: JsonNode
): MockResponse {
    val ad: JsonNode = jacksonObjectMapper().readTree(this.bodyAsString())
    (ad.at(rootPath) as ObjectNode).replace(key, newValue)

    return MockResponse()
        .setResponseCode(200)
        .setBody(jacksonObjectMapper().writeValueAsString(ad))
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
}

val MockWebServer.url: String
    get() = this.url("/").toString()
