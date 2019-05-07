package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

fun ResultActions.status(expected: HttpStatus): ResultActions =
    this.andExpect(status().`is`(expected.value()))

fun ResultActions.statusIsOk(): ResultActions =
    this.andExpect(status().isOk)

data class JsonPathEquals(val expression: String, val resultActions: ResultActions) {
    fun equalsValue(value: Any): ResultActions = resultActions.andExpect(jsonPath(expression, Matchers.equalTo(value)))

    fun equalsObject(
        expected: Any,
        objectMapper: ObjectMapper = TestObjectMapperConfigurer.objectMapper
    ): ResultActions {
        val expectedValue = objectMapper.convertValue<LinkedHashMap<String, *>>(expected)
        return resultActions.andExpect {
            val response = JsonPath.read<LinkedHashMap<String, *>>(it.response.contentAsString, expression)
            Assertions.assertEquals(expectedValue, response)
        }
    }

    fun contains(expected: String): ResultActions =
        resultActions.andExpect(jsonPath(expression, Matchers.containsString(expected)))

    fun isEmpty(): ResultActions = resultActions.andExpect(jsonPath(expression).isEmpty)
    fun isNotEmpty(): ResultActions = resultActions.andExpect(jsonPath(expression).isNotEmpty)
    fun isTrue(): ResultActions = resultActions.andExpect(jsonPath(expression).value(true))
    fun isFalse(): ResultActions = resultActions.andExpect(jsonPath(expression).value(false))
}

data class HeaderEquals(val name: String, val resultActions: ResultActions) {
    fun equals(value: String): ResultActions = resultActions.andExpect(header().string(name, Matchers.equalTo(value)))
    fun startsWith(value: String): ResultActions =
        resultActions.andExpect(header().string(name, Matchers.startsWith(value)))
}

fun ResultActions.responseJsonPath(jsonPath: String = "$") = JsonPathEquals(jsonPath, this)
fun ResultActions.responseHeader(name: String) = HeaderEquals(name, this)
fun ResultActions.printResponseBody() = println(this.andReturn().response.contentAsString)