package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.BlogPostEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.BlogPostMongoRepository;
import app.quantun.blog.shared.valueobject.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

/**
 * Tests para BlogPostCommandRepositoryAdapter
 * Enfocados en operaciones de escritura y comandos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BlogPost Command Repository Adapter")
class BlogPostCommandRepositoryAdapterTest {

    @Mock
    private BlogPostMongoRepository mongoRepository;

    @Mock
    private BlogPostEntityMapper mapper;

    private BlogPostCommandRepositoryAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new BlogPostCommandRepositoryAdapter(mongoRepository, mapper);
    }

    // Helper methods
    private BlogPost createBlogPost(String title) {
        return BlogPost.builder()
                .id(PostId.generate())
                .title(title)
                .content("Content for " + title)
                .summary("Summary for " + title)
                .slug(Slug.fromTitle(title))
                .authorId(AuthorId.generate())
                .status(PostStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Note: Find operations and existence checks are handled by the Query adapter, not the Command adapter

    private BlogPostEntity createBlogPostEntity(String id, String title) {
        BlogPostEntity entity = new BlogPostEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setContent("Content for " + title);
        entity.setSummary("Summary for " + title);
        entity.setSlug(Slug.fromTitle(title).value());
        entity.setAuthorId("author-123");
        entity.setStatus("DRAFT");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTest {

        @Test
        @DisplayName("Should save blog post and return mapped domain object")
        void shouldSaveBlogPostAndReturnMappedDomainObject() {
            // Given
            BlogPost blogPost = createBlogPost("Test Post");
            BlogPostEntity entity = createBlogPostEntity("post-123", "Test Post");
            BlogPostEntity savedEntity = createBlogPostEntity("post-123", "Test Post");
            BlogPost savedBlogPost = createBlogPost("Test Post");

            when(mapper.toEntity(blogPost)).thenReturn(entity);
            when(mongoRepository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedBlogPost);

            // When
            BlogPost result = commandAdapter.save(blogPost);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedBlogPost);

            verify(mapper).toEntity(blogPost);
            verify(mongoRepository).save(entity);
            verify(mapper).toDomain(savedEntity);
        }

        @Test
        @DisplayName("Should handle save with complex domain object")
        void shouldHandleSaveWithComplexDomainObject() {
            // Given
            BlogPost complexBlogPost = BlogPost.builder()
                    .id(PostId.generate())
                    .title("Complex Post")
                    .content("Complex content with multiple paragraphs")
                    .summary("Complex summary")
                    .slug(Slug.fromTitle("Complex Post"))
                    .authorId(AuthorId.generate())
                    .status(PostStatus.PUBLISHED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .publishedAt(LocalDateTime.now())
                    .build();

            BlogPostEntity complexEntity = createBlogPostEntity("complex-123", "Complex Post");
            BlogPostEntity savedComplexEntity = createBlogPostEntity("complex-123", "Complex Post");
            BlogPost savedComplexBlogPost = complexBlogPost;

            when(mapper.toEntity(complexBlogPost)).thenReturn(complexEntity);
            when(mongoRepository.save(complexEntity)).thenReturn(savedComplexEntity);
            when(mapper.toDomain(savedComplexEntity)).thenReturn(savedComplexBlogPost);

            // When
            BlogPost result = commandAdapter.save(complexBlogPost);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getTitle()).isEqualTo("Complex Post"),
                    () -> assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED)
            );

            verify(mapper).toEntity(complexBlogPost);
            verify(mongoRepository).save(complexEntity);
            verify(mapper).toDomain(savedComplexEntity);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTest {

        @Test
        @DisplayName("Should delete blog post by ID")
        void shouldDeleteBlogPostById() {
            // Given
            PostId postId = PostId.generate();

            // When
            commandAdapter.deleteById(postId);

            // Then
            verify(mongoRepository).deleteById(postId.value());
        }

        @Test
        @DisplayName("Should handle delete operation gracefully")
        void shouldHandleDeleteOperationGracefully() {
            // Given
            PostId postId = PostId.generate();
            doNothing().when(mongoRepository).deleteById(postId.value());

            // When
            commandAdapter.deleteById(postId);

            // Then - No exception should be thrown
            verify(mongoRepository).deleteById(postId.value());
        }
    }
}
