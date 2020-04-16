package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import mu.KotlinLogging
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

typealias MockRule = RecordedRequest.() -> MockResponse?
typealias MockFlag = RecordedRequest.() -> Boolean?

data class MockRules(
    val check: MockFlag,
    val fn: MockRule,
    val id: String? = null
)

private val logger = KotlinLogging.logger {}

class HttpMock {

    val mockRules: MutableList<MockRules> = mutableListOf()
    var server: MockWebServer? = null

    fun start(port: Int? = null) = MockWebServer().apply {
        dispatcher = createDispatcher()
        port?.let { start(port) } ?: start()
    }.also {
        server = it
    }

    fun init() = MockWebServer().apply {
        dispatcher = createDispatcher()
    }.also {
        server = it
    }

    private fun createDispatcher() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            logger.trace {
                "Dispatcher called for ${request.path}, mockRules size: ${mockRules.size}"
            }
            val matchingRule = mockRules.asSequence().mapNotNull {
                // If there is a check and it returns true then run the request.
                // Note that if there check here returns null it will not fire, this makes it very usable
                if (it.check(request) == true) {

                    //If the fn function returns null the rule will be ignored and the next rule will be evaluated
                    it.fn(request)
                } else null
            }.firstOrNull()

            if (matchingRule == null) {
                logger.debug("No matching rules matches request=$request")
                throw IllegalArgumentException("No function matches request=$request")
            }

            return matchingRule
        }
    }

    fun rulePathEndsWith(endsWith: String, fn: MockRule): HttpMock {
        mockRules.add(MockRules({ path?.endsWith(endsWith) }, fn, endsWith))
        return this
    }

    fun rulePathContains(contains: String, fn: MockRule): HttpMock {
        mockRules.add(MockRules({ path?.contains(contains) }, fn, contains))
        return this
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

    fun rule(check: MockFlag = { true }, id: String, fn: MockRule): HttpMock {
        mockRules.add(MockRules(check, fn, id))
        return this
    }

    /*
    Executes the added rules.
    It is important to note that this function will not clear the list of HttpMocks.
     */
    fun executeRules(port: Int? = null, fn: (server: MockWebServer) -> Unit) {
        kotlin.runCatching {
            port?.let { server!!.start(port) } ?: server!!.start()
        }
        fn(server!!)
    }

    /*
    Executes the added rules and clears the HttpMocks.
     */
    fun executeRulesAndClearMocks(port: Int? = null, fn: (server: MockWebServer) -> Unit) {
        kotlin.runCatching {
            port?.let { server!!.start(port) } ?: server!!.start()
        }
        fn(server!!)
        clearAllHttpMocks()
    }

    companion object {
        var httpMocks: MutableList<MockWebServer> = mutableListOf()

        fun clearAllHttpMocks() {
            httpMocks.forEach {
                kotlin.runCatching {
                    it.shutdown()
                }
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

fun httpMockServer(block: HttpMock.() -> Unit = {}): MockWebServer {
    val instance = HttpMock()
    instance.block()
    val server = instance.start()
    HttpMock.httpMocks.add(server)
    return server
}

fun initHttpMockServer(block: HttpMock.() -> Unit = {}): HttpMock {
    val instance = HttpMock()
    instance.block()
    val server = instance.init()
    HttpMock.httpMocks.add(server)
    return instance
}
