package app.quantun.blog.domain.service;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.port.in.AddCommentUseCase.AddCommentCommand;
import app.quantun.blog.domain.port.out.BlogPostRepositoryPort;
import app.quantun.blog.domain.port.out.CommentRepositoryPort;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepositoryPort commentRepositoryPort;

    @Mock
    private BlogPostRepositoryPort blogPostRepositoryPort;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepositoryPort, blogPostRepositoryPort);
    }

    @Test
    void shouldAddCommentToPublishedBlogPost() {
        // Arrange
        PostId postId = PostId.generate();
        String content = "Great post!";
        String authorName = "John Doe";
        String authorEmail = "john@example.com";

        AddCommentCommand command = AddCommentCommand.builder()
                .postId(postId)
                .content(content)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .build();

        BlogPost publishedPost = BlogPost.builder()
                .id(postId)
                .title("Test Post")
                .content("Test content")
                .slug(Slug.fromTitle("Test Post"))
                .authorId("author-123")
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();

        Comment savedComment = Comment.create(postId, content, authorName, authorEmail);

        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.of(publishedPost));
        when(commentRepositoryPort.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        Comment result = commentService.addComment(command);

        // Assert
        assertNotNull(result);
        assertEquals(postId, result.getPostId());
        assertEquals(content, result.getContent());
        assertEquals(authorName, result.getAuthorName());
        assertEquals(authorEmail, result.getAuthorEmail());

        verify(blogPostRepositoryPort).findById(postId);
        verify(commentRepositoryPort).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenBlogPostNotFound() {
        // Arrange
        PostId postId = PostId.generate();
        AddCommentCommand command = AddCommentCommand.builder()
                .postId(postId)
                .content("Test comment")
                .authorName("John Doe")
                .authorEmail("john@example.com")
                .build();

        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        BlogPostNotFoundException exception = assertThrows(
                BlogPostNotFoundException.class,
                () -> commentService.addComment(command)
        );

        assertTrue(exception.getMessage().contains(postId.value()));
        verify(blogPostRepositoryPort).findById(postId);
        verify(commentRepositoryPort, never()).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenBlogPostIsNotPublished() {
        // Arrange
        PostId postId = PostId.generate();
        AddCommentCommand command = AddCommentCommand.builder()
                .postId(postId)
                .content("Test comment")
                .authorName("John Doe")
                .authorEmail("john@example.com")
                .build();

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

        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.of(draftPost));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> commentService.addComment(command)
        );

        assertEquals("Cannot add comments to unpublished posts", exception.getMessage());
        verify(blogPostRepositoryPort).findById(postId);
        verify(commentRepositoryPort, never()).save(any(Comment.class));
    }
}