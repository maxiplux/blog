package app.quantun.blog.application.query.service;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.application.query.port.out.BlogPostQueryRepositoryPort;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

/**
 * Tests para BlogPostQueryService
 * Enfocados en operaciones de lectura y consultas optimizadas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BlogPost Query Service")
class BlogPostQueryServiceTest {

    @Mock
    private BlogPostQueryRepositoryPort queryRepository;

    private BlogPostQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new BlogPostQueryService(queryRepository);
    }

    // Helper methods para crear objetos de test
    private BlogPostReadModel createBlogPostReadModel(PostId postId, String title) {
        return new BlogPostReadModel(
                postId.value(),
                title,
                "Content for " + title,
                "Summary for " + title,
                Slug.fromTitle(title),
                "author-123",
                "John Doe",
                "john@example.com",
                PostStatus.PUBLISHED,
                Set.of("test", "blog"),
                List.of(), // Empty comments for simplicity
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private BlogPostListItem createBlogPostListItem(String id, String title) {
        return new BlogPostListItem(
                id,
                title,
                "Summary for " + title,
                Slug.fromTitle(title),
                "author-123",
                "John Doe",
                PostStatus.PUBLISHED,
                Set.of("test"),
                0, // No comments
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Get Blog Post Queries")
    class GetBlogPostQueriesTest {

        @Test
        @DisplayName("Should get blog post by ID")
        void shouldGetBlogPostById() {
            // Given
            PostId postId = PostId.generate();
            BlogPostReadModel readModel = createBlogPostReadModel(postId, "Test Post");

            when(queryRepository.findById(postId)).thenReturn(Optional.of(readModel));

            // When
            BlogPostReadModel result = queryService.getById(postId);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.id()).isEqualTo(postId.value()),
                    () -> assertThat(result.title()).isEqualTo("Test Post"),
                    () -> assertThat(result.authorName()).isEqualTo("John Doe"),
                    () -> assertThat(result.authorEmail()).isEqualTo("john@example.com")
            );

            verify(queryRepository).findById(postId);
        }

        @Test
        @DisplayName("Should throw exception when blog post not found by ID")
        void shouldThrowExceptionWhenBlogPostNotFoundById() {
            // Given
            PostId nonExistentPostId = PostId.generate();
            when(queryRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> queryService.getById(nonExistentPostId))
                    .isInstanceOf(BlogPostNotFoundException.class)
                    .hasMessageContaining(nonExistentPostId.value());

            verify(queryRepository).findById(nonExistentPostId);
        }

        @Test
        @DisplayName("Should get blog post by slug")
        void shouldGetBlogPostBySlug() {
            // Given
            PostId postId = PostId.generate();
            Slug slug = Slug.fromTitle("Test Post");
            BlogPostReadModel readModel = createBlogPostReadModel(postId, "Test Post");

            when(queryRepository.findBySlug(slug)).thenReturn(Optional.of(readModel));

            // When
            BlogPostReadModel result = queryService.getBySlug(slug);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.slug()).isEqualTo(slug),
                    () -> assertThat(result.title()).isEqualTo("Test Post")
            );

            verify(queryRepository).findBySlug(slug);
        }

        @Test
        @DisplayName("Should throw exception when blog post not found by slug")
        void shouldThrowExceptionWhenBlogPostNotFoundBySlug() {
            // Given
            Slug nonExistentSlug = Slug.fromTitle("Non Existent Post");
            when(queryRepository.findBySlug(nonExistentSlug)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> queryService.getBySlug(nonExistentSlug))
                    .isInstanceOf(BlogPostNotFoundException.class)
                    .hasMessageContaining(nonExistentSlug.value());

            verify(queryRepository).findBySlug(nonExistentSlug);
        }
    }

    @Nested
    @DisplayName("Search Blog Post Queries")
    class SearchBlogPostQueriesTest {

        @Test
        @DisplayName("Should get all published posts with pagination")
        void shouldGetAllPublishedPostsWithPagination() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10, "createdAt", PageRequest.SortDirection.DESC);
            List<BlogPostListItem> listItems = List.of(
                    createBlogPostListItem("1", "Post 1"),
                    createBlogPostListItem("2", "Post 2")
            );
            PageResponse<BlogPostListItem> pageResponse = PageResponse.of(listItems, pageRequest, 2L);

            when(queryRepository.findAllPublished(pageRequest)).thenReturn(pageResponse);

            // When
            PageResponse<BlogPostListItem> result = queryService.getAllPublished(pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).hasSize(2),
                    () -> assertThat(result.totalElements()).isEqualTo(2),
                    () -> assertThat(result.page()).isEqualTo(0),
                    () -> assertThat(result.size()).isEqualTo(10)
            );

            verify(queryRepository).findAllPublished(pageRequest);
        }

        @Test
        @DisplayName("Should get posts by author with pagination")
        void shouldGetPostsByAuthorWithPagination() {
            // Given
            AuthorId authorId = AuthorId.generate();
            PageRequest pageRequest = PageRequest.defaultRequest();
            List<BlogPostListItem> listItems = List.of(
                    createBlogPostListItem("1", "Author Post 1"),
                    createBlogPostListItem("2", "Author Post 2")
            );
            PageResponse<BlogPostListItem> pageResponse = PageResponse.of(listItems, pageRequest, 2L);

            when(queryRepository.findByAuthorId(authorId, pageRequest)).thenReturn(pageResponse);

            // When
            PageResponse<BlogPostListItem> result = queryService.getAllByAuthor(authorId, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).hasSize(2),
                    () -> assertThat(result.totalElements()).isEqualTo(2)
            );

            verify(queryRepository).findByAuthorId(authorId, pageRequest);
        }

        @Test
        @DisplayName("Should search posts by title or content")
        void shouldSearchPostsByTitleOrContent() {
            // Given
            String searchTerm = "CQRS";
            PageRequest pageRequest = PageRequest.defaultRequest();
            List<BlogPostListItem> listItems = List.of(
                    createBlogPostListItem("1", "CQRS Implementation Guide")
            );
            PageResponse<BlogPostListItem> pageResponse = PageResponse.of(listItems, pageRequest, 1L);

            when(queryRepository.searchByTitleOrContent(searchTerm, pageRequest)).thenReturn(pageResponse);

            // When
            PageResponse<BlogPostListItem> result = queryService.searchByTitleOrContent(searchTerm, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).hasSize(1),
                    () -> assertThat(result.content().get(0).title()).contains("CQRS"),
                    () -> assertThat(result.totalElements()).isEqualTo(1)
            );

            verify(queryRepository).searchByTitleOrContent(searchTerm, pageRequest);
        }

        @Test
        @DisplayName("Should return empty result for empty search term")
        void shouldReturnEmptyResultForEmptySearchTerm() {
            // Given
            String emptySearchTerm = "";
            PageRequest pageRequest = PageRequest.defaultRequest();

            // When
            PageResponse<BlogPostListItem> result = queryService.searchByTitleOrContent(emptySearchTerm, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).isEmpty(),
                    () -> assertThat(result.totalElements()).isEqualTo(0)
            );

            verify(queryRepository, never()).searchByTitleOrContent(anyString(), any(PageRequest.class));
        }

        @Test
        @DisplayName("Should return empty result for null search term")
        void shouldReturnEmptyResultForNullSearchTerm() {
            // Given
            String nullSearchTerm = null;
            PageRequest pageRequest = PageRequest.defaultRequest();

            // When
            PageResponse<BlogPostListItem> result = queryService.searchByTitleOrContent(nullSearchTerm, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).isEmpty(),
                    () -> assertThat(result.totalElements()).isEqualTo(0)
            );

            verify(queryRepository, never()).searchByTitleOrContent(anyString(), any(PageRequest.class));
        }

        @Test
        @DisplayName("Should get posts by tag")
        void shouldGetPostsByTag() {
            // Given
            String tagName = "architecture";
            PageRequest pageRequest = PageRequest.defaultRequest();
            List<BlogPostListItem> listItems = List.of(
                    createBlogPostListItem("1", "Hexagonal Architecture Guide")
            );
            PageResponse<BlogPostListItem> pageResponse = PageResponse.of(listItems, pageRequest, 1L);

            when(queryRepository.findByTagName(tagName.toLowerCase(), pageRequest)).thenReturn(pageResponse);

            // When
            PageResponse<BlogPostListItem> result = queryService.getByTag(tagName, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).hasSize(1),
                    () -> assertThat(result.totalElements()).isEqualTo(1)
            );

            verify(queryRepository).findByTagName(tagName.toLowerCase(), pageRequest);
        }

        @Test
        @DisplayName("Should return empty result for empty tag name")
        void shouldReturnEmptyResultForEmptyTagName() {
            // Given
            String emptyTagName = "   ";
            PageRequest pageRequest = PageRequest.defaultRequest();

            // When
            PageResponse<BlogPostListItem> result = queryService.getByTag(emptyTagName, pageRequest);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.content()).isEmpty(),
                    () -> assertThat(result.totalElements()).isEqualTo(0)
            );

            verify(queryRepository, never()).findByTagName(anyString(), any(PageRequest.class));
        }
    }
}
