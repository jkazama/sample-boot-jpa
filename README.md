sample-boot-jpa
---

### Preface

This is a DDD sample implementation using [Spring Boot](https://spring.io/projects/spring-boot) / [Spring Security](https://spring.io/projects/spring-security) / [Spring Data JPA](https://spring.io/projects/spring-data-jpa).
This is not a framework, please use it as a base template when starting a project with Spring Boot.

#### Concept of Layering

This is a three-layered architecture based on the famous model, but considers the infrastructure layer as a cross-cutting concern.

| Layer          |                                                            |
| -------------- | ----------------------------------------------------------- |
| UI             | Receives use case requests                                  |
| Application    | Use case processing (including external resource access)   |
| Domain         | Pure domain logic (does not depend on external resources)  |
| Infrastructure | DI container, ORM, and various libraries                   |

Usually, public handling of the UI layer is performed using Thymeleaf, but this sample assumes the use of different types of clients and only provides APIs in RESTful format.

#### Spring Boot Usage Policy

Spring Boot is available for various use cases, but this sample follows the following policies:

- Components that require extended definitions are registered with @Bean. Other components are registered with @Component.
    - `ApplicationConfig` / `ApplicationDbConfig` / `ApplicationSecurityConfig`
- Exception handling is defined at endpoints (`RestErrorAdvice`). The whitelabel function is disabled.
- Specialized in Hibernate as the JPA implementation.
- The authentication method for Spring Security is HttpSession, not basic authentication.
- Easily provides basic utilities that Spring does not support.

#### Java Coding Policy

- Java 21 or higher
- Concepts and notations added in Java 21 are used actively.
- Use Lombok actively and reduce verbosity.
- Keep names as brief as possible.
- Do not abuse interfaces.
- DTOs that are part of the domain are defined as inner classes.

#### Resources

Refer to the following for the package and resource structure.

```
main
  java
    sample
      context                         … Infrastructure Layer
      controller                      … UI Layer
      model                           … Domain Layer
      usecase                         … Application Layer
      util                            … Utilities
      - Application.java              … Bootstrap
  resources
    - application.yml                 … Spring Boot Configuration
    - ehcache.xml                     … Spring Cache Configuration
    - logback-spring.xml              … Logging Configuration
    - messages-validation.properties  … Validation Message Resources
    - messages.properties             … Label Message Resources
```

## Use Case

Consider the following as a sample use case:

- A customer with an account balance requests a withdrawal. (Event T, Delivery T + 3)
- The system closes the withdrawal request. (Allows cancellation of the request until closing)
- The system advances the business day to the next day.
- The system reflects the cash flow on the delivery date to the account balance.

### Getting Started

This sample uses [Gradle](https://gradle.org/). You can check the operation easily with an IDE and console.

#### Server Start (VSCode DevContainer)

The following steps are required:

- Check that Docker is installed.
- Check that VSCode with DevContainer Extension is installed.

Prepare this sample with the following steps:

1. Navigate to the cloned *sample-boot-jpa* directory.
1. Run the command `code .`.
1. Choose *Open Container*

Start the server with the following steps:

1. Open VSCode "Run And Debug".
1. Choose `Run sample-boot-jpa`.
1. If the console shows "Started Application", the server has started on port 8080.
1. Run the command `curl http://localhost:8080/actuator/health`

#### Server Start (Console)

Run the application from a Windows/Mac console using Gradle.

The following steps are required:

- Check that JDK 21+ is installed.
- Prepare PostgreSQL and change the JDBC connection destination in application.yml.
    - DDL/DML files are placed under `data/db`.

Start the server with the following steps:

1. Navigate to the cloned *sample-boot-jpa* directory.
1. Run the command `./gradlew bootRun --args='--spring.profiles.active=developer'`.
1. If the console shows "Started Application", the server has started on port 8080.
1. Run the command `curl http://localhost:8080/actuator/health`

### Check Use Case

After launching the server on port 8080, you can test the RESTful API execution by accessing the following URLs from the console.

#### Customer Use Case

- `curl -X POST -c cookie.txt -d 'loginId=sample&password=sample' http://localhost:8080/api/login`
- `curl -X POST -b cookie.txt -H "Content-Type: application/json" -d '{"accountId"  : "sample" , "currency" : "USD", "absAmount": 1000}' http://localhost:8080/api/asset/cio/withdraw`
    - Request a withdrawal.
- `curl -b cookie.txt 'http://localhost:8080/api/asset/cio/unprocessedOut'`
    - Search for outstanding withdrawal requests.

#### Internal Use Case

- `curl -X POST -c cookie.txt -d 'loginId=ADMINISTRATOR-admin&password=admin' http://localhost:8080/api/login`
- `curl -b cookie.txt 'http://localhost:8080/api/admin/asset/cio?updFromDay=yyyy-MM-dd&updToDay=yyyy-MM-dd'`
    - Search for deposit and withdrawal requests.
    - Please set a real date for upd\*Day.

#### Batch Use Case

- `curl -X POST -c cookie.txt -d 'loginId=ADMINISTRATOR-admin&password=admin' http://localhost:8080/api/login`
- `curl -b cookie.txt -X POST http://localhost:8080/api/system/job/daily/closingCashOut`
    - Close withdrawal requests.
- `curl -b cookie.txt -X POST http://localhost:8080/api/system/job/daily/forwardDay`
    - Advance the business day to the next day.
- `curl -b cookie.txt -X POST http://localhost:8080/api/system/job/daily/realizeCashflow`
    - Realize cash flow. (Reflected in the balance on the delivery date)

> Please execute according to the business day appropriately.  
> When executing from a job agent, change the port or block the path with a load balancer, etc. This is an example based on the assumption that an administrator executes from the UI.

### License

The license for this sample code is the *MIT License*.
Use this as a base implementation when starting a project with Spring Boot.
