package no.skatteetaten.aurora.mockmvc.extensions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.ResultActions

data class MockMvcData(val path: Path, val results: ResultActions) : ResultActions by results {
    private val placeholder = Regex(pattern = "\\{.+?}")
    private val requestUrl = path.url

    fun request(method: HttpMethod): MappingBuilder {
        val url = getWireMockUrl()
        return when (method) {
            HttpMethod.GET -> url?.let { WireMock.get(it) } ?: WireMock.get(requestUrl)
            HttpMethod.POST -> url?.let { WireMock.post(it) } ?: WireMock.post(requestUrl)
            HttpMethod.PUT -> url?.let { WireMock.put(it) } ?: WireMock.put(requestUrl)
            HttpMethod.PATCH -> url?.let { WireMock.patch(it) }
                ?: WireMock.patch(UrlPattern(AnythingPattern(requestUrl), false))
            HttpMethod.DELETE -> url?.let { WireMock.delete(it) } ?: WireMock.delete(requestUrl)
            else -> throw IllegalArgumentException("MockMvc extensions does not support ${method.name}")
        }
    }

    fun getWireMockUrl(): UrlPattern? =
        if (requestUrl.contains(placeholder)) {
            UrlPattern(
                RegexPattern(
                    requestUrl
                        .replace(placeholder, Regex.escapeReplacement("[\\w-]+"))
                        .replace("?", "\\?")
                ), true
            )
        } else {
            null
        }

    fun setupWireMock(headers: HttpHeaders?, method: HttpMethod): MockMvcData {
        val mappingBuilder = this.request(method)

        headers?.keys?.forEach {
            mappingBuilder.withHeader(it, WireMock.matching(".+"))
        }
        val resultActions = this.andDo(WireMockRestDocs.verify().wiremock(mappingBuilder.atPriority(path.priority)))
        return this.copy(results = resultActions)
    }

    fun addDocumentation(method: HttpMethod, docsIdentifier: String?): MockMvcData {
        val snippetName = docsIdentifier ?: getSnippetName(method)
        return this.copy(results = this.andDo(document(snippetName)))
    }

    fun getSnippetName(method: HttpMethod) = method.name.toLowerCase() +
        path.url.replace("/", "-").replace("?", "_").replace(Regex("[{}]"), "")
}

class Path(
    val url: String,
    vararg val vars: String,
    val priority: Int = if (vars.isEmpty()) 1 else 2
)