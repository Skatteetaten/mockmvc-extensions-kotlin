package no.skatteetaten.aurora.mockmvc.extensions.testutils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TestObject(val value1: String = "123", val value2: String = "abc")

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class TestController {

    @GetMapping("/test")
    fun getTest() = """{ "value": "test" }"""

    @GetMapping("/test-with-header")
    fun getTestWithHeader(@RequestHeader(value = HttpHeaders.AUTHORIZATION) authorization: String) =
        """{ "header": "$authorization" }"""

    @GetMapping("/test-with-object")
    fun getTestWithObject(): String = jacksonObjectMapper().writeValueAsString(TestObject())

    @PostMapping("/test")
    fun postTest(@RequestBody value: String) = """{ "value": "test" }"""

    @PutMapping("/test")
    fun putTest(@RequestBody value: String) = """{ "value": "test" }"""

    @PatchMapping("/test")
    fun patchTest(@RequestBody value: String) = """{ "value": "test" }"""

    @DeleteMapping("/test")
    fun deleteTest(@RequestBody value: String) {
    }
}