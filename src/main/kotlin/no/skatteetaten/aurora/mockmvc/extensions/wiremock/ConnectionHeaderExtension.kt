package no.skatteetaten.aurora.mockmvc.extensions.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockExtensions
import org.springframework.http.HttpHeaders.CONNECTION

class MockMvcWireMockExtensions : WireMockExtensions {
    override fun extensions() = mutableListOf(ConnectionHeaderTransformer())
}

class ConnectionHeaderTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response =
        Response.Builder.like(response)
            .headers(HttpHeaders.copyOf(response?.headers).plus(HttpHeader(CONNECTION, "Close")))
            .build()

    override fun getName() = this::class.simpleName
}