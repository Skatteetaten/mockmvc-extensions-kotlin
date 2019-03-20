package no.skatteetaten.aurora.mockmvc.extensions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.ResultActions
import org.springframework.web.util.UriComponentsBuilder

data class MockMvcData(val path: Path, val results: ResultActions) : ResultActions by results {
    private val containsPlaceholder = Regex(pattern = "\\{.+?}")
    private val requestUrl = path.url

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

class Path(
    val url: String,
    vararg val vars: String
) {
    val priority = if (vars.isEmpty()) 1 else 2

    fun expandedUrl() =
        if (vars.isEmpty()) {
            url
        } else {
            UriComponentsBuilder.fromUriString(url).buildAndExpand(*vars).encode().toUri().toString()
        }
}