# MockMvc extensions Kotlin

## Usage

```
mockMvc.get(urlTemplate = UrlTemplate("/test")) {
   it.statusIsOk().responseJsonPath("$.value").equalsValue("test")
}
```

## Rest docs

To generate rest docs include the `docsIdentifier`:
```
mockMvc.get(docsIdentifier = "my-docs", urlTemplate = UrlTemplate("/test")) {
  ...
}
```

### Build script

The build script must package the stub-jar file,
see [this sample from Spring Cloud contract](https://github.com/spring-cloud-samples/spring-cloud-contract-samples/blob/master/producer_with_restdocs/build.gradle#L83)