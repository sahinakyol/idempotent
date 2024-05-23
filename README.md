# Idempotent Library

This Java library provides utilities for ensuring idempotent method calls using a combination of annotations, aspect-oriented programming (AOP), and various data sources to store idempotency keys. It supports Redis, PostgreSQL, and MongoDB as backends for storing these keys.

## Features

- **Idempotent Annotation**: Mark methods as idempotent using the `@Idempotent` annotation.
- **Aspect-Oriented Programming**: Intercept method calls to ensure they are executed only once within a specified time-to-live (TTL).
- **Multiple Data Sources**: Supports Redis, PostgreSQL, and MongoDB for storing idempotency keys.
- **Bloom Filter**: Uses a Bloom filter for quick existence checks to reduce the load on the primary data source.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven for dependency management

### Installation

Include the following dependencies in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/sahinakyol/idempotent</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
<dependency>
    <groupId>org.idempotent</groupId>
    <artifactId>idempotent</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Usage
```java
package com.example.demo;

import org.idempotent.IdempotencyAspect;
import org.idempotent.datasource.DataSource;
import org.idempotent.datasource.Redis;
import org.idempotent.ds.BloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy
@Import(IdempotencyAspect.class)
public class IdempotentAutoConfiguration {

    @Bean
    BloomFilter bloomFilter() {
        return new BloomFilter(1024, 3);
    }

    @Bean
    DataSource dataSource() {
        return new Redis("localhost",6379);
        //return new Postgres("jdbc:postgresql://localhost:5432/mydatabase","myuser","mypassword");
        //return new MongoDB("mongodb://localhost:27017", "testDB", "kvStore");
    }
}

@RestController
public class Controller {

    @PostMapping
    @Idempotent(ttl = 60000)
    public Person postConf(@RequestBody Person person) {
        return person;
    }
}
```
### License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
