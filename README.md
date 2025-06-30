# Blog Application with Hexagonal Architecture

## Introduction

This is a blog application built with Spring Boot 3.5.3 following Hexagonal Architecture principles. The application allows users to create, read, update, and publish blog posts, as well as add comments and manage authors.

## Architecture Overview

This application follows the Hexagonal Architecture (also known as Ports and Adapters) pattern, which separates the core domain logic from external concerns. The architecture consists of three main layers:

### Domain Layer

The domain layer contains the core business logic, entities, and business rules. It is completely isolated from external concerns like databases, UI, or external services.

Key components:
- **Domain Models**: BlogPost, Author, Comment, Tag
- **Domain Ports**: Interfaces that define how the application interacts with the outside world
  - Input Ports (Use Cases): Define operations that can be performed on the domain
  - Output Ports: Define interfaces for persistence and external services

### Application Layer

The application layer orchestrates the flow of data to and from the domain layer. It implements the use cases defined by the input ports.

Key components:
- **Use Cases**: Implementations of input ports that orchestrate domain operations
- **Domain Services**: Services that implement domain logic that doesn't naturally fit into entities

### Infrastructure Layer

The infrastructure layer contains all the technical details and implementations of the interfaces defined in the domain layer.

Key components:
- **Input Adapters**: Controllers that handle HTTP requests
- **Output Adapters**: Repository implementations that interact with the database
- **Configuration**: Spring configuration classes

## Directory Structure

```
src/
 ├── main/
 │   ├── java/
 │   │   └── app/quantun/blog/
 │   │       ├── BlogApplication.java (Main entry point)
 │   │       ├── domain/
 │   │       │   ├── model/        (Domain entities)
 │   │       │   │   ├── BlogPost.java
 │   │       │   │   ├── Author.java
 │   │       │   │   ├── Comment.java
 │   │       │   │   ├── Tag.java
 │   │       │   │   └── ...
 │   │       │   ├── port/
 │   │       │   │   ├── in/       (Input ports/use cases)
 │   │       │   │   │   ├── CreateBlogPostUseCase.java
 │   │       │   │   │   ├── GetBlogPostUseCase.java
 │   │       │   │   │   └── ...
 │   │       │   │   └── out/      (Output ports)
 │   │       │   │       ├── BlogPostRepositoryPort.java
 │   │       │   │       ├── AuthorRepositoryPort.java
 │   │       │   │       └── ...
 │   │       │   └── service/      (Domain services)
 │   │       │       ├── BlogPostService.java
 │   │       │       └── ...
 │   │       ├── infrastructure/
 │   │       │   ├── adapter/
 │   │       │   │   ├── in/       (Input adapters)
 │   │       │   │   │   ├── web/  (Controllers)
 │   │       │   │   │   │   ├── BlogPostController.java
 │   │       │   │   │   │   └── ...
 │   │       │   │   │   └── dto/  (Data Transfer Objects)
 │   │       │   │   │       ├── BlogPostResponse.java
 │   │       │   │   │       └── ...
 │   │       │   │   └── out/      (Output adapters)
 │   │       │   │       └── persistence/ (Repository implementations)
 │   │       │   │           ├── mongo/
 │   │       │   │           │   ├── adapter/
 │   │       │   │           │   │   ├── BlogPostRepositoryAdapter.java
 │   │       │   │           │   │   └── ...
 │   │       │   │           │   ├── entity/
 │   │       │   │           │   │   ├── BlogPostEntity.java
 │   │       │   │           │   │   └── ...
 │   │       │   │           │   ├── mapper/
 │   │       │   │           │   │   ├── BlogPostEntityMapper.java
 │   │       │   │           │   │   └── ...
 │   │       │   │           │   └── repository/
 │   │       │   │               ├── BlogPostMongoRepository.java
 │   │       │   │               └── ...
 │   │       │   └── config/       (Configuration)
 │   │       │       ├── BeanConfiguration.java
 │   │       │       └── ...
 │   │       └── shared/
 │   │           ├── exception/    (Shared exceptions)
 │   │           └── valueobject/  (Shared value objects)
 │   │               ├── Email.java
 │   │               ├── Slug.java
 │   │               └── ...
 │   └── resources/
 │       └── application.properties
 └── test/
     └── java/
         └── app/quantun/blog/
             ├── domain/        (Domain tests)
             ├── application/   (Use case tests)
             └── infrastructure/ (Adapter tests)
```

## Technologies Used

- **Java 21**: The programming language used
- **Spring Boot 3.5.3**: The framework used for building the application
- **Spring Data MongoDB**: For MongoDB database access
- **Spring Validation**: For input validation
- **Lombok**: For reducing boilerplate code
- **MapStruct**: For object mapping between domain objects and DTOs
- **SpringDoc OpenAPI**: For API documentation
- **Testcontainers**: For integration testing with real MongoDB instances
- **JUnit 5**: For unit and integration testing

## Getting Started

### Prerequisites

- Java 21 or higher
- Docker and Docker Compose (for running MongoDB)
- Gradle 8.x or higher

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application using Gradle:
   ```
   ./gradlew bootRun
   ```
4. The application will be available at http://localhost:8080

### API Documentation

Once the application is running, you can access the API documentation at:
- http://localhost:8080/swagger-ui.html

## Testing

To run the tests, use the following command:

```
./gradlew test
```

## Key Features

- Create, read, update, and publish blog posts
- Add comments to blog posts
- Manage authors
- Tag blog posts for categorization

## Hexagonal Architecture Benefits

By following Hexagonal Architecture principles, this application achieves:

1. **Separation of Concerns**: Clear separation between domain logic and infrastructure
2. **Testability**: Domain logic can be tested in isolation without infrastructure dependencies
3. **Flexibility**: Easy to swap out infrastructure components (e.g., switch from MongoDB to another database)
4. **Maintainability**: Changes to one layer don't affect other layers
5. **Domain-Centric**: Focus on the domain model and business rules

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.