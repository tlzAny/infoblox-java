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
   <version>1.2.0</version>
</dependency>
```

## Examples

#### Initializing Infoblox Client

```java
InfobloxClient client = InfobloxClient.builder()
            .endPoint("Infoblox Host")
            .userName("Infoblox User")
            .password("Infoblox Password")
            .tlsVerify(false)
            .build();
```
  - For TLS verification, set the CA truststore using [trustStore()][2] & [trustStorePassword()][3] methods.
  - Truststore should be of type [PKCS#12][4] format. 
  - For loading the Truststore from classpath use, `classpath:/<your/truststore/path>.p12`
  - To enable http debugging for troubleshooting, set [.debug(true)][5] to the [InfobloxClient.builder()][6]

#### **A** Record

```java
String fqdn = "test.xyz.com";
String ip = "10.11.12.13";

// CRUD operations
ARec aRec = client.createARec(fqdn, ip);
List<ARec> rec = client.getARec(fqdn);
List<ARec> aRecs = client.getARec(fqdn, ip);
List<ARec> modifedARec = client.modifyARec(fqdn, newFqdn);
List<String> delARec = client.deleteARec(fqdn);
```
#### **AAAA** Record

```java
String fqdn = "test.xyz.com";
String ipv6 = "fe80:0:0:0:f0ea:f6ff:fd97:5d51";

// CRUD operations
AAAA newAAAARec = client.createAAAARec(fqdn, ipv6);
List<AAAA> aaaaRec = client.getAAAARec(fqdn);
List<AAAA> modAAAARec = client.modifyAAAARec(fqdn, newFqdn);
List<String> delAAAARec = client.deleteAAAARec(fqdn);
```

#### **CNAME** Record

```java
String canonicalName = "test.xyz.com";
String alias = "app.xyz.com";

// CRUD operations
CNAME cname = client.createCNameRec(alias, canonicalName);
List<CNAME> rec = client.getCNameRec(alias);
List<CNAME> modCName = client.modifyCNameRec(alias, newAlias);
List<String> delCName = client.deleteCNameRec(alias);
```

Refer [JavaDocs][javadoc-url] for all record types (**MX, PTR, SRV, TXT** etc) APIs

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

## ToDo

   - Add support for `SRV`, `TXT`, `PTR`, `DNAME` records etc.
      
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
[2]: http://oneops.com/infoblox-java/javadocs/com/oneops/infoblox/InfobloxClient.Builder.html#trustStore-java.lang.String-
[3]: http://oneops.com/infoblox-java/javadocs/com/oneops/infoblox/InfobloxClient.Builder.html#trustStorePassword-java.lang.String-
[4]: https://en.wikipedia.org/wiki/PKCS_12
[5]: http://oneops.com/infoblox-java/javadocs/com/oneops/infoblox/InfobloxClient.Builder.html#debug-boolean-
[6]: http://oneops.com/infoblox-java/javadocs/com/oneops/infoblox/InfobloxClient.Builder.html


[maven-url]: http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.oneops%22%20AND%20a%3A%22infoblox-java%22
[maven-svg]: https://img.shields.io/maven-central/v/com.oneops/infoblox-java.svg?label=Maven%20Central&style=flat-square

[cl-url]: https://github.com/oneops/infoblox-java/blob/master/CHANGELOG.md
[cl-svg]: https://img.shields.io/badge/change--log-latest-green.svg?style=flat-square

[javadoc-url]: https://oneops.github.io/infoblox-java/javadocs/
[javadoc-svg]: https://img.shields.io/badge/api--doc-latest-cyan.svg?style=flat-square

