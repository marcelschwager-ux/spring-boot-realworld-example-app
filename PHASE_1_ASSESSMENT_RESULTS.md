# Phase 1: Pre-Migration Assessment Results

## Executive Summary

This document presents the findings from the Phase 1 Pre-Migration Assessment for updating the Spring Boot RealWorld Example Application from **Java 11** to **Java 21** (the latest LTS version). The assessment covers dependency compatibility analysis, codebase audit, risk assessment, and migration strategy recommendations.

**Current State:**
- Java Version: 11
- Spring Boot: 2.6.3
- Gradle: 7.4
- All tests passing (116 Java source files)

**Target State:**
- Java Version: 21 (LTS, released September 2023)
- Spring Boot: 3.x (recommended) or 2.7.x (conservative)

**Overall Assessment:** Migration is feasible but requires significant effort due to the Spring Boot 2.x to 3.x transition, which involves breaking changes in the Jakarta EE namespace migration and Spring Security configuration patterns.

---

## Activity 1: Dependency Compatibility Analysis

### 1.1 Core Dependencies

| Dependency | Current Version | Java 21 Compatible Version | Compatibility Status | Migration Effort |
|------------|-----------------|---------------------------|---------------------|------------------|
| Spring Boot | 2.6.3 | 3.2.x or 2.7.18 | Requires upgrade | HIGH |
| Spring Dependency Management | 1.0.11.RELEASE | 1.1.4 | Requires upgrade | LOW |
| MyBatis Spring Boot Starter | 2.2.2 | 3.0.3 | Requires upgrade | MEDIUM |
| Netflix DGS GraphQL | 4.9.21 | 8.x+ | Requires upgrade | HIGH |
| JJWT (JWT) | 0.11.2 | 0.12.5 | Requires upgrade | LOW |
| Flyway | (managed) | 10.x | Requires upgrade | MEDIUM |
| Lombok | (managed) | 1.18.30+ | Compatible | LOW |
| SQLite JDBC | 3.36.0.3 | 3.45.x | Compatible | LOW |
| Joda-Time | 2.10.13 | 2.12.5 | Compatible but deprecated | MEDIUM |

### 1.2 Spring Boot Compatibility Analysis

**Spring Boot 2.6.3 (Current):**
- Supports Java 11-17
- Does NOT support Java 21

**Spring Boot 2.7.x (Conservative Path):**
- Supports Java 11-19
- Limited Java 21 support (may work with some workarounds)
- End of OSS support: November 2023 (already EOL)

**Spring Boot 3.x (Modern Path):**
- Requires Java 17 minimum
- Full Java 21 support
- Jakarta EE 9+ (jakarta.* namespace)
- Spring Security 6.x (new configuration model)

### 1.3 Netflix DGS GraphQL Framework Analysis

**Critical Dependency:** The Netflix DGS framework has specific Spring Boot compatibility requirements.

| DGS Version | Spring Boot Compatibility | Java Support |
|-------------|--------------------------|--------------|
| 4.x (current) | Spring Boot 2.6.x | Java 11-17 |
| 5.x | Spring Boot 2.7.x | Java 11-17 |
| 6.x | Spring Boot 3.0.x | Java 17+ |
| 7.x | Spring Boot 3.1.x | Java 17+ |
| 8.x | Spring Boot 3.2.x | Java 17-21 |

**Recommendation:** Upgrade to DGS 8.x for full Java 21 and Spring Boot 3.2.x support.

### 1.4 JWT and Security Dependencies

**Current JJWT (0.11.2):**
- Uses `javax.crypto` package (compatible with Java 21)
- API is stable and backward compatible

**Upgrade Path:**
- JJWT 0.12.x provides improved security and Java 21 compatibility
- Minor API changes (deprecation of some builder methods)

### 1.5 Test Dependencies

| Dependency | Current Version | Java 21 Compatible Version | Notes |
|------------|-----------------|---------------------------|-------|
| Rest-Assured | 4.5.1 | 5.4.x | Major version upgrade needed |
| Spring Security Test | (managed) | 6.x | Follows Spring Boot |
| Spring Boot Test | (managed) | 3.2.x | Follows Spring Boot |
| MyBatis Test | 2.2.2 | 3.0.3 | Follows MyBatis starter |

---

## Activity 2: Codebase Audit

### 2.1 Java Version-Specific Features Currently in Use

**Java 8+ Features (Widely Used):**
- Lambda expressions (throughout codebase)
- Stream API (`Collectors.toList()`, `stream().map()`, etc.)
- Optional class (`Optional.of()`, `Optional.empty()`, `flatMap()`)
- Method references (`ResourceNotFoundException::new`)

