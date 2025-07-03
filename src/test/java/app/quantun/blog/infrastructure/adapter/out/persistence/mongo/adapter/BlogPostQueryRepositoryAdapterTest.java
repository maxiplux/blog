package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.AuthorEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.BlogPostEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.AuthorMongoRepository;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.BlogPostMongoRepository;
import app.quantun.blog.shared.valueobject.PageRequest;
import app.quantun.blog.shared.valueobject.PageResponse;
import app.quantun.blog.shared.valueobject.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests para BlogPostQueryRepositoryAdapter
 * Enfocados en verificar la conversión correcta entre entidades y read models
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BlogPost Query Repository Adapter")
class BlogPostQueryRepositoryAdapterTest {

    @Mock
    private BlogPostMongoRepository blogPostRepository;

    @Mock
    private AuthorMongoRepository authorRepository;

    @Mock
    private BlogPostEntityMapper entityMapper;

    private BlogPostQueryRepositoryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new BlogPostQueryRepositoryAdapter(blogPostRepository, authorRepository, entityMapper);
    }

    // Helper methods
    private BlogPostEntity createBlogPostEntity(String id, String title, String authorId) {
        BlogPostEntity entity = new BlogPostEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setContent("Content for " + title);
        entity.setSummary("Summary for " + title);
        entity.setSlug(Slug.fromTitle(title).value());
        entity.setAuthorId(authorId);
        entity.setStatus("PUBLISHED");
        entity.setTags(Set.of(
                new BlogPostEntity.TagEntity("test", "test"),
                new BlogPostEntity.TagEntity("blog", "blog")
        ));
        entity.setComments(List.of());
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setPublishedAt(LocalDateTime.now());
        return entity;
    }

    private AuthorEntity createAuthorEntity(String id, String name, String email) {
        AuthorEntity entity = new AuthorEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setEmail(email);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    @Nested
    @DisplayName("Find Individual Posts")
    class FindIndividualPostsTest {

        @Test
        @DisplayName("Should find blog post by ID and convert to read model")
        void shouldFindBlogPostByIdAndConvertToReadModel() {
            // Given
            PostId postId = PostId.generate();
            String authorId = "author-123";

            BlogPostEntity entity = createBlogPostEntity(postId.value(), "Test Post", authorId);
            AuthorEntity authorEntity = createAuthorEntity(authorId, "John Doe", "john@example.com");

            when(blogPostRepository.findById(postId.value())).thenReturn(Optional.of(entity));
            when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));

            // When
            Optional<BlogPostReadModel> result = queryAdapter.findById(postId);

            // Then
            assertThat(result).isPresent();
            BlogPostReadModel readModel = result.get();

            assertAll(
                    () -> assertThat(readModel.id()).isEqualTo(postId.value()),
                    () -> assertThat(readModel.title()).isEqualTo("Test Post"),
                    () -> assertThat(readModel.authorName()).isEqualTo("John Doe"),
                    () -> assertThat(readModel.authorEmail()).isEqualTo("john@example.com"),
                    () -> assertThat(readModel.status()).isEqualTo(PostStatus.PUBLISHED)
            );

            verify(blogPostRepository).findById(postId.value());
            verify(authorRepository).findById(authorId);
        }

        @Test
        @DisplayName("Should find blog post by slug and convert to read model")
        void shouldFindBlogPostBySlugAndConvertToReadModel() {
            // Given
            Slug slug = Slug.fromTitle("Test Post");
            String authorId = "author-123";

            BlogPostEntity entity = createBlogPostEntity("post-123", "Test Post", authorId);
            AuthorEntity authorEntity = createAuthorEntity(authorId, "Jane Doe", "jane@example.com");

            when(blogPostRepository.findBySlug(slug.value())).thenReturn(Optional.of(entity));
            when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));

            // When
            Optional<BlogPostReadModel> result = queryAdapter.findBySlug(slug);

            // Then
            assertThat(result).isPresent();
            BlogPostReadModel readModel = result.get();

            assertAll(
                    () -> assertThat(readModel.slug()).isEqualTo(slug),
                    () -> assertThat(readModel.title()).isEqualTo("Test Post"),
                    () -> assertThat(readModel.authorName()).isEqualTo("Jane Doe"),
                    () -> assertThat(readModel.authorEmail()).isEqualTo("jane@example.com")
            );

            verify(blogPostRepository).findBySlug(slug.value());
            verify(authorRepository).findById(authorId);
        }

        @Test
        @DisplayName("Should handle missing author when finding by ID")
        void shouldHandleMissingAuthorWhenFindingById() {
            // Given
            PostId postId = PostId.generate();
            String authorId = "author-123";

            BlogPostEntity entity = createBlogPostEntity(postId.value(), "Test Post", authorId);

            when(blogPostRepository.findById(postId.value())).thenReturn(Optional.of(entity));
            when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

            // When
            Optional<BlogPostReadModel> result = queryAdapter.findById(postId);

            // Then
            assertThat(result).isPresent();
            BlogPostReadModel readModel = result.get();

            assertAll(
                    () -> assertThat(readModel.authorName()).isNull(),
                    () -> assertThat(readModel.authorEmail()).isNull(),
                    () -> assertThat(readModel.authorId()).isEqualTo(authorId)
            );
        }

        @Test
        @DisplayName("Should find blog post domain object by ID")
        void shouldFindBlogPostDomainById() {
            // Given
            PostId postId = PostId.generate();
            String authorId = "author-123";

            BlogPostEntity entity = createBlogPostEntity(postId.value(), "Test Post", authorId);
            BlogPost domainObject = mock(BlogPost.class);
            when(domainObject.getId()).thenReturn(postId);
            when(domainObject.getTitle()).thenReturn("Test Post");
            when(domainObject.getAuthorId()).thenReturn(AuthorId.of(authorId));

            when(blogPostRepository.findById(postId.value())).thenReturn(Optional.of(entity));
            when(entityMapper.toDomain(entity)).thenReturn(domainObject);

            // When
            Optional<BlogPost> result = queryAdapter.findDomainById(postId);

            // Then
            assertThat(result).isPresent();
            BlogPost blogPost = result.get();

            assertAll(
                    () -> assertThat(blogPost.getId()).isEqualTo(postId),
                    () -> assertThat(blogPost.getTitle()).isEqualTo("Test Post"),
                    () -> assertThat(blogPost.getAuthorId().value()).isEqualTo(authorId)
            );

            verify(blogPostRepository).findById(postId.value());
            verify(entityMapper).toDomain(entity);
        }

        @Test
        @DisplayName("Should find blog post domain object by slug")
        void shouldFindBlogPostDomainBySlug() {
            // Given
            Slug slug = Slug.fromTitle("Test Post");
            String authorId = "author-123";
            PostId postId = PostId.of("post-123");

            BlogPostEntity entity = createBlogPostEntity(postId.value(), "Test Post", authorId);
            BlogPost domainObject = mock(BlogPost.class);
            when(domainObject.getId()).thenReturn(postId);
            when(domainObject.getTitle()).thenReturn("Test Post");
            when(domainObject.getSlug()).thenReturn(slug);

            when(blogPostRepository.findBySlug(slug.value())).thenReturn(Optional.of(entity));
            when(entityMapper.toDomain(entity)).thenReturn(domainObject);

            // When
            Optional<BlogPost> result = queryAdapter.findDomainBySlug(slug);

            // Then
            assertThat(result).isPresent();
            BlogPost blogPost = result.get();

            assertAll(
                    () -> assertThat(blogPost.getId().value()).isEqualTo("post-123"),
                    () -> assertThat(blogPost.getTitle()).isEqualTo("Test Post"),
                    () -> assertThat(blogPost.getSlug()).isEqualTo(slug)
            );

            verify(blogPostRepository).findBySlug(slug.value());
            verify(entityMapper).toDomain(entity);
        }

        @Test
        @DisplayName("Should check if slug exists")
        void shouldCheckIfSlugExists() {
            // Given
            Slug slug = Slug.fromTitle("Test Post");

            when(blogPostRepository.existsBySlug(slug.value())).thenReturn(true);

            // When
            boolean result = queryAdapter.existsBySlug(slug);

            // Then
            assertThat(result).isTrue();
            verify(blogPostRepository).existsBySlug(slug.value());
        }
    }

    @Nested
    @DisplayName("Paginated Queries")
    class PaginatedQueriesTest {

        @Test
        @DisplayName("Should find all published posts with pagination")
        void shouldFindAllPublishedPostsWithPagination() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10, "createdAt", PageRequest.SortDirection.DESC);

            List<BlogPostEntity> entities = List.of(
                    createBlogPostEntity("1", "Post 1", "author-1"),
                    createBlogPostEntity("2", "Post 2", "author-2")
            );
            Page<BlogPostEntity> page = new PageImpl<>(entities,
                    org.springframework.data.domain.PageRequest.of(0, 10), 2L);

            when(blogPostRepository.findByStatus(eq("PUBLISHED"), any(Pageable.class)))
                    .thenReturn(page);
            when(authorRepository.findById("author-1"))
                    .thenReturn(Optional.of(createAuthorEntity("author-1", "Author 1", "author1@example.com")));
            when(authorRepository.findById("author-2"))
                    .thenReturn(Optional.of(createAuthorEntity("author-2", "Author 2", "author2@example.com")));

            // When
            PageResponse<BlogPostListItem> result = queryAdapter.findAllPublished(pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result.content()).hasSize(2),
                    () -> assertThat(result.totalElements()).isEqualTo(2),
                    () -> assertThat(result.page()).isEqualTo(0),
                    () -> assertThat(result.size()).isEqualTo(10),
                    () -> assertThat(result.content().get(0).title()).isEqualTo("Post 1"),
                    () -> assertThat(result.content().get(0).authorName()).isEqualTo("Author 1"),
                    () -> assertThat(result.content().get(1).title()).isEqualTo("Post 2"),
                    () -> assertThat(result.content().get(1).authorName()).isEqualTo("Author 2")
            );

            verify(blogPostRepository).findByStatus(eq("PUBLISHED"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should find posts by author with pagination")
        void shouldFindPostsByAuthorWithPagination() {
            // Given
            AuthorId authorId = AuthorId.of("author-123");
            PageRequest pageRequest = PageRequest.defaultRequest();

            List<BlogPostEntity> entities = List.of(
                    createBlogPostEntity("1", "Author Post 1", authorId.value())
            );
            Page<BlogPostEntity> page = new PageImpl<>(entities,
                    org.springframework.data.domain.PageRequest.of(0, 10), 1L);

            when(blogPostRepository.findByAuthorId(eq(authorId.value()), any(Pageable.class)))
                    .thenReturn(page);
            when(authorRepository.findById(authorId.value()))
                    .thenReturn(Optional.of(createAuthorEntity(authorId.value(), "Test Author", "test@example.com")));

            // When
            PageResponse<BlogPostListItem> result = queryAdapter.findByAuthorId(authorId, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result.content()).hasSize(1),
                    () -> assertThat(result.content().get(0).authorId()).isEqualTo(authorId.value()),
                    () -> assertThat(result.content().get(0).authorName()).isEqualTo("Test Author")
            );

            verify(blogPostRepository).findByAuthorId(eq(authorId.value()), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search posts by title or content")
        void shouldSearchPostsByTitleOrContent() {
            // Given
            String searchTerm = "CQRS";
            PageRequest pageRequest = PageRequest.defaultRequest();

            List<BlogPostEntity> entities = List.of(
                    createBlogPostEntity("1", "CQRS Implementation", "author-1")
            );
            Page<BlogPostEntity> page = new PageImpl<>(entities,
                    org.springframework.data.domain.PageRequest.of(0, 10), 1L);

            when(blogPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    eq(searchTerm), eq(searchTerm), any(Pageable.class)))
                    .thenReturn(page);
            when(authorRepository.findById("author-1"))
                    .thenReturn(Optional.of(createAuthorEntity("author-1", "Author 1", "author1@example.com")));

            // When
            PageResponse<BlogPostListItem> result = queryAdapter.searchByTitleOrContent(searchTerm, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result.content()).hasSize(1),
                    () -> assertThat(result.content().get(0).title()).contains("CQRS"),
                    () -> assertThat(result.totalElements()).isEqualTo(1)
            );

            verify(blogPostRepository).findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    eq(searchTerm), eq(searchTerm), any(Pageable.class));
        }

        @Test
        @DisplayName("Should find posts by tag name")
        void shouldFindPostsByTagName() {
            // Given
            String tagName = "architecture";
            PageRequest pageRequest = PageRequest.defaultRequest();

            List<BlogPostEntity> entities = List.of(
                    createBlogPostEntity("1", "Architecture Post", "author-1")
            );
            Page<BlogPostEntity> page = new PageImpl<>(entities,
                    org.springframework.data.domain.PageRequest.of(0, 10), 1L);

            when(blogPostRepository.findByTagsName(eq(tagName), any(Pageable.class)))
                    .thenReturn(page);
            when(authorRepository.findById("author-1"))
                    .thenReturn(Optional.of(createAuthorEntity("author-1", "Author 1", "author1@example.com")));

            // When
            PageResponse<BlogPostListItem> result = queryAdapter.findByTagName(tagName, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result.content()).hasSize(1),
                    () -> assertThat(result.totalElements()).isEqualTo(1)
            );

            verify(blogPostRepository).findByTagsName(eq(tagName), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Count Operations")
    class CountOperationsTest {

        @Test
        @DisplayName("Should count published posts")
        void shouldCountPublishedPosts() {
            // Given
            when(blogPostRepository.countByStatus("PUBLISHED")).thenReturn(15L);

            // When
            long result = queryAdapter.countPublishedPosts();

            // Then
            assertThat(result).isEqualTo(15L);
            verify(blogPostRepository).countByStatus("PUBLISHED");
        }

        @Test
        @DisplayName("Should count posts by author")
        void shouldCountPostsByAuthor() {
            // Given
            AuthorId authorId = AuthorId.generate();
            when(blogPostRepository.countByAuthorId(authorId.value())).thenReturn(5L);

            // When
            long result = queryAdapter.countPostsByAuthor(authorId);

            // Then
            assertThat(result).isEqualTo(5L);
            verify(blogPostRepository).countByAuthorId(authorId.value());
        }
    }
}
