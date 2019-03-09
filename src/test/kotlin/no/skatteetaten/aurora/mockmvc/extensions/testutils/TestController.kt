package no.skatteetaten.aurora.mockmvc.extensions.testutils

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController() {

    @GetMapping("/test")
    fun getTest() : String = "Hello world"

}