**Java 11 Features (Limited Use):**
- `var` keyword: NOT USED
- New String methods: NOT USED
- HTTP Client API: NOT USED

**Modern Java Features (17-21) NOT in Use:**
- Records: NOT USED (could benefit domain classes)
- Sealed classes: NOT USED
- Pattern matching: NOT USED
- Virtual threads: NOT USED

### 2.2 Deprecated APIs Requiring Updates

#### 2.2.1 javax.* to jakarta.* Namespace Migration (CRITICAL)

The codebase contains **40+ imports** using the `javax.*` namespace that must be migrated to `jakarta.*` for Spring Boot 3.x:

**Validation API (14 files affected):**
```
javax.validation.Constraint -> jakarta.validation.Constraint
javax.validation.Payload -> jakarta.validation.Payload
javax.validation.Valid -> jakarta.validation.Valid
javax.validation.ConstraintValidator -> jakarta.validation.ConstraintValidator
javax.validation.ConstraintValidatorContext -> jakarta.validation.ConstraintValidatorContext
javax.validation.ConstraintViolation -> jakarta.validation.ConstraintViolation
javax.validation.ConstraintViolationException -> jakarta.validation.ConstraintViolationException
javax.validation.constraints.Email -> jakarta.validation.constraints.Email
javax.validation.constraints.NotBlank -> jakarta.validation.constraints.NotBlank
```

**Servlet API (1 file affected - JwtTokenFilter.java):**
```
javax.servlet.FilterChain -> jakarta.servlet.FilterChain
javax.servlet.ServletException -> jakarta.servlet.ServletException
javax.servlet.http.HttpServletRequest -> jakarta.servlet.http.HttpServletRequest
javax.servlet.http.HttpServletResponse -> jakarta.servlet.http.HttpServletResponse
```

**Crypto API (1 file affected - DefaultJwtService.java):**
```
javax.crypto.SecretKey -> (remains javax.crypto - part of JDK, not Jakarta EE)
javax.crypto.spec.SecretKeySpec -> (remains javax.crypto - part of JDK, not Jakarta EE)
```

#### 2.2.2 Spring Security Configuration (CRITICAL)

**WebSecurityConfigurerAdapter Deprecation:**

The `WebSecurityConfig.java` class extends `WebSecurityConfigurerAdapter`, which is:
- Deprecated in Spring Security 5.7
- Removed in Spring Security 6.0 (Spring Boot 3.x)

**Current Implementation (WebSecurityConfig.java:23):**
```java
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            // ...
    }
}
```

**Required Migration to Component-Based Configuration:**
```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // ...
            );
        return http.build();
    }
}
```

**antMatchers() to requestMatchers() Migration:**
- `antMatchers()` is deprecated in Spring Security 5.8
- `antMatchers()` is removed in Spring Security 6.0
- Must migrate to `requestMatchers()` for Spring Boot 3.x

### 2.3 Joda-Time Usage Analysis

The codebase extensively uses Joda-Time library (19 files affected):

**Files Using Joda-Time:**
- `Article.java` - Domain entity with DateTime fields
- `Comment.java` - Domain entity with DateTime fields
- `ArticleData.java` - DTO with DateTime fields
- `CommentData.java` - DTO with DateTime fields
- `DateTimeHandler.java` - MyBatis type handler
- `DateTimeCursor.java` - Pagination cursor
- `ArticleDatafetcher.java` - GraphQL resolver
- `CommentDatafetcher.java` - GraphQL resolver
- `JacksonCustomizations.java` - JSON serialization
- Multiple test files

**Recommendation:** While Joda-Time is compatible with Java 21, consider migrating to `java.time` API (JSR-310) for:
- Better performance
- Native JDK support
- Reduced dependencies
- Modern API design

### 2.4 Potential Blockers

1. **Netflix DGS Version Lock:** Current DGS 4.9.21 is incompatible with Spring Boot 3.x. Must upgrade to DGS 6.x+ for Spring Boot 3.x support.

2. **Spring Security Configuration:** Complete rewrite of security configuration required for Spring Boot 3.x.

3. **Jakarta EE Migration:** Extensive namespace changes required across 14+ files.

4. **Gradle Version:** Current Gradle 7.4 supports Java 21, but upgrading to Gradle 8.x is recommended for better toolchain support.

---

## Activity 3: Risk Assessment

### 3.1 High-Risk Components

#### Risk Level: CRITICAL

