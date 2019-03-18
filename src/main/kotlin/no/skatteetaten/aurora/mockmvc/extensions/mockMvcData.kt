package no.skatteetaten.aurora.mockmvc.extensions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.ResultActions
import org.springframework.web.util.UriComponentsBuilder

data class MockMvcData(val pathBuilder: StubPathBuilder, val results: ResultActions) : ResultActions by results {
    private val containsPlaceholder = Regex(pattern = "\\{.+?}")
    private val requestUrl = pathBuilder.url

    fun get(): MappingBuilder = getWireMockUrl()?.let { WireMock.get(it) } ?: WireMock.get(requestUrl)

    fun request(method: HttpMethod): MappingBuilder =
        when (method) {
            HttpMethod.GET -> getWireMockUrl()?.let { WireMock.get(it) } ?: WireMock.get(requestUrl)
            HttpMethod.POST -> getWireMockUrl()?.let { WireMock.post(it) } ?: WireMock.post(requestUrl)
            HttpMethod.PUT -> getWireMockUrl()?.let { WireMock.put(it) } ?: WireMock.put(requestUrl)
            HttpMethod.PATCH -> getWireMockUrl()?.let { WireMock.patch(it) } ?: WireMock.patch(
                UrlPattern(AnythingPattern(requestUrl), false)
            )
            HttpMethod.DELETE -> getWireMockUrl()?.let { WireMock.delete(it) } ?: WireMock.delete(requestUrl)
            else -> throw IllegalArgumentException("MockMvc extensions does not support ${method.name}")
        }

    fun getWireMockUrl(): UrlPattern? =
        if (requestUrl.contains(containsPlaceholder)) {
            UrlPattern(
                RegexPattern(
                    requestUrl.replace(containsPlaceholder, Regex.escapeReplacement("[\\w-]+"))
                        .replace("?", "\\?")
                ), true
            )
        } else {
            null
        }
}

interface StubPathBuilder {
    val url: String
    val vars: Array<out String>
        get() = emptyArray()

    val priority: Int
    fun expandedUrl(): String
}

class ExactPath(override val url: String, override val priority: Int = 1) : StubPathBuilder {
    override fun expandedUrl() = url
}

class PathTemplate(
    override val url: String,
    override vararg val vars: String,
    override val priority: Int = 2
) : StubPathBuilder {
    override fun expandedUrl() =
        UriComponentsBuilder.fromUriString(url).buildAndExpand(*vars).encode().toUri().toString()
}