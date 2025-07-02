package app.quantun.blog.application.service;

import app.quantun.blog.application.port.in.AddCommentUseCase.AddCommentCommand;
import app.quantun.blog.application.port.out.BlogPostRepositoryPort;
import app.quantun.blog.application.port.out.CommentRepositoryPort;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentApplicationServiceTest {

    @Mock
    private CommentRepositoryPort commentRepositoryPort;

    @Mock
    private BlogPostRepositoryPort blogPostRepositoryPort;

    private CommentApplicationService commentApplicationService;

    @BeforeEach
    void setUp() {
        commentApplicationService = new CommentApplicationService(commentRepositoryPort, blogPostRepositoryPort);
    }

    @Test
    void shouldAddCommentToPublishedPost() {
        // Arrange
        PostId postId = PostId.generate();
        String content = "This is a great post!";
        String authorName = "Jane Doe";
        String authorEmail = "jane@example.com";

        AddCommentCommand command = AddCommentCommand.builder()
                .postId(postId)
                .content(content)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .build();

        BlogPost publishedPost = mock(BlogPost.class);
        when(publishedPost.isPublished()).thenReturn(true);
        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.of(publishedPost));

        Comment savedComment = Comment.create(postId, content, authorName, authorEmail);
        when(commentRepositoryPort.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        Comment result = commentApplicationService.addComment(command);

        // Assert
        assertNotNull(result);
        assertEquals(postId, result.getPostId());
        assertEquals(content, result.getContent());
        assertEquals(authorName, result.getAuthorName());
        assertEquals(authorEmail, result.getAuthorEmail());

        verify(blogPostRepositoryPort).findById(postId);
        verify(publishedPost).isPublished();
        verify(commentRepositoryPort).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenBlogPostNotFound() {
        // Arrange
        PostId postId = PostId.generate();
        AddCommentCommand command = AddCommentCommand.builder()
                .postId(postId)
                .content("Test comment")
                .authorName("Jane Doe")
                .authorEmail("jane@example.com")
                .build();

        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        BlogPostNotFoundException exception = assertThrows(
                BlogPostNotFoundException.class,
                () -> commentApplicationService.addComment(command)
        );

        assertTrue(exception.getMessage().contains(postId.value()));
        verify(blogPostRepositoryPort).findById(postId);
        verify(commentRepositoryPort, never()).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingCommentToUnpublishedPost() {
        // Arrange
        PostId postId = PostId.generate();
        AddCommentCommand command = AddCommentCommand.builder()
                .postId(postId)
                .content("Test comment")
                .authorName("Jane Doe")
                .authorEmail("jane@example.com")
                .build();

        BlogPost draftPost = mock(BlogPost.class);
        when(draftPost.isPublished()).thenReturn(false);
        when(blogPostRepositoryPort.findById(postId)).thenReturn(Optional.of(draftPost));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> commentApplicationService.addComment(command)
        );

        assertTrue(exception.getMessage().contains("Cannot add comments to unpublished posts"));
        verify(blogPostRepositoryPort).findById(postId);
        verify(draftPost).isPublished();
        verify(commentRepositoryPort, never()).save(any(Comment.class));
    }

    @Test
    void shouldValidateAddCommentCommandWithNullPostId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AddCommentCommand.builder()
                        .postId(null)
                        .content("Test content")
                        .authorName("Jane Doe")
                        .authorEmail("jane@example.com")
                        .build()
        );

        assertTrue(exception.getMessage().contains("Post ID cannot be null"));
    }

    @Test
    void shouldValidateAddCommentCommandWithBlankContent() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AddCommentCommand.builder()
                        .postId(PostId.generate())
                        .content("")
                        .authorName("Jane Doe")
                        .authorEmail("jane@example.com")
                        .build()
        );

        assertTrue(exception.getMessage().contains("Content cannot be null or empty"));
    }

    @Test
    void shouldValidateAddCommentCommandWithBlankAuthorName() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AddCommentCommand.builder()
                        .postId(PostId.generate())
                        .content("Test content")
                        .authorName("")
                        .authorEmail("jane@example.com")
                        .build()
        );

        assertTrue(exception.getMessage().contains("Author name cannot be null or empty"));
    }

    @Test
    void shouldValidateAddCommentCommandWithBlankAuthorEmail() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AddCommentCommand.builder()
                        .postId(PostId.generate())
                        .content("Test content")
                        .authorName("Jane Doe")
                        .authorEmail("")
                        .build()
        );

        assertTrue(exception.getMessage().contains("Author email cannot be null or empty"));
    }
}