sample-boot-jpa
---

### Preface

It is DDD sample implementation from [Spring Boot](https://spring.io/projects/spring-boot) / [Spring Security](https://spring.io/projects/spring-security) / [Spring Data JPA](https://spring.io/projects/spring-data-jpa).
It is not a framework, please use it as a base template when you start a project using Spring Boot.

#### Concept of Layering

It is three levels of famous models, but considers the infrastructure layer as cross-sectional interpretation.

| Layer          |                                                            |
| -------------- | ----------------------------------------------------------- |
| UI             | Receive use case request                                    |
| Application    | Use case processing (including the outside resource access) |
| Domain         | Pure domain logic (not depend on the outside resource) |
| Infrastructure | DI container and ORM, various libraries |

Usually perform public handling of UI layer using Thymeleaf, but this sample assume use of different types of clients and perform only API offer in RESTfulAPI.

#### Use policy of Spring Boot

Spring Boot is available for various usage, but uses it in the following policy with this sample.

- Components that require extended definitions are registered with @Bean. register other component in @Component.
    - `ApplicationConfig` / `ApplicationDbConfig` / `ApplicationSecurityConfig`
- The exception handling defines it in a endpoint (`RestErrorAdvice`). The whitelabel function disabled it.
- Specialized in Hibernate as JPA implementation.
- The certification method of Spring Security is HttpSession not the basic certification.
- Easily prepare for the basic utility that Spring does not support.

#### Use policy of Java coding

- Java17 over
- The concept / notation added in Java17 is used positively.
- Use Lombok positively and remove diffuseness.
- The name as possible briefly.
- Do not abuse the interface.
- DTO becoming a part of the domain defines it in an internal class.

#### Resource

Refer to the following for the package / resource constitution.

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

Consider the following as a sample use case.

- A customer with an account balance requests withdrawal. (Event T, Delivery T + 3)
- The system closes the withdrawal request. (Allows cancellation of request until closing)
- The system sets the business day to the forward day.
- The system reflects the cash flow on delivery date to the account balance.

### Getting Started

This sample uses [Gradle](https://gradle.org/), you can check the operation without trouble with IDE and a console.

#### Server Start (VSCode DevContainer)

It is necessary to do the following step.

- Check Instablled Docker.
- Check Instablled VSCode with DevContainer Extension.

Do the preparations for this sample in the next step.

1. You move to the cloned *sample-boot-jpa* directory.
1. Run command `code .`.
1. Choose *Open Container*

Do the server start in the next step.

1. Open VSCode "Run And Debug".
1. Choose `Run sample-boot-jpa`.
1. If console show "Started Application", start is completed in port 8080.
1. Run command `curl http://localhost:8080/actuator/health`

#### Server Start (Console)

Run application from a console of Windows / Mac in Gradle.

It is necessary to do the following step.

- Check Instablled JDK17+.
- Prepare PostgreSQL and change JDBC connection destination in application.yml.
    - DDL/DML are placed under `data/db`.

Do the server start in the next step.

1. You move to the cloned *sample-boot-jpa* directory.
1. Run command `gradlew bootRun`.
1. If console show "Started Application", start is completed in port 8080
1. Run command `curl http://localhost:8080/actuator/health`

### Check Use Case

After launching the server on port 8080, you can test execution of RESTful API by accessing the following URL from console.

#### Customer Use Case

- `curl -X POST -c cookie.txt -d 'loginId=sample&password=sample' http://localhost:8080/api/login`
- `curl -X POST -b cookie.txt -H "Content-Type: application/json" -d '{"accountId"  : "sample" , "currency" : "USD", "absAmount": 1000}' http://localhost:8080/api/asset/cio/withdraw`
    - Request for withdrawal.
- `curl -b cookie.txt 'http://localhost:8080/api/asset/cio/unprocessedOut'`
    - Search for outstanding withdrawal requests

#### Internal Use Case

- `curl -X POST -c cookie.txt -d 'loginId=ADMINISTRATOR-admin&password=admin' http://localhost:8080/api/login`
- `curl -b cookie.txt 'http://localhost:8080/api/admin/asset/cio?updFromDay=yyyy-MM-dd&updToDay=yyyy-MM-dd'`
    - Search for deposit and withdrawal requests.
    - Please set real date for upd\*Day

#### Batch Use Case

- `curl -X POST -c cookie.txt -d 'loginId=ADMINISTRATOR-admin&password=admin' http://localhost:8080/api/login`
- `curl -b cookie.txt -X POST http://localhost:8080/api/system/job/daily/closingCashOut`
    - Close the withdrawal request.
- `curl -b cookie.txt -X POST http://localhost:8080/api/system/job/daily/forwardDay`
    - Set the business day to the next day.
- `curl -b cookie.txt -X POST http://localhost:8080/api/system/job/daily/realizeCashflow`
    - Realize cash flow. (Reflected to the balance on the delivery date)

> Please execute according to the business day appropriately.  
> When executing from a job agent, change the port or block the path with L/B, etc. Here is an example based on the assumption that the administrator executes from the UI

### License

The license of this sample includes a code and is all *MIT License*.
Use it as a base implementation at the time of the project start using Spring Boot.
