# giphy_example
Giphy Example

# Technology Stack
On the front-end, Bootstrap which is a CSS framework for building responsive sites that look great on any device.  

# The back-end is Spring Boot application and a MySQL database.   

# Maven Project
The Maven build tool for this project. Need the following dependencies:

1. spring-boot-starter-web
A set of basic dependencies needed to develop web applications with Spring. 

2. spring-boot-starter-data-jpa
Part of the umbrella Spring Data project that makes it easy to implement JPA-based repositories using Hibernate.  It can create repository implementations at runtime from a repository interface.

3. spring-boot-starter-security
Provides CSRF form protection as well as basic authentication on all HTTP endpoints 

4. spring-boot-starter-thymeleaf
Provides the Thymeleaf templating engine

5. thymeleaf-extras-springsecurity4
Allows us to use Spring Security dialect in Thymeleaf templates

6. nekohtml
Allows us to relax the strict HTML syntax checking rules for Thymeleaf templates

7. spring-boot-starter-mail
Provides JavaMailSender which we'll use to send plain-text e-mail

8. spring-boot-devtools
Provides automatic app restarts whenever files on the classpath change

9. mysql-connector-java
Provides MySQL database drivers

10. zxcvbn
Provides our password complexity library

pom.xml