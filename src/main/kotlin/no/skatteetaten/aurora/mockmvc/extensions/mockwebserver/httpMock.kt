package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory

typealias MockRule = RecordedRequest.() -> MockResponse?
typealias MockFlag = RecordedRequest.() -> Boolean?

data class MockRules(
    val check: MockFlag,
    val fn: MockRule
)

class HttpMock {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val mockRules: MutableList<MockRules> = mutableListOf()

    fun start(port: Int): MockWebServer {

        return MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val matchingRule = mockRules.asSequence().mapNotNull {
                        // If there is a check and it returns true then run the request.
                        // Note that if there check here returns null it will not fire, this makes it very usable
                        if (it.check(request) == true) {

                            //If the fn functino returns null the rule will be ignored and the next rule will be evaluated
                            it.fn(request)
                        } else null
                    }.firstOrNull()

                    if (matchingRule == null) {
                        logger.debug("No matching rules matches request=;request")
                        throw IllegalArgumentException("No function matches request=$request")
                    }

                    return matchingRule
                }
            }
            start(port)
        }
    }

    fun rule(r: MockRules): HttpMock {
        mockRules.add(r)
        return this
    }

    /*
    Add a rule to this mock. If fn returns null the rule will be ignored
     */
    fun rule(fn: MockRule): HttpMock {
        mockRules.add(MockRules({ true }, fn))
        return this
    }

    /*
              Record a rule in the mock. Add an optional check as the first parameter

              If the body of the rule returns null it will be ignored.

              The ordering of the rules matter, the first one that matches will be returned
             */
    fun rule(check: MockFlag = { true }, fn: MockRule): HttpMock {
        mockRules.add(MockRules(check, fn))
        return this
    }

    companion object {
        var httpMocks: MutableList<MockWebServer> = mutableListOf()

        fun clearAllHttpMocks() {
            httpMocks.forEach {
                it.shutdown()
            }
            httpMocks = mutableListOf()
        }
    }
}

fun httpMockServer(port: String, block: HttpMock.() -> Unit = {}): MockWebServer =
    httpMockServer(port.toInt(), block)

fun httpMockServer(port: Int, block: HttpMock.() -> Unit = {}): MockWebServer {
    val instance = HttpMock()
    instance.block()
    val server = instance.start(port)
    HttpMock.httpMocks.add(server)
    return server
}

