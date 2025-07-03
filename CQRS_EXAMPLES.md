# 🎯 CQRS - Ejemplos Prácticos de Uso

## 📋 **Tabla de Contenidos**

1. [Escenarios de Comando](#escenarios-de-comando)
2. [Escenarios de Query](#escenarios-de-query)
3. [Casos de Uso Complejos](#casos-de-uso-complejos)
4. [Patrones de Integración](#patrones-de-integración)
5. [Testing Patterns](#testing-patterns)
6. [Best Practices](#best-practices)

---

## 🔧 **Escenarios de Comando**

### **1. Crear un Blog Post Completo**

```java

@RestController
public class BlogPostController {

    private final CreateBlogPostCommand createCommand;

    @PostMapping("/api/posts")
    public ResponseEntity<BlogPostResponse> createPost(@RequestBody CreateBlogPostRequest request) {
        // 1. Validar entrada
        if (request.title().length() > 200) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Construir comando
        CreateBlogPostCommandData command = new CreateBlogPostCommandData(
                request.title(),
                request.content(),
                request.summary(),
                AuthorId.of(request.authorId()),
                request.tags()
        );

        // 3. Ejecutar comando (business logic)
        BlogPost createdPost = createCommand.createBlogPost(command);

        // 4. Retornar respuesta
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BlogPostResponse.fromDomain(createdPost));
    }
}
```

**Qué sucede internamente:**

```java

@Service
public class BlogPostCommandService {

    public BlogPost createBlogPost(CreateBlogPostCommandData command) {
        // ✅ Verificar autor existe
        Author author = authorRepository.findById(command.authorId())
                .orElseThrow(() -> new AuthorNotFoundException(...));

        // ✅ Verificar slug único
        Slug slug = Slug.fromTitle(command.title());
        if (commandRepository.existsBySlug(slug)) {
            slug = generateUniqueSlug(slug);
        }

        // ✅ Crear entity con business rules
        BlogPost blogPost = BlogPost.createDraft(
                command.title(),
                command.content(),
                command.summary(),
                command.authorId()
        );

        // ✅ Procesar tags (crear si no existen)
        Set<Tag> tags = processTagNames(command.tagNames());
        for (Tag tag : tags) {
            blogPost = blogPost.addTag(tag);
        }

        // ✅ Persistir
        return commandRepository.save(blogPost);
    }
}
```

### **2. Workflow de Publicación**

```java

@Component
public class PublishingWorkflow {

    private final PublishBlogPostCommand publishCommand;
    private final GetBlogPostQuery getQuery;

    public void publishBlogPost(String postId) {
        try {
            // 1. Verificar que el post existe y está en draft
            BlogPostReadModel readModel = getQuery.getById(PostId.of(postId));
            if (readModel.status() != PostStatus.DRAFT) {
                throw new IllegalStateException("Post must be in DRAFT status to publish");
            }

            // 2. Validaciones de negocio adicionales
            if (readModel.content().length() < 100) {
                throw new IllegalStateException("Post content too short for publishing");
            }

            // 3. Ejecutar comando de publicación
            BlogPost publishedPost = publishCommand.publishPost(PostId.of(postId));

            // 4. Registrar evento (si fuera necesario)
            log.info("Post published: {} by author {}",
                    publishedPost.getTitle(), publishedPost.getAuthorId());

        } catch (BlogPostNotFoundException e) {
            log.error("Cannot publish non-existent post: {}", postId);
            throw new PublishingException("Post not found: " + postId);
        }
    }
}
```

### **3. Actualización Compleja con Tags**

```java

@Service
public class BlogPostUpdateService {

    private final UpdateBlogPostCommand updateCommand;

    @Transactional
    public BlogPost updatePostWithTags(String postId, UpdatePostRequest request) {
        // Comando de actualización completa
        UpdateBlogPostCommandData command = new UpdateBlogPostCommandData(
                PostId.of(postId),
                request.title(),
                request.content(),
                request.summary(),
                request.tagNames() // Reemplaza todos los tags existentes
        );

        BlogPost updatedPost = updateCommand.updateBlogPost(command);

        // Log de auditoría
        log.info("Post updated: {} - Tags: {}",
                updatedPost.getTitle(),
                updatedPost.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.joining(", ")));

        return updatedPost;
    }
}
```

---

## 🔍 **Escenarios de Query**

### **1. Dashboard de Autor con Estadísticas**

```java

@Service
public class AuthorDashboardService {

    private final SearchBlogPostQuery searchQuery;
    private final GetBlogPostQuery getQuery;
    private final BlogPostQueryRepositoryPort queryRepository;

    public AuthorDashboard getDashboard(String authorId) {
        AuthorId author = AuthorId.of(authorId);

        // 1. Obtener posts recientes del autor
        PageRequest recentPosts = PageRequest.of(0, 5, "createdAt", SortDirection.DESC);
        PageResponse<BlogPostListItem> recent = searchQuery.getAllByAuthor(author, recentPosts);

        // 2. Obtener estadísticas
        long totalPosts = queryRepository.countPostsByAuthor(author);
        long publishedPosts = recent.content().stream()
                .filter(BlogPostListItem::isPublished)
                .count();

        // 3. Obtener posts más populares (por comentarios)
        PageRequest popularPosts = PageRequest.of(0, 3, "commentCount", SortDirection.DESC);
        PageResponse<BlogPostListItem> popular = searchQuery.getAllByAuthor(author, popularPosts);

        return AuthorDashboard.builder()
                .authorId(authorId)
                .totalPosts(totalPosts)
                .publishedPosts(publishedPosts)
                .recentPosts(recent.content())
                .popularPosts(popular.content())
                .build();
    }
}
```

### **2. Búsqueda Avanzada con Filtros**

```java

@Service
public class BlogSearchService {

    private final SearchBlogPostQuery searchQuery;

    public SearchResults performAdvancedSearch(SearchCriteria criteria) {
        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                criteria.sortBy(),
                criteria.sortDirection()
        );

        PageResponse<BlogPostListItem> results;

        // Diferentes tipos de búsqueda según criterios
        if (criteria.hasSearchTerm()) {
            results = searchQuery.searchByTitleOrContent(
                    criteria.searchTerm(), pageRequest);
        } else if (criteria.hasTag()) {
            results = searchQuery.getByTag(
                    criteria.tag(), pageRequest);
        } else if (criteria.hasAuthor()) {
            results = searchQuery.getAllByAuthor(
                    AuthorId.of(criteria.authorId()), pageRequest);
        } else {
            results = searchQuery.getAllPublished(pageRequest);
        }

        // Enriquecer resultados con metadata
        return SearchResults.builder()
                .posts(results.content())
                .pagination(PaginationInfo.fromPageResponse(results))
                .searchTerm(criteria.searchTerm())
                .totalResults(results.totalElements())
                .searchTime(System.currentTimeMillis() - criteria.startTime())
                .build();
    }
}

public record SearchCriteria(
        String searchTerm,
        String tag,
        String authorId,
        int page,
        int size,
        String sortBy,
        PageRequest.SortDirection sortDirection,
        long startTime
) {
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    public boolean hasTag() {
        return tag != null && !tag.trim().isEmpty();
    }

    public boolean hasAuthor() {
        return authorId != null && !authorId.trim().isEmpty();
    }
}
```

### **3. Feed de Blog con Cache-Friendly Queries**

```java

@Service
public class BlogFeedService {

    private final SearchBlogPostQuery searchQuery;

    @Cacheable(value = "blog-feed", key = "#pageRequest.toString()")
    public BlogFeed getPublicFeed(PageRequest pageRequest) {
        // Query optimizada para el feed público
        PageResponse<BlogPostListItem> posts = searchQuery.getAllPublished(pageRequest);

        // Transformar a formato de feed
        List<FeedItem> feedItems = posts.content().stream()
                .map(this::toFeedItem)
                .toList();

        return BlogFeed.builder()
                .items(feedItems)
                .lastUpdated(LocalDateTime.now())
                .totalPosts(posts.totalElements())
                .hasMore(posts.hasNext())
                .nextPage(posts.hasNext() ? posts.page() + 1 : null)
                .build();
    }

    private FeedItem toFeedItem(BlogPostListItem post) {
        return FeedItem.builder()
                .id(post.id())
                .title(post.title())
                .summary(post.summary())
                .slug(post.slug().value())
                .authorName(post.authorName())
                .publishedAt(post.publishedAt())
                .tags(post.tagNames())
                .commentCount(post.commentCount())
                .readTime(calculateReadTime(post.summary()))
                .build();
    }
}
```

---

## 🔄 **Casos de Uso Complejos**

### **1. Migración de Posts en Batch**

```java

@Service
public class BlogPostMigrationService {

    private final CreateBlogPostCommand createCommand;
    private final SearchBlogPostQuery searchQuery;
    private final PublishBlogPostCommand publishCommand;

    @Async
    public CompletableFuture<MigrationResult> migratePosts(List<LegacyPost> legacyPosts) {
        MigrationResult.Builder resultBuilder = MigrationResult.builder();

        for (LegacyPost legacyPost : legacyPosts) {
            try {
                // 1. COMMAND: Crear nuevo post
                CreateBlogPostCommandData command = new CreateBlogPostCommandData(
                        legacyPost.title(),
                        legacyPost.content(),
                        legacyPost.summary(),
                        AuthorId.of(legacyPost.authorId()),
                        legacyPost.tags()
                );

                BlogPost newPost = createCommand.createBlogPost(command);

                // 2. COMMAND: Publicar si estaba publicado
                if (legacyPost.wasPublished()) {
                    publishCommand.publishPost(newPost.getId());
                }

                // 3. QUERY: Verificar migración exitosa
                PageRequest verifyRequest = PageRequest.of(0, 1);
                PageResponse<BlogPostListItem> verification = searchQuery
                        .searchByTitleOrContent(newPost.getTitle(), verifyRequest);

                if (!verification.content().isEmpty()) {
                    resultBuilder.addSuccess(legacyPost.id(), newPost.getId().value());
                } else {
                    resultBuilder.addWarning(legacyPost.id(), "Post created but not found in search");
                }

            } catch (Exception e) {
                resultBuilder.addError(legacyPost.id(), e.getMessage());
            }
        }

        return CompletableFuture.completedFuture(resultBuilder.build());
    }
}
```

### **2. Analytics y Reporting**

```java

@Service
public class BlogAnalyticsService {

    private final SearchBlogPostQuery searchQuery;
    private final BlogPostQueryRepositoryPort queryRepository;

    public BlogAnalytics generateMonthlyReport(YearMonth month) {
        // Queries especializadas para analytics

        // 1. Posts publicados en el mes
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

        // 2. Top autores del mes
        Map<String, Long> topAuthors = findTopAuthorsInPeriod(startOfMonth, endOfMonth);

        // 3. Tags más populares
        Map<String, Long> popularTags = findPopularTagsInPeriod(startOfMonth, endOfMonth);

        // 4. Posts con más engagement
        PageRequest topPosts = PageRequest.of(0, 10, "commentCount", SortDirection.DESC);
        PageResponse<BlogPostListItem> mostCommented = searchQuery.getAllPublished(topPosts);

        return BlogAnalytics.builder()
                .period(month)
                .totalPublishedPosts(queryRepository.countPublishedPosts())
                .topAuthors(topAuthors)
                .popularTags(popularTags)
                .mostCommentedPosts(mostCommented.content())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private Map<String, Long> findTopAuthorsInPeriod(LocalDateTime start, LocalDateTime end) {
        // Implementación con queries optimizadas
        // Usando agregaciones de MongoDB
        return Map.of(); // Simplified for example
    }
}
```

---

## 🔗 **Patrones de Integración**

### **1. Event-Driven Architecture con CQRS**

```java

@Component
public class BlogPostEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Async
    public void handleBlogPostCreated(BlogPostCreatedEvent event) {
        // Procesar en background después del comando
        log.info("Processing blog post created: {}", event.getPostId());

        // Ejemplo: Actualizar índices de búsqueda
        updateSearchIndexes(event.getPostId());

        // Ejemplo: Notificar suscriptores
        notifySubscribers(event.getAuthorId(), event.getPostTitle());
    }

    @EventListener
    @Async
    public void handleBlogPostPublished(BlogPostPublishedEvent event) {
        // Procesar publicación
        log.info("Processing blog post published: {}", event.getPostId());

        // Ejemplo: Invalidar caches
        invalidateRelatedCaches(event.getPostId());

        // Ejemplo: Generar sitemap
        regenerateSitemap();
    }
}

// En el Command Service
@Service
public class BlogPostCommandService {

    private final ApplicationEventPublisher eventPublisher;

    public BlogPost createBlogPost(CreateBlogPostCommandData command) {
        BlogPost createdPost = commandRepository.save(blogPost);

        // Publicar evento después del comando exitoso
        eventPublisher.publishEvent(new BlogPostCreatedEvent(
                createdPost.getId().value(),
                createdPost.getTitle(),
                createdPost.getAuthorId().value()
        ));

        return createdPost;
    }
}
```

### **2. API Composition Pattern**

```java

@RestController
public class BlogPostCompositeController {

    // Comandos
    private final CreateBlogPostCommand createCommand;
    private final PublishBlogPostCommand publishCommand;

    // Queries
    private final GetBlogPostQuery getQuery;
    private final SearchBlogPostQuery searchQuery;

    // Servicios relacionados
    private final AuthorQueryService authorService;
    private final CommentQueryService commentService;

    @GetMapping("/api/posts/{id}/complete")
    public ResponseEntity<CompleteBlogPostView> getCompletePost(@PathVariable String id) {
        PostId postId = PostId.of(id);

        // 1. QUERY: Obtener post principal
        BlogPostReadModel post = getQuery.getById(postId);

        // 2. QUERY: Obtener posts relacionados del mismo autor  
        PageRequest related = PageRequest.of(0, 3, "createdAt", SortDirection.DESC);
        PageResponse<BlogPostListItem> relatedPosts = searchQuery
                .getAllByAuthor(AuthorId.of(post.authorId()), related);

        // 3. QUERY: Obtener posts con tags similares
        Set<String> tags = post.tagNames();
        List<BlogPostListItem> similarPosts = new ArrayList<>();
        for (String tag : tags.stream().limit(2).toList()) {
            PageResponse<BlogPostListItem> taggedPosts = searchQuery
                    .getByTag(tag, PageRequest.of(0, 2));
            similarPosts.addAll(taggedPosts.content());
        }

        // 4. QUERY: Información adicional del autor
        AuthorProfile authorProfile = authorService.getAuthorProfile(post.authorId());

        // 5. Componer respuesta completa
        CompleteBlogPostView completeView = CompleteBlogPostView.builder()
                .post(BlogPostResponse.fromReadModel(post))
                .authorProfile(authorProfile)
                .relatedPosts(relatedPosts.content())
                .similarPosts(similarPosts.stream().distinct().limit(4).toList())
                .build();

        return ResponseEntity.ok(completeView);
    }
}
```

---

## 🧪 **Testing Patterns**

### **1. Command Testing**

```java

@ExtendWith(MockitoExtension.class)
class BlogPostCommandServiceTest {

    @Mock
    private BlogPostCommandRepositoryPort commandRepository;
    @Mock
    private AuthorRepositoryPort authorRepository;
    @Mock
    private TagRepositoryPort tagRepository;

    @InjectMocks
    private BlogPostCommandService commandService;

    @Test
    @DisplayName("Should create blog post with business validation")
    void shouldCreateBlogPostWithBusinessValidation() {
        // Given: Setup mocks for dependencies
        AuthorId authorId = AuthorId.generate();
        Author author = mock(Author.class);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(commandRepository.existsBySlug(any(Slug.class))).thenReturn(false);

        // Given: Command data
        CreateBlogPostCommandData command = new CreateBlogPostCommandData(
                "Test Post",
                "Content with more than minimum length required",
                "Summary",
                authorId,
                Set.of("java", "testing")
        );

        // Given: Mock tags
        Tag javaTag = Tag.create("java");
        Tag testingTag = Tag.create("testing");
        when(tagRepository.findByName("java")).thenReturn(Optional.of(javaTag));
        when(tagRepository.findByName("testing")).thenReturn(Optional.of(testingTag));

        // Given: Mock save result
        BlogPost expectedPost = BlogPost.createDraft(
                command.title(), command.content(), command.summary(), authorId);
        when(commandRepository.save(any(BlogPost.class))).thenReturn(expectedPost);

        // When: Execute command
        BlogPost result = commandService.createBlogPost(command);

        // Then: Verify business logic was applied
        assertAll(
                () -> assertThat(result.getTitle()).isEqualTo("Test Post"),
                () -> assertThat(result.getStatus()).isEqualTo(PostStatus.DRAFT),
                () -> assertThat(result.getAuthorId()).isEqualTo(authorId)
        );

        // Then: Verify interactions
        verify(authorRepository).findById(authorId);
        verify(commandRepository).existsBySlug(any(Slug.class));
        verify(commandRepository).save(any(BlogPost.class));
    }
}
```

### **2. Query Testing**

```java

@ExtendWith(MockitoExtension.class)
class BlogPostQueryServiceTest {

    @Mock
    private BlogPostQueryRepositoryPort queryRepository;
    @InjectMocks
    private BlogPostQueryService queryService;

    @Test
    @DisplayName("Should return paginated search results")
    void shouldReturnPaginatedSearchResults() {
        // Given: Search criteria
        String searchTerm = "java";
        PageRequest pageRequest = PageRequest.of(0, 5, "createdAt", SortDirection.DESC);

        // Given: Mock repository response
        List<BlogPostListItem> mockItems = List.of(
                createMockListItem("1", "Java Basics"),
                createMockListItem("2", "Advanced Java")
        );
        PageResponse<BlogPostListItem> mockResponse = PageResponse.of(
                mockItems, pageRequest, 2L);

        when(queryRepository.searchByTitleOrContent(searchTerm, pageRequest))
                .thenReturn(mockResponse);

        // When: Execute query
        PageResponse<BlogPostListItem> result = queryService
                .searchByTitleOrContent(searchTerm, pageRequest);

        // Then: Verify query results
        assertAll(
                () -> assertThat(result.content()).hasSize(2),
                () -> assertThat(result.totalElements()).isEqualTo(2),
                () -> assertThat(result.page()).isEqualTo(0),
                () -> assertThat(result.hasNext()).isFalse(),
                () -> assertThat(result.content().get(0).title()).contains("Java"),
                () -> assertThat(result.content().get(1).title()).contains("Java")
        );

        verify(queryRepository).searchByTitleOrContent(searchTerm, pageRequest);
    }
}
```

### **3. Integration Testing**

```java

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CQRSIntegrationTest {

    @Container
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:5.0");

    @Autowired
    private CreateBlogPostCommand createCommand;
    @Autowired
    private SearchBlogPostQuery searchQuery;
    @Autowired
    private GetBlogPostQuery getQuery;
    @Autowired
    private AuthorRepositoryPort authorRepository;

    @Test
    @DisplayName("Should demonstrate complete CQRS workflow")
    void shouldDemonstrateCompleteCQRSWorkflow() {
        // Given: Create author first
        Author author = Author.create("Integration Test Author",
                Email.of("integration@test.com"));
        Author savedAuthor = authorRepository.save(author);

        // When: COMMAND - Create blog post
        CreateBlogPostCommandData createCommand = new CreateBlogPostCommandData(
                "Integration Test Post",
                "This is a comprehensive integration test for CQRS implementation",
                "Integration testing with CQRS",
                savedAuthor.getId(),
                Set.of("integration", "testing", "cqrs")
        );

        BlogPost createdPost = this.createCommand.createBlogPost(createCommand);

        // Then: QUERY - Verify through read model
        BlogPostReadModel readModel = getQuery.getById(createdPost.getId());

        assertAll(
                () -> assertThat(readModel.title()).isEqualTo("Integration Test Post"),
                () -> assertThat(readModel.authorName()).isEqualTo("Integration Test Author"),
                () -> assertThat(readModel.authorEmail()).isEqualTo("integration@test.com"),
                () -> assertThat(readModel.tagNames()).containsExactlyInAnyOrder(
                        "integration", "testing", "cqrs")
        );

        // Then: QUERY - Verify through search
        PageRequest pageRequest = PageRequest.defaultRequest();
        PageResponse<BlogPostListItem> searchResults = searchQuery
                .searchByTitleOrContent("Integration", pageRequest);

        assertThat(searchResults.content()).hasSize(1);
        assertThat(searchResults.content().get(0).title())
                .isEqualTo("Integration Test Post");
    }
}
```

---

## ✅ **Best Practices**

### **1. Separación Clara de Responsabilidades**

```java
// ✅ CORRECTO: Separación clara
@RestController
public class BlogPostController {

    // Commands - Solo para escrituras
    private final CreateBlogPostCommand createCommand;
    private final PublishBlogPostCommand publishCommand;
    private final UpdateBlogPostCommand updateCommand;

    // Queries - Solo para lecturas  
    private final GetBlogPostQuery getQuery;
    private final SearchBlogPostQuery searchQuery;

    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(@RequestBody CreateRequest request) {
        // Solo usa commands para escritura
        return ResponseEntity.ok(createCommand.createBlogPost(...));
    }

    @GetMapping("/api/posts")
    public ResponseEntity<?> searchPosts(@RequestParam Map<String, String> params) {
        // Solo usa queries para lectura
        return ResponseEntity.ok(searchQuery.getAllPublished(...));
    }
}

// ❌ INCORRECTO: Mezclando responsabilidades
@RestController
public class BlogPostController {

    private final BlogPostService mixedService; // ❌ Service mixto

    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(@RequestBody CreateRequest request) {
        // ❌ Service que mezcla command y query
        BlogPost created = mixedService.createPost(...);
        List<BlogPost> similar = mixedService.findSimilar(...); // ❌ Query en command
        return ResponseEntity.ok(...);
    }
}
```

### **2. Uso Correcto de Read Models**

```java
// ✅ CORRECTO: Read models especializados
@Service
public class BlogPostQueryService {

    public BlogPostReadModel getById(PostId postId) {
        // Retorna read model enriquecido para vista detalle
        return queryRepository.findById(postId)
                .orElseThrow(() -> new BlogPostNotFoundException(...));
    }

    public PageResponse<BlogPostListItem> getAllPublished(PageRequest pageRequest) {
        // Retorna list items optimizados para listados
        return queryRepository.findAllPublished(pageRequest);
    }
}

// ❌ INCORRECTO: Usando entities de dominio para queries
@Service
public class BlogPostQueryService {

    public BlogPost getById(PostId postId) {
        // ❌ Retorna entity completa cuando no se necesita
        return queryRepository.findById(postId)
                .orElseThrow(() -> new BlogPostNotFoundException(...));
    }
}
```

### **3. Paginación Consistente**

```java
// ✅ CORRECTO: Paginación tipada y consistente
@RestController
public class BlogPostController {

    @GetMapping("/api/posts")
    public ResponseEntity<PageResponse<BlogPostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") PageRequest.SortDirection sortDirection) {

        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
        PageResponse<BlogPostListItem> result = searchQuery.getAllPublished(pageRequest);

        // Conversión consistente a response
        PageResponse<BlogPostResponse> response = new PageResponse<>(
                result.content().stream().map(BlogPostResponse::fromListItem).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious()
        );

        return ResponseEntity.ok(response);
    }
}

// ❌ INCORRECTO: Paginación inconsistente
@RestController
public class BlogPostController {

    @GetMapping("/api/posts")
    public ResponseEntity<List<BlogPostResponse>> getAllPosts(
            @RequestParam(required = false) Integer page) { // ❌ Paginación opcional

        if (page != null) {
            // ❌ Lógica de paginación inconsistente
            return ResponseEntity.ok(someService.getPage(page));
        } else {
            // ❌ Sin paginación puede causar problemas de memoria
            return ResponseEntity.ok(someService.getAll());
        }
    }
}
```

### **4. Validación en Capas Apropiadas**

```java
// ✅ CORRECTO: Validación en commands con business rules
@Service
public class BlogPostCommandService {

    public BlogPost createBlogPost(CreateBlogPostCommandData command) {
        // ✅ Validaciones de negocio en el command service
        if (command.title().length() > 200) {
            throw new IllegalArgumentException("Title too long");
        }

        if (command.content().length() < 50) {
            throw new IllegalArgumentException("Content too short");
        }

        // ✅ Verificaciones de consistencia
        authorRepository.findById(command.authorId())
                .orElseThrow(() -> new AuthorNotFoundException(...));

        // ✅ Business logic
        return commandRepository.save(BlogPost.createDraft(...));
    }
}

// Request validation en controller
@RestController
public class BlogPostController {

    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(@Valid @RequestBody CreateBlogPostRequest request) {
        // ✅ @Valid para validaciones básicas (format, null, etc.)
        // Business rules se validan en el command service
        return ResponseEntity.ok(createCommand.createBlogPost(...));
    }
}

// ❌ INCORRECTO: Validación solo en controller
@RestController
public class BlogPostController {

    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(@RequestBody CreateBlogPostRequest request) {
        // ❌ Validaciones de business rules en controller
        if (request.title().length() > 200) {
            return ResponseEntity.badRequest().body("Title too long");
        }

        // ❌ Business logic se filtra al controller
        if (!authorExists(request.authorId())) {
            return ResponseEntity.badRequest().body("Author not found");
        }

        return ResponseEntity.ok(createCommand.createBlogPost(...));
    }
}
```

---

## 🎯 **Conclusión**

La implementación de CQRS en este proyecto demuestra cómo:

1. **Separar responsabilidades** sin romper APIs existentes
2. **Optimizar operaciones** según su naturaleza (read vs write)
3. **Escalar arquitectura** manteniendo simplicidad
4. **Mejorar mantenibilidad** con código más organizado

Los ejemplos mostrados cubren desde casos simples hasta workflows complejos, proporcionando patrones reutilizables para
futuras expansiones del sistema.

**¡CQRS implementado exitosamente!** 🚀
