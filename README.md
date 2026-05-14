# Spring Annotations — Learning Project

A minimal Spring Boot project that demonstrates the five annotation concepts
from the study questions. Every file is heavily commented; read them in order.

---

## Reading order

| File | Concept covered |
|------|----------------|
| `LearningApplication.java` | `@SpringBootApplication` |
| `model/Book.java` | `@Entity`, `@Table`, `@Id`, `@Column` |
| `repository/BookRepository.java` | `@Repository` — exception translation |
| `service/BookService.java` | `@Service` stereotype, `@Transactional` |
| `controller/BookController.java` | `@RestController`, `@RequestParam`, `@PathVariable`, `@RequestBody` |
| `config/AppConfig.java` | `@Configuration`, `@Bean` vs `@Component` |
| `exception/GlobalExceptionHandler.java` | `@RestControllerAdvice`, `@ExceptionHandler` |

---

## Running the project

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

H2 console (inspect the in-memory database): **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:bookdb`
- Username: `sa` / Password: (empty)

---

## API endpoints to try

### @RequestParam — query parameters

```
GET /api/books                          all books
GET /api/books?author=George Orwell     filter by author
GET /api/books?title=clean              search by title keyword
```

### @PathVariable — path segment

```
GET  /api/books/1          get book with id 1
PUT  /api/books/1          update book with id 1
DELETE /api/books/1        delete book with id 1
```

### @RequestBody — JSON payload

```
POST /api/books
Content-Type: application/json

{
  "title": "Domain-Driven Design",
  "author": "Eric Evans",
  "price": 44.99
}
```

---

## Key concepts at a glance

### Stereotype annotations
All register a bean with Spring. Differ only in intent (and @Repository adds exception translation):

```
@Component   generic
@Service     business logic   ← BookService
@Repository  data access      ← BookRepository  (+ exception translation)
@Controller  web layer        ← BookController  (via @RestController)
```

### @Bean vs @Component

```java
// @Component — your own class, detected automatically
@Service
public class BookService { ... }

// @Bean — third-party class, you control construction
@Configuration
public class AppConfig {
    @Bean
    public Clock clock() { return Clock.systemDefaultZone(); }
}
```

### @Configuration guarantee
CGLIB proxies the class so that calling one @Bean method from another
returns the same singleton, not a new instance.

### @Transactional
Opens a transaction before the method, commits on success, rolls back on
any RuntimeException — no manual try/catch needed.

### @Repository exception translation
`SQLException` → `DataAccessException` happens automatically.
`@Component` does NOT do this.
