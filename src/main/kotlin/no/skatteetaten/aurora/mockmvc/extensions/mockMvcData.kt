package no.skatteetaten.aurora.mockmvc.extensions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.ResultActions
import org.springframework.web.util.UriComponentsBuilder

data class MockMvcData(val path: Path, val results: ResultActions) : ResultActions by results {
    private val placeholder = Regex(pattern = "\\{.+?}")
    private val uriComponents = UriComponentsBuilder.fromUriString(path.url).build()

    fun request(method: HttpMethod): MappingBuilder {
        val url = getWireMockUrl()
        val wiremock = when (method) {
            HttpMethod.GET -> WireMock.get(url)
            HttpMethod.POST -> WireMock.post(url)
            HttpMethod.PUT -> WireMock.put(url)
            HttpMethod.PATCH -> WireMock.patch(url)
            HttpMethod.DELETE -> WireMock.delete(url)
            else -> throw IllegalArgumentException("MockMvc extensions does not support ${method.name}")
        }

        val queryParams = uriComponents.queryParams.mapValues { RegexPattern("[\\w-\\.]+") }
        return wiremock.withQueryParams(queryParams)
    }

    fun getWireMockUrl() = UrlPattern(
        RegexPattern(
            uriComponents.path!!.replace(placeholder, Regex.escapeReplacement("[\\w-\\.]+"))
        ), true
    )

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
        path.url.replace("/", "-")
            .replace("?", "_")
            .replace("-_", "_")
            .replace(Regex("[{}]"), "")
            .removeSuffix("-")
}

class Path(
    val url: String,
    vararg val vars: String,
    val priority: Int = if (vars.isEmpty()) 1 else 2
)