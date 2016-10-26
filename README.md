sample-boot-hibernate
---

### Preface

It is DDD sample implementation from [Spring Boot](http://projects.spring.io/spring-boot/) / [Spring Security](http://projects.spring.io/spring-security/) / [Hibernate ORM](http://hibernate.org/orm/).  
It is not a framework, please use it as a base template when you start a project using Spring Boot.

Refer to [sample-ui-vue (JP)](https://github.com/jkazama/sample-ui-vue) / [sample-ui-react (JP)](https://github.com/jkazama/sample-ui-react) for the implementation sample on the UI side.

#### Concept of Layering

It is three levels of famous models, but considers the infrastructure layer as cross-sectional interpretation.

| Layer          |                                                            |
| -------------- | ----------------------------------------------------------- |
| UI             | Receive use case request                                    |
| Application    | Use case processing (including the outside resource access) |
| Domain         | Pure domain logic (not depend on the outside resource) |
| Infrastructure | DI container and ORM, various libraries |

Usually perform public handling of UI layer using Thymeleaf or JSP, but this sample assume use of different types of clients and perform only API offer in RESTfulAPI.

#### Use policy of Spring Boot

Spring Boot is available for various usage, but uses it in the following policy with this sample.

- The configuration file uses yml. do not use xml files for Bean definition.
- Register the component of the infrastructure layer in @Bean. register other component in @Component.
    - `ApplicationConfig` / `ApplicationDbConfig` / `ApplicationSecurityConfig`
- The exception handling defines it in a endpoint (RestErrorAdvice / RestErrorCotroller). The whitelabel function disabled it.
- Specialized in Hibernate as JPA implementation.
- The certification method of Spring Security is HttpSession not the basic certification.
- Easily prepare for the basic utility that Spring does not support.

#### Use policy of Java coding

- Java8 over
- The concept / notation added in Java8 is used positively.
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

### Getting Started

This sample uses [Gradle](https://gradle.org/), you can check the operation without trouble with IDE and a console.

*Because library downloading is automatically carried out, please carry it out at the terminal which is available for Internet connection.*

#### Server Start (Eclipse)

It is necessary to do the following step.

- Check Instablled JDK8+.
- Apply a patch of [Lombok](http://projectlombok.org/download.html)
- Check Instablled Gradle Plugin [Buildship].

> Recommend after Eclipse Mars because of Gradle Plugin [Buildship].

Do the preparations for this sample in the next step.

1. Choose "right-click - Import - Project" among package Explorer.
1. Choose Gradle Project* and push down *Next*
1. Choose downloaded *sample-boot-hibernate* in *Project root directory* and push down *Next*
1. Push down *Next* in *Import Options*
1. If *sample-boot-hibernate* is displayed by *Gradle project structure*, push down *Finish*
    -  dependency library downloading is carried out here

Do the server start in the next step.

1. Do "right-click - Run As - Java Application" for *Application.java*
1. If console show "Started Application", start is completed in port 8080
1. Start a browser and confirm a state in "http://localhost:8080/api/management/health"

> You can easily run the main class via Boot Dashboard of GUI when you use plug in of STS (Spring Tool Suite).

#### Server Start (Console)

Run application from a console of Windows / Mac in Gradle.

It is necessary to do the following step.

- Check Instablled JDK8+.

Do the server start in the next step.

1. You move to the downloaded *sample-boot-hibernate* directory.
1. Run command "gradlew bootRun".
1. If console show "Started Application", start is completed in port 8080
1. Start a browser and confirm a state in "http://localhost:8080/api/management/health"

#### Client Check

- After starting application, run `SampleClient`.

### Make Executable Jar

you can do application with a single distribution file in Spring Boot by making Executable Jar (jar which contains a library or the static resource).

1. Run command "gradlew build".
1. Ouput jar to `build/libs`, you distribute it to release environment on Java8+
1. Run command "java -jar xxx.jar" in release environment.

> You can change the property value (via profile) of application.yml. add "--spring.profiles.active=[profile name]" to a start argument of java.

### Copy Project

When you want to make a project resource from this sample, do the following step.

1. Modify a variable of copyProject in build.gradle.
1. Run command "gradlew copyProject".

### License

The license of this sample includes a code and is all *MIT License*.
Use it as a base implementation at the time of the project start using Spring Boot.
