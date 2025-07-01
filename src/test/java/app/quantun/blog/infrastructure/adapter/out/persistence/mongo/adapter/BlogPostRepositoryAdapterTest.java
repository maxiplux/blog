package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.BlogPostEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.BlogPostMongoRepository;
import app.quantun.blog.shared.valueobject.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogPostRepositoryAdapterTest {

    @Mock
    private BlogPostMongoRepository mongoRepository;

    @Mock
    private BlogPostEntityMapper mapper;

    private BlogPostRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        repositoryAdapter = new BlogPostRepositoryAdapter(mongoRepository, mapper);
    }

    @Test
    void shouldSaveBlogPost() {
        // Arrange
        BlogPost blogPost = createSampleBlogPost();
        BlogPostEntity entity = new BlogPostEntity();
        BlogPostEntity savedEntity = new BlogPostEntity();

        when(mapper.toEntity(blogPost)).thenReturn(entity);
        when(mongoRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(blogPost);

        // Act
        BlogPost result = repositoryAdapter.save(blogPost);

        // Assert
        assertNotNull(result);
        assertEquals(blogPost, result);

        verify(mapper).toEntity(blogPost);
        verify(mongoRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    void shouldFindBlogPostById() {
        // Arrange
        PostId postId = PostId.generate();
        BlogPostEntity entity = new BlogPostEntity();
        BlogPost blogPost = createSampleBlogPost();

        when(mongoRepository.findById(postId.value())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(blogPost);

        // Act
        Optional<BlogPost> result = repositoryAdapter.findById(postId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(blogPost, result.get());

        verify(mongoRepository).findById(postId.value());
        verify(mapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenBlogPostNotFoundById() {
        // Arrange
        PostId postId = PostId.generate();
        when(mongoRepository.findById(postId.value())).thenReturn(Optional.empty());

        // Act
        Optional<BlogPost> result = repositoryAdapter.findById(postId);

        // Assert
        assertTrue(result.isEmpty());
        verify(mongoRepository).findById(postId.value());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void shouldFindBlogPostBySlug() {
        // Arrange
        Slug slug = Slug.fromTitle("test-post");
        BlogPostEntity entity = new BlogPostEntity();
        BlogPost blogPost = createSampleBlogPost();

        when(mongoRepository.findBySlug(slug.value())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(blogPost);

        // Act
        Optional<BlogPost> result = repositoryAdapter.findBySlug(slug);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(blogPost, result.get());

        verify(mongoRepository).findBySlug(slug.value());
        verify(mapper).toDomain(entity);
    }

    @Test
    void shouldFindBlogPostsByStatus() {
        // Arrange
        PostStatus status = PostStatus.PUBLISHED;
        List<BlogPostEntity> entities = List.of(new BlogPostEntity(), new BlogPostEntity());

        when(mongoRepository.findByStatus(status.name())).thenReturn(entities);
        when(mapper.toDomain(any(BlogPostEntity.class))).thenReturn(createSampleBlogPost());

        // Act
        List<BlogPost> result = repositoryAdapter.findByStatus(status);

        // Assert
        assertEquals(2, result.size());

        verify(mongoRepository).findByStatus(status.name());
        verify(mapper, times(2)).toDomain(any(BlogPostEntity.class));
    }

    @Test
    void shouldFindBlogPostsByAuthorId() {
        // Arrange
        String authorId = "author-123";
        List<BlogPostEntity> entities = List.of(new BlogPostEntity(), new BlogPostEntity());

        when(mongoRepository.findByAuthorId(authorId)).thenReturn(entities);
        when(mapper.toDomain(any(BlogPostEntity.class))).thenReturn(createSampleBlogPost());

        // Act
        List<BlogPost> result = repositoryAdapter.findByAuthorId(authorId);

        // Assert
        assertEquals(2, result.size());

        verify(mongoRepository).findByAuthorId(authorId);
        verify(mapper, times(2)).toDomain(any(BlogPostEntity.class));
    }

    @Test
    void shouldFindBlogPostsByTagName() {
        // Arrange
        String tagName = "java";
        List<BlogPostEntity> entities = List.of(new BlogPostEntity(), new BlogPostEntity());

        when(mongoRepository.findByTagsName(tagName)).thenReturn(entities);
        when(mapper.toDomain(any(BlogPostEntity.class))).thenReturn(createSampleBlogPost());

        // Act
        List<BlogPost> result = repositoryAdapter.findByTagName(tagName);

        // Assert
        assertEquals(2, result.size());

        verify(mongoRepository).findByTagsName(tagName);
        verify(mapper, times(2)).toDomain(any(BlogPostEntity.class));
    }

    @Test
    void shouldCheckIfBlogPostExistsBySlug() {
        // Arrange
        Slug slug = Slug.fromTitle("test-post");
        when(mongoRepository.existsBySlug(slug.value())).thenReturn(true);

        // Act
        boolean result = repositoryAdapter.existsBySlug(slug);

        // Assert
        assertTrue(result);
        verify(mongoRepository).existsBySlug(slug.value());
    }

    @Test
    void shouldDeleteBlogPostById() {
        // Arrange
        PostId postId = PostId.generate();

        // Act
        repositoryAdapter.deleteById(postId);

        // Assert
        verify(mongoRepository).deleteById(postId.value());
    }

    private BlogPost createSampleBlogPost() {
        return BlogPost.builder()
                .id(PostId.generate())
                .title("Test Post")
                .content("Test content")
                .summary("Test summary")
                .slug(Slug.fromTitle("Test Post"))
                .authorId("author-123")
                .status(PostStatus.DRAFT)
                .tag(Tag.create("java"))
                .tag(Tag.create("spring"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
