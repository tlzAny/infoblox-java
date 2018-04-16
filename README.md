<img src="docs/images/dns-icon.png" alt="Infoblox" width=25 height=25> Infoblox Java Client
----------
[![Maven Central][maven-svg]][maven-url] [![changelog][cl-svg]][cl-url] [![javadoc][javadoc-svg]][javadoc-url]  

A pure java API for Infoblox DNS appliance.

Download
--------

Download [the latest JAR][1] or grab via Maven:
```xml
<dependency>
   <groupId>com.oneops</groupId>
   <artifactId>infoblox-java</artifactId>
   <version>1.0.1</version>
</dependency>
```

## Examples

#### Initializing Client

```java
InfobloxClient client = InfobloxClient.builder()
            .endPoint("Infoblox Host")
            .userName("Infoblox User")
            .password("Infoblox Password")
            .tlsVerify(false)
            .build();
```
  - Keystore should be of type [PKCS#12][2] format. 
  - For loading the keystore from classpath use, `classpath:/<your/cws/keystore/path>.p12`
  - If the keystore contains multiple cert entries, use [.keyAlias("cws-client-key")][3] to select the 
    proper client private key.
  - To enable http debugging for troubleshooting, set [.debug(true)][4] to the [CwsClient.builder()][5]

## Testing

Set the following env variables and run `./mvnw clean test` to execute the unit tests.

```bash
export iba_host=<Infoblox Hostname>
export iba_user=<Infoblox Username>
export iba_password=<Infoblox Password>
export iba_domain=<Infoblox Zone>
```

## Dependencies

   - [Retrofit](https://github.com/square/retrofit/)
   - [OkHttp](https://github.com/square/okhttp)
   - [Moshi](https://github.com/square/Moshi/)
   - [Failsafe](https://github.com/jhalterman/failsafe)
   - [Ok2Curl](https://github.com/mrmike/Ok2Curl)

## ToDo

   - Add support for `SRV`, `TXT`, `PTR` records etc.
   - Add more APIs to query/update record specific attributes.
      
License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



<!-- Badges -->

[1]: https://search.maven.org/remote_content?g=com.oneops&a=infoblox-java&v=LATEST

[maven-url]: http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.oneops%22%20AND%20a%3A%22infoblox-java%22
[maven-svg]: https://img.shields.io/maven-central/v/com.oneops/infoblox-java.svg?label=Maven%20Central&style=flat-square

[cl-url]: https://github.com/oneops/infoblox-java/blob/master/CHANGELOG.md
[cl-svg]: https://img.shields.io/badge/change--log-latest-green.svg?style=flat-square

[javadoc-url]: https://oneops.github.io/infoblox-java/javadocs/
[javadoc-svg]: https://img.shields.io/badge/api--doc-latest-cyan.svg?style=flat-square

