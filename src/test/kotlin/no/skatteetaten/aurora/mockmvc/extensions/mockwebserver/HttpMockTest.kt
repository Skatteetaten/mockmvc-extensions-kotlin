package no.skatteetaten.aurora.mockmvc.extensions.mockwebserver

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.messageContains
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class HttpMocktest {

    val sithRule = MockRules({ path?.endsWith("/sith") }, { MockResponse().setBody("Darth Vader") })

    @AfterEach
    fun tearDown() {
        HttpMock.clearAllHttpMocks()
    }

    @Test
    fun `assert single rule`() {
        val server = httpMockServer(8282) {
            rule {
                MockResponse().setBody("Yoda")
            }
        }

        val response1 = RestTemplate().getForEntity<String>("${server.url}/jedi")
        assertThat(response1.body).isEqualTo("Yoda")
    }

    @Test
    fun `assert two rules`() {
        val server = httpMockServer("8181") {
            rule({ path?.endsWith("jedi") }) {
                MockResponse().setBody("Yoda")
            }

            rule(sithRule)

        }

        val response1 = RestTemplate().getForEntity<String>("${server.url}/jedi")
        val response2 = RestTemplate().getForEntity<String>("${server.url}/sith")
        assertThat(response1.body).isEqualTo("Yoda")
        assertThat(response2.body).isEqualTo("Darth Vader")
    }

    @Test
    fun `replay json test`() {
        val server = httpMockServer {
            rule {
                replayRequestJsonWithModification(
                    rootPath = "/result",
                    key = "status",
                    newValue = TextNode("Success")
                )
            }
        }

        val body = """{
           "result" : {
              "status" : "Pending"
           }
        }""".trimMargin()
        val result: JsonNode? =
            RestTemplate().postForObject<JsonNode>(server.url("/test").toString(), body, JsonNode::class.java)
        assertThat(result?.at("/result/status")?.textValue()).isEqualTo("Success")
    }

    @Test
    fun `Init httpMockServer and add rule`() {
        val httpMock = initHttpMockServer {
            rule({ path?.endsWith("sith") }) {
                MockResponse().setBody("Darth Vader")
            }
        }
        httpMock.rule({ path?.endsWith("jedi") }) {
            MockResponse().setBody("Yoda")
        }

        httpMock.executeRules {
            val response = RestTemplate().getForEntity<String>("${it.url}/jedi")
            assertThat(response.body).isEqualTo("Yoda")
        }

        httpMock.executeRules {
            val response = RestTemplate().getForEntity<String>("${it.url}/sith")
            assertThat(response.body).isEqualTo("Darth Vader")
        }

        httpMock.executeRulesAndClearMocks {
            val response1 = RestTemplate().getForEntity<String>("${it.url}/jedi")
            val response2 = RestTemplate().getForEntity<String>("${it.url}/sith")
            assertThat(response1.body).isEqualTo("Yoda")
            assertThat(response2.body).isEqualTo("Darth Vader")
        }
    }

    @Test
    fun `Multiple different rules`() {
        val server = httpMockServer(8283) {
            rule({ path?.endsWith("sith") }) {
                MockResponse().setResponseCode(404)
            }

            rule({ path?.endsWith("jedi") }) {
                MockResponse().setBody("Yoda")
            }
        }

        assertThat { RestTemplate().getForEntity<String>("${server.url}/sith") }.isFailure().messageContains("404")
        val response = RestTemplate().getForEntity<String>("${server.url}/jedi")
        assertThat(response.body).isEqualTo("Yoda")
    }
}