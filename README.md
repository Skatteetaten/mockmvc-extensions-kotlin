# MockMvc extensions Kotlin

This project was created to have one source, the unit test, for documentation and contracts.
It simplifies the setup of `MockMvc` tests that generates [restdocs](https://spring.io/projects/spring-restdocs)
and wiremock stubs using [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract).

For examples look at the [unit tests](https://github.com/Skatteetaten/mockmvc-extensions-kotlin/blob/master/src/test/kotlin/no/skatteetaten/aurora/mockmvc/extensions/ControllerIntegrationTest.kt).

## Usage

```
mockMvc.get(
  headers = HttpHeaders().authorization("test"),
  docsIdentifier = "my-docs",
  urlTemplate = UrlTemplate("/test")
) {
   it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
}
```

---

There are also convenience methods for asserting with JsonPath.  
* *Equals value:* `it.statusIsOk().responseJsonPath("$.value").equalsValue("test")`
* *Equals object:* `it.statusIsOk().responseJsonPath("$").equalsObject(TestObject())`

## Rest docs

To generate rest docs add `@AutoConfigureRestDocs` to your unit test class and include the `docsIdentifier`:
```
mockMvc.get(docsIdentifier = "my-docs", urlTemplate = UrlTemplate("/test")) { ... }
```

This will by default generate the restdocs snippets in `<target/build>/generated-snippets/<docsIdentifier>/*.adoc`

## WireMock

The stubs generated will automatically use the input to the test.
If you send in placeholders in the `UrlTemplate`, for example `UrlTemplate("/test/{id}", "123")`,
the WireMock stubs generated will contain a wildcard for the placeholder.
This means that both the path `/test/123` and `/test/abc` will match.

Header values will also be checked if they contain a value and not a specific value.
Meaning that a Header with `Bearer abc123` will also match `Bearer 12345`.

For information on how to setup the contract consumer see the [Spring Cloud Contract documentation](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html#_client_side)

### Build script

The build script must package the stub-jar file,
for more details take a look at [this sample from Spring Cloud Contract](https://github.com/spring-cloud-samples/spring-cloud-contract-samples/blob/master/producer_with_restdocs/build.gradle#L83)