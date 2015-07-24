# totp-api

An API server for performing TOTP (for example, Google Authenticator) one-time-password authentication,
with secrets stored in an LDAP backend.

This service is intended to be served by a frontend web server that is performing TLS client certificate
authorization, such as [NGINX](https://tools.ietf.org/html/rfc4226). As such, there is no access control or
authorization on this service's API.

Currently the service can be used by any HTTP-enabled client that needs to perform TOTP+LDAP based authentication.
The current API format is parseable by [pam_url](https://github.com/mricon/pam_url) for use in PAM
authentication.

This project was conceived to fill a need that [totp-cgi](https://github.com/mricon/totp-cgi) did not solve: namely,
the provisioning and storage of TOTP secrets, scratch codes, and potential state in an LDAP backend.

## Features

* TOTP+LDAP password authentication.
* LDAP-based secret storage with STARTTLS support.
* Generation, provisioning, and deprovisioning of TOTP secrets in the backing store.
* One-time-use scratch code support.
* Easy to deploy - a single JAR.

#### Upcoming Features

* Additional secret storage backends.
* State information: rate limiting, code re-use prevention.

### Runtime Dependencies

* **Java 8**: Installation instructions will differ based on your platform. [General Download Link](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* **LDAP**: [OpenLDAP](http://www.openldap.org/) is the only tested implementation currently.
The [provided cn=config schema](./schema/totp.ldif) needs to be added.

## Configuration

See the included [sample configuration](./config/sample.conf).

The configuration is provided by [Typesafe Config](https://github.com/typesafehub/config), so see their documentation for information on things such as includes and environment variable support.

## Building

### Build Dependencies

* **sbt**: [Scala Simple Build Tool](http://www.scala-sbt.org/)

### Building and Running

1. Clone this repo.
1. Run `sbt -Dconfig.file=<path/to/config> run` to run locally.
2. Run `sbt assembly` to build a single JAR file in the `target/scala-2.11` directory.
4. Run the jar using `java -Dconfig.file=<path/to/config> -jar <path/to/>totp-api-assembly-0.1.0-SNAPSHOT.jar`

## Additional Information

### Further Reading

* [RFC 4226: HMAC-Based One-Time Password Algorithm](https://tools.ietf.org/html/rfc4226)
* [RFC 6238: Time Based One-Time Password Algorithm](https://tools.ietf.org/html/rfc6238)

### License

This project is licensed under the [Apache License](./LICENSE).