| Component | Risk Description | Impact | Mitigation Strategy |
|-----------|-----------------|--------|---------------------|
| WebSecurityConfig.java | Complete rewrite required for Spring Security 6.x | Authentication/Authorization may break | Comprehensive testing, staged rollout |
| Netflix DGS Integration | Major version upgrade (4.x -> 8.x) with breaking changes | GraphQL API may break | Review DGS migration guide, test all queries/mutations |
| Jakarta EE Migration | 40+ import changes across 14+ files | Compilation failures if incomplete | Automated refactoring tools, thorough testing |

#### Risk Level: HIGH

| Component | Risk Description | Impact | Mitigation Strategy |
|-----------|-----------------|--------|---------------------|
| JwtTokenFilter.java | Servlet API namespace change + potential filter chain changes | JWT authentication may fail | Unit and integration testing |
| MyBatis Configuration | MyBatis 3.x has configuration changes | Database operations may fail | Review MyBatis migration guide |
| Test Suite | Test dependencies require major upgrades | CI/CD pipeline may break | Update test dependencies first |

#### Risk Level: MEDIUM

| Component | Risk Description | Impact | Mitigation Strategy |
|-----------|-----------------|--------|---------------------|
| Joda-Time Usage | Library is in maintenance mode | Technical debt accumulation | Consider migration to java.time |
| Flyway Migrations | Flyway 10.x has breaking changes | Database migrations may fail | Test migrations in staging |
| Rest-Assured Tests | Major version upgrade required | Test failures | Update test assertions |

#### Risk Level: LOW

| Component | Risk Description | Impact | Mitigation Strategy |
|-----------|-----------------|--------|---------------------|
| JJWT Library | Minor API changes | Minimal code changes | Review changelog |
| Lombok | Fully compatible | None expected | Update version |
| SQLite JDBC | Fully compatible | None expected | Update version |

### 3.2 Integration Points Requiring Special Attention

1. **GraphQL API (DGS Framework)**
   - All datafetchers use DGS annotations
   - Code generation plugin needs upgrade
   - Schema handling may change

2. **REST API (Spring MVC)**
   - Controllers use standard Spring annotations
   - Validation annotations need namespace change
   - Exception handlers need review

3. **Database Layer (MyBatis)**
   - Type handlers (DateTimeHandler) need testing
   - Mapper XML files should remain compatible
   - Configuration properties may change

4. **Authentication (JWT + Spring Security)**
   - Token filter implementation needs update
   - Security configuration needs complete rewrite
   - CORS configuration needs review

---

## Activity 4: Migration Strategy Decision

### 4.1 Option A: Conservative Approach (Spring Boot 2.7.x)

**Description:** Upgrade to Spring Boot 2.7.18 (latest 2.x) with Java 17, then plan separate Java 21 migration.

**Pros:**
- Smaller initial change
- Maintains javax.* namespace
- WebSecurityConfigurerAdapter still available (deprecated but functional)
- Lower risk of breaking changes

**Cons:**
- Spring Boot 2.7.x is EOL (November 2023)
- No official Java 21 support
- Requires second migration to Spring Boot 3.x eventually
- DGS 5.x still has limitations

**Estimated Effort:** 2-3 days
**Recommended:** NO - Creates technical debt and requires future re-migration

### 4.2 Option B: Modern Approach (Spring Boot 3.2.x + Java 21)

**Description:** Direct migration to Spring Boot 3.2.x with Java 21, including all necessary dependency upgrades.

**Pros:**
- Full Java 21 support with all new features
- Long-term support (Spring Boot 3.2.x supported until 2026)
- Access to virtual threads, pattern matching, records
- Netflix DGS 8.x with latest features
- Single migration effort

**Cons:**
- Larger scope of changes
- Jakarta EE namespace migration required
- Spring Security configuration rewrite required
- Higher initial risk

**Estimated Effort:** 5-7 days
**Recommended:** YES - Best long-term investment

### 4.3 Recommended Strategy: Modern Approach (Option B)

**Rationale:**
1. Spring Boot 2.7.x is already end-of-life
2. Java 21 is the current LTS with support until 2031
3. Single migration avoids duplicate effort
4. Modern features (virtual threads, records) provide performance benefits
5. Netflix DGS 8.x provides better GraphQL support

### 4.4 Proposed Migration Phases

**Phase 2: Dependency Updates (2-3 days)**
- Update Gradle wrapper to 8.5+
- Update Spring Boot to 3.2.x
- Update Netflix DGS to 8.x
- Update MyBatis to 3.0.x
- Update test dependencies

**Phase 3: Code Migration (2-3 days)**
- Migrate javax.* to jakarta.* imports
- Rewrite WebSecurityConfig for Spring Security 6.x
- Update JwtTokenFilter for jakarta.servlet
- Review and update MyBatis configuration

