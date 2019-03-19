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
  pathTemplate = PathTemplate("/test/{id}", "123")
) {
   it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
}
```

| Name | Description | WireMock usage |
|------|-------------|----------------|
| headers         | The HTTP headers used in the request                                | Check that the header is present, does not check the value |
| body            | The request body, used for POST, PUT, PATCH and DELETE.             | Not used in the wiremock stub |
| docsIdentifier  | The name of the snippet generated by restdocs.                      | Not used in stubs  |
| pathBuilder     | The request path. Can be `ExactPath` (full path) or `PathTemplate` (path with placeholders). | `ExactPath` will trigger a full comparison in the WireMock, the default priority is 1.<br><br>`PathTemplate` will replace the placeholders with a regex, for example `PathTemplate("/test/{id}", "123")`, the WireMock stubs generated will contain a wildcard for the placeholder. This means that both the path `/test/123` and `/test/abc` will match. By default `PathTemplate` is set with priority 2.<br><br>If two paths overlap, the one configured with the lowest priority wins. By default `ExactPath` will be used before  `PathTemplate` if there is overlap in the setup. |

---

There are also convenience methods for asserting with JsonPath.  
* *Equals value:* `it.statusIsOk().responseJsonPath("$.value").equalsValue("test")`
* *Equals object:* `it.statusIsOk().responseJsonPath("$").equalsObject(TestObject())`

## Rest docs

To generate rest docs add `@AutoConfigureRestDocs` to your unit test class and include the `docsIdentifier`:
```
mockMvc.get(docsIdentifier = "my-docs", pathTemplate = ExactPath("/test")) { ... }
```

This will by default generate the restdocs snippets in `<target/build>/generated-snippets/<docsIdentifier>/*.adoc`

## WireMock

For information on how to setup the contract consumer see the [Spring Cloud Contract documentation](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html#_client_side)

### Build script

The build script must package the stub-jar file,
for more details take a look at [this sample from Spring Cloud Contract](https://github.com/spring-cloud-samples/spring-cloud-contract-samples/blob/master/producer_with_restdocs/build.gradle#L83)