# MockMvc extensions Kotlin

This project was created to have one source, the unit test, for documentation and contracts.
It simplifies the setup of `MockMvc` tests that generates [restdocs](https://spring.io/projects/spring-restdocs)
and wiremock stubs using [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract).

For examples look at the [unit tests](https://github.com/Skatteetaten/mockmvc-extensions-kotlin/blob/master/src/test/kotlin/no/skatteetaten/aurora/mockmvc/extensions/ControllerIntegrationTest.kt).

## Usage

**GET request**
```
mockMvc.get(
  headers = HttpHeaders().authorization("test"),
  docsIdentifier = "my-docs",
  path = Path("/test/{id}", "123")
) {
   it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
}
```

**POST request with `body`**
```
mockMvc.post(
  path = Path("/test"),
  body = "test123"
) {
    it.statusIsOk().responseJsonPath("$.key").equalsValue("test123")
}
```


| Name | Description | WireMock usage |
|------|-------------|----------------|
| headers         | The HTTP headers used in the request                                | Check that the header is present, does not check the value |
| body            | The request body, used for POST, PUT, PATCH and DELETE.             | Not used in the wiremock stub |
| docsIdentifier  | The name of the snippet generated by restdocs.                      | Not used in stubs  |
| path            | The request path. For example: `Path("/test")` or `Path("/test/{id}", "123")` | `Path` without placeholders (vars) will trigger a full comparison in the WireMock stub, the priority is set to 1.<br><br>`Path` with placeholders (vars) replaces the placeholders with a regex, for example `Path("/test/{id}", "123")`, the WireMock stubs generated will contain a wildcard for the placeholder, `/test/[\w-]+`. This means that both the path `/test/123` and `/test/abc` will match. Path with placeholder has priority 2.<br><br>If two paths overlap, the one with the lowest priority number wins.|

---

There are also convenience methods for asserting with JsonPath.  
* *Equals value:* `it.statusIsOk().responseJsonPath("$.value").equalsValue("test")`
* *Equals object:* `it.statusIsOk().responseJsonPath("$").equalsObject(TestObject())`

## Rest docs

To generate rest docs add `@AutoConfigureRestDocs` to your unit test class and include the `docsIdentifier`:
```
mockMvc.get(docsIdentifier = "my-docs", path = Path("/test")) { ... }
```

This will by default generate the restdocs snippets in `<target/build>/generated-snippets/<docsIdentifier>/*.adoc`

## Mock responses

In contract tests it is often useful to store the responses in json files, this will ensure that changes to the returned object still works with the existing contract values.
This library provides a `withContractResponse` function that makes it easy to setup the mocks with data from the json files.

In the example below we mock the `getTestObject()` function to return the object populated with the values from the json file `contracts/test-response.json`.

```
given(mock.getTestObject()).withContractResponse("test-response"){ willReturn(content) }
```

It is also possible to get the object created from the json file
```
val testObject = given(mock.getTestObject()).withContractResponse("test-response"){ willReturn(content) }.mockResponse
```


## WireMock

For information on how to setup the contract consumer see the [Spring Cloud Contract documentation](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html#_client_side)

### Build script

The build script must package the stub-jar file,
for more details take a look at [this sample from Spring Cloud Contract](https://github.com/spring-cloud-samples/spring-cloud-contract-samples/blob/master/producer_with_restdocs/build.gradle#L83)


