package no.skatteetaten.aurora.mockmvc.extensions

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

fun ResultActions.status(expected: HttpStatus): ResultActions =
    this.andExpect(MockMvcResultMatchers.status().`is`(expected.value()))

fun ResultActions.statusIsOk(): ResultActions =
    this.andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.OK.value()))

data class JsonPathEquals(val expression: String, val resultActions: ResultActions) {
    fun equalsValue(value: Any): ResultActions {
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(expression, Matchers.equalTo(value)))
        return resultActions
    }

    fun equalsObject(expected: Any): ResultActions {
        val expectedValue = jacksonObjectMapper().convertValue<LinkedHashMap<String, *>>(expected)
        resultActions.andExpect {
            val response = JsonPath.read<LinkedHashMap<String, *>>(it.response.contentAsString, expression)
            Assertions.assertEquals(expectedValue, response)
        }
        return resultActions
    }
}

fun ResultActions.responseJsonPath(jsonPath: String) = JsonPathEquals(jsonPath, this)