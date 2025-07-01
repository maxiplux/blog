package app.quantun.blog.domain.service;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.port.in.CreateBlogPostUseCase.CreateBlogPostCommand;
import app.quantun.blog.domain.port.out.AuthorRepositoryPort;
import app.quantun.blog.domain.port.out.BlogPostRepositoryPort;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepositoryPort blogPostRepositoryPort;

    @Mock
    private AuthorRepositoryPort authorRepositoryPort;

    private BlogPostService blogPostService;

    @BeforeEach
    void setUp() {
        blogPostService = new BlogPostService(blogPostRepositoryPort, authorRepositoryPort);
    }

    // CreateBlogPostUseCase tests

    @Test
    void shouldCreateBlogPost() {
        // Arrange
        String authorId = "author-123";
        String title = "Test Post";
        String content = "Test content";
        String summary = "Test summary";
        Set<String> tagNames = Set.of("tag1", "tag2");

        CreateBlogPostCommand command = new CreateBlogPostCommand(
                title, content, summary, authorId, tagNames
        );

        Author author = mock(Author.class);
        when(authorRepositoryPort.findById(authorId)).thenReturn(Optional.of(author));

        BlogPost savedBlogPost = BlogPost.createDraft(title, content, summary, authorId);
        when(blogPostRepositoryPort.save(any(BlogPost.class))).thenReturn(savedBlogPost);

        // Act
        BlogPost result = blogPostService.createBlogPost(command);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(content, result.getContent());
        assertEquals(summary, result.getSummary());
        assertEquals(authorId, result.getAuthorId());
        assertEquals(PostStatus.DRAFT, result.getStatus());

        verify(authorRepositoryPort).findById(authorId);
        verify(blogPostRepositoryPort).save(any(BlogPost.class));
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFound() {
        // Arrange
        String authorId = "non-existent-author";
        CreateBlogPostCommand command = new CreateBlogPostCommand(
                "Test Post", "Test content", "Test summary", authorId, null
        );

        when(authorRepositoryPort.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        AuthorNotFoundException exception = assertThrows(
                AuthorNotFoundException.class,
                () -> blogPostService.createBlogPost(command)
        );

        assertTrue(exception.getMessage().contains(authorId));
        verify(authorRepositoryPort).findById(authorId);
        verify(blogPostRepositoryPort, never()).save(any(BlogPost.class));
    }

    // GetBlogPostUseCase tests

    @Test
    void shouldGetBlogPostById() {
        // Arrange
        PostId postId = PostId.generate();
        BlogPost blogPost = mock(BlogPost.class);
        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.of(blogPost));

        // Act
        BlogPost result = blogPostService.getById(postId);

        // Assert
        assertNotNull(result);
        assertEquals(blogPost, result);
        verify(blogPostRepositoryPort).findById(postId);
    }

    @Test
    void shouldThrowExceptionWhenBlogPostNotFoundById() {
        // Arrange
        PostId postId = PostId.generate();
        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        BlogPostNotFoundException exception = assertThrows(
                BlogPostNotFoundException.class,
                () -> blogPostService.getById(postId)
        );

        assertTrue(exception.getMessage().contains(postId.value()));
        verify(blogPostRepositoryPort).findById(postId);
    }

    @Test
    void shouldGetBlogPostBySlug() {
        // Arrange
        Slug slug = Slug.fromTitle("test-post");
        BlogPost blogPost = mock(BlogPost.class);
        when(blogPostRepositoryPort.findBySlug(slug)).thenReturn(Optional.of(blogPost));

        // Act
        BlogPost result = blogPostService.getBySlug(slug);

        // Assert
        assertNotNull(result);
        assertEquals(blogPost, result);
        verify(blogPostRepositoryPort).findBySlug(slug);
    }

    @Test
    void shouldThrowExceptionWhenBlogPostNotFoundBySlug() {
        // Arrange
        Slug slug = Slug.fromTitle("non-existent-post");
        when(blogPostRepositoryPort.findBySlug(slug)).thenReturn(Optional.empty());

        // Act & Assert
        BlogPostNotFoundException exception = assertThrows(
                BlogPostNotFoundException.class,
                () -> blogPostService.getBySlug(slug)
        );

        assertTrue(exception.getMessage().contains(slug.value()));
        verify(blogPostRepositoryPort).findBySlug(slug);
    }

    @Test
    void shouldGetAllPublishedBlogPosts() {
        // Arrange
        List<BlogPost> publishedPosts = List.of(
                mock(BlogPost.class),
                mock(BlogPost.class)
        );
        when(blogPostRepositoryPort.findByStatus(PostStatus.PUBLISHED)).thenReturn(publishedPosts);

        // Act
        List<BlogPost> result = blogPostService.getAllPublished();

        // Assert
        assertEquals(publishedPosts.size(), result.size());
        assertEquals(publishedPosts, result);
        verify(blogPostRepositoryPort).findByStatus(PostStatus.PUBLISHED);
    }

    @Test
    void shouldGetAllBlogPostsByAuthor() {
        // Arrange
        String authorId = "author-123";
        List<BlogPost> authorPosts = List.of(
                mock(BlogPost.class),
                mock(BlogPost.class)
        );
        when(blogPostRepositoryPort.findByAuthorId(authorId)).thenReturn(authorPosts);

        // Act
        List<BlogPost> result = blogPostService.getAllByAuthor(authorId);

        // Assert
        assertEquals(authorPosts.size(), result.size());
        assertEquals(authorPosts, result);
        verify(blogPostRepositoryPort).findByAuthorId(authorId);
    }

    // PublishBlogPostUseCase tests

    @Test
    void shouldPublishBlogPost() {
        // Arrange
        PostId postId = PostId.generate();
        
        BlogPost draftPost = BlogPost.builder()
                .id(postId)
                .title("Draft Post")
                .content("Draft content")
                .slug(Slug.fromTitle("Draft Post"))
                .authorId("author-123")
                .status(PostStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        BlogPost publishedPost = draftPost.toBuilder().build().publish();
        
        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.of(draftPost));
        when(blogPostRepositoryPort.save(any(BlogPost.class))).thenReturn(publishedPost);

        // Act
        BlogPost result = blogPostService.publishPost(postId);

        // Assert
        assertNotNull(result);
        assertEquals(PostStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublishedAt());
        
        verify(blogPostRepositoryPort).findById(postId);
        verify(blogPostRepositoryPort).save(any(BlogPost.class));
    }

    @Test
    void shouldThrowExceptionWhenPublishingNonExistentBlogPost() {
        // Arrange
        PostId postId = PostId.generate();
        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        BlogPostNotFoundException exception = assertThrows(
                BlogPostNotFoundException.class,
                () -> blogPostService.publishPost(postId)
        );

        assertTrue(exception.getMessage().contains(postId.value()));
        verify(blogPostRepositoryPort).findById(postId);
        verify(blogPostRepositoryPort, never()).save(any(BlogPost.class));
    }
}