**Phase 4: Testing and Validation (1-2 days)**
- Run full test suite
- Manual API testing (REST and GraphQL)
- Performance benchmarking
- Security testing

**Phase 5: CI/CD Updates (0.5-1 day)**
- Update GitHub Actions workflow for Java 21
- Update build scripts
- Configure deployment pipeline

**Phase 6: Deployment (0.5-1 day)**
- Staged rollout
- Monitoring and validation
- Rollback plan execution if needed

---

## Appendix A: Files Requiring Modification

### A.1 Build Configuration
- `build.gradle` - Update all dependency versions, Java compatibility
- `gradle/wrapper/gradle-wrapper.properties` - Update Gradle version
- `.github/workflows/gradle.yml` - Update Java version to 21

### A.2 Security Layer
- `src/main/java/io/spring/api/security/WebSecurityConfig.java` - Complete rewrite
- `src/main/java/io/spring/api/security/JwtTokenFilter.java` - Namespace migration

### A.3 Validation Layer (javax.validation -> jakarta.validation)
- `src/main/java/io/spring/api/ArticleApi.java`
- `src/main/java/io/spring/api/ArticlesApi.java`
- `src/main/java/io/spring/api/CommentsApi.java`
- `src/main/java/io/spring/api/CurrentUserApi.java`
- `src/main/java/io/spring/api/UsersApi.java`
- `src/main/java/io/spring/application/article/ArticleCommandService.java`
- `src/main/java/io/spring/application/article/DuplicatedArticleConstraint.java`
- `src/main/java/io/spring/application/article/DuplicatedArticleValidator.java`
- `src/main/java/io/spring/application/article/NewArticleParam.java`
- `src/main/java/io/spring/application/user/DuplicatedEmailConstraint.java`
- `src/main/java/io/spring/application/user/DuplicatedEmailValidator.java`
- `src/main/java/io/spring/application/user/DuplicatedUsernameConstraint.java`
- `src/main/java/io/spring/application/user/DuplicatedUsernameValidator.java`
- `src/main/java/io/spring/application/user/RegisterParam.java`
- `src/main/java/io/spring/application/user/UpdateUserParam.java`
- `src/main/java/io/spring/application/user/UserService.java`

### A.4 Exception Handling
- `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java`
- `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java`
- `src/main/java/io/spring/graphql/UserMutation.java`

### A.5 GraphQL Layer (DGS Upgrade)
- All files in `src/main/java/io/spring/graphql/` - Review for DGS 8.x compatibility
- `build.gradle` - Update DGS codegen plugin version

---

## Appendix B: Recommended Dependency Versions

```groovy
plugins {
    id 'org.springframework.boot' version '3.2.5'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
    id "com.netflix.dgs.codegen" version "6.2.1"
    id "com.diffplug.spotless" version "6.25.0"
}

sourceCompatibility = '21'
targetCompatibility = '21'

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-hateoas'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:8.5.0'
    implementation 'org.flywaydb:flyway-core'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
    implementation 'joda-time:joda-time:2.12.7'
    implementation 'org.xerial:sqlite-jdbc:3.45.3.0'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'io.rest-assured:rest-assured:5.4.0'
    testImplementation 'io.rest-assured:json-path:5.4.0'
    testImplementation 'io.rest-assured:xml-path:5.4.0'
    testImplementation 'io.rest-assured:spring-mock-mvc:5.4.0'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3'
}
```

---

## Appendix C: CI/CD Configuration Update

```yaml
# .github/workflows/gradle.yml
name: Java CI

on:
  push:
    branches:
      - '**'
  pull_request:
    branches:
      - '**'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '21'
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
    - name: Test with Gradle
      run: ./gradlew clean test
```

---

## Conclusion

The migration from Java 11 to Java 21 is recommended using the **Modern Approach** (Option B), which involves upgrading to Spring Boot 3.2.x alongside the Java version upgrade. While this approach requires more initial effort, it provides the best long-term benefits including full Java 21 support, access to modern language features, and alignment with actively supported framework versions.

The primary challenges are:
1. Jakarta EE namespace migration (40+ import changes)
2. Spring Security configuration rewrite
3. Netflix DGS major version upgrade

With proper planning and phased execution, the migration can be completed in approximately 5-7 days with acceptable risk levels.

**Next Steps:**
1. Review and approve this assessment
2. Create detailed Phase 2 implementation plan
3. Set up feature branch for migration work
4. Begin dependency updates

---

*Document generated: December 4, 2025*
*Assessment performed by: Devin AI*
*Repository: marcelschwager-ux/spring-boot-realworld-example-app*
