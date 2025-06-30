# Spring Boot 3.4 Best Practices Guidelines with Domain-Driven Design

## Table of Contents

1. [Introduction](#introduction)
2. [Domain-Driven Design Architecture](#1-domain-driven-design-architecture)
    1. [Strategic Design](#11-strategic-design)
    2. [Tactical Design Patterns](#12-tactical-design-patterns)
    3. [Hexagonal Architecture](#13-hexagonal-architecture)
    4. [Directory Structure](#14-directory-structure)
3. [Application Layer](#2-application-layer)
    1. [Use Cases](#21-use-cases)
    2. [Application Services](#22-application-services)
    3. [Domain Event Handling](#23-domain-event-handling)
4. [Infrastructure Layer](#3-infrastructure-layer)
    1. [REST Controllers](#31-rest-controllers)
    2. [Request/Response Objects](#32-requestresponse-objects)
    3. [Exception Handling](#33-exception-handling)
    4. [Persistence Implementations](#34-persistence-implementations)
5. [Testing in DDD](#4-testing-in-ddd)
    1. [Domain Model Testing](#41-domain-model-testing)
    2. [Use Case Testing](#42-use-case-testing)
    3. [Infrastructure Testing](#43-infrastructure-testing)
    4. [Working with MockBean](#44-working-with-mockbean)
    5. [Testing with Slices](#45-testing-with-slices-and-mockbean)
6. [Libraries and Dependencies](#5-libraries-and-dependencies)
    1. [Core Libraries](#51-core-libraries)
    2. [Database](#52-database)
    3. [Logging](#53-logging)
    4. [API Documentation](#54-api-documentation)
    5. [HTTP Client with RestClient](#55-http-client-with-restclient)
7. [Common Pitfalls and Gotchas](#6-common-pitfalls-and-gotchas)
    1. [DDD-Specific Challenges](#61-ddd-specific-challenges)
    2. [Edge Cases to Consider](#62-edge-cases-to-consider)
    3. [Version Compatibility](#63-version-compatibility)
    4. [Anti-Patterns to Avoid](#64-anti-patterns-to-avoid)
8. [Performance Optimization Techniques](#7-performance-optimization-techniques)
    1. [Database Query Optimization](#71-database-query-optimization)
    2. [Caching](#72-caching)
    3. [Asynchronous Processing](#73-asynchronous-processing)
    4. [Pagination](#74-pagination)
    5. [Load Testing](#75-load-testing)
9. [Development Environment and Tooling](#8-development-environment-and-tooling)
    1. [Recommended Tools](#81-recommended-tools)
    2. [Code Quality Tools](#82-code-quality-tools)
10. [General Best Practices](#9-general-best-practices)
    1. [Code Quality](#91-code-quality)
    2. [Performance Optimization](#92-performance-optimization)
    3. [Documentation](#93-documentation)
11. [Additional Considerations](#10-additional-considerations)
    1. [Internationalization and Localization](#101-internationalization-and-localization)
    2. [Advanced API Design Principles](#102-advanced-api-design-principles)

## Introduction

This document outlines the architecture, development, and testing guidelines for applications built with Spring Boot
3.4. Following these practices will help ensure scalable, maintainable, and robust applications.

## 1. Domain-Driven Design Architecture

### 1.1 Strategic Design

Domain-Driven Design (DDD) starts with strategic design to understand and model the business domain:

- **Ubiquitous Language**: Establish a common language between developers and domain experts
- **Bounded Contexts**: Define clear boundaries for different domain models
- **Context Mapping**: Document relationships between bounded contexts
- **Core Domain**: Identify and focus on the most valuable part of your application
- **Subdomains**: Divide the domain into manageable parts (Core, Supporting, Generic)

Example of a context map for an e-commerce application:

```
+------------------------+       +------------------------+
|  Product Catalog       |       |  Order Management      |
|  -----------------     |       |  -----------------     |
|  - Products            |<----->|  - Orders              |
|  - Categories          |       |  - Order Items         |
|  - Product Reviews     |       |  - Shipping            |
+------------------------+       +------------------------+
           ^                               ^
           |                               |
           v                               v
+------------------------+       +------------------------+
|  Customer Management   |       |  Payment Processing    |
|  -----------------     |<----->|  -----------------     |
|  - Customers           |       |  - Payments            |
|  - Addresses           |       |  - Refunds             |
|  - Preferences         |       |  - Payment Methods     |
+------------------------+       +------------------------+
```

### 1.2 Tactical Design Patterns

Implement these tactical patterns to create a rich domain model:

- **Entities**: Objects with identity that changes over time
  ```java
  public class Customer extends AggregateRoot {
      private CustomerId id;
      private String name;
      private Email email;
      // Business methods that enforce invariants
  }
  ```

- **Value Objects**: Immutable objects defined by their attributes
  ```java
  public class Email extends ValueObject {
      private final String address;

      public Email(String address) {
          if (!isValid(address)) {
              throw new DomainException("Invalid email format");
          }
          this.address = address;
      }

      private boolean isValid(String email) {
          // Validation logic
          return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
      }
  }
  ```

- **Aggregates**: Cluster of entities and value objects with a root entity
  ```java
  public class Order extends AggregateRoot {
      private OrderId id;
      private CustomerId customerId;
      private Set<OrderItem> items;
      private OrderStatus status;

      public void addItem(Product product, int quantity) {
          // Business logic with invariant enforcement
          if (status != OrderStatus.DRAFT) {
              throw new DomainException("Cannot modify a non-draft order");
          }
          items.add(new OrderItem(product.getId(), quantity, product.getPrice()));
      }

      public void submit() {
          if (items.isEmpty()) {
              throw new DomainException("Cannot submit an empty order");
          }
          status = OrderStatus.SUBMITTED;
      }
  }
  ```

- **Domain Services**: Operations that don't belong to a specific entity
  ```java
  public class OrderPricingService {
      public Money calculateTotalPrice(Order order, TaxRules taxRules) {
          // Complex pricing logic involving multiple entities
      }
  }
  ```

- **Domain Events**: Record of something significant that happened in the domain
  ```java
  public class OrderSubmittedEvent extends DomainEvent {
      private final OrderId orderId;
      private final CustomerId customerId;

      public OrderSubmittedEvent(OrderId orderId, CustomerId customerId) {
          super();
          this.orderId = orderId;
          this.customerId = customerId;
      }
  }
  ```

- **Repositories**: Abstraction for persistence operations
  ```java
  public interface OrderRepository {
      Order findById(OrderId id);
      void save(Order order);
      List<Order> findByCustomerId(CustomerId customerId);
  }
  ```

### 1.3 Hexagonal Architecture

Implement a hexagonal architecture (ports and adapters) to separate domain logic from external concerns:

- **Domain Layer**: Core business logic, entities, value objects, domain services
- **Application Layer**: Use cases, application services, orchestration
- **Infrastructure Layer**: Adapters for external systems, persistence, UI, messaging

```
+------------------------------------------+
|                                          |
|  +----------------------------------+    |
|  |                                  |    |
|  |        Domain Layer              |    |
|  |  (Entities, Value Objects,       |    |
|  |   Aggregates, Domain Services)   |    |
|  |                                  |    |
|  +----------------------------------+    |
|                   ^                      |
|                   |                      |
|  +----------------------------------+    |
|  |                                  |    |
|  |       Application Layer          |    |
|  |  (Use Cases, Application         |    |
|  |   Services, Domain Events)       |    |
|  |                                  |    |
|  +----------------------------------+    |
|                   ^                      |
|                   |                      |
|  +----------------------------------+    |
|  |                                  |    |
|  |      Infrastructure Layer        |    |
|  |  (Controllers, Repositories,     |    |
|  |   External Services Adapters)    |    |
|  |                                  |    |
|  +----------------------------------+    |
|                                          |
+------------------------------------------+
```

### 1.4 Directory Structure

Follow a DDD-oriented directory structure to reflect bounded contexts and layers:

```
src/
 ├── main/
 │   ├── java/
 │   │   └── app/quantun/blog/
 │   │       ├── Application.java (Main entry point)
 │   │       ├── shared/
 │   │       │   ├── domain/
 │   │       │   │   ├── AggregateRoot.java
 │   │       │   │   ├── ValueObject.java
 │   │       │   │   └── DomainException.java
 │   │       │   └── infrastructure/
 │   │       │       ├── config/
 │   │       │       └── security/
 │   │       ├── content/              (Bounded Context)
 │   │       │   ├── domain/
 │   │       │   │   ├── model/        (Entities, Value Objects)
 │   │       │   │   │   ├── Article.java
 │   │       │   │   │   ├── ArticleId.java
 │   │       │   │   │   └── Content.java
 │   │       │   │   ├── repository/   (Repository interfaces)
 │   │       │   │   │   └── ArticleRepository.java
 │   │       │   │   └── service/      (Domain Services)
 │   │       │   │       └── ArticleValidationService.java
 │   │       │   ├── application/
 │   │       │   │   ├── usecase/      (Use Cases)
 │   │       │   │   │   ├── CreateArticleUseCase.java
 │   │       │   │   │   └── PublishArticleUseCase.java
 │   │       │   │   └── ArticleMapper.java
 │   │       │   ├── infrastructure/
 │   │       │   │   ├── persistence/  (Repository implementations)
 │   │       │   │   │   └── MongoArticleRepository.java
 │   │       │   │   └── web/          (Controllers)
 │   │       │   │       └── ArticleController.java
 │   │       │   └── dto/              (Data Transfer Objects)
 │   │       │       ├── ArticleResponse.java
 │   │       │       └── CreateArticleRequest.java
 │   │       └── user/                 (Another Bounded Context)
 │   │           ├── domain/
 │   │           ├── application/
 │   │           ├── infrastructure/
 │   │           └── dto/
 │   └── resources/
 │       ├── application.properties or application.yml
 │       ├── static/            (Static resources)
 │       └── templates/         (View templates)
 └── test/
     ├── java/
     │   └── app/quantun/blog/
     │       ├── content/
     │       │   ├── domain/    (Domain tests)
     │       │   ├── application/ (Use case tests)
     │       │   └── infrastructure/ (Controller/Repository tests)
     │       └── user/
     └── resources/
         └── application-test.properties or application-test.yml
```

Key principles for DDD directory structure:

- **Bounded Context Separation**: Organize code by business domains
- **Layer Separation**: Clearly separate domain, application, and infrastructure layers
- **Domain Model Isolation**: Keep domain model free from infrastructure concerns
- **Shared Kernel**: Common code shared between bounded contexts goes in `shared` package

## 2. Application Layer

The application layer orchestrates the flow of domain objects to accomplish specific use cases. It acts as a thin layer between the domain and infrastructure.

### 2.1 Use Cases

Use cases represent specific business operations or user interactions:

- Implement one class per use case following the Single Responsibility Principle
- Focus on orchestration rather than business logic
- Use domain services and repositories to accomplish tasks
- Return DTOs rather than domain objects

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateArticleUseCase {
    private final ArticleRepository articleRepository;
    private final ArticleSlugService slugService;
    private final ArticleMapper mapper;

    @Transactional
    public ArticleResponse execute(CreateArticleRequest request, String authorId) {
        log.info("Creating new article with title: {}", request.getTitle());

        // Create domain objects
        Title title = new Title(request.getTitle());
        Content content = new Content(request.getContent());
        AuthorId owner = new AuthorId(authorId);
        ArticleId articleId = new ArticleId(UUID.randomUUID().toString());

        // Generate slug using domain service
        Slug slug = slugService.generateUniqueSlug(title);

        // Create aggregate using factory method
        Article article = Article.create(articleId, title, content, owner);

        // Add tags if provided
        if (request.getTags() != null) {
            request.getTags().forEach(article::addTag);
        }

        // Persist through repository
        Article savedArticle = articleRepository.save(article);

        // Map to response DTO
        return mapper.toArticleResponse(savedArticle);
    }
}
```

Key principles for use cases:

- **Input Validation**: Validate input at the boundary
- **Transaction Management**: Handle transactions at this level
- **Error Handling**: Translate domain exceptions to application exceptions
- **Logging**: Log the beginning and end of use case execution
- **Authorization**: Verify user permissions before executing domain logic

### 2.2 Application Services

Application services coordinate multiple use cases or provide cross-cutting functionality:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleManagementService {
    private final CreateArticleUseCase createArticleUseCase;
    private final UpdateArticleUseCase updateArticleUseCase;
    private final PublishArticleUseCase publishArticleUseCase;
    private final GetArticleUseCase getArticleUseCase;

    public ArticleResponse createDraftArticle(CreateArticleRequest request, String authorId) {
        log.info("Creating draft article for author: {}", authorId);
        return createArticleUseCase.execute(request, authorId);
    }

    public ArticleResponse createAndPublishArticle(CreateArticleRequest request, String authorId) {
        log.info("Creating and publishing article for author: {}", authorId);
        ArticleResponse draftArticle = createArticleUseCase.execute(request, authorId);
        return publishArticleUseCase.execute(draftArticle.getId(), authorId);
    }

    public ArticleResponse updateAndPublishIfReady(String articleId, UpdateArticleRequest request, String authorId) {
        log.info("Updating article: {} and publishing if ready", articleId);
        ArticleResponse updatedArticle = updateArticleUseCase.execute(articleId, request, authorId);

        // Check if article meets publishing criteria
        if (request.isPublishAfterUpdate() && isReadyToPublish(updatedArticle)) {
            return publishArticleUseCase.execute(articleId, authorId);
        }

        return updatedArticle;
    }

    private boolean isReadyToPublish(ArticleResponse article) {
        // Application-level validation logic
        return article.getContent().split("\\s+").length >= 50 && !article.getTags().isEmpty();
    }
}
```

### 2.3 Domain Event Handling

Domain events allow for loose coupling between different parts of the application:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleEventHandler {
    private final NotificationService notificationService;
    private final SearchIndexService searchIndexService;

    @EventListener
    public void handleArticlePublishedEvent(ArticlePublishedEvent event) {
        log.info("Handling article published event for article: {}", event.getArticleId());

        // Notify followers
        notificationService.notifyFollowers(event.getAuthorId(), 
            "New article published", 
            "Check out the new article: " + event.getTitle());

        // Index for search
        searchIndexService.indexArticle(event.getArticleId(), event.getTitle(), event.getContent());
    }

    @EventListener
    public void handleArticleUpdatedEvent(ArticleUpdatedEvent event) {
        log.info("Handling article updated event for article: {}", event.getArticleId());

        // Update search index
        if (event.isPublished()) {
            searchIndexService.updateArticleIndex(event.getArticleId(), event.getTitle(), event.getContent());
        }
    }
}
```

Domain event implementation:

```java
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}

public class ArticlePublishedEvent {
    private final String articleId;
    private final String authorId;
    private final String title;
    private final String content;
    private final LocalDateTime publishedAt;

    // Constructor and getters
}
```

## 3. Infrastructure Layer

The infrastructure layer implements technical capabilities that support the higher layers, providing implementations for the interfaces defined in the domain and application layers.

### 3.1 REST Controllers

Controllers in DDD serve as adapters between the HTTP interface and the application layer:

- Keep controllers thin, focusing only on HTTP concerns
- Delegate all business logic to application use cases
- Map HTTP requests to application layer DTOs
- Handle HTTP-specific concerns (status codes, headers, etc.)

```java
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final CreateArticleUseCase createArticleUseCase;
    private final GetArticleUseCase getArticleUseCase;
    private final UpdateArticleUseCase updateArticleUseCase;
    private final PublishArticleUseCase publishArticleUseCase;
    private final ListArticlesUseCase listArticlesUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createArticle(
            @Valid @RequestBody CreateArticleRequest request,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Received request to create article from user: {}", userId);
        return createArticleUseCase.execute(request, userId);
    }

    @GetMapping("/{id}")
    public ArticleResponse getArticleById(@PathVariable String id) {
        log.info("Retrieving article with id: {}", id);
        return getArticleUseCase.executeById(id);
    }

    @GetMapping("/slug/{slug}")
    public ArticleResponse getArticleBySlug(@PathVariable String slug) {
        log.info("Retrieving article with slug: {}", slug);
        return getArticleUseCase.executeBySlug(slug);
    }

    @PutMapping("/{id}")
    public ArticleResponse updateArticle(
            @PathVariable String id,
            @Valid @RequestBody UpdateArticleRequest request,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Updating article with id: {}", id);
        return updateArticleUseCase.execute(id, request, userId);
    }

    @PostMapping("/{id}/publish")
    public ArticleResponse publishArticle(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Publishing article with id: {}", id);
        return publishArticleUseCase.execute(id, userId);
    }

    @GetMapping
    public Page<ArticleSummaryResponse> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag) {
        log.info("Listing articles page: {}, size: {}, tag: {}", page, size, tag);
        return listArticlesUseCase.execute(page, size, tag);
    }
}
```

### 3.2 Request/Response Objects

DTOs serve as the contract between the client and the application:

- Use dedicated DTOs for request and response objects
- Implement input validation using Bean Validation (JSR-380)
- Keep DTOs simple and focused on data transfer
- Use Jackson annotations for JSON customization when needed

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 50, message = "Content must be at least 50 characters")
    private String content;

    private Set<String> tags;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String authorId;
    private String status;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private int viewCount;
}
```

### 3.3 Exception Handling

Implement a global exception handler to translate domain and application exceptions to HTTP responses:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        log.warn("Domain exception: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Domain Rule Violation");
        problem.setProperty("errorCategory", "DOMAIN_RULE_VIOLATION");
        problem.setProperty("timestamp", LocalDateTime.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Validation Error");
        problem.setProperty("errors", errors);
        problem.setProperty("errorCategory", "VALIDATION_ERROR");
        problem.setProperty("timestamp", LocalDateTime.now());

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setProperty("errorCategory", "SYSTEM_ERROR");
        problem.setProperty("timestamp", LocalDateTime.now());

        return problem;
    }
}
```

### 3.4 Persistence Implementations

Repository implementations connect the domain model to the database:

```java
@Repository
@RequiredArgsConstructor
@Slf4j
public class ArticleRepositoryImpl implements ArticleRepository {
    private final MongoArticleRepository mongoRepository;

    @Override
    public Article save(Article article) {
        log.debug("Saving article with ID: {}", article.getArticleId().getValue());
        return mongoRepository.save(article);
    }

    @Override
    public Optional<Article> findById(ArticleId id) {
        log.debug("Finding article by ID: {}", id.getValue());
        return mongoRepository.findById(id.getValue());
    }

    @Override
    public Optional<Article> findBySlug(Slug slug) {
        log.debug("Finding article by slug: {}", slug.getValue());
        return mongoRepository.findBySlug(slug.getValue());
    }

    @Override
    public Page<Article> findPublishedArticles(int page, int size) {
        log.debug("Finding published articles page: {}, size: {}", page, size);
        return mongoRepository.findByStatus(
            ArticleStatus.PUBLISHED, 
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        );
    }

    @Override
    public Page<Article> findPublishedArticlesByTag(String tag, int page, int size) {
        log.debug("Finding published articles with tag: {}, page: {}, size: {}", tag, page, size);
        return mongoRepository.findByStatusAndTagsContaining(
            ArticleStatus.PUBLISHED,
            tag.toLowerCase(),
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        );
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        log.debug("Checking if article exists with slug: {}", slug.getValue());
        return mongoRepository.existsBySlug(slug.getValue());
    }
}
```

Spring Data repository interface:

```java
public interface MongoArticleRepository extends MongoRepository<Article, String> {
    Optional<Article> findBySlug(String slug);

    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);

    Page<Article> findByStatusAndTagsContaining(ArticleStatus status, String tag, Pageable pageable);

    boolean existsBySlug(String slug);
}
```

## 4. Testing in DDD

Testing in Domain-Driven Design requires a strategic approach that respects the layered architecture and focuses on testing business rules and behaviors at the appropriate level.

### 4.1 Domain Model Testing

Domain model tests focus on the business rules and invariants of your domain objects:

- Test domain entities, value objects, and aggregates in isolation
- Focus on business rules and invariants
- Use simple unit tests without frameworks when possible
- Test domain services with their collaborators mocked

```java
@ExtendWith(MockitoExtension.class)
class ArticleTest {

    @Test
    @DisplayName("Should create article in draft status")
    void shouldCreateArticleInDraftStatus() {
        // Arrange
        ArticleId id = new ArticleId("123");
        Title title = new Title("Test Article");
        Content content = new Content("This is a test article content");
        AuthorId authorId = new AuthorId("author-1");

        // Act
        Article article = Article.create(id, title, content, authorId);

        // Assert
        assertThat(article.getArticleId()).isEqualTo(id);
        assertThat(article.getTitle()).isEqualTo(title);
        assertThat(article.getContent()).isEqualTo(content);
        assertThat(article.getAuthorId()).isEqualTo(authorId);
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.getTags()).isEmpty();
        assertThat(article.getViewCount()).isZero();
        assertThat(article.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when updating title of published article")
    void shouldThrowException_whenUpdatingTitleOfPublishedArticle() {
        // Arrange
        Article article = createPublishedArticle();
        Title newTitle = new Title("Updated Title");

        // Act & Assert
        assertThatThrownBy(() -> article.updateTitle(newTitle))
            .isInstanceOf(DomainException.class)
            .hasMessage("Cannot update title of published article");
    }

    @Test
    @DisplayName("Should add tag to article")
    void shouldAddTagToArticle() {
        // Arrange
        Article article = createDraftArticle();

        // Act
        article.addTag("java");
        article.addTag("spring");

        // Assert
        assertThat(article.getTags()).containsExactlyInAnyOrder("java", "spring");
    }

    private Article createDraftArticle() {
        return Article.create(
            new ArticleId("123"),
            new Title("Test Article"),
            new Content("This is a test article content"),
            new AuthorId("author-1")
        );
    }

    private Article createPublishedArticle() {
        Article article = createDraftArticle();
        article.publish();
        return article;
    }
}
```

### 4.2 Use Case Testing

Use case tests verify that application services correctly orchestrate domain objects:

- Mock repositories and domain services
- Focus on the orchestration flow
- Verify that domain objects are used correctly
- Test error handling and edge cases

```java
@ExtendWith(MockitoExtension.class)
class CreateArticleUseCaseTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleSlugService slugService;

    @Mock
    private ArticleMapper mapper;

    @InjectMocks
    private CreateArticleUseCase useCase;

    @Test
    @DisplayName("Should create article successfully")
    void shouldCreateArticleSuccessfully() {
        // Arrange
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("Test Article");
        request.setContent("This is a test article with sufficient content for testing purposes");
        request.setTags(Set.of("test", "article"));

        String authorId = "author-1";

        // Capture the article being saved
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);

        // Mock slug service
        when(slugService.generateUniqueSlug(any(Title.class))).thenReturn(new Slug("test-article"));

        // Mock repository
        when(articleRepository.save(articleCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // Mock mapper
        ArticleResponse expectedResponse = new ArticleResponse();
        expectedResponse.setId("123");
        expectedResponse.setTitle("Test Article");
        when(mapper.toArticleResponse(any(Article.class))).thenReturn(expectedResponse);

        // Act
        ArticleResponse result = useCase.execute(request, authorId);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);

        // Verify the article properties
        Article savedArticle = articleCaptor.getValue();
        assertThat(savedArticle.getTitle().getValue()).isEqualTo("Test Article");
        assertThat(savedArticle.getContent().getValue()).isEqualTo(request.getContent());
        assertThat(savedArticle.getAuthorId().getValue()).isEqualTo(authorId);
        assertThat(savedArticle.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(savedArticle.getTags()).containsExactlyInAnyOrder("test", "article");

        // Verify interactions
        verify(slugService).generateUniqueSlug(any(Title.class));
        verify(articleRepository).save(any(Article.class));
        verify(mapper).toArticleResponse(any(Article.class));
    }
}
```

### 4.3 Infrastructure Testing

Infrastructure tests verify that adapters correctly implement interfaces defined in the domain:

- Test repository implementations with real or in-memory databases
- Test controllers with MockMvc
- Focus on the mapping between domain and external systems
- Use Spring Boot test slices to isolate components

```java
@DataMongoTest
class ArticleRepositoryImplTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoArticleRepository mongoRepository;

    private ArticleRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ArticleRepositoryImpl(mongoRepository);
        mongoTemplate.dropCollection(Article.class);
    }

    @Test
    @DisplayName("Should save and retrieve article by ID")
    void shouldSaveAndRetrieveArticleById() {
        // Arrange
        Article article = createArticle();

        // Act
        Article savedArticle = repository.save(article);
        Optional<Article> foundArticle = repository.findById(article.getArticleId());

        // Assert
        assertThat(foundArticle).isPresent();
        assertThat(foundArticle.get().getArticleId()).isEqualTo(article.getArticleId());
        assertThat(foundArticle.get().getTitle()).isEqualTo(article.getTitle());
    }

    @Test
    @DisplayName("Should find article by slug")
    void shouldFindArticleBySlug() {
        // Arrange
        Article article = createArticle();
        repository.save(article);

        // Act
        Optional<Article> foundArticle = repository.findBySlug(article.getSlug());

        // Assert
        assertThat(foundArticle).isPresent();
        assertThat(foundArticle.get().getSlug()).isEqualTo(article.getSlug());
    }

    private Article createArticle() {
        return Article.create(
            new ArticleId("test-id"),
            new Title("Test Article"),
            new Content("Test content for the article"),
            new AuthorId("author-1")
        );
    }
}
```

### 4.4 Working with MockBean

`@MockBean` is a Spring Boot test annotation that adds Mockito mocks to the Spring ApplicationContext:

```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateArticleUseCase createArticleUseCase;

    @MockBean
    private GetArticleUseCase getArticleUseCase;

    @Test
    @DisplayName("Should create article and return 201 Created")
    void shouldCreateArticleAndReturn201Created() throws Exception {
        // Arrange
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("Test Article");
        request.setContent("This is a test article with sufficient content for testing purposes");

        ArticleResponse response = new ArticleResponse();
        response.setId("123");
        response.setTitle("Test Article");

        when(createArticleUseCase.execute(any(), eq("user-1"))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/articles")
                .header("X-User-ID", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.title").value("Test Article"));

        verify(createArticleUseCase).execute(any(), eq("user-1"));
    }

    private String asJsonString(Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
```

#### Key points when working with MockBean:

1. **Context Management**: `@MockBean` replaces or adds beans to the Spring context
2. **Slower Tests**: Using `@MockBean` causes the Spring context to reload, so use sparingly
3. **Reset After Tests**: Mocks are automatically reset after each test
4. **Bean Naming**: `@MockBean` can specify the name of the bean to replace with `name` attribute
5. **Verification**: Always verify important interactions with the mock

### 4.5 Testing with Slices and MockBean

Spring Boot provides test slice annotations to load only specific parts of the application:

```java
// Test only MongoDB repositories
@DataMongoTest
class MongoArticleRepositoryTest {

    @Autowired
    private MongoArticleRepository repository;

    @Test
    @DisplayName("Should find published articles by tag")
    void shouldFindPublishedArticlesByTag() {
        // Test implementation
    }
}

// Test only REST controllers
@WebMvcTest(ArticleController.class)
class ArticleControllerSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateArticleUseCase createArticleUseCase;

    @MockBean
    private GetArticleUseCase getArticleUseCase;

    @Test
    @DisplayName("Should validate input")
    void shouldValidateInput() throws Exception {
        // Test implementation
    }
}
```

## 5. Libraries and Dependencies

### 5.1 Core Libraries

- **Spring Boot**: Foundation for the application
- **Spring Data MongoDB**: For MongoDB database access
- **Spring Validation**: Input validation using Bean Validation (JSR-380)
- **Spring HATEOAS**: For creating REST representations with hypermedia links
- **Spring Security**: Authentication and authorization
- **Lombok**: Reduce boilerplate code with annotations like `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`
- **MapStruct**: Type-safe bean mapping between domain objects and DTOs
- **Problem Spring Web**: RFC 7807 Problem Details implementation for error responses

### 5.2 Database

- **MongoDB**: Document database that works well with DDD aggregates
- **Spring Data MongoDB**: Repository abstraction for MongoDB
- **MongoDB Testcontainers**: For integration testing with real MongoDB instances
- **Embedded MongoDB**: For faster integration tests

### 5.3 Logging

- **SLF4J with Logback**: Logging facade and implementation
- **Logstash Logback Encoder**: JSON formatting for structured logging
- **MDC (Mapped Diagnostic Context)**: For adding context to log entries

### 5.4 API Documentation

- **SpringDoc OpenAPI**: OpenAPI 3.0 documentation
- **SpringDoc UI**: Swagger UI for API exploration

### 5.5 HTTP Client with RestClient

Spring Boot 3.4 includes support for the newer RestClient from Spring Framework 6.1, which is particularly useful for implementing adapters to external systems in a DDD architecture:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalAuthorServiceAdapter implements AuthorService {

    private final RestClient restClient;

    public ExternalAuthorServiceAdapter(RestClient.Builder restClientBuilder, 
                                       @Value("${services.author.baseUrl}") String baseUrl) {
        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                log.error("Author service client error: {} {}", 
                          response.getStatusCode(), response.getBodyAsString());
                throw new ExternalServiceException("Author service error: " + response.getStatusCode());
            })
            .build();
    }

    @Override
    public AuthorDetails getAuthorDetails(AuthorId authorId) {
        log.info("Fetching author details for ID: {}", authorId.getValue());
        try {
            ExternalAuthorDto dto = restClient.get()
                .uri("/authors/{id}", authorId.getValue())
                .retrieve()
                .body(ExternalAuthorDto.class);

            return mapToAuthorDetails(dto);
        } catch (Exception e) {
            log.error("Failed to fetch author details", e);
            throw new DomainException("Unable to retrieve author information");
        }
    }

    private AuthorDetails mapToAuthorDetails(ExternalAuthorDto dto) {
        // Mapping logic
        return new AuthorDetails(
            new AuthorId(dto.getId()),
            dto.getName(),
            dto.getBio(),
            dto.getFollowersCount()
        );
    }
}
```

## 6. Common Pitfalls and Gotchas

### 6.1 DDD-Specific Challenges

- **Anemic Domain Model**: Avoid creating domain models that are just data holders without behavior
- **Misidentifying Aggregates**: Make aggregates as small as possible while maintaining consistency boundaries
- **Overusing Value Objects**: Not everything needs to be a value object; use them for concepts with identity based on attributes
- **Ignoring Bounded Contexts**: Failing to identify and separate different contexts leads to a muddled model
- **Repository Overuse**: Don't create a repository for every entity; repositories are for aggregate roots only
- **Domain Logic in Application Layer**: Keep business rules in the domain layer, not in application services
- **Leaking Domain Objects**: Don't expose domain objects to the outside world; use DTOs at the boundaries

```java
// AVOID: Anemic domain model
public class Article {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String status;

    // Getters and setters only, no behavior
}

// BETTER: Rich domain model with behavior
public class Article extends AggregateRoot {
    private ArticleId id;
    private Title title;
    private Content content;
    private AuthorId authorId;
    private ArticleStatus status;

    // Private constructor to enforce factory method
    private Article() {}

    // Factory method
    public static Article create(ArticleId id, Title title, Content content, AuthorId authorId) {
        // Creation logic with validation
    }

    // Business methods that enforce invariants
    public void publish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new DomainException("Article is already published");
        }
        // Publishing logic
    }
}
```

### 6.2 Edge Cases to Consider

- **Eventual Consistency**: In distributed systems, handle the fact that data might not be immediately consistent
- **Concurrent Modifications**: Use optimistic locking with version fields to detect concurrent modifications
- **Long-Running Processes**: Consider using sagas or process managers for operations that span multiple aggregates
- **Large Aggregates**: Be cautious with large aggregates that might cause performance issues; consider breaking them down
- **Cross-Bounded Context Queries**: Complex queries across bounded contexts might require specialized query models

### 6.3 Version Compatibility

- **Spring Boot Version**: Ensure dependencies are compatible with your Spring Boot version
- **Java Version**: Verify Java version compatibility with Spring Boot release
- **MongoDB Driver**: Check MongoDB driver compatibility with your MongoDB server version

### 6.4 Anti-Patterns to Avoid

- **Smart UI Anti-Pattern**: Don't put domain logic in controllers or UI components
- **Transaction Script**: Avoid procedural service methods that implement entire use cases without domain objects
- **Active Record**: Don't mix persistence concerns with domain logic in the same class
- **God Class**: Break down large classes into smaller, focused ones
- **Feature Envy**: Methods that are more interested in another class's data than their own should be moved
- **Shotgun Surgery**: If a change requires modifications in many classes, your design might need improvement

```java
// AVOID: Transaction Script anti-pattern
@Service
public class ArticleService {
    private final JdbcTemplate jdbcTemplate;

    public void publishArticle(String articleId, String userId) {
        // Direct database operations without domain model
        jdbcTemplate.update(
            "UPDATE articles SET status = 'PUBLISHED', published_at = ? WHERE id = ? AND author_id = ?",
            LocalDateTime.now(), articleId, userId
        );
    }
}

// BETTER: Domain-driven approach
@Service
public class PublishArticleUseCase {
    private final ArticleRepository articleRepository;
    private final ArticleValidationService validationService;

    @Transactional
    public ArticleResponse execute(String articleId, String userId) {
        ArticleId id = new ArticleId(articleId);
        AuthorId authorId = new AuthorId(userId);

        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new DomainException("Article not found"));

        validationService.validateAuthorPermission(article, authorId);
        validationService.validateArticleCanBePublished(article);

        article.publish();
        Article savedArticle = articleRepository.save(article);

        return mapper.toArticleResponse(savedArticle);
    }
}
```

## 7. Performance Optimization Techniques

### 7.1 Database Query Optimization

In DDD applications, database optimization requires special attention:

- **Aggregate Design**: Design aggregates to support efficient querying and loading
- **Read Models**: Create specialized read models for complex queries
- **Command-Query Responsibility Segregation (CQRS)**: Separate read and write models for high-performance scenarios
- **Indexes**: Create indexes for frequently queried fields, especially aggregate identifiers
- **Projections**: Use MongoDB projections to retrieve only needed fields
- **Pagination**: Always paginate when retrieving collections of aggregates

```java
@Repository
@RequiredArgsConstructor
@Slf4j
public class ArticleQueryRepository {
    private final MongoTemplate mongoTemplate;

    public Page<ArticleSummaryDto> findPublishedArticleSummaries(int page, int size) {
        log.debug("Finding published article summaries, page: {}, size: {}", page, size);

        // Create a query that only retrieves necessary fields
        Query query = new Query()
            .addCriteria(Criteria.where("status").is(ArticleStatus.PUBLISHED.name()))
            .with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));

        // Use projection to retrieve only needed fields
        query.fields()
            .include("articleId")
            .include("title")
            .include("slug")
            .include("authorId")
            .include("publishedAt")
            .include("tags");

        // Execute query
        List<ArticleSummaryDto> summaries = mongoTemplate.find(query, ArticleSummaryDto.class, "articles");
        long total = mongoTemplate.count(query.skip(-1).limit(-1), "articles");

        return new PageImpl<>(summaries, PageRequest.of(page, size), total);
    }
}
```

### 7.2 Caching

Implement strategic caching for read-heavy operations:

```java
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        log.info("Initializing cache manager");
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
            new ConcurrentMapCache("articles"),
            new ConcurrentMapCache("articleSummaries"),
            new ConcurrentMapCache("authors")
        ));
        return cacheManager;
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
public class CachedArticleRepository implements ArticleRepository {
    private final MongoArticleRepository mongoRepository;

    @Override
    public Article save(Article article) {
        log.debug("Saving article with ID: {}", article.getArticleId().getValue());
        return mongoRepository.save(article);
    }

    @Override
    @Cacheable(value = "articles", key = "#id.value")
    public Optional<Article> findById(ArticleId id) {
        log.debug("Finding article by ID: {} (cache miss)", id.getValue());
        return mongoRepository.findById(id.getValue());
    }

    @Override
    @CacheEvict(value = "articles", key = "#article.articleId.value")
    public void delete(Article article) {
        log.debug("Deleting article with ID: {}", article.getArticleId().getValue());
        mongoRepository.delete(article);
    }
}
```

Key caching considerations in DDD:

- Cache aggregate roots by their identifiers
- Invalidate cache entries when aggregates are modified
- Consider using Redis or Hazelcast for distributed caching in multi-instance environments
- Be cautious with caching in write-heavy scenarios

### 7.3 Asynchronous Processing

Use asynchronous processing for operations that don't require immediate consistency:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticlePublicationService {
    private final ArticleRepository articleRepository;
    private final DomainEventPublisher eventPublisher;

    @Async
    public CompletableFuture<Void> processArticlePublication(ArticleId articleId) {
        log.info("Asynchronously processing publication for article: {}", articleId.getValue());

        try {
            // Retrieve the article
            Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new DomainException("Article not found"));

            // Perform time-consuming operations
            generateArticleThumbnails(article);
            updateSearchIndex(article);
            notifySubscribers(article);

            // Publish domain event
            eventPublisher.publish(new ArticlePublicationCompletedEvent(articleId));

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to process article publication", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void generateArticleThumbnails(Article article) {
        // Time-consuming operation
    }

    private void updateSearchIndex(Article article) {
        // Time-consuming operation
    }

    private void notifySubscribers(Article article) {
        // Time-consuming operation
    }
}
```

### 7.4 Pagination

Implement pagination for all collection-based queries:

```java
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ListArticlesUseCase listArticlesUseCase;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ArticleSummaryResponse>>> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag,
            PagedResourcesAssembler<ArticleSummaryResponse> assembler) {

        log.info("Listing articles page: {}, size: {}, tag: {}", page, size, tag);
        Page<ArticleSummaryResponse> articlePage = listArticlesUseCase.execute(page, size, tag);

        return ResponseEntity.ok(
            assembler.toModel(
                articlePage,
                linkTo(methodOn(ArticleController.class).listArticles(page, size, tag, assembler))
                    .withSelfRel()
            )
        );
    }
}
```

### 7.5 Load Testing

- Test each bounded context separately to identify bottlenecks
- Focus on aggregate root loading performance
- Measure repository implementation efficiency
- Test with realistic data volumes that match production
- Monitor memory usage to detect potential aggregate size issues

## 8. Development Environment and Tooling

### 8.1 Recommended Tools for DDD

- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code with Spring Boot extensions
- **Build Tool**: Gradle with multi-module support for bounded contexts
- **Version Control**: Git with conventional commit messages and feature branches per bounded context
- **API Testing**: Postman or Insomnia with environment variables for different contexts
- **Database Tools**: MongoDB Compass for document database visualization
- **Diagram Tools**: Draw.io or PlantUML for context maps and aggregate visualizations
- **Event Storming Tools**: Miro or Mural for collaborative domain modeling

### 8.2 Code Quality Tools

- **Static Code Analysis**: SonarQube with custom rules for DDD patterns
- **Architecture Validation**: ArchUnit to enforce DDD architectural constraints
- **Style Enforcement**: Checkstyle to ensure coding style consistency
- **Code Quality**: PMD, SpotBugs to detect potential problems
- **EditorConfig**: Use EditorConfig to maintain consistent formatting across editors

Example of ArchUnit tests to enforce DDD architecture:

```java
@AnalyzeClasses(packages = "app.quantun.blog")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domainShouldNotDependOnApplication =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule repositoriesShouldBeImplementedInInfrastructure =
        classes().that().haveNameMatching(".*Repository")
            .and().areNotInterfaces()
            .should().resideInAPackage("..infrastructure.persistence..");

    @ArchTest
    static final ArchRule aggregateRootsShouldExtendAggregateRoot =
        classes().that().areAnnotatedWith(Document.class)
            .should().beAssignableTo(AggregateRoot.class);

    @ArchTest
    static final ArchRule valueObjectsShouldExtendValueObject =
        classes().that().haveSimpleNameEndingWith("Id")
            .or().haveSimpleNameEndingWith("Name")
            .or().haveSimpleNameEndingWith("Email")
            .or().haveSimpleNameEndingWith("Address")
            .should().beAssignableTo(ValueObject.class);
}
```

## 9. General Best Practices

### 9.1 Code Quality in DDD

- Follow the ubiquitous language consistently in code
- Implement peer code reviews with domain experts when possible
- Keep methods small and focused on a single responsibility
- Use meaningful names that reflect domain concepts
- Write comprehensive tests at all levels (domain, application, infrastructure)
- Document domain decisions and the reasoning behind them
- Use comments to explain "why" not "what" the code does
- Refactor continuously as your understanding of the domain evolves

### 9.2 Performance Optimization

- Design aggregates with performance in mind (size, loading patterns)
- Use read models and projections for complex queries
- Consider CQRS for high-performance scenarios
- Implement domain-specific caching strategies
- Use asynchronous domain events for non-critical operations
- Monitor aggregate loading and saving performance

### 9.3 Documentation

- Maintain a glossary of domain terms (ubiquitous language)
- Document bounded contexts and their relationships (context map)
- Create visual representations of aggregates and their relationships
- Document architecture decisions (ADRs) with domain context
- Keep API documentation synchronized with code
- Include domain event flows and process diagrams
- Document anti-corruption layers between bounded contexts

Example of a domain glossary:

```
# Blog Domain Glossary

## Article
A piece of content written by an Author, containing a title, content body, and optional tags.

## Author
A person who creates and publishes Articles.

## Slug
A URL-friendly version of an Article's title, used in web addresses.

## Tag
A keyword or term assigned to an Article, making it easier to find related content.

## Publication
The process of making an Article publicly visible and available to readers.
```

## 10. Additional Considerations

### 10.1 Internationalization and Localization

When implementing internationalization in a DDD context:

- Keep translation concerns in the infrastructure layer
- Use value objects for locale-sensitive concepts
- Consider cultural differences as part of the domain model when relevant
- Implement domain services for locale-specific business rules
- Use Spring's `MessageSource` for externalized messages
- Configure locale resolution strategies

```java
@Component
@RequiredArgsConstructor
public class LocalizedContentService {
    private final MessageSource messageSource;

    public Content getLocalizedContent(Content originalContent, Locale locale) {
        // Domain logic for content localization
        if (isDefaultLocale(locale)) {
            return originalContent;
        }

        // Get localized version or fallback to original
        return getLocalizedVersionOrFallback(originalContent, locale);
    }

    public Title getLocalizedTitle(Title originalTitle, Locale locale) {
        // Domain logic for title localization
        if (isDefaultLocale(locale)) {
            return originalTitle;
        }

        // Get localized version or fallback to original
        return getLocalizedTitleOrFallback(originalTitle, locale);
    }
}
```

### 10.2 Advanced API Design Principles

When designing APIs for DDD-based systems:

- Align API resources with aggregate boundaries
- Use hypermedia (HATEOAS) to represent domain relationships
- Design comprehensive error responses that reflect domain exceptions
- Version APIs to accommodate domain model evolution
- Implement consistent naming that reflects the ubiquitous language
- Consider GraphQL for complex domain queries across aggregates

```java
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final GetArticleUseCase getArticleUseCase;

    @GetMapping("/{id}")
    public EntityModel<ArticleResponse> getArticle(@PathVariable String id) {
        ArticleResponse article = getArticleUseCase.executeById(id);

        // Add hypermedia links that reflect domain relationships
        return EntityModel.of(article,
            linkTo(methodOn(ArticleController.class).getArticle(id)).withSelfRel(),
            linkTo(methodOn(AuthorController.class).getAuthor(article.getAuthorId())).withRel("author"),
            linkTo(methodOn(ArticleController.class).getArticlesByTag(null, article.getTags().iterator().next()))
                .withRel("similarArticles")
        );
    }
}
```

## Conclusion

These guidelines are designed to ensure quality, maintainability, and robustness for Spring Boot 3.4 applications built using Domain-Driven Design principles. By focusing on the domain model as the core of your application and organizing code around business capabilities, teams can create more maintainable and flexible systems that better align with business needs.

Remember that DDD is not just about technical patterns but also about collaboration between domain experts and developers to create a shared understanding of the problem domain. The ubiquitous language developed during this collaboration should be reflected in your code, documentation, and discussions.

Teams should adapt these practices to their specific requirements while maintaining the core DDD principles outlined in this document:

- Focus on the core domain and domain logic
- Base complex designs on models of the domain
- Collaborate with domain experts to improve the application model
- Continuously refine the model as the domain evolves

By following these guidelines, you'll be well-positioned to create Spring Boot applications that not only meet technical requirements but also accurately reflect and solve business problems.
